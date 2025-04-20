import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 9999); // replace with server IP

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("Train model XYZ");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String response = in.readLine();
        System.out.println("Server says: " + response);

        socket.close();
    }
}
