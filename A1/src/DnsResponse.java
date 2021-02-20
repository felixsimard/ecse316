import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DnsResponse {

    public byte[] Id = new byte[2];
    public byte[] dataBuff;
    public int AA;
    public int ANCOUNT;
    public int ARCOUNT;
    public int lenght;
    public QueryType qType;

    public int header_offset = 0;
    public int answer_offset;

    public ArrayList<DnsRecord> AnRecords = new ArrayList<>();
    public ArrayList<DnsRecord> ArRecords = new ArrayList<>();

    public DnsResponse(byte[] dataBuff, int length, QueryType qType) {
        this.dataBuff = dataBuff;
        this.lenght = length;
        this.qType = qType;

        parseDnsResponse(length);
    }

    private void parseDnsResponse(int length) {
        /* HEADER */
        getId();
        parseSecondRow();
        getANCOUNT();
        getARCOUNT();

        /* ANSWER */
        answer_offset = length;
        for (int i = 0; i < ANCOUNT; i++) {
            DnsRecord dr = new DnsRecord(this.AA);

            String domain = getString(answer_offset);
            answer_offset += 2;

            QueryType type = getType();
            getClassRow();
            int ttl = getTTL();
            int rdLength = getRdLength();
            String rdata = getRData(type, dr);

            dr.setQueryType(type);
            dr.setrData(domain);
            dr.setTTL(ttl);
            dr.setrData(rdata);

            this.AnRecords.add(dr);

            answer_offset += rdLength;
        }

        /* ADDITIONAL */
        for (int i = 0; i < ARCOUNT; i++) {
            DnsRecord dr = new DnsRecord(this.AA);

            String domain = getString(answer_offset);
            answer_offset += 2;

            QueryType type = getType();
            getClassRow();
            int ttl = getTTL();
            int rdLength = getRdLength();
            String rdata = getRData(type, dr);

            dr.setQueryType(type);
            dr.setrData(domain);
            dr.setTTL(ttl);
            dr.setrData(rdata);

            this.ArRecords.add(dr);

            answer_offset += rdLength;
        }
    }

    /**
     * Display the response, and possible records
     */
    public void printResponse() {
        System.out.println();
        if (this.ANCOUNT <= 0) {
            System.out.println("NOTFOUND");
            return;
        }

        System.out.println("***Answer Section (" + this.ANCOUNT + " records)***");
        for (DnsRecord rec : this.AnRecords) {
            rec.outputRecord();
        }

        System.out.println();
        if (this.ARCOUNT > 0) {
            System.out.println("***Additional Section (" + this.ARCOUNT + " records)***");
            for (DnsRecord rec : this.ArRecords) {
                rec.outputRecord();
            }
        }
    }

    private void getId() {
        this.Id[0] = this.dataBuff[header_offset++];
        this.Id[1] = this.dataBuff[header_offset++];
    }

    private void parseSecondRow() {
        byte[] secondrow = new byte[2];
        secondrow[0] = this.dataBuff[header_offset++];
        secondrow[1] = this.dataBuff[header_offset++];

        int qr = getBit(secondrow[0], 7);
        if (qr != 1) {
            throw new RuntimeException("ERROR\tReceived packet is not a response.");
        }

        this.AA = getBit(secondrow[0], 2);

        int rcode = secondrow[1] & 0x0F;
        if (rcode == 1) {
            throw new RuntimeException("Format error in response packet.");
        } else if (rcode == 2) {
            throw new RuntimeException("Server failure.");
        } else if (rcode == 3) {
            if (this.AA == 1) {
                System.out.println("NOTFOUND");
                System.exit(1);
            }
        } else if (rcode == 4) {
            throw new RuntimeException("Request kind of query not implemented.");
        } else if (rcode == 5) {
            throw new RuntimeException("Name server refuses to perform operation for policy reasons.");
        }
    }

    private void getANCOUNT() {
        // skip qdcount
        header_offset += 2;
        byte[] ancount = new byte[2];
        ancount[0] = this.dataBuff[header_offset++];
        ancount[1] = this.dataBuff[header_offset++];

        this.ANCOUNT = (ancount[0] << 4) + ancount[1];
    }

    private void getARCOUNT() {
        // skip nscount
        header_offset += 2;
        byte[] arcount = new byte[2];
        arcount[0] = this.dataBuff[header_offset++];
        arcount[1] = this.dataBuff[header_offset++];

        this.ARCOUNT = (arcount[0] << 4) + arcount[1];
    }

    private QueryType getType() {
        byte[] arr = new byte[2];
        arr[0] = this.dataBuff[answer_offset++];
        arr[1] = this.dataBuff[answer_offset++];

        if (arr[1] == 1) {
            return QueryType.A;
        } else if (arr[1] == 2) {
            return QueryType.NS;
        } else if (arr[1] == 15) {
            return QueryType.MX;
        } else {
            // return QueryType.OTHER;
            throw new RuntimeException("Unknown type in response record.");
        }
    }

    private void getClassRow() {
        byte[] arr = new byte[2];
        arr[0] = this.dataBuff[answer_offset++];
        arr[1] = this.dataBuff[answer_offset++];

        if (arr[1] != 1) {
            throw new RuntimeException("Class row not equal to 0x0001");
        }
    }

    private int getTTL() {
        byte[] arr = new byte[4];
        arr[0] = this.dataBuff[answer_offset++];
        arr[1] = this.dataBuff[answer_offset++];
        arr[2] = this.dataBuff[answer_offset++];
        arr[3] = this.dataBuff[answer_offset++];

        return ByteBuffer.wrap(arr).getInt();
    }

    private int getRdLength() {
        byte[] arr = new byte[2];
        arr[0] = this.dataBuff[answer_offset++];
        arr[1] = this.dataBuff[answer_offset++];

        return (arr[0] << 4) + arr[1];
    }

    private int getPreference() {
        byte[] arr = new byte[2];
        arr[0] = this.dataBuff[answer_offset++];
        arr[1] = this.dataBuff[answer_offset++];

        return (arr[0] << 4) + arr[1];
    }

    private String getRData(QueryType type, DnsRecord dr) {
        String result = "";
        if (type == QueryType.A) {
            byte [] arr = new byte[4];
            for (int i = 0; i < 4; i++) {
                arr[i] = this.dataBuff[answer_offset++];
            }
            try {
                InetAddress iNetAddress = InetAddress.getByAddress(arr);
                result = iNetAddress.toString().replace("/", "");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        } else if (type == QueryType.NS) {
            result = getString(answer_offset);
        }
        else if (type == QueryType.MX) {
            int pref = getPreference();
            dr.setPref(pref);
            result = getString(answer_offset);
        }

        return result;

    }

    private String getString(int index) {
        String result = "";

        int length = this.dataBuff[index];

        boolean first = true;
        while(length != 0) {
            if (!first) {
                result += ".";
            } else first = false;

            if ((length & 0xC0) == 0xC0) {
                // keep the pointer to the data
                byte[] offset = {(byte) (this.dataBuff[index] & 0x3F), this.dataBuff[index + 1]};
                ByteBuffer wrapped = ByteBuffer.wrap(offset);
                result += getString(wrapped.getShort());
                break;
            } else {
                for (int i = 0; i < length; i++) {
                    // System.out.println("CHAR : " + (char) dataBuff[index + i + 1]);
                    result += (char) dataBuff[index + i + 1];
                }
                index += length + 1;
                length = this.dataBuff[index];
            }
        }
        return result;
    }

    public int getBit(byte b, int position)
    {
        return (b >> position) & 1;
    }
}
