package dns;


public class DNSRecord {

    private int ttl, rd_length, max_preference;
    private String name, domain;
    private byte[] qclass;
    private QueryType qtype;
    private boolean auth;
    private int byte_length;

    public DNSRecord(boolean auth) {
        this.auth = auth;
    }

    public void outputRecord() {
        switch (this.qtype) {
            case A:
                this.outputATypeRecords();
                break;
            case NS:
                this.outputNSTypeRecords();
                break;
            case MX:
                this.outputMXTypeRecords();
                break;
            case CNAME:
                this.outputCNameTypeRecords();
                break;
            default:
                break;
        }
    }

    private void outputATypeRecords() {
        String authString = this.auth ? "auth" : "nonauth";
        System.out.println("IP\t" + this.domain + "\t" + this.ttl + "\t" + authString);
    }

    private void outputNSTypeRecords() {
        String authString = this.auth ? "auth" : "nonauth";
        System.out.println("NS\t" + this.domain + "\t" + this.ttl + "\t" + authString);
    }

    private void outputMXTypeRecords() {
        String authString = this.auth ? "auth" : "nonauth";
        System.out.println("MX\t" + this.domain + "\t" + max_preference + "\t" + this.ttl + "\t" + authString);
    }

    private void outputCNameTypeRecords() {
        String authString = this.auth ? "auth" : "nonauth";
        System.out.println("CNAME\t" + this.domain + "\t" + this.ttl + "\t" + authString);
    }

    public int getByteLength() {
        return byte_length;
    }

    public void setByteLength(int byteLength) {
        this.byte_length = byteLength;
    }

    public int getTTL() {
        return ttl;
    }

    public void setTTL(int ttl) {
        this.ttl = ttl;
    }

    public int getRdLength() {
        return rd_length;
    }

    public void setRdLength(int rdLength) {
        this.rd_length = rdLength;
    }

    public int getMaxPreference() {
        return max_preference;
    }

    public void setMaxPreference(int max_preference) {
        this.max_preference = max_preference;
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
        return qclass;
    }

    public void setQueryClass(byte[] queryClass) {
        this.qclass = queryClass;
    }

    public QueryType getQueryType() {
        return qtype;
    }

    public void setQueryType(QueryType queryType) {
        this.qtype = queryType;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }
}