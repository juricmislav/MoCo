package de.htwg.moco.bulbdj;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.AppProperties;
import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.detector.Modes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests which will execute on the development machine (host).
 */
@RunWith(MockitoJUnitRunner.class)
public class AppUnitTests {

    /**
     * Test the singleton implementation of <<code>{@link AudioManager}</code>
     * @throws Exception
     */
    @Test
    public void audioManagerSingleton() throws Exception {
        assertSame(AudioManager.getInstance(), AudioManager.getInstance());
    }

    /**
     * Test the recording settings.
     */
    @Test
    public void testSettings() {

        assertFalse(AudioManager.getInstance().isRunning());

        assertTrue(AudioManager.getInstance().getSensitivity(Modes.AUTOMATIC) == 1.35F);

        AudioManager.getInstance().setMode(Modes.DANCE);
        assertTrue(AudioManager.getInstance().getSensitivity(Modes.DANCE) == 1.42F);

        AudioManager.getInstance().stop();
    }

    /**
     * Test the singleton implementation of <<code>{@link BridgeController}</code>o
     * @throws Exception
     */
    @Test
    public void bridgeControllerSingleton() throws Exception {
        assertSame(BridgeController.getInstance(), BridgeController.getInstance());
    }

    /**
     * Test the singleton implementation of <<code>{@link de.htwg.moco.bulbdj.data.AppProperties}</code>
     * @throws Exception
     */
    @Test
    public void appPropSingleton() throws Exception {
        assertSame(AppProperties.getInstance(), AppProperties.getInstance());
    }

    /**
     * Test behaviour of <<code>{@link BridgeController}</code>o
     * @throws Exception
     */
    @Test
    public void bridgeBehaviour() throws Exception {
        if (!BridgeController.getInstance().isConnected()) {
            assertEquals(true, BridgeController.getInstance().isLightsEmpty());

            assertEquals(null, BridgeController.getInstance().getAllLights());
        }
    }
}