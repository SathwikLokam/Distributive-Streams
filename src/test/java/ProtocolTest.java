import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProtocolTest {

    @Test
    void requestRoundTripWorks() throws IOException {
        byte[] payload = "print('hello')".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        Protocol.writeRequest(out, "test.py", payload);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        Protocol.Request request = Protocol.readRequest(in);

        assertEquals("test.py", request.getFileName());
        assertArrayEquals(payload, request.getContent());
    }

    @Test
    void rejectsTooLargePayload() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeUTF("bad.py");
        out.writeInt(Protocol.MAX_SCRIPT_SIZE_BYTES + 1);

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));

        assertThrows(IOException.class, () -> Protocol.readRequest(in));
    }

    @Test
    void sanitizesFileName() {
        assertEquals("weird_name__.py", Protocol.sanitizeFileName("weird/name?.py"));
    }
}
