package de.htwg.moco.bulbdj.detector;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 *
 * Class records from microphone in real time.
 * Result is an FFT Data Array.
 *
 * @author Daniel Steidinger
 * @version 1.0
 */
public class AudioRecorder extends AsyncTask<Void, double[], Void> {

    /**
     * Interface of {@link AudioRecorder} class.
     *
     * @author Daniel Steidinger
     * @version 1.0
     */
    public interface AudioRecorderListener {

        /**
         * FFT update.
         * @param result the raw fft data.
         */
        void onUpdate(double[] result);
    }

    /**
     * Instance of {@link AudioRecorderListener} class.
     */
    private AudioRecorderListener listener;

    /**
     * Setter method.
     * @param listener sets the listener of {@link AudioRecorderListener} class.
     */
    public void setAudioRecorderListener(AudioRecorderListener listener) {
        this.listener = listener;
    }

    /**
     * The sampling rate.
     */
    private int sampleRate;

    /**
     * The channel configuration of the recording.
     */
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;

    /**
     * The encoding of the recording.
     */
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * The block size of the buffer.
     */
    private int blockSize;

    /**
     * Was the recoding started.
     */
    private boolean started = false;

    /**
     * Default constructor.
     * @param sampleRate of the recording
     * @param blockSize of the buffer
     */
    public AudioRecorder(int sampleRate, int blockSize) {
        this.listener = null;
        this.sampleRate = sampleRate;
        this.blockSize = blockSize;
    }

    /**
     * Is the recording running.
     * @return true if recording was started
     */
    public boolean isRunning() {
        return started;
    }

    /**
     * Start the recording.
     *
     * Record from the microphone in background task.
     * Convert the results to FFT data and publish the process.
     *
     */
    public void start() {
        started = true;
        this.execute();
    }

    /**
     * Stop the recording.
     */
    public void stop() {
        started = false;
        this.cancel(true);
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        try {
            int bufferSize =
                    AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);

            AudioRecord audioRecord =
                    new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                            channelConfig, audioEncoding, bufferSize);

            short[] buffer = new short[blockSize];
            double[] result = new double[blockSize];

            audioRecord.startRecording();

            while (started) {
                int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                // Short to Double
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    result[i] = (double) buffer[i] / 32768.0;
                }

                // Execute FFT
                DoubleFFT_1D doubleFFT = new DoubleFFT_1D(blockSize);
                doubleFFT.realForward(result);

                publishProgress(result);
            }

            audioRecord.stop();

        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("AudioRecord", "Record Failed");
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(double[]... result) {
        if (listener != null) {
            listener.onUpdate(result[0]);
        }
    }
}
