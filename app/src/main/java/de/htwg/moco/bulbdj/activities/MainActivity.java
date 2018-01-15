package de.htwg.moco.bulbdj.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.AppProperties;
import de.htwg.moco.bulbdj.data.ConnectionProperties;
import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.detector.Modes;
import de.htwg.moco.bulbdj.renderers.LEDRenderer;
import de.htwg.moco.bulbdj.views.DemoView;
import de.htwg.moco.bulbdj.views.VisualizerView;

/**
 * Class represents main activity that is shown when application is started.
 * <p>
 * Functionalities of app:
 * <br>Setup of connection to the bridge
 * <br>Listening to the surrounding sound and tweeting the lights depending on the rhythm
 * and user preferences.
 * <br>Finding new lights
 * <br>Adjustment of app: sensitivity, mode(s), maximum frequency, brightness.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Result code identificator.
     */
    public static final int DISPLAY_RESULT_CODE = 500;

    /**
     * Permission request for record audio.
     */
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    /**
     * Audio manager reference.
     */
    private AudioManager audioManager;

    /**
     * LED renderer manager reference.
     */
    private LEDRenderer ledRenderer;

    /**
     * Visualizer view reference.
     */
    @BindView(R.id.visualizer_view)
    VisualizerView visualizerView;

    /**
     * Demo view reference.
     */
    @BindView(R.id.demo_view)
    DemoView demoView;

    /**
     * Start / stop switch reference.
     */
    @BindView(R.id.start_stop_btn)
    Button recordButton;

    /**
     * Toolbar reference.
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Spinner mode reference.
     */
    @BindView(R.id.mode_spinner)
    Spinner modeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        BridgeController.setContext(this.getApplicationContext());
        BridgeController.getInstance().registerPhsdkListener(phsdkListener);
        AppProperties.getInstance().setContext(this.getApplicationContext());

        if (BridgeController.getInstance().getConnectionProperties().isAutoStart() &&
                BridgeController.getInstance().propertiesDefined()) {
            autoConnect();
        }

        visualizerView.setRadius(recordButton.getWidth());
        ledRenderer = LEDRenderer.getInstance();
        audioManager = AudioManager.getInstance();

        initModeSpinner();

        initAudioManager();

        initLEDRenderer();

        loadSettings();

        if (!BridgeController.getInstance().isConnected()) {
            demoView.setVisibility(View.VISIBLE);
        }

        if (audioManager.isRunning())
            recordButton.setText(R.string.stop);
    }

    /**
     * Load all settings.
     */
    private void loadSettings() {
        audioManager.setSettings(AppProperties.getInstance().getSensitivity());
        audioManager.setBeatDetectorOn(AppProperties.getInstance().isModeSwitch());
    }

    /**
     * Methods initializes mode spinner component.
     */
    private void initModeSpinner() {
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.modes_array));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(spinnerAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ledRenderer.setModeAuto(i == 0);
                ledRenderer.setMode(Modes.values()[i]);

                int value = 100 - (int) ((audioManager.getSensitivity(Modes.values()[i]) - 1F) * 100F);

                AppProperties.getInstance().setSensitivity(value);
                AppProperties.getInstance().saveProperties();

                audioManager.setMode(Modes.values()[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Methods initializes audio manager.
     */
    private void initAudioManager() {
        audioManager.setAudioMangerListener(new AudioManager.AudioManagerListener() {
            @Override
            public void onBeatDetected(ArrayList<Object[]> beats) {
                if (audioManager.isDetectorOn())
                    ledRenderer.updateBeats(beats);
            }

            @Override
            public void onStop() {
                visualizerView.stop();
                ledRenderer.stop();
            }

            @Override
            public void onUpdated(double[] result) {
                visualizerView.updateVisualizer(result);
                if (!audioManager.isDetectorOn())
                    ledRenderer.updateFrequency(result);
            }

        });
    }

    /**
     * Methods initializes LED renderer.
     */
    private void initLEDRenderer() {
        ledRenderer.setLEDRendererListener(new LEDRenderer.LEDRendererListener() {

            @Override
            public void onUpdate(int[] bulbColors) {

                demoView.updateVisualizer(bulbColors);

                BridgeController bridgeController = BridgeController.getInstance();
                if (bridgeController.isLightsEmpty()) return;

                int counter = 0;
                for (String light : bridgeController.getAllLights()) {
                    bridgeController.setLightColor(light, bulbColors[counter++ % 3]);
                }
            }

            @Override
            public void onStop() {
                demoView.stop();
            }
        });
    }

    /**
     * Methods sets brightness of lights
     *
     * @param brightnessValue value of lights' brightness
     */
    private void setLightBrightness(int brightnessValue) {
        if (!BridgeController.getInstance().isConnected() || BridgeController.getInstance().isLightsEmpty())
            return;

        for (String idn : BridgeController.getInstance().getAllLights()) {
            BridgeController.getInstance().setLightBrightness(idn, brightnessValue);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecorder();

                } else {
                    stopRecorder(recordButton);
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, DISPLAY_RESULT_CODE);
                return true;
            case R.id.action_find_light:
                if (!BridgeController.getInstance().isConnected()) {
                    Toast.makeText(this.getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                    return false;
                }
                intent = new Intent(this, FindLightActivity.class);
                startActivityForResult(intent, DISPLAY_RESULT_CODE);
                return true;
            case R.id.action_manual:
                intent = new Intent(this, AdjustActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method called if properties are set. Automatically connects to the bridge, without interrupting the user.
     */
    private void autoConnect() {
        ConnectionProperties connectionProperties = BridgeController.getInstance().getConnectionProperties();
        String ipAddress = connectionProperties.getIpAddress();
        String macAddress = connectionProperties.getMacAddress();
        String userName = connectionProperties.getUserName();

        PHAccessPoint accessPoint = new PHAccessPoint(ipAddress, userName, macAddress);
        BridgeController.getInstance().getPHHueSDK().connect(accessPoint);
    }

    /**
     * Start the recorder.
     *
     * @param button
     */
    private void startRecorder(Button button) {
        button.setText(R.string.stop);
        startRecorder();
    }

    /**
     * Start the recorder.
     */
    private void startRecorder() {
        audioManager.start();
    }

    /**
     * Stop the recorder.
     *
     * @param button
     */
    private void stopRecorder(Button button) {
        button.setText(R.string.record);
        stopRecorder();
    }

    /**
     * Stop the recorder.
     */
    private void stopRecorder() {
        audioManager.stop();
    }

    @OnClick(R.id.start_stop_btn)
    void startStop(Button button) {
        if (audioManager.isRunning()) {
            stopRecorder(button);
        } else {
            checkMicPermission();
            startRecorder(button);
        }
    }

    /**
     * Check for microphone permissions.
     */
    private void checkMicPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    /**
     * Method is executed on activity result.
     * <br>In case result code equals SetupActivity.STATUS_OK, app is connected to bridge.
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DISPLAY_RESULT_CODE && resultCode == SettingsActivity.STATUS_OK) {
            Toast.makeText(this.getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
            setLightBrightness(AppProperties.getInstance().getBrightness());
            loadSettings();
        }
        if (requestCode == DISPLAY_RESULT_CODE && resultCode == SettingsActivity.STATUS_DISCONNECTED) {
            Toast.makeText(this.getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == DISPLAY_RESULT_CODE && resultCode == FindLightActivity.LIGHT_FOUND) {
            Toast.makeText(this.getApplicationContext(), "Light found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Listener for notifications from Philips Hue SDK.
     */
    private PHSDKListener phsdkListener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> accessPointsList) {
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
        }

        /**
         * If bridge connected, user is notified by toast message.
         * @param bridge connected bridge
         * @param username given username by bridge, which is needed to authenticate for auto connection
         */
        @Override
        public void onBridgeConnected(PHBridge bridge, String username) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                }
            });
            BridgeController.getInstance().setConnected(true);
            setLightBrightness(AppProperties.getInstance().getBrightness());
            loadSettings();
        }

        @Override
        public void onCacheUpdated(List<Integer> cache, PHBridge bridge) {
        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
        }

        @Override
        public void onConnectionResumed(PHBridge bridge) {
        }

        @Override
        public void onError(int code, final String message) {
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
        }
    };

    /**
     * Before destroying, {@link BridgeController} instance is terminated.
     */
    @Override
    protected void onDestroy() {
        BridgeController.getInstance().terminate();
        super.onDestroy();
    }

}
