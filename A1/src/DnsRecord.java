public class DnsRecord {

    private int ttl;
    private int rdLength;
    private int pref;
    private String name;
    private String rData;
    private byte[] qClass;
    private QueryType qType;
    private int auth;
    private int byteLength;

    /**
     * Constructor.
     *
     * @param auth
     */
    public DnsRecord(int auth) {
        this.auth = auth;
    }

    /**
     * Print the record depending on the query type.
     */
    public void outputRecord() {
        String authoritative;
        if (auth == 1) {
            authoritative = "auth";
        } else {
            authoritative = "nonauth";
        }
        if (qType == QueryType.A) {
            System.out.println("IP\t" + rData + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.NS) {
            System.out.println("NS\t" + rData + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.MX) {
            System.out.println("MX\t" + rData + "\t" + pref + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.CNAME) {
            System.out.println("CNAME\t" + rData + "\t" + ttl + "\t" + authoritative);
        }
    }

    /* GETTERS AND SETTERS */

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public int getTTL() {
        return ttl;
    }

    public void setTTL(int ttl) {
        this.ttl = ttl;
    }

    public int getRdLength() {
        return rdLength;
    }

    public void setRdLength(int rdLength) {
        this.rdLength = rdLength;
    }

    public int getPref() {
        return pref;
    }

    public void setPref(int pref) {
        this.pref = pref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getrData() {
        return rData;
    }

    public void setrData(String rData) {
        this.rData = rData;
    }

    public byte[] getQueryClass() {
        return qClass;
    }

    public void setQueryClass(byte[] queryClass) {
        this.qClass = queryClass;
    }

    public QueryType getQueryType() {
        return qType;
    }

    public void setQueryType(QueryType queryType) {
        this.qType = queryType;
    }

    public int isAuth() {
        return auth;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }
}