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
import com.mearvk.imaging.FileTypeDetector;

import java.text.SimpleDateFormat;
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

    static ArrayList<File> image_and_video_files = new ArrayList<>();

    static ArrayList<ImageSorter.ImageCollection> image_collection = new ArrayList<>();

    static ArrayList<ImageSorter.ImageCollection> image_collection_sorted = new ArrayList<>();

    public static void main(String...args)
    {
        Object object = null;

        try{ System.setOut(new PrintStream(new FileOutputStream(std_output_directory))); } catch (Exception e) {}

        try{ System.setErr(new PrintStream(new FileOutputStream(err_output_directory))); } catch (Exception e) {}

        try{ new File("/home/mearvk/Desktop/Videos/MOVs").mkdir(); } catch (Exception e) {}

        try{ new File("/home/mearvk/Desktop/Videos/MP4s").mkdir(); } catch (Exception e) {}

        ImageOpener opener = new ImageOpener(Main.initial_directory);

        ImageSorter sorter = new ImageSorter(Main.image_collection);

        sorter.collection();

        sorter.sort();

        sorter.finalize();
    }

    public static class ImageSorter
    {
        public ArrayList<ImageCollection> image_collection = new ArrayList<ImageCollection>();

        public static class Date implements Comparable<Date>
        {
            protected java.util.Date date;

            public Date(java.util.Date date)
            {
                this.date = date;
            }

            protected String next_file_name;

            protected File next_file;

            @Override
            public int compareTo(Date date)
            {
                return this.date.compareTo(date.date);
            }
        }

        public static class ImageCollection
        {
            protected String file_name;

            protected File file;

            protected Date file_date;

            protected java.util.Date _file_date;

            public ImageCollection(String file_name, File file, Date file_date)
            {
                this.file_name = file_name;

                this.file = file;

                this.file_date = file_date;
            }

            public ImageCollection(String file_name, File file, java.util.Date file_date)
            {
                this.file_name = file_name;

                this.file = file;

                this._file_date = file_date;
            }
        }

        public ImageSorter(ArrayList<ImageCollection> image_collection)
        {
            this.image_collection = this.image_collection;

            for (ImageCollection collection : this.image_collection)
            {
                try
                {
                    File file = collection.file;

                    String file_name = collection.file_name;

                    Date file_date = collection.file_date;

                    Metadata data = ImageMetadataReader.readMetadata(file);

                    Iterable <Directory> directories =  data.getDirectories();

                    System.out.println("File name >> "+file.getAbsolutePath());

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
            Integer number_of_files = this.image_collection.size();

            for(int i=0; i<number_of_files; i++)
            {
                try
                {
                    Metadata data = ImageMetadataReader.readMetadata(this.image_collection.get(i).file);

                    MetadataReader reader = data.getFirstDirectoryOfType(MetadataReader.class);

                    java.util.Date file_date = reader.getDate(MetadataReader.TAG_DATETIME_ORIGINAL);

                    String file_name = "iPhone_13_file_"+new SimpleDateFormat("dd-MM-yyyy").format(file_date)+".jpg";

                    Main.image_collection.add(new ImageCollection(file_name, this.image_collection.get(i).file, file_date));
                }
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

        public void sort()
        {
            ArrayList<Date> sorted_dates = new ArrayList<>();

            for(int i=0; i<Main.image_collection.size(); i++)
            {
                ImageCollection collection = Main.image_collection.get(i);

                Date file_date = collection.file_date;

                file_date.next_file_name = collection.file_name;

                file_date.next_file = collection.file;

                sorted_dates.add(file_date);
            }

            Collections.sort(sorted_dates);

            for(int i=0; i<sorted_dates.size(); i++)
            {
                ImageCollection collection = Main.image_collection.get(i);

                Date date = sorted_dates.get(i);

                collection.file_date = date;

                collection.file = date.next_file;

                collection.file_name = date.next_file_name;

                Main.image_collection_sorted.add(collection);
            }
        }

        public void finalize()
        {
            try
            {
                for(ImageCollection collection : Main.image_collection_sorted)
                {
                    File file = collection.file;

                    String file_name = collection.file_name;

                    System.out.println(Path.of(file.getParent()+"/"+file_name));

                    Files.copy(file.toPath(), Path.of(file.getParent()+"/"+file_name));

                    Files.delete(file.toPath());
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
                        System.out.println("Moving [MOV] >> " + file.getAbsolutePath());

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
                            System.out.println("Moving [MP4] >> " + file.getAbsolutePath());

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
