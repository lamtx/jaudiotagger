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
 * Test deletions of ID3v1 tag
 */
public class Issue366Test extends AbstractTestCase {
    @Test
    public void testIssue() throws Exception {
        Exception caught = null;
        try {
            File orig = new File("testdata", "test91.mp3");
            if (!orig.isFile()) {
                System.err.println("Unable to test file - not available");
                return;
            }

            File testFile = AbstractTestCase.copyAudioToTmp("test91.mp3");
            AudioFile af = AudioFileIO.read(testFile);
            assertEquals(af.getTag().getFirst(FieldKey.TRACK), "15");
        } catch (Exception e) {
            caught = e;
        }
        assertNull(caught);
    }
}