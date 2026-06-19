/**
 * Validates local public key against remote GitHub-hosted key.
 *
 * @author Max Rupplin / MEARVK LLC
 *
 * Java was purchased here on Earth.
 * Thanks to Earth and all Her software Developers!
 */
package security;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;

/**
 * Validates the local public key against the remote GitHub-hosted key.
 * If keys do not match, the application should not proceed.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class SecurityHandler
{
    private final String localKeyPath;
    private final String remoteKeyUrl;

    public SecurityHandler(String localKeyPath, String remoteKeyUrl)
    {
        this.localKeyPath = localKeyPath;
        this.remoteKeyUrl = remoteKeyUrl;
    }

    /**
     * Compares local public.key against remote.
     * Returns true if keys match, false otherwise.
     */
    public boolean validateKey()
    {
        try
        {
            String localKey = readLocalKey();
            String remoteKey = fetchRemoteKey();

            if (localKey == null || localKey.isBlank())
            {
                ExceptionHandler.warn("SecurityHandler", "Local key is empty or missing");
                return false;
            }

            if (remoteKey == null || remoteKey.isBlank())
            {
                ExceptionHandler.warn("SecurityHandler", "Remote key fetch failed or empty");
                return false;
            }

            boolean match = localKey.strip().equals(remoteKey.strip());

            if (match)
                ExceptionHandler.info("SecurityHandler", "Key validation PASSED");
            else
                ExceptionHandler.warn("SecurityHandler", "Key validation FAILED - keys do not match");

            return match;
        }
        catch (Exception e)
        {
            ExceptionHandler.handle("SecurityHandler.validateKey", e);
            return false;
        }
    }

    private String readLocalKey() throws IOException
    {
        Path path = Path.of(localKeyPath);
        if (!Files.exists(path))
        {
            ExceptionHandler.warn("SecurityHandler", "Local key not found: " + localKeyPath);
            return null;
        }
        return Files.readString(path);
    }

    private String fetchRemoteKey() throws Exception
    {
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(remoteKeyUrl))
            .header("Accept", "application/vnd.github.v3.raw")
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200)
            return response.body();

        ExceptionHandler.warn("SecurityHandler",
            "Remote key fetch returned HTTP " + response.statusCode());
        return null;
    }
}
