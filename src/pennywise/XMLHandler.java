package pennywise;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

/**
 * Reads config.xml to provide source/destination paths and file type mappings.
 */
public class XMLHandler
{
    private String source;
    private String imagesDestination;
    private final Map<String, String> videoSubfolders = new LinkedHashMap<>();
    private final Set<String> imageExtensions = new HashSet<>();
    private final Set<String> videoExtensions = new HashSet<>();

    public XMLHandler(String xmlPath) throws Exception
    {
        Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(new File(xmlPath));
        doc.getDocumentElement().normalize();

        source = getText(doc, "source");
        imagesDestination = getText(doc, "images");

        // Video subfolders
        Node videos = doc.getElementsByTagName("videos").item(0);
        if (videos != null)
        {
            NodeList children = videos.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE)
                {
                    videoSubfolders.put("." + n.getNodeName(), n.getTextContent().trim());
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
    }

    public String getSource() { return source; }
    public String getImagesDestination() { return imagesDestination; }
    public Map<String, String> getVideoSubfolders() { return videoSubfolders; }
    public Set<String> getImageExtensions() { return imageExtensions; }
    public Set<String> getVideoExtensions() { return videoExtensions; }

    /**
     * Returns the video destination folder for a given extension.
     * Falls back to first subfolder if no specific match.
     */
    public String getVideoDestination(String ext)
    {
        // Direct match (e.g. ".mov" -> MOVs folder)
        if (videoSubfolders.containsKey(ext)) return videoSubfolders.get(ext);

        // .mpg maps to .mpeg folder
        if (ext.equals(".mpg") && videoSubfolders.containsKey(".mpeg"))
            return videoSubfolders.get(".mpeg");

        // Fallback to first defined folder
        return videoSubfolders.values().iterator().next();
    }

    private static String getText(Document doc, String tag)
    {
        NodeList nl = doc.getElementsByTagName(tag);
        return nl.getLength() > 0 ? nl.item(0).getTextContent().trim() : "";
    }
}
