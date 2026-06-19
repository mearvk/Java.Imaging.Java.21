package pennywise;

import com.mearvk.imaging.ImageMetadataReader;
import com.mearvk.imaging.ImageMetadataReader.Metadata;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Max Rupplin / MEARVK LLC
 *
 * XML-configured iPhone media organizer.
 * Reads config.xml for source/destination paths and file type definitions.
 * Separates videos into type-specific subfolders (MOVs/, MP4s/, etc.)
 * and images into a single Images/ folder, all renamed by date.
 *
 * Usage: java pennywise.Main [path/to/config.xml]
 */
public class Main
{
    public static void main(String... args)
    {
        try
        {
            String configPath = args.length > 0 ? args[0] : "src/pennywise/config.xml";
            XMLHandler config = new XMLHandler(configPath);

            // Create destination directories
            new File(config.getImagesDestination()).mkdirs();
            for (String dir : config.getVideoSubfolders().values())
                new File(dir).mkdirs();

            File base = new File(config.getSource());
            File[] files = base.listFiles();
            if (files == null)
            {
                System.err.println("Cannot list: " + config.getSource());
                return;
            }

            ArrayList<MediaEntry> entries = new ArrayList<>();

            for (File file : files)
            {
                if (file.isDirectory()) continue;

                String ext = getExtension(file.getName()).toLowerCase();
                boolean isVideo = config.getVideoExtensions().contains(ext);
                boolean isImage = config.getImageExtensions().contains(ext);

                if (!isVideo && !isImage)
                {
                    System.out.println("Skipped: " + file.getName());
                    continue;
                }

                Date date = null;

                if (isImage)
                {
                    try
                    {
                        Metadata metadata = ImageMetadataReader.readMetadata(file);
                        date = metadata.getDateOriginal();
                    }
                    catch (Exception ignored) {}
                }

                if (date == null)
                {
                    long mod = file.lastModified();
                    if (mod > 0) date = new Date(mod);
                    else date = new Date();
                }

                String destDir = isVideo
                    ? config.getVideoDestination(ext)
                    : config.getImagesDestination();

                entries.add(new MediaEntry(file, date, destDir, ext));
            }

            entries.sort(Comparator.comparing(e -> e.date));

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            Map<String, Integer> nameCount = new HashMap<>();
            int moved = 0;

            for (MediaEntry entry : entries)
            {
                String key = entry.destDir + "/" + fmt.format(entry.date) + entry.ext;
                int seq = nameCount.getOrDefault(key, 0) + 1;
                nameCount.put(key, seq);

                String newName = fmt.format(entry.date) + "_" + String.format("%03d", seq) + entry.ext;
                Path dest = Path.of(entry.destDir, newName);

                try
                {
                    Files.copy(entry.file.toPath(), dest);
                    Files.delete(entry.file.toPath());
                    System.out.println(entry.file.getName() + " -> " + dest);
                    moved++;
                }
                catch (Exception e)
                {
                    System.err.println("Failed: " + entry.file.getName() + " - " + e.getMessage());
                }
            }

            System.out.println("Done. Moved " + moved + " files.");
        }
        catch (Exception e)
        {
            System.err.println("Fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getExtension(String filename)
    {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    static class MediaEntry
    {
        final File file;
        final Date date;
        final String destDir;
        final String ext;

        MediaEntry(File file, Date date, String destDir, String ext)
        {
            this.file = file;
            this.date = date;
            this.destDir = destDir;
            this.ext = ext;
        }
    }
}
