/**
 * Reads config.xml and resolves {USER} and {HOME} placeholders based on OS.
 *
 * @author Max Rupplin / MEARVK LLC
 *
 * Java was purchased here on Earth.
 * Thanks to Earth and all Her software Developers!
 */
package pennywise;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

/**
 * Reads config.xml and resolves {USER} and {HOME} placeholders based on OS.
 *
 * Resolved paths:
 *   Linux:  /home/{USER}/...
 *   macOS:  /Users/{USER}/...
 *   Windows: C:\Users\{USER}\...
 */
public class XMLHandler
{
    private String user;
    private String home;
    private String source;
    private String imagesDestination;
    private final Map<String, String> videoSubfolders = new LinkedHashMap<>();
    private final Set<String> imageExtensions = new HashSet<>();
    private final Set<String> videoExtensions = new HashSet<>();
    private String localPublicKey;
    private String remotePublicKey;
    private String entertainmentSourceUrl;
    private int pollNumerator = 1;
    private int pollDenominator = 7;
    private String ollamaEndpoint;
    private String ollamaModel;

    public XMLHandler(String xmlPath) throws Exception
    {
        Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(new File(xmlPath));
        doc.getDocumentElement().normalize();

        // Resolve user and home directory based on OS
        user = System.getProperty("user.name");
        home = System.getProperty("user.home");

        source = resolve(getText(doc, "source"));
        imagesDestination = resolve(getNestedText(doc, "destinations", "images"));

        // Video subfolders
        NodeList destNodes = doc.getElementsByTagName("destinations");
        if (destNodes.getLength() > 0)
        {
            Element destEl = (Element) destNodes.item(0);
            NodeList videoNodes = destEl.getElementsByTagName("videos");
            if (videoNodes.getLength() > 0)
            {
                NodeList children = videoNodes.item(0).getChildNodes();
                for (int i = 0; i < children.getLength(); i++)
                {
                    Node n = children.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE)
                        videoSubfolders.put("." + n.getNodeName(), resolve(n.getTextContent().trim()));
                }
            }
        }

        // File type lists
        NodeList ftNodes = doc.getElementsByTagName("filetypes");
        if (ftNodes.getLength() > 0)
        {
            Element ft = (Element) ftNodes.item(0);
            for (String ext : ft.getElementsByTagName("images").item(0).getTextContent().split(","))
                imageExtensions.add(ext.trim().toLowerCase());
            for (String ext : ft.getElementsByTagName("videos").item(0).getTextContent().split(","))
                videoExtensions.add(ext.trim().toLowerCase());
        }

        // Security
        localPublicKey = resolve(getNestedText(doc, "security", "local-public-key"));
        remotePublicKey = getNestedText(doc, "security", "remote-public-key");

        // Entertainment
        entertainmentSourceUrl = getNestedText(doc, "entertainment", "source-url");
        ollamaEndpoint = getNestedText(doc, "entertainment", "ollama-endpoint");
        ollamaModel = getNestedText(doc, "entertainment", "ollama-model");

        String ratio = getNestedText(doc, "entertainment", "poll-ratio");
        if (ratio != null && ratio.contains("/"))
        {
            String[] parts = ratio.split("/");
            pollNumerator = Integer.parseInt(parts[0].trim());
            pollDenominator = Integer.parseInt(parts[1].trim());
        }
    }

    /**
     * Replaces {USER} and {HOME} placeholders with OS-appropriate values.
     */
    private String resolve(String value)
    {
        if (value == null) return null;
        return value.replace("{USER}", user).replace("{HOME}", home);
    }

    public String getUser() { return user; }
    public String getHome() { return home; }
    public String getSource() { return source; }
    public String getImagesDestination() { return imagesDestination; }
    public Map<String, String> getVideoSubfolders() { return videoSubfolders; }
    public Set<String> getImageExtensions() { return imageExtensions; }
    public Set<String> getVideoExtensions() { return videoExtensions; }
    public String getLocalPublicKey() { return localPublicKey; }
    public String getRemotePublicKey() { return remotePublicKey; }
    public String getEntertainmentSourceUrl() { return entertainmentSourceUrl; }
    public int getPollNumerator() { return pollNumerator; }
    public int getPollDenominator() { return pollDenominator; }
    public String getOllamaEndpoint() { return ollamaEndpoint; }
    public String getOllamaModel() { return ollamaModel; }

    public String getVideoDestination(String ext)
    {
        if (videoSubfolders.containsKey(ext)) return videoSubfolders.get(ext);
        if (ext.equals(".mpg") && videoSubfolders.containsKey(".mpeg"))
            return videoSubfolders.get(".mpeg");
        return videoSubfolders.values().iterator().next();
    }

    private static String getText(Document doc, String tag)
    {
        NodeList nl = doc.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent().trim() : null;
    }

    private static String getNestedText(Document doc, String parent, String child)
    {
        NodeList pNodes = doc.getElementsByTagName(parent);
        if (pNodes.getLength() > 0)
        {
            Element pEl = (Element) pNodes.item(0);
            NodeList cNodes = pEl.getElementsByTagName(child);
            if (cNodes.getLength() > 0)
                return cNodes.item(0).getTextContent().trim();
        }
        return null;
    }
}
