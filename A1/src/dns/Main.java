package dns;

public class Main {

    public static void main(String[] args) {
        try {
            DNSClient c = new DNSClient(args);
            c.attemptRequest();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
