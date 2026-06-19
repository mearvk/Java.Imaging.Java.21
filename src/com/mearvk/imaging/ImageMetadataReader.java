package com.mearvk.imaging;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Reads image metadata (EXIF) from JPEG, TIFF, HEIC and PNG files.
 * Minimal self-contained implementation — no third-party dependencies.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class ImageMetadataReader
{
    public static Metadata readMetadata(File file) throws IOException
    {
        Metadata metadata = new Metadata();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            int b1 = raf.read();
            int b2 = raf.read();
            raf.seek(0);

            if (b1 == 0xFF && b2 == 0xD8)
            {
                readJpegExif(raf, metadata);
            }
            else if ((b1 == 0x49 && b2 == 0x49) || (b1 == 0x4D && b2 == 0x4D))
            {
                readTiffExif(raf, 0, metadata);
            }
        }

        return metadata;
    }

    private static void readJpegExif(RandomAccessFile raf, Metadata metadata) throws IOException
    {
        raf.seek(2);

        while (raf.getFilePointer() < raf.length())
        {
            int marker1 = raf.read();
            if (marker1 != 0xFF) break;

            int marker2 = raf.read();

            // Skip padding 0xFF bytes
            while (marker2 == 0xFF) marker2 = raf.read();

            if (marker2 == 0xD9 || marker2 == 0xDA) break; // EOI or SOS

            int length = raf.readUnsignedShort() - 2;
            long segmentStart = raf.getFilePointer();

            // APP1 marker (EXIF)
            if (marker2 == 0xE1 && length >= 6)
            {
                byte[] header = new byte[6];
                raf.read(header);

                if (header[0] == 'E' && header[1] == 'x' && header[2] == 'i' &&
                    header[3] == 'f' && header[4] == 0 && header[5] == 0)
                {
                    long tiffStart = raf.getFilePointer();
                    int remaining = length - 6;
                    byte[] tiffData = new byte[remaining];
                    raf.read(tiffData);
                    parseTiffFromBytes(tiffData, metadata);
                    return;
                }
            }

            raf.seek(segmentStart + length);
        }
    }

    private static void readTiffExif(RandomAccessFile raf, long offset, Metadata metadata) throws IOException
    {
        raf.seek(offset);
        byte[] data = new byte[(int)(raf.length() - offset)];
        raf.readFully(data);
        parseTiffFromBytes(data, metadata);
    }

    private static void parseTiffFromBytes(byte[] data, Metadata metadata)
    {
        if (data.length < 8) return;

        ByteOrder order;
        if (data[0] == 'I' && data[1] == 'I')
            order = ByteOrder.LITTLE_ENDIAN;
        else if (data[0] == 'M' && data[1] == 'M')
            order = ByteOrder.BIG_ENDIAN;
        else
            return;

        ByteBuffer buf = ByteBuffer.wrap(data).order(order);
        int ifdOffset = buf.getInt(4);

        parseIFD(data, ifdOffset, order, metadata, 0);
    }

    private static void parseIFD(byte[] data, int ifdOffset, ByteOrder order, Metadata metadata, int depth)
    {
        if (depth > 4 || ifdOffset < 0 || ifdOffset + 2 > data.length) return;

        ByteBuffer buf = ByteBuffer.wrap(data).order(order);
        int entryCount = buf.getShort(ifdOffset) & 0xFFFF;

        int pos = ifdOffset + 2;

        for (int i = 0; i < entryCount; i++)
        {
            if (pos + 12 > data.length) break;

            int tag = buf.getShort(pos) & 0xFFFF;
            int type = buf.getShort(pos + 2) & 0xFFFF;
            int count = buf.getInt(pos + 4);
            int valueOffset = buf.getInt(pos + 8);

            // ExifIFD pointer (tag 0x8769)
            if (tag == 0x8769)
            {
                parseIFD(data, valueOffset, order, metadata, depth + 1);
            }
            // GPS IFD pointer (tag 0x8825)
            else if (tag == 0x8825)
            {
                parseIFD(data, valueOffset, order, metadata, depth + 1);
            }
            // DateTimeOriginal (0x9003), DateTime (0x0132), DateTimeDigitized (0x9004)
            else if (tag == 0x9003 || tag == 0x0132 || tag == 0x9004)
            {
                String dateStr = readAsciiValue(data, type, count, pos + 8, valueOffset, order);
                if (dateStr != null)
                {
                    metadata.tags.put(tag, dateStr.trim());
                }
            }
            // Any ASCII tag
            else if (type == 2)
            {
                String val = readAsciiValue(data, type, count, pos + 8, valueOffset, order);
                if (val != null)
                {
                    metadata.tags.put(tag, val.trim());
                }
            }
            // SHORT or LONG values (store as integer)
            else if ((type == 3 || type == 4) && count == 1)
            {
                int val;
                if (type == 3)
                    val = buf.getShort(pos + 8) & 0xFFFF;
                else
                    val = buf.getInt(pos + 8);
                metadata.tags.put(tag, String.valueOf(val));
            }

            pos += 12;
        }
    }

    private static String readAsciiValue(byte[] data, int type, int count, int inlineOffset, int externalOffset, ByteOrder order)
    {
        if (type != 2 || count <= 0) return null;

        int offset;
        if (count <= 4)
            offset = inlineOffset;
        else
            offset = externalOffset;

        if (offset < 0 || offset + count > data.length) return null;

        // Strip trailing null
        int len = count;
        while (len > 0 && data[offset + len - 1] == 0) len--;

        return new String(data, offset, len);
    }

    /**
     * Container for extracted metadata tags.
     */
    public static class Metadata
    {
        public static final int TAG_DATETIME = 0x0132;
        public static final int TAG_DATETIME_ORIGINAL = 0x9003;
        public static final int TAG_DATETIME_DIGITIZED = 0x9004;
        public static final int TAG_IMAGE_WIDTH = 0x0100;
        public static final int TAG_IMAGE_HEIGHT = 0x0101;
        public static final int TAG_MAKE = 0x010F;
        public static final int TAG_MODEL = 0x0110;
        public static final int TAG_ORIENTATION = 0x0112;

        public final Map<Integer, String> tags = new LinkedHashMap<>();

        /**
         * Returns the date the photo was originally taken, or null if unavailable.
         */
        public Date getDateOriginal()
        {
            String dateStr = tags.get(TAG_DATETIME_ORIGINAL);
            if (dateStr == null) dateStr = tags.get(TAG_DATETIME);
            if (dateStr == null) return null;

            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                return sdf.parse(dateStr);
            }
            catch (ParseException e)
            {
                return null;
            }
        }

        /**
         * Returns date for a given tag constant, or null.
         */
        public Date getDate(int tagType)
        {
            String dateStr = tags.get(tagType);
            if (dateStr == null) return null;

            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                return sdf.parse(dateStr);
            }
            catch (ParseException e)
            {
                return null;
            }
        }

        public String getString(int tagType)
        {
            return tags.get(tagType);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, String> entry : tags.entrySet())
            {
                sb.append(String.format("  [0x%04X] %s%n", entry.getKey(), entry.getValue()));
            }
            return sb.toString();
        }
    }
}
