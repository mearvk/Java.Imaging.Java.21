package com.mearvk.metadata.wav;

import com.mearvk.lang.annotations.NotNull;
import com.mearvk.lang.annotations.Nullable;
import com.mearvk.metadata.TagDescriptor;

/**
 * @author Payton Garland
 */
public class WavDescriptor extends TagDescriptor<WavDirectory>
{
    public WavDescriptor(@NotNull WavDirectory directory)
    {
        super(directory);
    }

    @Override
    @Nullable
    public String getDescription(int tagType)
    {
        return super.getDescription(tagType);
    }
}
