package de.htwg.moco.bulbdj.detector;

import java.util.ArrayList;

/**
 * Class detects some beats like kick, snare and hat.
 * For this some sub bands are created to
 * separate the frequency in some divisions.
 * Kick, Snare and Hat have their own frequency range.
 *
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class BeatDetector {

    /**
     * Interface of {@link BeatDetector} class.
     *
     * @author Daniel Steidinger
     * @version 1.0
     */
    public interface BeatDetectorListener {

        /**
         * Beats were detected.
         * @param beats that were detected
         */
        void onBeatDetected(ArrayList<BEAT_TYPE> beats);
    }

    /**
     * Instance of {@link BeatDetectorListener} class.
     */
    private BeatDetectorListener listener;

    /**
     * Enum of beat types.
     */
    public enum BEAT_TYPE { KICK, SNARE, HAT, MANUAL };

    private float[] magnitude = null;
    private float[] avgMagnitude = null;
    private float[] fftSubBands = null;
    private float[] fftVariance = null;
    private float[] beatValues = null;
    private float[][] energyHistory = null;
    private float[] averageEnergy = null;
    private ArrayList<BEAT_TYPE> beats = new ArrayList<BEAT_TYPE>();

    private int samplingRate = -1;
    private int bufferSize = -1;
    private int fftSize = -1;
    private int historyPos = 0;
    private int divisions = 2;
    private int timeToWait = 0;    // In milliseconds
    private int fftSubBandsCount = 64;  //32;   // More Bands = more sensitivity. Less Bands = more recognizations for different music types.
    private long lastBeat = 0;
    private float threshold = 0.05F;
    private float sensitivity = 1.35F;
    private int manualLow = -1;
    private int manualHigh = -1;

    /**
     * Default constructor.
     * @param samplingRate of the recorded data.
     * @param fftSize of the recorded data.
     * @param bufferSize for the energy history.
     */
    public BeatDetector(int samplingRate, int fftSize, int bufferSize) {
        this.listener = null;
        this.samplingRate = samplingRate;
        this.bufferSize = bufferSize;
        this.fftSize = fftSize / divisions;
        this.fftSubBands = new float[fftSubBandsCount];
        this.fftVariance = new float[fftSubBandsCount];
        this.beatValues = new float[fftSubBandsCount];
        this.averageEnergy = new float[fftSubBandsCount];
        this.energyHistory = new float[fftSubBandsCount][samplingRate / bufferSize];
    }

    /**
     * Setter method.
     * @param listener sets the listener of {@link BeatDetectorListener} class.
     */
    public void setBeatDetectorListener(BeatDetectorListener listener) {
        this.listener = listener;
    }

    /**
     * Beats were detected.
     * @param beats
     */
    private void beatDetected(ArrayList<BEAT_TYPE> beats) {
        if (listener != null) {
            listener.onBeatDetected(beats);
        }
    }

    /**
     * The initialization of each update.
     * @param length of the fft.
     */
    private void init(int length) {
        magnitude = new float[length];
        avgMagnitude = new float[length];
        for (int i = 0; i < length; i++) {
            magnitude[i] = 0;
            avgMagnitude[i] = 0;
        }

        for (int i = 0; i < fftSubBandsCount; i++) {
            for (int i2 = 0; i2 < samplingRate / bufferSize; i2++) {
                energyHistory[i][i2] = 0;
            }
        }
    }

    /**
     * Setter method.
     * @param percentage of the sensitivity from 0 to 100
     */
    public void setSensitivityPercent(int percentage) {
        if (percentage >= 0) {
            float sensitivity = (100F - percentage) / 100F;
            float maxWeight = 2.0F;
            float minWeight = 1.0F;
            float range = maxWeight - minWeight;

            this.sensitivity = minWeight + range * sensitivity;
        } else {
            throw new RuntimeException("Sensitivity is negative.");
        }
    }

    /**
     * Setter method.
     * @param sensitivity of the beat detection
     */
    public void setSensitivity(float sensitivity) {
        if (sensitivity >= 0) {
            this.sensitivity = sensitivity;
        } else {
            throw new RuntimeException("Sensitivity is negative.");
        }
    }

    /**
     * Setter method. Manual setter for a specific range.
     * @param low sets the low frequency
     * @param high sets the high frequency
     */
    public void setManualRange(int low, int high) {
        low = (int) (((float) (low / 100F)) * (fftSubBandsCount - 1));
        high = (int) (((float) (high / 100F)) * (fftSubBandsCount - 1));

        if (low > high || low < 0)
            throw new RuntimeException("Invalid values.");
        else if (high > fftSubBandsCount)
            throw new RuntimeException("High value is bigger then SubbandsCount (" + fftSubBandsCount + ")");

        manualLow = low;
        manualHigh = high;
    }

    /**
     * Clear the manual range.
     */
    public void clearManualRange() {
        manualLow = -1;
        manualHigh = -1;
    }

    /**
     * Update fft data and check for any type of beat.
     * @param input of the fft data.
     */
    public void update(double[] input) {

        if (magnitude == null) {
            init(fftSize);
        }

        calcAll(input);

        beatDetected(detectBeat());
    }

    /**
     * Detect beats.
     * @return type of beats.
     */
    private ArrayList<BEAT_TYPE> detectBeat() {
        beats.clear();

        if (manualLow < 0 || manualHigh < 0) {
            if (isKick()) {
                beats.add(BEAT_TYPE.KICK);
            } else if (isSnare()) {
                beats.add(BEAT_TYPE.SNARE);
            } else if (isHat()) {
                beats.add(BEAT_TYPE.HAT);
            }
        } else if (isBeatRange(manualLow, manualHigh)) {
            beats.add(BEAT_TYPE.MANUAL);
        }

        return beats;
    }

    /**
     * Is current update in a kick frequency.
     * @return true if kick was detected
     */
    public boolean isKick() {
        // Frequency for Kicks in sub band ~0.
        return isBeatRange(0, 0);
    }

    /**
     * Is current update in a snare frequency.
     * @return true if snare was detected
     */
    public boolean isSnare() {
        // ~ Values for Snares
        int low = 1;
        int high = fftSubBandsCount / 3;
        return isBeatRange(low, high);
    }

    /**
     * Is current update in a hat frequency.
     * @return true if hat was detected
     */
    public boolean isHat() {
        // ~ Values for Hats
        int low = fftSubBandsCount / 2;
        int high = fftSubBandsCount - 1;
        return isBeatRange(low, high);
    }

    /**
     * Is beat in specific range.
     * @param low sets the low frequency
     * @param high sets the high frequency
     * @return true if beat was detected in specific range
     */
    private boolean isBeatRange(int low, int high) {
        int thresholdBeatCounts = (high-low) / 3;

        int beatCounts = 0;
        for(int i = low; i <= high; i++) {
            if (isBeat(i))
                beatCounts++;
        }

        boolean beatDetected = (beatCounts > thresholdBeatCounts && ((System.currentTimeMillis() - lastBeat) > timeToWait));
        if (beatDetected)
            lastBeat = System.currentTimeMillis();
        return beatDetected;
    }

    /**
     * Do all calculations after update.
     * @param input of fft data
     */
    private void calcAll(double[] input) {
        // Weight average higher then current magnitude
        float factor = 0.09F;

        for (int i = 0; i < fftSize; i++) {
            double rfk = input[divisions * i];
            double ifk = input[divisions * i + 1];

            magnitude[i] =  (float) Math.pow(2 * Math.sqrt(rfk * rfk + ifk * ifk), 0.5);    // Math.max(0, Math.log10(rfk * rfk + ifk * ifk));

            avgMagnitude[i] = (magnitude[i] * factor) + (avgMagnitude[i] * (1 - factor));
        }

        for (int i = 0; i < fftSubBandsCount; i++) {
            for (int i2 = 0; i2 < fftSize / fftSubBandsCount; i2++) {
                fftSubBands[i] += magnitude[i * (fftSize / fftSubBandsCount) + i2];
            }
            fftSubBands[i] *= (float) fftSubBandsCount / (float) fftSize;

            for (int i2 = 0; i2 < fftSize / fftSubBandsCount; i2++) {
                fftVariance[i] += Math.pow(magnitude[i * (fftSize / fftSubBandsCount) + i2] - fftSubBands[i], 2);
            }
            fftVariance[i] = fftVariance[i] * (float) fftSubBandsCount / (float) fftSize;

            beatValues[i] = (float) (-0.0025714 * fftVariance[i]) + sensitivity;
        }

        for (int i = 0; i < fftSubBandsCount; i++) {
            averageEnergy[i] = 0;
            for(int h = 0; h < samplingRate / bufferSize; h++) {

                averageEnergy[i] += energyHistory[i][h];
            }

            averageEnergy[i] /= ((float) samplingRate / bufferSize);
        }

        for (int i = 0; i < fftSubBandsCount; i++) {

            energyHistory[i][historyPos] = fftSubBands[i];
        }

        historyPos = (historyPos + 1) % (samplingRate / bufferSize);
    }

    /**
     * Is this sub band a beat?
     * @param i interval of possible beats in sub bands.
     * @return true if a beat was detected.
     */
    private boolean isBeat(int i) {
        return fftSubBands[i] > (averageEnergy[i] * beatValues[i]) && fftSubBands[i] > threshold;
    }
}
