package de.htwg.moco.bulbdj.renderers;

import android.graphics.Color;
import android.util.Log;

import de.htwg.moco.bulbdj.detector.BeatDetector;
import de.htwg.moco.bulbdj.detector.Modes;

import java.util.ArrayList;
import java.util.Arrays;

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
         * @param bulbs are the different colors of the bulbs.
         */
        void onUpdate(int [] bulbs);

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
     * Number of bulbs to display. Default is 3.
     */
    private int bulbCount = 3;

    /**
     * Value of the last updated time.
     */
    private long lastUpdateTime = 0;

    /**
     * Value of the last color change.
     */
    private long lastColorChange = 0;

    /**
     * Last colors.
     */
    private int [] lastColors;

    /**
     * Value of the max DB.
     */
    private int maxDbValue = 30;

    /**
     * Time of the max DB.
     */
    private long maxDbTime = 0;

    /**
     * Red, Green, Blue
     */
    private int bulbs[];

    /**
     * Display mode
     */
    private int mode;

    /**
     * Possible colors for all modes.
     */
    private final int [][] allColors = {{Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW},  // DEFAULT / AUTOMATICAL
                                    {Color.RED},    //POP
                                    {Color.YELLOW}, // RAP
                                    {Color.MAGENTA},    // ROCK
                                    {Color.BLUE},   // DANCE
                                    {Color.CYAN}};  // ELECTRO

    /**
     * Different interval for all modes
     */
    private final int [] colorInterval = {2000,
            2000,
            2000,
            2000,
            2000,
            2000
    };

    /**
     * Default constructor.
     */
    public LEDRenderer() {
        this.listener = null;
        bulbs = new int[bulbCount];
        lastColors = new int[bulbCount];
    }

    /**
     * Setter method.
     *
     * @param mode of the colors
     */
    public void setMode(Modes mode) {
        int modeI;
        switch(mode) {
            case POP:
                modeI = 1;
                break;
            case RAP:
                modeI = 2;
                break;
            case ROCK:
                modeI = 3;
                break;
            case DANCE:
                modeI = 4;
                break;
            case ELECTRO:
                modeI = 5;
                break;
            case AUTOMATICAL:
            default:
                modeI = 0;
        }
        this.mode = modeI;
    }

    /**
     * Renders the beats as colors output.
     * @param beats the detected beats.
     */
    public void updateBeats(ArrayList<BeatDetector.BEAT_TYPE> beats) {
        int [] bulbs = new int[bulbCount];
        bulbs = calcColors(bulbs);

        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.KICK))
            bulbs[0] = Color.argb(0, Color.red(bulbs[0]), Color.green(bulbs[0]), Color.blue(bulbs[0]));
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.SNARE))
            bulbs[1] = Color.argb(0, Color.red(bulbs[1]), Color.green(bulbs[1]), Color.blue(bulbs[1]));
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.HAT))
            bulbs[2] = Color.argb(0, Color.red(bulbs[2]), Color.green(bulbs[2]), Color.blue(bulbs[2]));


        if (beats != null) {
            if (beats.contains(BeatDetector.BEAT_TYPE.KICK))
                bulbs[0] = Color.argb(255, Color.red(bulbs[0]), Color.green(bulbs[0]), Color.blue(bulbs[0]));
            if (beats.contains(BeatDetector.BEAT_TYPE.SNARE))
                bulbs[1] = Color.argb(255, Color.red(bulbs[1]), Color.green(bulbs[1]), Color.blue(bulbs[1]));
            if (beats.contains(BeatDetector.BEAT_TYPE.HAT))
                bulbs[2] = Color.argb(255, Color.red(bulbs[2]), Color.green(bulbs[2]), Color.blue(bulbs[2]));
            if (beats.contains(BeatDetector.BEAT_TYPE.MANUAL))
                bulbs[0] = Color.argb(255, Color.red(bulbs[0]), Color.green(bulbs[0]), Color.blue(bulbs[0]));
        }

        doUpdate(bulbs);
    }

    /**
     * Renders the raw fft data as colors output.
     * @param data the fft data.
     */
    public void updateFrequency(double[] data) {
        int r = 0, g = 0, b = 0;
        int limit = data.length / 2;
        int limitThird = limit / 3;

        for (int i = 0; i < limit; i+=limitThird) {
            double rfk = data[2 * i];
            double ifk = data[2 * i + 1];
            double magnitude = (rfk * rfk + ifk * ifk);
            int dbValueRaw = (int) (10 * Math.log10(magnitude));
            dbValueRaw += 20;               // Sensitivity. Lowest db Value is -30db.
            int dbValuePositive = Math.max(0, dbValueRaw);
            float dbValue = (float) dbValuePositive / maxDbValue * 255;
            dbValue = Math.min(dbValue, 255);

            if (i < limitThird)
                r+= dbValue;
            else if (i < limitThird * 2)
                g+= dbValue;
            else
                b+= dbValue;
        }

        if (r > maxDbValue) {
            maxDbValue = r;
            maxDbTime = System.currentTimeMillis();
        }
        if (g > maxDbValue) {
            maxDbValue = g;
            maxDbTime = System.currentTimeMillis();
        }
        if (b > maxDbValue) {
            maxDbValue = b;
            maxDbTime = System.currentTimeMillis();
        }

        if (maxDbTime > System.currentTimeMillis() - 5000)
            maxDbValue = 10;

        int [] bulbs = new int[bulbCount];
        bulbs = calcColors(bulbs);
        doUpdate(new int[]{Color.argb(r, Color.red(bulbs[0]), Color.green(bulbs[0]), Color.blue(bulbs[0])), Color.argb(g, Color.red(bulbs[1]), Color.green(bulbs[1]), Color.blue(bulbs[1])), Color.argb(b, Color.red(bulbs[2]), Color.green(bulbs[2]), Color.blue(bulbs[2]))});
    }

    /**
     * Call the final update function considering the delay.
     * @param bulbs are the different colors of the bulbs.
     */
    private void doUpdate(int[] bulbs) {

        if (listener != null && System.currentTimeMillis() - delay > lastUpdateTime && !Arrays.equals(bulbs, this.bulbs)) {
            this.bulbs = bulbs;
            listener.onUpdate(bulbs);
            lastUpdateTime = System.currentTimeMillis();
            String s = "";
            for (int color: bulbs) {
                s += String.valueOf(color) + " ";
            }
            Log.d("LED update", s);
        }
    }

    /**
     *
     * Calculate the colors by time and selected mode.
     *
     * @param bulbs to modify color
     * @return calculated colors
     */
    private int[] calcColors(int[] bulbs) {

        if (System.currentTimeMillis() - colorInterval[mode] > lastColorChange) {
            lastColorChange = System.currentTimeMillis();
            for (int i = 0; i < bulbs.length; i++) {
                bulbs[i] = nextColor(i);
                lastColors[i] = bulbs[i];
            }
        } else {
            for (int i = 0; i < bulbs.length; i++) {
                bulbs[i] = lastColors[i];
            }
        }

        return bulbs;
    }

    /**
     * Get the next color of all possible colors of the current mode.
     *
     * @param iGlobal index of the current bulb
     * @return color of the bulb
     */
    private int nextColor(int iGlobal) {
        int color;
        int nextI = 0;
        for(int i = 0; i < allColors[mode].length; i++) {
            if (allColors[mode][i] == lastColors[iGlobal]) {
                nextI = i;
                break;
            }
        }
        nextI = ++nextI % allColors[mode].length;
        color = allColors[mode][nextI];
        return color;
    }

    /**
     * Call the stop function.
     */
    public void stop() {
        if(listener != null)
            listener.onStop();
    }
}
