import java.net.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.*;

/**
    command line: java Server -document_root /home/ec2-user/files -port 8080
 */

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
            String requestTargetExtension = request.getRequestTargetExtension();

            // START: Working code for HTML and TXT Files
            // PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // out.println("HTTP/1.1 200 OK");
            // switch (requestTargetExtension) {
            //     case ".htm":
            //     case ".html":
            //         out.println("Content-Type: text/html");
            //         break;
            //     case ".txt":
            //         out.println("Content-Type: text/plain");
            //         break;
            //     case ".jpg":
            //     case ".jpeg":
            //         out.println("Content-Type: image/jpeg");
            //         break;
            //     case ".gif":
            //         out.println("Content-Type: image/gif");
            //         break;
            //     default:
            // }

            // StringBuilder contentBuilder = new StringBuilder();
            // try {
            //     BufferedReader in2 = new BufferedReader(new FileReader(documentRoot + requestTarget));
            //     String s;
            //     while ((s = in2.readLine()) != null) {
            //         contentBuilder.append(s);
            //     }
            //     in2.close();
            // } catch (IOException e) {
            //     System.out.println(e.getMessage());
            // }
            // String response = contentBuilder.toString();

            // out.println("Content-Length: " + response.length());
            // out.println("Date: " + new Date());
            // out.println("\r\n");
            
            // out.println(response);
            // END: Working code for HTML and TXT Files


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

                out.writeBytes("Content-Length: " + response.length() + "\r\n");
                out.writeBytes("Date: " + new Date() + "\r\n");
                out.writeBytes("\r\n");
                out.writeBytes(response + "\r\n");
            } 
            
            else if (requestTargetExtension.equals(".jpeg") || requestTargetExtension.equals(".jpg") || requestTargetExtension.equals(".gif")) {
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

                File file = new File(documentRoot + requestTarget);
                int numOfBytes = (int) file.length();
                FileInputStream fis = new FileInputStream(documentRoot + requestTarget);
                byte[] fileInBytes = new byte[numOfBytes];
                fis.read(fileInBytes);



                out.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                out.writeBytes("Date: " + new Date() + "\r\n");
                out.writeBytes("\r\n");
                out.write(fileInBytes, 0, numOfBytes);
                out.close();

                // out.println("Content-Length: " + response.length());
                // out.println("Date: " + new Date());
                // out.println("\r\n");

                // START: NOT WORKING
                // BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(documentRoot + requestTarget)));
                // BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                
                // byte[] buffer = new byte[8192];
                // int numOfBytes;
                // while ((numOfBytes = bis.read(buffer)) != -1) {
                //     bos.write(buffer, 0, numOfBytes);
                // }
                // bos.flush();
                // bos.close();
                // END: NOT WORKING

                // START NOT WORKING
                // File file = new File(documentRoot + requestTarget);
                // ImageInputStream imageIn = ImageIO.createImageInputStream(file);
                // long size = imageIn.length();

                // BufferedImage bufferedImage = ImageIO.read(file);

                // boolean success = ImageIO.write(bufferedImage, "jpeg", socket.getOutputStream());

                // out.println("\r\n");
                // END NOT WORKING
            }
            
            
            
            else {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("HTTP/1.1 404 Not Found");
                out.println("Date: " + new Date());
                out.println("Connection: close");
                out.close();
            }


            // else if (requestTargetExtension.equals(".jpeg") || requestTargetExtension.equals(".jpg")) {
            //     System.out.println("jpg file");
            //     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //     out.println("HTTP/1.1 200 OK");
            //     switch (requestTargetExtension) {
            //         case ".htm":
            //         case ".html":
            //             out.println("Content-Type: text/html");
            //             break;
            //         case ".txt":
            //             out.println("Content-Type: text/plain");
            //             break;
            //         case ".jpg":
            //         case ".jpeg":
            //             out.println("Content-Type: image/jpeg");
            //             break;
            //         case ".gif":
            //             out.println("Content-Type: image/gif");
            //             break;
            //         default:
            //     }

                // out.println("Content-Length: " + response.length());
            //     out.println("Date: " + new Date());
            //     out.println("\r\n");

            //     BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(documentRoot + requestTarget)));
            //     BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                
            //     byte[] buffer = new byte[8192];
            //     int numOfBytes;
            //     while ((numOfBytes = bis.read(buffer)) != -1) {
            //         bos.write(buffer, 0, numOfBytes);
            //     }
            //     bos.flush();
            //     bos.close();
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
        return this.getRequestTarget().substring(this.getRequestTarget().lastIndexOf('.'));
    }
}