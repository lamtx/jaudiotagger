package org.jaudiotagger.tag.mp4;

import org.jaudiotagger.audio.mp4.util.Mp4BoxHeader;
import org.jaudiotagger.audio.mp4.util.AbstractMp4Box;
import org.jaudiotagger.audio.generic.Utils;

import java.nio.ByteBuffer;

/**
 * This box is used within ---- boxes to hold the issuer 
 *
 */
public class Mp4MeanBox extends AbstractMp4Box
{
    public static final String IDENTIFIER = "mean";


    private String       issuer;

    public static final int VERSION_LENGTH = 1;
    public static final int FLAGS_LENGTH = 3;
    public static final int PRE_DATA_LENGTH = VERSION_LENGTH + FLAGS_LENGTH;

    /**
     *
     * @param header header info
     * @param dataBuffer data of box (doesnt include header data)
     */
    public Mp4MeanBox(Mp4BoxHeader header, ByteBuffer dataBuffer)
    {
        this.header     = header;

        //Double check
        if(!header.getId().equals(IDENTIFIER))
        {
            throw new RuntimeException("Unabel to process data");
        }

        //Make slice so operations here don't effect position of main buffer
        this.dataBuffer = dataBuffer.slice();

        //issuer
        this.issuer=Utils.getString(this.dataBuffer, PRE_DATA_LENGTH, header.getDataLength() - PRE_DATA_LENGTH, header.getEncoding());

    }



    public String getIssuer()
    {
        return issuer;
    }
}