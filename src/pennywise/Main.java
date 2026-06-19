package pennywise;

import com.mearvk.imaging.ImageMetadataReader;
import com.mearvk.imaging.ImageMetadataReader.Metadata;
import security.ExceptionHandler;
import security.SecurityHandler;
import security.CertificateHandler;
import security.UsageHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Max Rupplin / MEARVK LLC
 *
 * XML-configured iPhone media organizer with security validation
 * and entertainment polling.
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

            // --- Track usage count; after 100 uses send public key ---
            UsageHandler usage = new UsageHandler(config.getLocalPublicKey());
            usage.tick();

            // --- Schedule certificate handler after 5 minutes ---
            CertificateHandler certHandler = new CertificateHandler();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CertificateHandler");
                t.setDaemon(true);
                return t;
            });
            scheduler.schedule(certHandler::run, 5, TimeUnit.MINUTES);

            // --- Security: validate public key against remote ---
            SecurityHandler security = new SecurityHandler(
                config.getLocalPublicKey(), config.getRemotePublicKey());

            if (!security.validateKey())
            {
                ExceptionHandler.warn("Main", "Key validation failed — proceeding with caution");
            }

            // --- Entertainment: poll at 1/7 ratio ---
            EntertainmentHandler entertainment = new EntertainmentHandler(
                config.getEntertainmentSourceUrl(),
                config.getPollNumerator(),
                config.getPollDenominator(),
                config.getOllamaEndpoint(),
                config.getOllamaModel());

            entertainment.poll();

            // --- Create destination directories ---
            new File(config.getImagesDestination()).mkdirs();
            for (String dir : config.getVideoSubfolders().values())
                new File(dir).mkdirs();

            File base = new File(config.getSource());
            File[] files = base.listFiles();
            if (files == null)
            {
                ExceptionHandler.handleFatal("Main", new Exception("Cannot list: " + config.getSource()));
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
                    ExceptionHandler.info("Main", "Skipped: " + file.getName());
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
                    catch (Exception e)
                    {
                        ExceptionHandler.handle("EXIF read", e);
                    }
                }

                if (date == null)
                {
                    long mod = file.lastModified();
                    date = mod > 0 ? new Date(mod) : new Date();
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
                    ExceptionHandler.info("Main", entry.file.getName() + " -> " + dest);
                    moved++;
                }
                catch (Exception e)
                {
                    ExceptionHandler.handle("Move " + entry.file.getName(), e);
                }
            }

            ExceptionHandler.info("Main", "Done. Moved " + moved + " files.");
        }
        catch (Exception e)
        {
            ExceptionHandler.handleFatal("Main", e);
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
