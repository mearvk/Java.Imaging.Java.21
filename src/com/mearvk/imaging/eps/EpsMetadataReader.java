package com.mearvk.imaging.eps;

import com.mearvk.lang.annotations.NotNull;
import com.mearvk.metadata.Metadata;
import com.mearvk.metadata.eps.EpsReader;
import com.mearvk.metadata.file.FileSystemMetadataReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Obtains metadata from EPS files.
 *
 * @author Payton Garland
 */
public class EpsMetadataReader {
    @NotNull
    public static Metadata readMetadata(@NotNull File file) throws IOException
    {
        Metadata metadata = new Metadata();

        FileInputStream stream = new FileInputStream(file);

        try {
            new EpsReader().extract(stream, metadata);
        } finally {
            stream.close();
        }

        new FileSystemMetadataReader().read(file, metadata);
        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream) throws IOException
    {
        Metadata metadata = new Metadata();
        new EpsReader().extract(inputStream, metadata);
        return metadata;
    }
}
