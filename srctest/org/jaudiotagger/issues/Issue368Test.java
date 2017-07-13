package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test handling mp4s with dodgy values for discno
 */
public class Issue368Test extends AbstractTestCase {
    @Test
    public void testIssue() throws Exception {
        Exception caught = null;
        try {
            File orig = new File("testdata", "test95.m4a");
            if (!orig.isFile()) {
                System.err.println("Unable to test file - not available");
                return;
            }

            File testFile = AbstractTestCase.copyAudioToTmp("test95.m4a");
            AudioFile af = AudioFileIO.read(testFile);
            assertEquals(af.getTag().getFirst(FieldKey.DISC_NO), "2");
        } catch (Exception e) {
            caught = e;
            e.printStackTrace();
        }
        assertNull(caught);
    }
}