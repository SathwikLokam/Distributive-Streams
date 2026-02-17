import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client {
    private static final int DEFAULT_PORT = 4331;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <script-path> [port]");
            return;
        }

        String host = args[0];
        Path scriptPath = Path.of(args[1]);
        int port = args.length >= 3 ? Integer.parseInt(args[2]) : DEFAULT_PORT;

        if (!Files.exists(scriptPath)) {
            System.err.println("Script file does not exist: " + scriptPath);
            return;
        }

        try {
            Protocol.Response response = sendScript(host, port, scriptPath);
            if (response.isSuccess()) {
                System.out.println("Script executed successfully.");
            } else {
                System.out.println("Script execution failed. exitCode=" + response.getExitCode());
            }
            System.out.println("--- Script Output ---");
            System.out.print(response.getOutput());
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    private static Protocol.Response sendScript(String host, int port, Path scriptPath) throws IOException {
        byte[] scriptContent = Files.readAllBytes(scriptPath);
        if (scriptContent.length > Protocol.MAX_SCRIPT_SIZE_BYTES) {
            throw new IOException("Script file exceeds max allowed size of " + Protocol.MAX_SCRIPT_SIZE_BYTES + " bytes");
        }

        try (Socket socket = new Socket(host, port);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {

            Protocol.writeRequest(outputStream, scriptPath.getFileName().toString(), scriptContent);
            return Protocol.readResponse(inputStream);
        }
    }
}
