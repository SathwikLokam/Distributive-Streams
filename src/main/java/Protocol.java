import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class Protocol {
    public static final int MAX_SCRIPT_SIZE_BYTES = 5 * 1024 * 1024;

    private Protocol() {
    }

    public static void writeRequest(DataOutputStream outputStream, String fileName, byte[] fileContent) throws IOException {
        outputStream.writeUTF(fileName);
        outputStream.writeInt(fileContent.length);
        outputStream.write(fileContent);
        outputStream.flush();
    }

    public static Request readRequest(DataInputStream inputStream) throws IOException {
        String fileName = inputStream.readUTF();
        int length = inputStream.readInt();

        if (length < 0 || length > MAX_SCRIPT_SIZE_BYTES) {
            throw new IOException("Invalid script size: " + length + " bytes");
        }

        byte[] content = new byte[length];
        inputStream.readFully(content);
        return new Request(fileName, content);
    }

    public static void writeResponse(DataOutputStream outputStream, Response response) throws IOException {
        outputStream.writeBoolean(response.success);
        outputStream.writeInt(response.exitCode);
        outputStream.writeUTF(response.output);
        outputStream.flush();
    }

    public static Response readResponse(DataInputStream inputStream) throws IOException {
        boolean success = inputStream.readBoolean();
        int exitCode = inputStream.readInt();
        String output = inputStream.readUTF();
        return new Response(success, exitCode, output);
    }

    public static String sanitizeFileName(String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.isBlank() ? "script.py" : sanitized;
    }

    public static final class Request {
        private final String fileName;
        private final byte[] content;

        public Request(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }
    }

    public static final class Response {
        private final boolean success;
        private final int exitCode;
        private final String output;

        public Response(boolean success, int exitCode, String output) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("success=").append(success)
                    .append(", exitCode=").append(exitCode)
                    .append(", output=").append(output)
                    .toString();
        }
    }
}
