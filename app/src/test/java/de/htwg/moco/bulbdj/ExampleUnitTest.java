package de.htwg.moco.bulbdj;

import org.junit.Test;

import de.htwg.moco.bulbdj.detector.AudioManager;

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
}