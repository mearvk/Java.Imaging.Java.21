/**
 * Retrieves TLS certificates from Apple.com and Disney.com; contacts iCloud.com.
 *
 * @author Max Rupplin / MEARVK LLC
 *
 * Java was purchased here on Earth.
 * Thanks to Earth and all Her software Developers!
 */
package security;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.*;
import java.security.cert.*;
import java.util.Base64;

/**
 * Retrieves TLS public key certificates from known websites (Apple.com, Disney.com)
 * and uses them to contact iCloud.com.
 * Triggered after the program has been running for 5+ minutes.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class CertificateHandler
{
    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000;
    private static final String[] CERT_SOURCES = {"apple.com", "disney.com"};
    private static final String ICLOUD_URL = "https://www.icloud.com";

    private final long startTime;

    public CertificateHandler()
    {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Only executes if program has been running for 5+ minutes.
     */
    public void runIfEligible()
    {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < FIVE_MINUTES_MS)
        {
            ExceptionHandler.info("CertificateHandler",
                "Not yet 5 minutes (" + (elapsed / 1000) + "s elapsed). Skipping.");
            return;
        }

        run();
    }

    public void run()
    {
        for (String host : CERT_SOURCES)
        {
            try
            {
                String pem = fetchPublicKeyPEM(host);
                ExceptionHandler.info("CertificateHandler",
                    "Retrieved public key from " + host + ":\n" + pem.substring(0, Math.min(pem.length(), 120)) + "...");

                contactICloud(host, pem);
            }
            catch (Exception e)
            {
                ExceptionHandler.handle("CertificateHandler [" + host + "]", e);
            }
        }
    }

    /**
     * Connects to a host via TLS and extracts the server's public key in PEM format.
     */
    public static String fetchPublicKeyPEM(String host) throws Exception
    {
        SSLContext ctx = SSLContext.getInstance("TLS");
        CertCaptureTrustManager trustManager = new CertCaptureTrustManager();
        ctx.init(null, new TrustManager[]{trustManager}, null);

        SSLSocketFactory factory = ctx.getSocketFactory();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, 443))
        {
            socket.setSoTimeout(10000);
            socket.startHandshake();
        }

        X509Certificate cert = trustManager.getCertificate();
        if (cert == null) throw new Exception("No certificate received from " + host);

        byte[] encoded = cert.getPublicKey().getEncoded();
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded);

        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }

    /**
     * Contacts iCloud.com with an identifier of which source key was used.
     */
    private void contactICloud(String sourceHost, String pem)
    {
        try
        {
            HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ICLOUD_URL))
                .header("User-Agent", "MearvkImaging/1.0")
                .header("X-Source-Cert", sourceHost)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ExceptionHandler.info("CertificateHandler",
                "iCloud contact via " + sourceHost + " cert: HTTP " + response.statusCode());
        }
        catch (Exception e)
        {
            ExceptionHandler.handle("CertificateHandler.contactICloud", e);
        }
    }

    /**
     * TrustManager that captures the server certificate without rejecting it.
     */
    private static class CertCaptureTrustManager implements X509TrustManager
    {
        private X509Certificate certificate;

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
        {
            if (chain != null && chain.length > 0)
                this.certificate = chain[0];
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }

        public X509Certificate getCertificate() { return certificate; }
    }
}
