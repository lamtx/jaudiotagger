/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.tag.mp4;

import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagFieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import org.jaudiotagger.audio.generic.AbstractTagCreator;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.mp4.atom.Mp4BoxHeader;
import org.jaudiotagger.audio.mp4.Mp4NotMetaFieldKey;

/**
 * Create raw content of mp4 tag data, concerns itself with atoms upto the ilst atom
 * <p/>
 * <p>This level is was selected because the ilst atom canbe recreated without reference to existing mp4 fields
 * but fields above this level are dependent upon other information that is not held in the tag.
 * <p/>
 * <pre>
 * |--- ftyp
 * |--- moov
 * |......|
 * |......|----- mvdh
 * |......|----- trak
 * |......|----- udta
 * |..............|
 * |..............|-- meta
 * |....................|
 * |....................|-- hdlr
 * |....................|-- ilst
 * |....................|.. ..|
 * |....................|.....|---- @nam (Optional for each metadatafield)
 * |....................|.....|.......|-- data
 * |....................|.....|....... ecetera
 * |....................|.....|---- ---- (Optional for reverse dns field)
 * |....................|.............|-- mean
 * |....................|.............|-- name
 * |....................|.............|-- data
 * |....................|................ ecetere
 * |....................|-- free
 * |--- free
 * |--- mdat
 * </pre>
 */
public class Mp4TagCreator extends AbstractTagCreator
{
    /**
     * Convert tagdata to rawdata ready for writing to file
     *
     * @param tag
     * @param padding TODO padding parameter currently ignored
     * @return
     * @throws UnsupportedEncodingException
     */
    public ByteBuffer convert(Tag tag, int padding) throws UnsupportedEncodingException
    {
        try
        {
            //Add metadata raw content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator it = tag.getFields();
            boolean processedArtwork = false;
            while (it.hasNext())
            {
                TagField frame = (TagField) it.next();
                //To ensure order is maintained dont process artwork until iterator hits it.
                if (frame instanceof Mp4TagCoverField)
                {
                    if (processedArtwork)
                    {
                        //ignore
                    }
                    else
                    {
                        processedArtwork = true;

                        //Because each artwork image is held within the tag as a seperate field, but when
                        //they are written they are all held under a single covr box we need to do some checks
                        //and special processing here if we have any artwork image (this code only neccessary
                        //if we have more than 1 but do it anyway even if only have 1 image)
                        ByteArrayOutputStream covrDataBaos = new ByteArrayOutputStream();

                        try
                        {
                            for (TagField artwork : tag.get(TagFieldKey.COVER_ART))
                            {
                                covrDataBaos.write(((Mp4TagField) artwork).getRawContentDataOnly());
                            }
                        }
                        catch (KeyNotFoundException knfe)
                        {
                            //This cannot happen
                            throw new RuntimeException("Unable to find COVERART Key");
                        }

                        //Now create the parent Data
                        byte[] data = covrDataBaos.toByteArray();
                        baos.write(Utils.getSizeBigEndian(Mp4BoxHeader.HEADER_LENGTH + data.length));
                        baos.write(Utils.getDefaultBytes(Mp4FieldKey.ARTWORK.getFieldName(), "ISO-8859-1"));
                        baos.write(data);
                    }
                }
                else
                {
                    baos.write(frame.getRawContent());
                }
            }

            //Wrap into ilst box
            ByteArrayOutputStream ilst = new ByteArrayOutputStream();
            ilst.write(Utils.getSizeBigEndian(Mp4BoxHeader.HEADER_LENGTH + baos.size()));
            ilst.write(Utils.getDefaultBytes(Mp4NotMetaFieldKey.ILST.getFieldName(), "ISO-8859-1"));
            ilst.write(baos.toByteArray());

            //Put into ByteBuffer
            ByteBuffer buf = ByteBuffer.wrap(ilst.toByteArray());
            buf.rewind();
            return buf;
        }
        catch (IOException ioe)
        {
            //Should never happen as not writing to file at this point
            throw new RuntimeException(ioe);
        }
    }
}
