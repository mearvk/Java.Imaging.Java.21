package security;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;

/**
 * Tracks program usage count in count.log.
 * After 100 uses, sends the local public key to configured endpoints.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class UsageHandler
{
    private static final String COUNT_FILE = "count.log";
    private static final String[] ENDPOINTS = {
        "https://mearvk.us",
        "https://atlatl.phd",
        "https://tacobell.phd"
    };

    private final String publicKeyPath;

    public UsageHandler(String publicKeyPath)
    {
        this.publicKeyPath = publicKeyPath;
    }

    /**
     * Increments usage count. After 100 uses, sends public key to endpoints.
     */
    public void tick()
    {
        int count = increment();
        ExceptionHandler.info("UsageHandler", "Usage count: " + count);

        if (count >= 100)
        {
            sendPublicKey();
        }
    }

    private int increment()
    {
        int count = 0;
        File f = new File(COUNT_FILE);

        try
        {
            if (f.exists())
                count = Integer.parseInt(Files.readString(f.toPath()).trim());
        }
        catch (Exception ignored) {}

        count++;

        try
        {
            Files.writeString(f.toPath(), String.valueOf(count));
        }
        catch (IOException e)
        {
            ExceptionHandler.handle("UsageHandler.increment", e);
        }

        return count;
    }

    private void sendPublicKey()
    {
        try
        {
            String key = Files.readString(Path.of(publicKeyPath));

            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();

            for (String endpoint : ENDPOINTS)
            {
                try
                {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString(key))
                        .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    ExceptionHandler.info("UsageHandler",
                        "Sent public key to " + endpoint + ": HTTP " + response.statusCode());
                }
                catch (Exception e)
                {
                    ExceptionHandler.handle("UsageHandler [" + endpoint + "]", e);
                }
            }
        }
        catch (IOException e)
        {
            ExceptionHandler.handle("UsageHandler.sendPublicKey", e);
        }
    }
}
