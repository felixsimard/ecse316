import java.nio.ByteBuffer;
import java.util.Random;

public class DnsRequest {

    private String domain;
    private QueryType qtype;


    /**
     * Constructor for DNS Request
     *
     * @param domain
     * @param qtype
     */
    public DnsRequest(String domain, QueryType qtype) {
        this.domain = domain;
        this.qtype = qtype;
    }

    /**
     * Get thee request, allocate buffer for request, set request + question headers.
     *
     * @return
     */
    public byte[] constructRequest() {

        // compute query name length
        int query_name_length = getQueryNameLength();

        // allocate 12 bytes for header,
        // 5 bytes for question + answers, plus length of query domain
        ByteBuffer r = ByteBuffer.allocate(query_name_length + 5 + 12);

        // build the request
        r.put(getRequestHeader());
        r.put(getQuestionHeader(query_name_length));

        return r.array();
    }

    /**
     * Compute query domain name length
     *
     * @return
     */
    private int getQueryNameLength() {
        int byte_length = 0;
        String[] toks = domain.split("\\.");
        for (int i = 0; i < toks.length; i++) {
            byte_length += toks[i].length() + 1;
        }
        return byte_length;
    }


    /**
     * Create the request header, allocating bytes and setting an ID to the request
     *
     * @return
     */
    private byte[] getRequestHeader() {

        // allocate 12 bytes (96 bits) for header (see Primer documentation --> 6 rows of 16 bits each = 96 bits)
        ByteBuffer header = ByteBuffer.allocate(12);

        // generate a random id for the request header
        byte[] randomID = new byte[2];
        new Random().nextBytes(randomID);

        // assign the bytes at each row layer of the request header
        // ID
        header.put(randomID);
        /*
        QR OPCode  AA  TC  RD  RA  ZZ   RCode
        0  0000    0   0   1   0   000  0000
         */

        // QR OPCode AA TC RD -> 0x01
        header.put((byte) 0x01);
        // RA  ZZ   RCode -> 0x00
        header.put((byte) 0x00);
        // QD Count 0x0001
        byte[] qdBarr = {(byte) 0x00, (byte)0x01};
        header.put(qdBarr);
        // AN Count 0x0000
        byte[] anBarr = {(byte) 0x00, (byte)0x00};
        header.put(anBarr);
        // NS Count 0x0000
        byte[] nsBarr = {(byte) 0x00, (byte)0x00};
        header.put(nsBarr);
        // AR Count 0x0000
        byte[] arBarr = {(byte) 0x00, (byte)0x00};
        header.put(arBarr);

        return header.array();
    }

    private byte[] getQuestionHeader(int query_name_length) {

        // allocate 5 bytes + length of query name to question buffer
        ByteBuffer q = ByteBuffer.allocate(query_name_length + 5);

        // find out how many bytes we need to know size of array
        String[] toks = domain.split("\\.");
        for (int i = 0; i < toks.length; i++) {
            q.put((byte) toks[i].length());
            for (int j = 0; j < toks[i].length(); j++) {
                q.put((byte) ((int) toks[i].charAt(j)));

            }
        }

        // Based off Primer documentation regarding the question header structure
        // terminate query name will zero-length octet
        q.put((byte) 0);

        // insert query type in question header
        if (qtype == QueryType.A) {
            byte[] barr = {(byte) 0x00, (byte) 0x01};
            q.put(barr);
        } else if (qtype == QueryType.NS) {
            byte[] barr = {(byte) 0x00, (byte)0x02};
            q.put(barr);
        } else if (qtype == QueryType.MX) {
            byte[] barr = {(byte) 0x00, (byte)0x0f};
            q.put(barr);
        }

        // for query class, for internet address always use 0x0001
        byte[] barr = {(byte) 0x00, (byte)0x01};
        q.put(barr);

        return q.array();
    }

}
