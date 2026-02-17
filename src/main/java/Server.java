import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int DEFAULT_PORT = 4331;
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService workerPool = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                workerPool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            workerPool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        try (Socket clientSocket = socket;
             DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream())) {

            clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
            Protocol.Request request = Protocol.readRequest(inputStream);
            String safeFileName = Protocol.sanitizeFileName(request.getFileName());
            Path tempScript = Files.createTempFile("uploaded_", "_" + safeFileName);

            try {
                Files.write(tempScript, request.getContent());
                Protocol.Response response = ScriptExecutor.executePythonScript(tempScript);
                Protocol.writeResponse(outputStream, response);
            } finally {
                Files.deleteIfExists(tempScript);
            }
        } catch (Exception e) {
            System.err.println("Failed to process client request: " + e.getMessage());
        }
    }
}
