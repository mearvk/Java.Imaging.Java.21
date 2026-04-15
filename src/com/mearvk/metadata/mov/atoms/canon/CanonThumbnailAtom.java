package com.mearvk.metadata.mov.atoms.canon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.mearvk.imaging.jpeg.JpegProcessingException;
import com.mearvk.imaging.jpeg.JpegSegmentData;
import com.mearvk.imaging.jpeg.JpegSegmentMetadataReader;
import com.mearvk.imaging.jpeg.JpegSegmentReader;
import com.mearvk.imaging.jpeg.JpegSegmentType;
import com.mearvk.lang.SequentialReader;
import com.mearvk.lang.StreamReader;
import com.mearvk.lang.annotations.Nullable;
import com.mearvk.metadata.Directory;
import com.mearvk.metadata.Metadata;
import com.mearvk.metadata.Tag;
import com.mearvk.metadata.exif.ExifDirectoryBase;
import com.mearvk.metadata.exif.ExifIFD0Directory;
import com.mearvk.metadata.exif.ExifReader;
import com.mearvk.metadata.mov.QuickTimeDirectory;
import com.mearvk.metadata.mov.atoms.Atom;

/**
 *
 * @author PerB
 */
public class CanonThumbnailAtom extends Atom
{
    @Nullable
    private String dateTime;

    public CanonThumbnailAtom(SequentialReader reader) throws IOException
    {
        super(reader);
        readCNDA(reader);
    }

    /**
     * Canon Data Block (Exif/TIFF ThumbnailImage)
     */
    private void readCNDA(SequentialReader reader) throws IOException
    {
        if (this.type.equals("CNDA")) {
            if (this.size > Integer.MAX_VALUE || this.size <= 0)
                return;

            // From JpegMetadataReader
            JpegSegmentMetadataReader exifReader = new ExifReader();
            InputStream exifStream = new ByteArrayInputStream(reader.getBytes((int) this.size));
            Set<JpegSegmentType> segmentTypes = new HashSet<JpegSegmentType>();
            for (JpegSegmentType type : exifReader.getSegmentTypes()) {
                segmentTypes.add(type);
            }
            JpegSegmentData segmentData;
            try {
                segmentData = JpegSegmentReader.readSegments(new StreamReader(exifStream), segmentTypes);
            } catch (JpegProcessingException e) {
                return;
            }

            // TODO should we keep all extracted metadata here?
            Metadata metadata = new Metadata();
            for (JpegSegmentType segmentType : exifReader.getSegmentTypes()) {
                exifReader.readJpegSegments(segmentData.getSegments(segmentType), metadata, segmentType);
            }

            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagType() == ExifDirectoryBase.TAG_DATETIME) {
                        dateTime = tag.getDescription();
                    }
                }
            }
        }
    }

    public void addMetadata(QuickTimeDirectory directory)
    {
        if (dateTime != null) {
            directory.setString(QuickTimeDirectory.TAG_CANON_THUMBNAIL_DT, dateTime);
        }
    }
}
