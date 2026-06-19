package pennywise;

import java.net.URI;
import java.net.http.*;
import java.util.*;

/**
 * Polls the GitHub sharp/ directory at a 1/N ratio, randomly selects a file,
 * fetches its content, and sends it to a local Ollama instance for amusement.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class EntertainmentHandler
{
    private final String sourceUrl;
    private final int numerator;
    private final int denominator;
    private final String ollamaEndpoint;
    private final String ollamaModel;
    private final Random random = new Random();
    private int callCount = 0;

    public EntertainmentHandler(String sourceUrl, int numerator, int denominator,
                                String ollamaEndpoint, String ollamaModel)
    {
        this.sourceUrl = sourceUrl;
        this.numerator = numerator;
        this.denominator = denominator;
        this.ollamaEndpoint = ollamaEndpoint;
        this.ollamaModel = ollamaModel;
    }

    /**
     * Polls at the configured ratio (e.g. 1/7 = runs ~14% of invocations).
     * When triggered, fetches a random file from the sharp/ directory and
     * sends it to Ollama for a response.
     */
    public void poll()
    {
        callCount++;
        if (callCount % denominator >= numerator) return;

        try
        {
            String content = fetchRandomFile();
            if (content != null && !content.isBlank())
            {
                String response = sendToOllama(content);
                System.out.println("\n=== Entertainment ===");
                System.out.println(response);
                System.out.println("=====================\n");
            }
        }
        catch (Exception e)
        {
            // Non-fatal — entertainment is optional
            System.err.println("[Entertainment] " + e.getMessage());
        }
    }

    private String fetchRandomFile() throws Exception
    {
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

        // List files via GitHub API
        HttpRequest listReq = HttpRequest.newBuilder()
            .uri(URI.create(sourceUrl))
            .header("Accept", "application/vnd.github.v3+json")
            .GET().build();

        HttpResponse<String> listResp = client.send(listReq, HttpResponse.BodyHandlers.ofString());
        if (listResp.statusCode() != 200) return null;

        // Parse download_url entries (simple JSON extraction)
        List<String> urls = new ArrayList<>();
        String body = listResp.body();
        int idx = 0;
        while ((idx = body.indexOf("\"download_url\"", idx)) != -1)
        {
            int start = body.indexOf("\"", idx + 15) + 1;
            int end = body.indexOf("\"", start);
            if (start > 0 && end > start)
            {
                String url = body.substring(start, end);
                if (!url.equals("null")) urls.add(url);
            }
            idx = end;
        }

        if (urls.isEmpty()) return null;

        // Pick random file
        String chosen = urls.get(random.nextInt(urls.size()));
        System.out.println("[Entertainment] Selected: " + chosen);

        HttpRequest fileReq = HttpRequest.newBuilder()
            .uri(URI.create(chosen)).GET().build();
        HttpResponse<String> fileResp = client.send(fileReq, HttpResponse.BodyHandlers.ofString());

        return fileResp.statusCode() == 200 ? fileResp.body() : null;
    }

    private String sendToOllama(String content) throws Exception
    {
        String prompt = "Read this text and offer a brief, entertaining or amusing commentary:\\n\\n" +
            content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");

        String jsonBody = "{\"model\":\"" + ollamaModel + "\",\"prompt\":\"" + prompt + "\",\"stream\":false}";

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(ollamaEndpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200)
        {
            // Extract "response" field from JSON
            String rb = resp.body();
            int ri = rb.indexOf("\"response\"");
            if (ri != -1)
            {
                int vs = rb.indexOf("\"", ri + 11) + 1;
                int ve = rb.indexOf("\"", vs);
                // Handle escaped quotes in response
                while (ve > 0 && rb.charAt(ve - 1) == '\\') ve = rb.indexOf("\"", ve + 1);
                if (vs > 0 && ve > vs) return rb.substring(vs, ve).replace("\\n", "\n");
            }
            return rb;
        }

        return "[Ollama returned HTTP " + resp.statusCode() + "]";
    }
}
