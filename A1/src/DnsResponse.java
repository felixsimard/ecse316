import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class DnsResponse {

    private byte[] res;
    private byte[] id;
    private boolean QR, AA, TC, RD, RA;
    private int R_code, QD_count, AN_count, NS_count, AR_count;
    private DnsRecord[] answer_records;
    private DnsRecord[] additional_records;
    private QueryType qtype;
    private boolean no_records = false;

    /**
     * DnsResponse constructor
     * @param res
     * @param resSize
     * @param qtype
     */
    public DnsResponse(byte[] res, int resSize, QueryType qtype) {
        this.res = res;
        this.qtype = qtype;

        this.validateResponseQuestionType();
        this.parseHeader();
        
        // Fetch all DNS records from response
        answer_records = new DnsRecord[AN_count];
        int offSet = resSize;
        for (int i = 0; i < AN_count; i++) {
            answer_records[i] = this.parseAnswer(offSet);
            offSet += answer_records[i].getByteLength();
        }

        // NS count only
        for (int i = 0; i < NS_count; i++) {
            offSet += parseAnswer(offSet).getByteLength();
        }

        additional_records = new DnsRecord[AR_count];
        for (int i = 0; i < AR_count; i++) {
            additional_records[i] = this.parseAnswer(offSet);
            offSet += additional_records[i].getByteLength();
        }

        // Look for possible errors
        try {
            this.checkRCodeForErrors();
        } catch (RuntimeException e) {
            no_records = true;
        }

        // Simply checks if QR byte is set to 1
        this.validateQueryTypeIsResponse();
    }

    /**
     * Display the response, and possible records
     */
    public void outputResponse() {
        System.out.println();
        if (this.AN_count <= 0 || no_records) {
            System.out.println("NOTFOUND");
            return;
        }
        System.out.println("***Answer Section (" + this.AN_count + " records)***");
        for (DnsRecord record : answer_records) {
            record.outputRecord();
        }
        System.out.println();
        if (this.AR_count > 0) {
            System.out.println("***Additional Section (" + this.AR_count + " records)***");
            for (DnsRecord record : additional_records) {
                record.outputRecord();
            }
        }
    }

    /**
     * Check code for errors
     */
    private void checkRCodeForErrors() {
        switch (this.R_code) {
            case 0:
                //No error
                break;
            case 1:
                throw new RuntimeException("ERROR Format: Name server unable to process query.");
            case 2:
                throw new RuntimeException("ERROR Server failure: Name server unable to process query due to a problem with the name server.");
            case 3:
                throw new RuntimeException();
            case 4:
                throw new RuntimeException("ERROR Not implemented: Name server does not support the requested query.");
            case 5:
                throw new RuntimeException("ERROR Refused: Name server refuses to perform the requested operation.");
        }
    }


    /**
     * Parse header of DNS record
     */
    private void parseHeader() {
        // id
        byte[] id = new byte[2];
        id[0] = res[0];
        id[1] = res[1];
        this.id = id;

        // QR
        this.QR = getBitFromByte(res[2], 7) == 1;
        // AA
        this.AA = getBitFromByte(res[2], 2) == 1;
        // TC
        this.TC = getBitFromByte(res[2], 1) == 1;
        // RD
        this.RD = getBitFromByte(res[2], 0) == 1;
        // RA
        this.RA = getBitFromByte(res[3], 7) == 1;
        // RCODE
        this.R_code = res[3] & 0x0F;

        // QDCount
        byte[] QDCount = {res[4], res[5]};
        ByteBuffer w = ByteBuffer.wrap(QDCount);
        this.QD_count = w.getShort();
        // ANCount
        byte[] ANCount = {res[6], res[7]};
        w = ByteBuffer.wrap(ANCount);
        this.AN_count = w.getShort();
        // NSCount
        byte[] NSCount = {res[8], res[9]};
        w = ByteBuffer.wrap(NSCount);
        this.NS_count = w.getShort();
        // ARCount
        byte[] ARCount = {res[10], res[11]};
        w = ByteBuffer.wrap(ARCount);
        this.AR_count = w.getShort();
    }

    /**
     * Parsing the relevant information contained in a response record
     * @param index
     * @return
     */
    private DnsRecord parseAnswer(int index) {
        DnsRecord result = new DnsRecord(this.AA);

        String domain = "";
        int count_byte = index;

        rawEntry domain_result = getDomainFromIndex(count_byte);
        count_byte += domain_result.getBytes();
        domain = domain_result.getDomain();

        // Set domain name to result record
        result.setName(domain);

        // Set type to result record
        byte[] ans_type = new byte[2];
        ans_type[0] = res[count_byte];
        ans_type[1] = res[count_byte + 1];

        result.setQueryType(qtypeFromByteArray(ans_type));

        count_byte += 2;
        // Class
        byte[] ans_class = new byte[2];
        ans_class[0] = res[count_byte];
        ans_class[1] = res[count_byte + 1];
        if (ans_class[0] != 0 && ans_class[1] != 1) {
            throw new RuntimeException(("ERROR\tClass field in the response answer is not 1."));
        }
        result.setQueryClass(ans_class);

        count_byte += 2;

        // TTL
        byte[] TTL = {res[count_byte], res[count_byte + 1], res[count_byte + 2], res[count_byte + 3]};
        ByteBuffer wrapped = ByteBuffer.wrap(TTL);
        result.setTTL(wrapped.getInt());

        count_byte += 4;

        // RDLength
        byte[] RDLength = {res[count_byte], res[count_byte + 1]};
        wrapped = ByteBuffer.wrap(RDLength);
        int rdLength = wrapped.getShort();
        result.setRdLength(rdLength);

        count_byte += 2;
        switch (result.getQueryType()) {
            case A:
                result.setDomain(parseAType(rdLength, count_byte));
                break;
            case NS:
                result.setDomain(parseNSType(rdLength, count_byte));
                break;
            case MX:
                result.setDomain(parseMXType(rdLength, count_byte, result));
                break;
            case CNAME:
                result.setDomain(parseCNAMEType(rdLength, count_byte));
                break;
            case OTHER:
                break;
        }
        result.setByteLength(count_byte + rdLength - index);
        return result;
    }

    private String parseAType(int rdLength, int countByte) {
        String address = "";
        byte[] byteAddress = {res[countByte], res[countByte + 1], res[countByte + 2], res[countByte + 3]};
        try {
            InetAddress inetaddress = InetAddress.getByAddress(byteAddress);
            address = inetaddress.toString().substring(1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;

    }

    private String parseNSType(int rdLength, int countByte) {
        rawEntry result = getDomainFromIndex(countByte);
        String nameServer = result.getDomain();

        return nameServer;
    }

    private String parseMXType(int rdLength, int countByte, DnsRecord record) {
        byte[] mxPreference = {this.res[countByte], this.res[countByte + 1]};
        ByteBuffer buf = ByteBuffer.wrap(mxPreference);
        record.setMaxPreference(buf.getShort());
        return getDomainFromIndex(countByte + 2).getDomain();
    }

    private String parseCNAMEType(int rdLength, int countByte) {
        rawEntry result = getDomainFromIndex(countByte);
        String cname = result.getDomain();

        return cname;
    }

    private void validateQueryTypeIsResponse() {
        if (!this.QR) {
            throw new RuntimeException("ERROR\tInvalid response from server: Message is not a response.");
        }
    }

    /**
     * Validate Response Question Type
     */
    private void validateResponseQuestionType() {

        // question starts at byte 13, indexed at 11
        int index = 12;

        // get to the end of question header
        while (this.res[index] != 0) {
            index++;
        }
        byte[] qType = {this.res[index + 1], this.res[index + 2]}; // question type composed of 2 bytes

        if (this.qtypeFromByteArray(qType) != this.qtype) {
            throw new RuntimeException("ERROR\tResponse query type and request query type do not match.");
        }
    }

    private rawEntry getDomainFromIndex(int index) {
        rawEntry result = new rawEntry();
        int wordSize = res[index];
        String domain = "";
        boolean start = true;
        int count = 0;
        while (wordSize != 0) {
            if (!start) {
                domain += ".";
            }
            if ((wordSize & 0xC0) == (int) 0xC0) {
                byte[] offset = {(byte) (res[index] & 0x3F), res[index + 1]};
                ByteBuffer wrapped = ByteBuffer.wrap(offset);
                domain += getDomainFromIndex(wrapped.getShort()).getDomain();
                index += 2;
                count += 2;
                wordSize = 0;
            } else {
                domain += getWordFromIndex(index);
                index += wordSize + 1;
                count += wordSize + 1;
                wordSize = res[index];
            }
            start = false;

        }
        result.setDomain(domain);
        result.setBytes(count);
        return result;
    }

    private String getWordFromIndex(int index) {
        String word = "";
        int wordSize = res[index];
        for (int i = 0; i < wordSize; i++) {
            word += (char) res[index + i + 1];
        }
        return word;
    }

    private int getBitFromByte(byte b, int position) {
        return (b >> position) & 1;
    }

    /**
     * Determine the qtype from a byte array (ie: {0, 0, 0 , 1})
     *
     * @param qType
     * @return
     */
    private QueryType qtypeFromByteArray(byte[] qType) {
        if (qType[0] == 0) {
            if (qType[1] == 1) {
                return QueryType.A;
            } else if (qType[1] == 2) {
                return QueryType.NS;
            } else if (qType[1] == 15) {
                return QueryType.MX;
            } else if (qType[1] == 5) {
                return QueryType.CNAME;
            } else {
                return QueryType.OTHER;
            }
        } else {
            return QueryType.OTHER;
        }
    }


}
