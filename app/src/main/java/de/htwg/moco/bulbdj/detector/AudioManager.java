package de.htwg.moco.bulbdj.detector;

import java.util.ArrayList;

/**
 * Class handles the audio recording and the FFT Output / BeatDetection.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class AudioManager {

    /**
     * Instance of {@AudioManager} class.
     */
    private static AudioManager instance = null;

    /**
     * Interface of {@link AudioManager} class.
     *
     * @author Daniel Steidinger
     * @version 1.0
     */
    public interface AudioManagerListener {

        /**
         * Beat was detected.
         * @param beats the detected beats
         */
        void onBeatDetected(ArrayList<BeatDetector.BEAT_TYPE> beats);

        /**
         * FFT update.
         * @param result the raw fft data.
         */
        void onUpdated(double[] result);

        /**
         * Stop the audio recording.
         */
        void onStop();
    }

    /**
     * Instance of {@link AudioManagerListener} class.
     */
    private AudioManagerListener listener;

    /**
     * Setter method.
     * @param listener sets the listener of {@link AudioManagerListener} class.
     */
    public void setAudioMangerListener(AudioManagerListener listener) {
        this.listener = listener;
    }

    /**
     * The sampling rate.
     */
    private int samplingRate = 22050;//11025;   // Sampling rate and Frequency spectrum = samplingRate / 2 in Hz

    /**
     * The block size of the buffer.
     */
    private int blockSize = 512;

    /**
     * Flag for run detection.
     */
    private boolean isDetectorOn = true;

    /**
     * Instance of {@AudioRecorder} class.
     */
    private AudioRecorder audioRecorder;

    /**
     * Instance of {@BeatDetector} class.
     */
    private BeatDetector detector;

    /**
     * Is AudioManager running.
     */
    private boolean running = false;

    /**
     * Singleton instance of {@link AudioManager} class.
     */
    public static AudioManager getInstance() {
        if (instance == null)
            instance = new AudioManager();
        return instance;
    }

    /**
     * Default constructor. This method is private because of the singleton instance.
     */
    private AudioManager() {
        this.listener = null;
        detector = new BeatDetector(samplingRate, blockSize, blockSize * 2);
    }

    /**
     * Setter method.
     * @param sensitivity sets the sensitivity of detection.
     */
    public void setSettings(int sensitivity) {

        if (sensitivity >= 0)
            detector.setSensitivity(sensitivity);
    }

    /**
     * Setter method. Manual setter for a specific range.
     * @param low sets the low frequency
     * @param high sets the high frequency
     */
    public void setFrequencyRange(int low, int high) {
        detector.setManualRange(low, high);
    }

    /**
     * Is the current recorder running.
     * @return true if recorder is running
     */
    public boolean isRunning() {
        return audioRecorder != null && audioRecorder.isRunning();
    }

    /**
     * Starts the recording and beat detection.
     */
    public void start() {
        running = true;
        audioRecorder = new AudioRecorder(samplingRate, blockSize);
        audioRecorder.start();
        audioRecorder.setAudioRecorderListener(new AudioRecorder.AudioRecorderListener() {
            @Override
            public void onUpdate(double[] result) {
                if (running) {
                    if (isDetectorOn)
                        detector.update(result);
                    listener.onUpdated(result);
                }
            }
        });
        detector.setBeatDetectorListener(new BeatDetector.BeatDetectorListener() {
            @Override
            public void onBeatDetected(ArrayList<BeatDetector.BEAT_TYPE> beats) {
                if (listener != null)
                    listener.onBeatDetected(beats);
            }
        });
    }

    /**
     * Setter method.
     * @param on sets the detection on or off.
     */
    public void setBeatDetectorOn(boolean on) {
        this.isDetectorOn = on;
    }

    /**
     *
     */
    public boolean isDetectorOn() {
        return isDetectorOn;
    }

    /**
     * Stops the recorder and call the stop method for the listener.
     */
    public void stop() {
        running = false;
        audioRecorder.stop();
        listener.onStop();
    }
}
