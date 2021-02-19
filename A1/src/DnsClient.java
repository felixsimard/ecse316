
import java.net.*;

/**
 * Class implementing the dns client that handles the UDP communication and packet creation and reception.
 */
public class DnsClient {

    public QueryType qType = QueryType.A;
    private int timeout = 5000;
    private int MAXRETRIES = 3;
    private byte[] server = new byte[4];
    private String address;
    private String name;
    private int port = 53;

    /**
     * Program entry point.
     * @param args request arguments
     */
    public static void main(String[] args) {
        try {
            DnsClient c = new DnsClient(args);
            c.executeRequest(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * DNSClient constructor
     *
     * @param args
     */
    public DnsClient(String[] args) {
        try {
            this.parseCommandArguments(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Please check arguments and try again");
        }
        if (server == null || name == null) {
            throw new IllegalArgumentException("ERROR\tIncorrect input syntax: Server IP and domain name must be provided.");
        }
    }

    /**
     * Execute the request
     *
     */
    public void executeRequest(int retries) {
        // print request information on first try
        if (retries == 1) {
            String requestDescription = "DnsClient sending request for " + name + "\n" +
                    "Server: " + address + "\n" +
                    "Request type: " + qType + "\n";
            System.out.println(requestDescription);
        }

        if (retries > MAXRETRIES) {
            System.out.println("ERROR\tMaximum number of retries " + MAXRETRIES + " exceeded.");
            return;
        }

        try {
            // Setting up socket
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeout);

            // Create InetAddress used by datagram packet
            InetAddress ipAddress = InetAddress.getByAddress(server);

            // Setting up request
            DnsRequest request = new DnsRequest(name, qType);
            byte[] sendData = request.constructRequest();
            byte[] receiveData = new byte[1024];

            // Setting up packets
            DatagramPacket clientPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // measure the time taken to send and receive query
            long start = System.currentTimeMillis();
            clientSocket.send(clientPacket);
            clientSocket.receive(receivePacket);
            long end = System.currentTimeMillis();
            clientSocket.close();

            System.out.println("Response received after " + (end - start) / 1000. + " seconds " + "(" + (retries - 1) + " retries)");

            // output response information
            DnsResponse res = new DnsResponse(receivePacket.getData(), sendData.length, qType);
            res.outputResponse();

        } catch (SocketException e) {
            System.out.println("ERROR\tFailed to create the socket.");
        } catch (UnknownHostException e) {
            System.out.println("ERROR\tThe host is unknown.");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tThe socket has timed out.\nRetrying request.");
            executeRequest(++retries);
        } catch (Exception e) {
            String message = e.getMessage();
            System.out.println("ERROR\t" + message);
        }
    }

    /**
     * Parse the command line arguments.
     *
     * @param args
     * @throws Exception
     */
    private void parseCommandArguments(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-t":
                    int seconds = Integer.parseInt(args[++i]);
                    timeout = seconds * 1000;
                    break;
                case "-r":
                    MAXRETRIES = Integer.parseInt(args[++i]);
                    break;
                case "-p":
                    port = Integer.parseInt(args[++i]);
                    break;
                case "-mx":
                    qType = QueryType.MX;
                    break;
                case "-ns":
                    qType = QueryType.NS;
                    break;
                default:
                    if (arg.contains("@")) {
                        address = arg.replaceFirst("@", "");

                        String[] tokens = address.split("\\.");
                        int j = 0;
                        for (String token : tokens) {
                            int ipComponent = Integer.parseInt(token);
                            if (ipComponent > 255 || ipComponent < 0) {
                                throw new Exception("Ip token invalid (not between 0 and 255).");
                            }
                            server[j++] = (byte) ipComponent;
                        }
                        name = args[++i];
                    }
                    break;
            }
        }
    }
}
