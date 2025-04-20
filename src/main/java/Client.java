import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {
        int port = 4331;  // Change to your required port number

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("Client connected");

                    // Receive the file from the client
                    File receivedFile = receiveFile(socket);

                    // Process the Python file and return the result
                    String result = executePythonScript(receivedFile.getAbsolutePath());

                    // Send the result back to the client
                    sendResultToClient(socket, result);

                    // Delete the temporary file after processing and sending the response
                    if (receivedFile.delete()) {
                        System.out.println("Temporary file deleted successfully: " + receivedFile.getName());
                    } else {
                        System.out.println("Failed to delete the temporary file: " + receivedFile.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File receiveFile(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Reading the filename
        String fileName = dataInputStream.readUTF();
        System.out.println("Receiving file: " + fileName);

        // Saving the received Python file as a temporary file with a unique name
        File file = new File("received_" + System.currentTimeMillis() + "_" + fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                // Break if the buffer is smaller than expected, indicating end of file
                if (bytesRead < buffer.length) break;
            }
        }
        System.out.println("File received successfully: " + file.getName());
        return file;  // Return the received file for further processing
    }

    private static String executePythonScript(String filePath) {
        StringBuilder output = new StringBuilder();
        try {
            // Running the Python script using system's Python3
            ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
            processBuilder.redirectErrorStream(true); // Merge stdout and stderr
            Process process = processBuilder.start();

            // Reading the output from the executed Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Error: Python script exited with code ").append(exitCode).append("\n");
            }

        } catch (Exception e) {
            output.append("Failed to execute script: ").append(e.getMessage());
        }

        return output.toString();
    }

    private static void sendResultToClient(Socket socket, String result) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Send the result length to client first
        dataOutputStream.writeInt(result.length());
        dataOutputStream.flush();

        // Send the result content
        dataOutputStream.writeUTF(result);
        dataOutputStream.flush();

        System.out.println("Result sent to client.");
    }
}