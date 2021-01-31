import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    public static void main(String[] args) {

        int port = 8080;
        String document_root = "";

        // Processing Command Line Arguments (if any)
        if (args.length == 0) {
            System.out.println("no args");
        } else if (args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-document_root":
                        document_root=args[i+1];
                        break;
                    case "-port":
                        port = Integer.parseInt(args[i+1]);
                        break;
                    default:
                }
            }
        } else {
            System.out.println("Invalid arguments");
            System.exit(0);
        }

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine, outputLine;
            StringBuilder request = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                request.append(inputLine).append("\r\n");
                System.out.println(inputLine);
                if (inputLine.isEmpty()) {
                    break;
                }
            }
            System.out.println("request is handled here");

            StringBuilder contentBuilder = new StringBuilder();
            try {
                BufferedReader in2 = new BufferedReader(new FileReader("SCU_HOME.html"));
                String s;
                while ((s = in2.readLine()) != null) {
                    System.out.println(s);
                    contentBuilder.append(s);
                }
                in2.close();
            } catch (IOException e) {
                System.out.println("error");
            }

            String response = contentBuilder.toString();
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println("Content-Length: " + response.length());
            out.println("Date: " + new Date());
            out.println("\r\n");
            out.println(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}