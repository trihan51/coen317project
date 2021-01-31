import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        String documentRoot = ".";

        // Processing Command Line Arguments (if any)
        if (args.length == 0) {
            System.out.println("no args");
        } else if (args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-document_root":
                        documentRoot=args[i+1];
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

        boolean listening = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (listening) {
                new ClientHandler(serverSocket.accept(), documentRoot).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket = null;
    private String documentRoot = null;

    public ClientHandler(Socket socket, String documentRoot) {
        super("ClientHandler");
        this.socket = socket;
        this.documentRoot = documentRoot;
    }

    public void run() {  
        try {
            // Parsing the HTTP Request
            HTTPRequest request = new HTTPRequest(socket);

            // Fetching the content
            String requestTarget = request.getRequestTarget();
            System.out.println(requestTarget);
            StringBuilder contentBuilder = new StringBuilder();
            try {
                BufferedReader in2 = new BufferedReader(new FileReader(documentRoot + requestTarget));
                String s;
                while ((s = in2.readLine()) != null) {
                    contentBuilder.append(s);
                }
                in2.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            String response = contentBuilder.toString();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
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

class HTTPRequest {
    String requestLine = "";
    HashMap<String, String> headers = new HashMap<String, String>();
    String request = "";

    public HTTPRequest(Socket socket) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String inputLine;
        int i = 0;
        StringBuilder request = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            if (i == 0) {
                this.requestLine = inputLine;
                i++;
            }
            request.append(inputLine).append("\r\n");
            if (inputLine.isEmpty()) {
                break;
            }
        }
        this.request = request.toString();
    }

    public String getRequestTarget() {
        String target = this.requestLine.split(" ")[1];
        return target.equals("/") ? "/index.html" : target;
    }
}