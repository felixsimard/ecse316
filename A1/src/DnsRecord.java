public class DnsRecord {

    private int ttl;
    private int rdLength;
    private int maxPreference;
    private String name;
    private String domain;
    private byte[] qClass;
    private QueryType qType;
    private boolean auth;
    private int byteLength;

    /**
     * Constructor.
     *
     * @param auth
     */
    public DnsRecord(boolean auth) {
        this.auth = auth;
    }

    /**
     * Print the record depending on the query type.
     */
    public void outputRecord() {
        String authoritative;
        if (auth) {
            authoritative = "auth";
        } else {
            authoritative = "nonauth";
        }
        if (qType == QueryType.A) {
            System.out.println("IP\t" + domain + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.NS) {
            System.out.println("NS\t" + domain + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.MX) {
            System.out.println("MX\t" + domain + "\t" + maxPreference + "\t" + ttl + "\t" + authoritative);
        } else if (qType == QueryType.CNAME) {
            System.out.println("CNAME\t" + domain + "\t" + ttl + "\t" + authoritative);
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

    public int getMaxPreference() {
        return maxPreference;
    }

    public void setMaxPreference(int max_preference) {
        this.maxPreference = max_preference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }
}