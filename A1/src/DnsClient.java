
import java.net.*;

public class DnsClient {

    public QueryType qtype = QueryType.A;
    private int timeout = 5000;
    private int MAX_RETRIES = 3;
    private byte[] server = new byte[4];
    String address;
    private String name;
    private int port = 53;

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
     * Attempt a request
     */
    public void attemptRequest() {
        System.out.println("DnsClient sending request for " + name);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + qtype);
        executeRequest(1);
    }

    /**
     * Perform the request
     *
     * @param retries
     */
    private void executeRequest(int retries) {

        if (retries > MAX_RETRIES) {
            System.out.println("ERROR\tMaximum number of retries " + MAX_RETRIES + " exceeded");
            return;
        }


        try {
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(timeout);

            InetAddress ipAddress = InetAddress.getByAddress(server);

            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            DnsRequest request = new DnsRequest(name, qtype);
            sendData = request.constructRequest();

            DatagramPacket clientPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            long start = System.currentTimeMillis();
            clientSocket.send(clientPacket);
            clientSocket.receive(receivePacket);
            long end = System.currentTimeMillis();
            clientSocket.close();

            // Show elapsed time for receiving response
            System.out.println("Response received after " + (end - start) / 1000. + " seconds " + "(" + (retries - 1) + " retries)");

            DnsResponse res = new DnsResponse(receivePacket.getData(), sendData.length, qtype);
            res.outputResponse();

        } catch (SocketException e) {
            System.out.println("ERROR\tCould not create socket");
        } catch (UnknownHostException e) {
            System.out.println("ERROR\tUnknown host");
        } catch (SocketTimeoutException e) {
            System.out.println("ERROR\tSocket Timeout");
            System.out.println("Reattempting request...");
            executeRequest(++retries);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }


    private void parseCommandArguments(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-t":
                    int seconds = Integer.parseInt(args[++i]);
                    timeout = seconds * 1000;
                    break;
                case "-r":
                    MAX_RETRIES = Integer.parseInt(args[++i]);
                    break;
                case "-p":
                    port = Integer.parseInt(args[++i]);
                    break;
                case "-mx":
                    qtype = QueryType.MX;
                    break;
                case "-ns":
                    qtype = QueryType.NS;
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
