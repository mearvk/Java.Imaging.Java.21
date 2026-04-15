/*
 * Copyright 2002-2019 Drew Noakes and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.mearvk.imaging.avi;

import com.mearvk.imaging.riff.RiffProcessingException;
import com.mearvk.imaging.riff.RiffReader;
import com.mearvk.lang.StreamReader;
import com.mearvk.lang.annotations.NotNull;
import com.mearvk.metadata.Metadata;
import com.mearvk.metadata.avi.AviRiffHandler;
import com.mearvk.metadata.file.FileSystemMetadataReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Obtains metadata from AVI files.
 *
 * @author Payton Garland
 */
public class AviMetadataReader
{
    @NotNull
    public static Metadata readMetadata(@NotNull File file) throws IOException, RiffProcessingException
    {
        InputStream inputStream = new FileInputStream(file);
        Metadata metadata;
        try {
            metadata = readMetadata(inputStream);
        } finally {
            inputStream.close();
        }
        new FileSystemMetadataReader().read(file, metadata);
        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream) throws IOException, RiffProcessingException
    {
        Metadata metadata = new Metadata();
        new RiffReader().processRiff(new StreamReader(inputStream), new AviRiffHandler(metadata));
        return metadata;
    }
}
