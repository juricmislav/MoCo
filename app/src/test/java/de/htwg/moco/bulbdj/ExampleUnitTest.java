package de.htwg.moco.bulbdj;

import org.junit.Test;

import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.detector.Modes;
import de.htwg.moco.bulbdj.renderers.LEDRenderer;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    /**
     * Test the singleton implementation if AudioManager
     * @throws Exception
     */
    @Test
    public void singleton_isCorrect() throws Exception {
        assertSame(AudioManager.getInstance(), AudioManager.getInstance());
    }

    /**
     * Test the recording settings.
     */
    @Test
    public void testSettings() {

        assertFalse(AudioManager.getInstance().isRunning());

        assertTrue(AudioManager.getInstance().getSensitivity(Modes.AUTOMATICAL) == 1.35F);

        AudioManager.getInstance().setMode(Modes.DANCE);
        assertTrue(AudioManager.getInstance().getSensitivity(Modes.DANCE) == 1.42F);

        AudioManager.getInstance().stop();
    }
}