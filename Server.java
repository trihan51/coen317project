import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

/**
    command line: java Server -document_root /home/ec2-user/files -port 8080
 */

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        String documentRoot = "./files";

        // Processing Command Line Arguments (if any)
        if (args.length == 0) {

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
        System.out.println("Client request ACCEPTED, handled by thread name: " + currentThread().getName() + ", thread id: " + currentThread().getId());
        try {
            // Parsing the HTTP Request
            HTTPRequest request = new HTTPRequest(socket);

            // Fetching the content
            String requestTarget = request.getRequestTarget();
            String requestTargetExtension = request.getRequestTargetExtension();

            File f = new File(documentRoot + requestTarget);
            if (!f.exists()) {
                throw new FileNotFoundException();
            } else if (f.isDirectory()) {
                throw new IsDirectoryException("Is Directory");
            } else { // permissions
                Path path = Paths.get(documentRoot + requestTarget);
                PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
                String permissionsString = PosixFilePermissions.toString(attrs.permissions());
                if (permissionsString.charAt(6) == '-') {
                    throw new PermissionsException("File does not have proper permissions");
                }
            }
            
            if (requestTargetExtension.equals(".html") || requestTargetExtension.equals(".txt")) {
                // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                out.writeBytes("HTTP/1.1 200 OK\r\n");
                switch (requestTargetExtension) {
                    case ".htm":
                    case ".html":
                        out.writeBytes("Content-Type: text/html\r\n");
                        break;
                    case ".txt":
                        out.writeBytes("Content-Type: text/plain\r\n");
                        break;
                    case ".jpg":
                    case ".jpeg":
                        out.writeBytes("Content-Type: image/jpeg\r\n");
                        break;
                    case ".gif":
                        out.writeBytes("Content-Type: image/gif\r\n");
                        break;
                    default:
                }

                StringBuilder contentBuilder = new StringBuilder();
                try {
                    BufferedReader in2 = new BufferedReader(new FileReader(f));
                    String s;
                    while ((s = in2.readLine()) != null) {
                        contentBuilder.append(s);
                    }
                    in2.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                String response = contentBuilder.toString();

                out.writeBytes("Content-Length: " + response.length() + "\r\n");
                out.writeBytes("Date: " + new Date() + "\r\n");
                out.writeBytes("\r\n");
                out.writeBytes(response + "\r\n");
            } else if (requestTargetExtension.equals(".jpeg") || requestTargetExtension.equals(".jpg") || requestTargetExtension.equals(".gif")) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                out.writeBytes("HTTP/1.1 200 OK\r\n");
                switch (requestTargetExtension) {
                    case ".htm":
                    case ".html":
                        out.writeBytes("Content-Type: text/html\r\n");
                        break;
                    case ".txt":
                        out.writeBytes("Content-Type: text/plain\r\n");
                        break;
                    case ".jpg":
                    case ".jpeg":
                        out.writeBytes("Content-Type: image/jpeg\r\n");
                        break;
                    case ".gif":
                        out.writeBytes("Content-Type: image/gif\r\n");
                        break;
                    default:
                }

                int bytes = (int) f.length();
                FileInputStream fis = new FileInputStream(documentRoot + requestTarget);
                byte[] fileBuffer = new byte[bytes];
                fis.read(fileBuffer);

                out.writeBytes("Content-Length: " + bytes + "\r\n");
                out.writeBytes("Date: " + new Date() + "\r\n");
                out.writeBytes("\r\n");
                out.write(fileBuffer, 0, bytes);
                out.close();
            } else {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.1 404 Not Found");
                out.println("Date: " + new Date());
                out.println("Connection: close");
                out.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.1 404 Not Found");
                out.println("Date: " + new Date());
                out.println("Connection: close");
                out.close();
            } catch (Exception e2) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause().getStackTrace());
            }
        } catch (IsDirectoryException e) {
            System.out.println(e.getMessage());
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.1 400 Bad Request, Requested Directory");
                out.println("Date: " + new Date());
                out.println("Connection: close");
                out.close();
            } catch (Exception e2) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause().getStackTrace());
            }
        } catch (PermissionsException e) {
            System.out.println(e.getMessage());
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.1 403 Forbidden");
                out.println("Date: " + new Date());
                out.println("Connection: close");
                out.close();
            } catch (Exception e2) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause().getStackTrace());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCause().getStackTrace());
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
            else {
                if (!inputLine.isEmpty()) {
                    this.headers.put(inputLine.split(":")[0], inputLine.split(":")[1]);
                }
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

    public String getRequestTargetExtension() {
        return this.getRequestTarget().lastIndexOf('.') == -1 ? "" : this.getRequestTarget().substring(this.getRequestTarget().lastIndexOf('.'));
    }
}

class IsDirectoryException extends Exception {
    public IsDirectoryException() {
        super();
    }

    public IsDirectoryException(String message) {
        super(message);
    }
}

class PermissionsException extends Exception {
    public PermissionsException() {
        super();
    }

    public PermissionsException(String message) {
        super(message);
    }
}