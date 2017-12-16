package de.htwg.moco.bulbdj.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.ConnectionProperties;

/**
 * Activity that provides functionality for connecting to new bridge.
 * <br>User can search for bridges on the network and connect to selected access point.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class SetupActivity extends AppCompatActivity {

    /**
     * Adapter used for listing found access points in list view.
     */
    private ArrayAdapter adapter;

    /**
     * List of found access points.
     */
    private List<PHAccessPoint> accessPointsList;

    /**
     * Search for new access point button.
     */
    @BindView(R.id.search)
    Button search;

    /**
     * List view of found access points.
     */
    @BindView(R.id.list)
    ListView list;

    /**
     * Image view for displaying authentication instruction image.
     */
    @BindView(R.id.image)
    ImageView image;

    /**
     * Progress bar which is displayed while searching for new access points.
     */
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    /**
     * Status OK identificator.
     */
    public static final int STATUS_OK = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ButterKnife.bind(this);
        BridgeController.getInstance().registerPhsdkListener(phsdkListener);

        adapter = new ArrayAdapter<String>(this, R.layout.list_layout);
        list.setAdapter(adapter);
        setListItemListener();

        image.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.SRC_IN);

        initToolbar();
    }

    /**
     * Method sets list's item listener.
     */
    private void setListItemListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long l) {
                connectToBridge(accessPointsList.get(pos));
            }
        });
    }

    /**
     * Method initializes activity's toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setup_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.setup);
    }

    /**
     * Methods sets toolbar navigation.
     *
     * @return always true
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Method is executed when search button clicked.
     * It search for new access points.
     */
    @OnClick(R.id.search)
    void onClickSearch() {
        PHBridgeSearchManager sm = (PHBridgeSearchManager) BridgeController.getInstance().getPHHueSDK().getSDKService(PHHueSDK.SEARCH_BRIDGE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        sm.search(true, true);
    }


    /**
     * Method connects to provided access point.
     *
     * @param accessPoint to connect
     */
    private void connectToBridge(PHAccessPoint accessPoint) {
        ConnectionProperties connectionProperties = BridgeController.getInstance().getConnectionProperties();
        connectionProperties.setIpAddress(accessPoint.getIpAddress());
        accessPoint.setUsername(connectionProperties.getUserName());
        BridgeController.getInstance().getPHHueSDK().connect(accessPoint);
    }

    /**
     * Listener for notifications from Philips Hue SDK.
     */
    private PHSDKListener phsdkListener = new PHSDKListener() {

        /**
         * If access points found, progress bar terminates and access points
         * are listed in list view.
         * @param accessPointsList list of found access points
         */
        @Override
        public void onAccessPointsFound(List<PHAccessPoint> accessPointsList) {
            SetupActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.GONE);
                }
            });
            SetupActivity.this.accessPointsList = accessPointsList;
            for (final PHAccessPoint accessPoint : accessPointsList) {
                SetupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(accessPoint.getIpAddress());
                    }
                });
            }
        }

        /**
         * If authentication required, instruction image is displayed.
         * @param accessPoint for which authentication is needed
         */
        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            BridgeController.getInstance().getPHHueSDK().startPushlinkAuthentication(accessPoint);
            SetupActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    list.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                }
            });
        }

        /**
         * If bridge connected, new properties are saved and status is returned to main activity.
         * @param bridge connected bridge
         * @param username given username by bridge, which is needed to authenticate for auto connection
         */
        @Override
        public void onBridgeConnected(PHBridge bridge, String username) {
            ConnectionProperties connectionProperties = BridgeController.getInstance().getConnectionProperties();
            connectionProperties.setUserName(username);
            connectionProperties.saveProperties();
            SetupActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    setResult(STATUS_OK, intent);
                    finish();
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
     * Before destroying, created instance of PHSDKListener is unregistered from SDK listeners.
     */
    @Override
    protected void onDestroy() {
        BridgeController.getInstance().getPHHueSDK().getNotificationManager().unregisterSDKListener(phsdkListener);
        super.onDestroy();
    }
}
