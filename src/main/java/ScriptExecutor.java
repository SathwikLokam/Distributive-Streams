import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class ScriptExecutor {
    private static final long PROCESS_TIMEOUT_SECONDS = 20;

    private ScriptExecutor() {
    }

    public static Protocol.Response executePythonScript(Path scriptPath) {
        String pythonCommand = System.getenv().getOrDefault("PYTHON_CMD", "python3");
        ProcessBuilder processBuilder = new ProcessBuilder(pythonCommand, scriptPath.toString());
        processBuilder.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();
        int exitCode = -1;

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                output.append("Script execution timed out after ")
                        .append(PROCESS_TIMEOUT_SECONDS)
                        .append(" seconds")
                        .append(System.lineSeparator());
                return new Protocol.Response(false, -1, output.toString());
            }

            exitCode = process.exitValue();
            return new Protocol.Response(exitCode == 0, exitCode, output.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            output.append("Script execution interrupted: ").append(e.getMessage());
            return new Protocol.Response(false, exitCode, output.toString());
        } catch (IOException e) {
            output.append("Failed to execute script: ").append(e.getMessage());
            return new Protocol.Response(false, exitCode, output.toString());
        }
    }
}
