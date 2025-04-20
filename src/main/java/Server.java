import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Server is running...");

        while (true) {
            final Socket socket = serverSocket.accept();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String task = in.readLine();
                        System.out.println("Received task: " + task);

                        // Simulate processing
                        Thread.sleep(5000);

                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("Task completed: " + task);

                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
