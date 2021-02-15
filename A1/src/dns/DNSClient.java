package dns;

import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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


        try {  // execute request

            // UDP socket, request, response


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


    private void parseCommandLineArguments(String[] args) {
        List<String> argsList = Arrays.asList(args);
        ListIterator<String> iterator = argsList.listIterator();

        while (iterator.hasNext()) {


        }

    }

}
