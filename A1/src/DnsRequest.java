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
        header.put(randomID);
        header.put((byte) 0x01);
        header.put((byte) 0x00);
        header.put((byte) 0x00);
        header.put((byte) 0x01);

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
        // for query domain name
        q.put((byte) 0x00);
        // insert query type in question header
        q.put((byte) 0x00);
        // for query class, for internet address
        q.put((byte) 0x0001);

        return q.array();

    }


    private char hexValueFromQueryType(QueryType qtype) {
        if (qtype == QueryType.A) {
            return '1';
        } else if (qtype == QueryType.NS) {
            return '2';
        } else {
            return 'F';
        }
    }

    private static byte[] hexStringToByteArray(String hex_str) {
        int len = hex_str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex_str.charAt(i), 16) << 4)
                    + Character.digit(hex_str.charAt(i + 1), 16));
        }
        return data;
    }

}
