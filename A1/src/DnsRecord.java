public class DnsRecord {

    private int ttl;
    private int pref;
    private String rData;
    private QueryType qType;
    private int auth;

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

    /* SETTERS */

    public void setTTL(int ttl) {
        this.ttl = ttl;
    }

    public void setPref(int pref) {
        this.pref = pref;
    }

    public void setrData(String rData) {
        this.rData = rData;
    }

    public void setQueryType(QueryType queryType) {
        this.qType = queryType;
    }

}