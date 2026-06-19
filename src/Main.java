/**
 * @author Max Rupplin / MEARVK LLC
 *
 * iPhone Photo/Video Organizer:
 * - Scans a source folder of mixed iPhone media
 * - Moves VIDEO files (.mov, .mp4, .mpeg, .mpg, .avi) to a Videos/ destination
 * - Moves IMAGE files (.jpg, .jpeg, .heic, .png, .tiff, .bmp, .gif) to an Images/ destination
 * - Renames all files by date (EXIF for images, lastModified fallback)
 *   so that alphabetical order = chronological order
 * - Format: YYYY-MM-DD_HH-mm-ss_NNN.ext (NNN = sequence to prevent collisions)
 */

import com.mearvk.imaging.ImageMetadataReader;
import com.mearvk.imaging.ImageMetadataReader.Metadata;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main
{
    static final String source_directory = "/home/mearvk/Desktop/Photos";
    static final String images_destination = "/home/mearvk/Desktop/Organized/Images";
    static final String videos_destination = "/home/mearvk/Desktop/Organized/Videos";

    static final Set<String> VIDEO_EXTENSIONS = Set.of(
        ".mov", ".mp4", ".mpeg", ".mpg", ".avi", ".m4v"
    );

    static final Set<String> IMAGE_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".heic", ".png", ".tiff", ".tif", ".bmp", ".gif", ".webp"
    );

    public static void main(String... args)
    {
        String src = args.length > 0 ? args[0] : source_directory;

        new File(images_destination).mkdirs();
        new File(videos_destination).mkdirs();

        File base = new File(src);
        File[] files = base.listFiles();
        if (files == null)
        {
            System.err.println("Cannot list directory: " + src);
            return;
        }

        ArrayList<MediaEntry> entries = new ArrayList<>();

        for (File file : files)
        {
            if (file.isDirectory()) continue;

            String ext = getExtension(file.getName()).toLowerCase();
            boolean isVideo = VIDEO_EXTENSIONS.contains(ext);
            boolean isImage = IMAGE_EXTENSIONS.contains(ext);

            if (!isVideo && !isImage)
            {
                System.out.println("Skipped (unknown type): " + file.getName());
                continue;
            }

            Date date = null;

            // Try EXIF date for images
            if (isImage)
            {
                try
                {
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    date = metadata.getDateOriginal();
                }
                catch (Exception e)
                {
                    // Fall through to lastModified
                }
            }

            // Fallback: file last-modified time
            if (date == null)
            {
                long mod = file.lastModified();
                if (mod > 0) date = new Date(mod);
            }

            if (date == null) date = new Date(); // last resort

            entries.add(new MediaEntry(file, date, isVideo, ext));
        }

        // Sort all by date
        entries.sort(Comparator.comparing(e -> e.date));

        // Rename and move, using sequence numbers to avoid collisions
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Map<String, Integer> nameCount = new HashMap<>();

        int movedImages = 0, movedVideos = 0;

        for (MediaEntry entry : entries)
        {
            String baseName = fmt.format(entry.date);

            // Append sequence if duplicate timestamp
            int seq = nameCount.getOrDefault(baseName + entry.ext, 0) + 1;
            nameCount.put(baseName + entry.ext, seq);

            String newName = baseName + "_" + String.format("%03d", seq) + entry.ext;
            String destDir = entry.isVideo ? videos_destination : images_destination;
            Path dest = Path.of(destDir, newName);

            try
            {
                Files.copy(entry.file.toPath(), dest);
                Files.delete(entry.file.toPath());
                System.out.println(entry.file.getName() + " -> " + destDir + "/" + newName);

                if (entry.isVideo) movedVideos++;
                else movedImages++;
            }
            catch (Exception e)
            {
                System.err.println("Failed: " + entry.file.getName() + " - " + e.getMessage());
            }
        }

        System.out.println("Done. Images: " + movedImages + ", Videos: " + movedVideos);
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
        final boolean isVideo;
        final String ext;

        MediaEntry(File file, Date date, boolean isVideo, String ext)
        {
            this.file = file;
            this.date = date;
            this.isVideo = isVideo;
            this.ext = ext;
        }
    }
}
