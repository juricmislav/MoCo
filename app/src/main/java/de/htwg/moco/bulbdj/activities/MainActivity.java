package de.htwg.moco.bulbdj.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.ConnectionProperties;

/**
 * Class represents main activity that is shown when application is started.
 * <br>
 * Functionalities of app:
 * <p>Setup of connection to the bridge
 * <p>Listening to the surrounding sound and tweeting the lights depending on the rhythm
 * and user preferences.
 *
 * ...TO DO...
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Random lights button.
     */
    @BindView(R.id.random_lights)
    Button randomLights;

    /**
     * Seek bar for brightness.
     */
    @BindView(R.id.seek_bar)
    SeekBar seekBar;

    /**
     * Result code identificator.
     */
    public static final int DISPLAY_RESULT_CODE = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BridgeController.setContext(this);
        BridgeController.getInstance().registerPhsdkListener(phsdkListener);

        if (BridgeController.getInstance().getConnectionProperties().isAutoStart() &&
                BridgeController.getInstance().propertiesDefined()) {
            autoConnect();
        }
        setSeekBarListener();
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, DISPLAY_RESULT_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method called if properties are set. Automatically connects to the bridge, without interrupting the user.
     */
    private void autoConnect() {
        ConnectionProperties connectionProperties = BridgeController.getInstance().getConnectionProperties();
        String ipAddress = connectionProperties.getIpAddress();
        String userName = connectionProperties.getUserName();

        PHAccessPoint accessPoint = new PHAccessPoint(ipAddress, userName, null);
        BridgeController.getInstance().getPHHueSDK().connect(accessPoint);
    }

    @OnClick(R.id.random_lights)
    void randomLights() {
        if (!BridgeController.getInstance().isConnected()) return;
        PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
        PHBridgeResourcesCache cache = bridge.getResourceCache();

        List<PHLight> allLights = cache.getAllLights();
        Random rand = new Random();

        for (PHLight light : allLights) {

            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(65535));
            lightState.setBrightness(light.getLastKnownLightState().getBrightness());
            bridge.updateLightState(light, lightState);
        }
    }

    /**
     * Method initializes listener for the seek bar.
     */
    void setSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!BridgeController.getInstance().isConnected()) return;
                PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
                PHBridgeResourcesCache cache = bridge.getResourceCache();

                List<PHLight> allLights = cache.getAllLights();
                Random rand = new Random();

                for (PHLight light : allLights) {

                    PHLightState lightState = new PHLightState();
                    lightState.setHue(light.getLastKnownLightState().getHue());
                    lightState.setBrightness((int) (2.55 * i));
                    bridge.updateLightState(light, lightState);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == DISPLAY_RESULT_CODE && resultCode == SettingsActivity.STATUS_DISCONNECTED) {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                }
            });
            BridgeController.getInstance().setConnected(true);
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
