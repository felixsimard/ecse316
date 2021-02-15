package dns;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.net.*;
import java.util.*;

public class DNSClient {

    public QueryType query_type = QueryType.A;
    private int timeout = 5000;
    private int MAX_RETRIES = 3;
    private byte[] server = new byte[4];
    String address;
    private String name;
    private int port = 53;

    /**
     * DNSClient constructor
     * @param args
     */
    public DNSClient(String[] args) {
        this.parseCommandArguments(args);
    }

    /**
     * Attempt a request
     */
    public void attemptRequest() {
        System.out.println("DnsClient sending request for " + name);
        System.out.println("Server: " + address);
        System.out.println("Request type: " + query_type);
        executeRequest(1);
    }

    /**
     * Perform the request
     * @param retries
     */
    private void executeRequest(int retries) {

        if(retries > MAX_RETRIES) {
            System.out.println("ERROR\tMaximum number of retries " + MAX_RETRIES + " exceeded");
            return;
        }


        try {
            DatagramSocket clientSocket = new DatagramSocket();

            InetAddress ipAddress = new InetAddress.getByAddress(server);

            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            sendData = name.getBytes();

            DatagramPacket clientPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.send(clientPacket);
            clientSocket.receive(receivePacket);


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


    private void parseCommandArguments(String[] args) {
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
                    query_type = QueryType.MX;
                    break;
                case "-ns":
                    query_type = QueryType.NS;
                    break;
                default:
                    if (arg.contains("@")) {
                        String addr = arg.replaceFirst("@", "");
                        String[] tokens = addr.split("\\.");

                        for (String token: tokens) {
                            int ipComponent = Integer.parseInt(token);
                            if (ipComponent > 255 || ipComponent < 0) {
                                throw new ValueException("Ip token invalid (not between 0 and 255).");
                            }
                            server[i] = (byte) ipComponent;
                        }
                        name = args[++i];
                    }
                    break;
            }
        }

    }

}
