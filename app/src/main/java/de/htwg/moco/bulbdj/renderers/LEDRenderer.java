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

        /**
         * Mode was detected.
         * @param mode that was automatically detected
         */
        void onAutoModeChanged(int mode);
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
     * Instance of {@LEDRenderer} class.
     */
    private static LEDRenderer instance = null;

    /**
     * The delay of the updates.
     */
    private int delay = 50; // In milliseconds

    /**
     * Check mode delay
     */
    private int checkModeDelay = 15000; // In milliseconds

    /**
     * Number of bulbs to display. Default is 3.
     */
    private int bulbCount = 3;

    /**
     * Value of the last updated time.
     */
    private long lastUpdateTime = 0;

    /**
     * Value of last checked automatic mode
     */
    private long lastModeChecked = 0;

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
     * Counter of beats.
     */
    private int countBeats = 0;

    /**
     * Is the mode automatic.
     */
    private boolean autoMode = true;

    /**
     * Possible colors for all modes.
     */
    private final int [][] allColors = {{Color.parseColor("#C40D00"), Color.parseColor("#BAA702"), Color.parseColor("#0078C4"), Color.parseColor("#00AD1D")},  // DEFAULT / AUTOMATICAL
                                    {Color.parseColor("#C40D00"), Color.parseColor("#BAA702"), Color.parseColor("#0078C4"), Color.parseColor("#00AD1D")},    //POP
                                    {Color.parseColor("#BA9F02"), Color.parseColor("#C41C0A"), Color.parseColor("#AAAAAA")}, // RAP
                                    {Color.parseColor("#BA9C02"), Color.parseColor("#C48C0A"), Color.parseColor("#C44D0A"), Color.parseColor("#BA2509")},    // ROCK
                                    {Color.parseColor("#BA0276"), Color.parseColor("#04B1BA"), Color.parseColor("#0A59C4")},   // DANCE
                                    {Color.parseColor("#0039AD"), Color.parseColor("#8C03BA"), Color.parseColor("#0AAAC4"), Color.parseColor("#04BA5A")},   // ELECTRO
                                    {Color.parseColor("#FF24DB"), Color.parseColor("#E8680C"), Color.parseColor("#FF4839"), Color.parseColor("#F3FF97")}};  // ROMATIC

    /**
     * Colors when the lights are off (background colors).
     */
    private final int [] offColors = {Color.parseColor("#FFFFFF"),
            Color.parseColor("#FFDC00"),
            Color.parseColor("#FF9B0C"),
            Color.parseColor("#FF6E4D"),
            Color.parseColor("#396FFF"),
            Color.parseColor("#3799FF"),
            Color.parseColor("#E8478A")};

    /**
     * Different interval for all modes
     */
    private final int [] colorInterval = {2000,
            2000,
            2000,
            2000,
            2000,
            2000,
            2000
    };

    /**
     * Singleton instance of {@link LEDRenderer} class.
     * @return instance of {@link LEDRenderer}
     */
    public static LEDRenderer getInstance() {
        if (instance == null)
            instance = new LEDRenderer();
        return instance;
    }

    /**
     * Default constructor.
     */
    private LEDRenderer() {
        this.listener = null;
        bulbs = new int[bulbCount];
        lastColors = new int[bulbCount];
    }

    public void setDelay(int delay) {
        if (delay >= 20 && delay <= 5000)
            this.delay = delay;
        else
            throw new RuntimeException("Delay is out of range.");
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
            case ROMANTIC:
                modeI = 6;
                break;
            case AUTOMATIC:
                lastModeChecked = System.currentTimeMillis();
            default:
                modeI = 0;
        }
        this.mode = modeI;
    }

    /**
     * Setter method.
     *
     * @param autoMode is true if the mode is automatically detected.
     */
    public void setModeAuto(boolean autoMode) {
        this.autoMode = autoMode;
    }

    /**
     * Renders the beats as colors output.
     * @param beatsObjs the detected beats.
     */
    public void updateBeats(ArrayList<Object[]> beatsObjs) {
        int [] bulbs = new int[bulbCount];
        bulbs = calcColors(bulbs);

        ArrayList<BeatDetector.BEAT_TYPE> beats = new ArrayList<BeatDetector.BEAT_TYPE>();
        for (Object[] beatsObj : beatsObjs) {
            if (beatsObj != null && beatsObj.length > 0)
                beats.add((BeatDetector.BEAT_TYPE) beatsObj[0]);
        }

        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.KICK))
            bulbs[0] = offColors[mode];
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.SNARE))
            bulbs[1] = offColors[mode];
        if (beats == null || !beats.contains(BeatDetector.BEAT_TYPE.HAT))
            bulbs[2] = offColors[mode];

        if (beats != null) {
            for (Object[] beatObj : beatsObjs) {
                int energy = (int) (((float) beatObj[1]) * 110) + 80;
                energy = Math.min(255, energy);
                energy = Math.max(0, energy);
                if (beatObj[0].equals(BeatDetector.BEAT_TYPE.KICK))
                    bulbs[0] = Color.argb(energy, Color.red(bulbs[0]), Color.green(bulbs[0]), Color.blue(bulbs[0]));
                if (beatObj[0].equals(BeatDetector.BEAT_TYPE.SNARE))
                    bulbs[1] = Color.argb(energy, Color.red(bulbs[1]), Color.green(bulbs[1]), Color.blue(bulbs[1]));
                if (beatObj[0].equals(BeatDetector.BEAT_TYPE.HAT))
                    bulbs[2] = Color.argb(energy, Color.red(bulbs[2]), Color.green(bulbs[2]), Color.blue(bulbs[2]));
            }
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

            lastUpdateTime = System.currentTimeMillis();

            if (lastModeChecked > 0)
                countBeats++;
            if (autoMode && lastModeChecked > 0 && lastUpdateTime - checkModeDelay > lastModeChecked) {
                lastModeChecked = 0;
                changeMode();
                countBeats = 0;
            }

            // Log
            String s = "";
            for (int color: bulbs) {
                if (Color.alpha(color) <= 0)
                    s +=  "0 ";
                else
                    s +=  String.valueOf(color) + " ";
            }
            Log.d("LED update", s);

            // Call onUpdate
            listener.onUpdate(bulbs);
        }
    }

    /**
     * Change the mode depending on beat counts.
     */
    private void changeMode() {
        Modes mode = Modes.AUTOMATIC;

        if (countBeats > 0 && countBeats < 30) {
            mode = Modes.ROMANTIC;
        } else if (countBeats < 40) {
            mode = Modes.RAP;
        } else if (countBeats < 50) {
            mode = Modes.ROCK;
        } else if (countBeats < 60) {
            mode = Modes.DANCE;
        } else if (countBeats < 70) {
            mode = Modes.POP;
        } else if (countBeats < 80) {
            mode = Modes.ELECTRO;
        }

        setMode(mode);
        Log.d("Mode change", "Mode change to: " + mode.name() + " (detected " + countBeats + " beats)");
        if (listener != null) {
            listener.onAutoModeChanged(this.mode);
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
        lastModeChecked = 0;
        if(listener != null) {
            listener.onStop();
            if (autoMode) {
                listener.onAutoModeChanged(0);
            }
        }
    }
}
