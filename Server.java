import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) {
        int port = 8053;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
                System.out.println("new line");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}