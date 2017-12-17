package de.htwg.moco.bulbdj.renderers;

import de.htwg.moco.bulbdj.detector.BeatDetector;

import java.util.ArrayList;

/**
 * Class handles the visualisation of different modes and display it as RGB alpha.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class LEDRenderer {

    /**
     * Interface of {@link LEDRenderer} class.
     *
     * @author Daniel Steidinger
     * @version 1.0
     */
    public interface LEDRendererListener {

        /**
         * Update LED alpha.
         * @param r is the alpha of the red color.
         * @param g is the alpha of the green color.
         * @param b is the alpha of the blue color.
         */
        void onUpdate(int r, int g, int b);

        /**
         * Stop the LED lights.
         */
        void onStop();
    }

    /**
     * Instance of {@link LEDRendererListener} class.
     */
    private LEDRendererListener listener;

    /**
     * Setter method.
     * @param listener sets the listener of {@link LEDRendererListener} class.
     */
    public void setLEDRendererListener(LEDRendererListener listener) {
        this.listener = listener;
    }

    /**
     * The delay of the updates.
     */
    private int delay = 25; // In milliseconds

    /**
     * Value of the last updated time.
     */
    private long lastUpdateTime = 0;

    /**
     * Default constructor.
     */
    public LEDRenderer() {
        this.listener = null;
    }

    /**
     * Renders the beats as r,g,b output.
     * @param beats the detected beats.
     */
    public void updateBeats(ArrayList<BeatDetector.BEAT_TYPE> beats) {
        int r = 0, g = 0, b = 0;

        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.KICK))
            r = 0;
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.SNARE))
            g = 0;
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.HAT))
            b = 0;


        if (beats != null) {
            if (beats.contains(BeatDetector.BEAT_TYPE.KICK))
                r = 255;
            if (beats.contains(BeatDetector.BEAT_TYPE.SNARE))
                g = 255;
            if (beats.contains(BeatDetector.BEAT_TYPE.HAT))
                b = 255;
            if (beats.contains(BeatDetector.BEAT_TYPE.MANUAL))
                r = 255;
        }

        doUpdate(r, g, b);
    }

    /**
     * Renders the raw fft data as r,g,b output.
     * @param data the fft data.
     */
    public void updateFrequency(double[] data) {

        // TODO flexible maxDbValue.

        int r = 0, g = 0, b = 0;
        int limit = data.length / 2;
        int limitThird = limit / 3;

        for (int i = 0; i < limit; i+=limitThird) {
            double rfk = data[2 * i];
            double ifk = data[2 * i + 1];
            double magnitude = (rfk * rfk + ifk * ifk);
            int dbValueRaw = (int) (10 * Math.log10(magnitude));
            //dbValueRaw += 30;               // Sensitivity. Lowest db Value is -30db.
            int dbValuePositive = Math.max(0, dbValueRaw);
            int maxDbValue = 30;            // Max Sensitivity is 30db.
            float dbValue = (float) dbValuePositive / maxDbValue * 255;
            dbValue = Math.min(dbValue, 255);

            if (i < limitThird)
                r+= dbValue;
            else if (i < limitThird * 2)
                g+= dbValue;
            else
                b+= dbValue;
        }

        doUpdate(r, g, b);
    }

    /**
     * Call the final update function considering the delay.
     * @param r is the alpha of the red color.
     * @param g is the alpha of the green color.
     * @param b is the alpha of the blue color.
     */
    private void doUpdate(int r, int g, int b) {

        if (listener != null && System.currentTimeMillis() - delay > lastUpdateTime) {
            listener.onUpdate(r, g, b);
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    /**
     * Call the stop function.
     */
    public void stop() {
        if(listener != null)
            listener.onStop();
    }
}
