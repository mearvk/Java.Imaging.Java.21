/**
 * @author Max Rupplin
 * @date April 4 2016
 * @date Nov. 21st 2721
 *
 * @us.governor Caesar Bernini
 */

import com.mearvk.imaging.ImageMetadataReader;
import com.mearvk.metadata.exif.MetadataReader;
import com.mearvk.metadata.Directory;
import com.mearvk.metadata.Metadata;
import com.mearvk.metadata.Tag;

import java.util.Collections;
import java.util.Date;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.TreeMap;

public class Main
{
    static final String std_output_directory = "/home/mearvk/Desktop/Photos/std.output.txt";

    static final String err_output_directory = "/home/mearvk/Desktop/Photos/err.output.txt";

    static final String initial_directory = "/home/mearvk/Desktop/Photos";

    static final String mov_destination = "/home/mearvk/Desktop/Videos/MOVs";

    static final String mp4_destination = "/home/mearvk/Desktop/Videos/MP4s";

    static HashMap<File, Date> sorted_dates_v_files = new HashMap<File,Date>();

    static ArrayList<String> sorted_file_names = new ArrayList<String>();

    static ArrayList<File> image_and_video_files = new ArrayList<File>();

    public static void main(String...args)
    {
        Object object = null;

        //try{ System.setOut(new PrintStream(new FileOutputStream(std_output_directory))); } catch (Exception e) {}

        try{ System.setErr(new PrintStream(new FileOutputStream(err_output_directory))); } catch (Exception e) {}

        try{ new File("/home/mearvk/Desktop/Videos/MOVs").mkdir(); } catch (Exception e) {}

        try{ new File("/home/mearvk/Desktop/Videos/MP4s").mkdir(); } catch (Exception e) {}

        ImageOpener opener = new ImageOpener(Main.initial_directory);

        ImageSorter sorter = new ImageSorter(Main.image_and_video_files);

        sorter.collection();

        sorter.sort();

        sorter.finalize();
    }

    public static class ImageSorter
    {
        public ArrayList<File> images = new ArrayList<File>();

        public ImageSorter(ArrayList<File> images)
        {
            this.images = images;

            for (File image : this.images)
            {
                try
                {
                    Metadata data = ImageMetadataReader.readMetadata(image);

                    Iterable <Directory> directories =  data.getDirectories();

                    //System.out.println("File name >> "+image.getAbsolutePath());

                    for(Directory directory : directories)
                    {
                        System.out.println("\t"+directory);

                        Iterable <Tag> tags = directory.getTags();

                        for(Tag tag : tags)
                        {
                            System.out.println("\t\t >> "+tag);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

        public void collection()
        {
            ArrayList<String> names = new ArrayList<String>();

            HashMap<File, Date> dates = new HashMap<File, Date>();

            Integer number_of_files = this.images.size();

            for(int i=0; i<number_of_files; i++)
            {
                String file_name = String.format("file_%5d.jpg", i);

                names.add(file_name);
            }

            for(int i=0; i<number_of_files; i++)
            {
                try
                {
                    Metadata data = ImageMetadataReader.readMetadata(this.images.get(i));

                    MetadataReader reader = data.getFirstDirectoryOfType(MetadataReader.class);

                    Date date = reader.getDate(MetadataReader.TAG_DATETIME_ORIGINAL);

                    dates.put(this.images.get(i), date);
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }

            Main.sorted_file_names = names;

            Main.sorted_dates_v_files = dates;
        }

        public void sort()
        {
            TreeMap<File, Date> sorted_map = new TreeMap<>(Collections.reverseOrder());

            sorted_map.putAll(Main.sorted_dates_v_files);
        }

        public void finalize()
        {
            Integer iterator = 0;

            try
            {
                for(HashMap.Entry<File, Date> entry : Main.sorted_dates_v_files.entrySet())
                {
                    File file = entry.getKey();

                    Date date = entry.getValue();

                    System.out.println(Path.of(file.getParent()+"/"+Main.sorted_file_names.get(iterator)));

                    Files.copy(file.toPath(), Path.of(file.getParent()+"/"+Main.sorted_file_names.get(iterator)));

                    Files.delete(file.toPath());

                    iterator++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    public static class ImageOpener
    {
        public ArrayList<File> images = new ArrayList<File>();

        public ImageOpener(String url)
        {
            File base = new File(url);

            File[] file_list = base.listFiles();

            for (File file : file_list)
            {
                if(file.getName().toLowerCase().endsWith(".mov"))
                {
                    try
                    {
                        System.out.println("Moving >> " + file.getAbsolutePath());

                        Files.copy(file.getAbsoluteFile().toPath(), Path.of(Main.mov_destination + "/" + file.getName()));

                        Files.delete(file.getAbsoluteFile().toPath());
                    }
                    catch (Exception e)
                    {
                        if (e instanceof FileAlreadyExistsException)
                        {
                            System.out.println("System >> " + Main.mov_destination + "/" + file.getName());
                        }
                        else e.printStackTrace(System.err);
                    }
                }
                else if(file.getName().toLowerCase().endsWith(".mp4"))
                {
                        try
                        {
                            System.out.println("Moving >> " + file.getAbsolutePath());

                            Files.copy(file.getAbsoluteFile().toPath(), Path.of(Main.mp4_destination + "/" + file.getName()));

                            Files.delete(file.getAbsoluteFile().toPath());
                        }
                        catch (Exception e)
                        {
                            if (e instanceof FileAlreadyExistsException)
                            {
                                System.out.println("System >> " + Main.mp4_destination + "/" + file.getName());
                            }
                            else e.printStackTrace(System.err);
                        }
                }
                else
                {
                    Main.image_and_video_files.add(file);
                }
            }
        }
    }
}
