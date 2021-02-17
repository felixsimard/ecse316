public class Main {

    public static void main(String[] args) {
        try {
            DnsClient c = new DnsClient(args);
            c.attemptRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
