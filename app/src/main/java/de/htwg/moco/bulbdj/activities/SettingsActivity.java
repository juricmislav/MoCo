package de.htwg.moco.bulbdj.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.ConnectionProperties;

/**
 * Activity that provides functionality for connection settings.
 * <br>User can set auto connection on start of app, so that setup
 * is done immediately when app starts, if previous saved connection
 * properties are valid.
 * <p>
 * User can disconnect from currently connected bridge, and setup a
 * new connection to selected bridge.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Auto connect switch when app starts.
     */
    @BindView(R.id.auto_connect)
    Switch autoConnect;

    /**
     * Disconnect from currently connected bridge button.
     */
    @BindView(R.id.disconnect)
    Button disconnect;

    /**
     * Setup new connection.
     */
    @BindView(R.id.setup)
    Button setup;

    /**
     * Status OK identificator.
     */
    public static final int STATUS_OK = 100;

    /**
     * Status disconnected identificator.
     */
    public static final int STATUS_DISCONNECTED = 105;

    /**
     * Result code identificator.
     */
    public static final int DISPLAY_RESULT_CODE = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        refreshButtonStatus();
        initToolbar();
    }

    /**
     * Method initializes activity's toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);
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
     * Method refreshes buttons' status, depending on bridge connection.
     */
    private void refreshButtonStatus() {
        if (BridgeController.getInstance().isConnected()) {
            disconnect.setEnabled(true);
            setup.setEnabled(false);
        } else {
            disconnect.setEnabled(false);
            setup.setEnabled(true);
        }
        if (BridgeController.getInstance().getConnectionProperties().isAutoStart()) {
            autoConnect.setChecked(true);
        }
    }

    /**
     * Method is executed on change of switch status.
     * It aves auto connect property.
     */
    @OnCheckedChanged(R.id.auto_connect)
    void onClickAutoConnect() {
        ConnectionProperties properties = BridgeController.getInstance().getConnectionProperties();
        properties.setAutoStart(autoConnect.isChecked());
        properties.saveProperties();
    }

    /**
     * Method is executed when disconnect button clicked.
     * It disconnects from currently connected bridge.
     */
    @OnClick(R.id.disconnect)
    void onClickDisconnect() {
        BridgeController.getInstance().terminate();
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        refreshButtonStatus();
    }

    /**
     * Method is executed when setup button clicked.
     * It starts new setup activity.
     */
    @OnClick(R.id.setup)
    void onClickSetup() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivityForResult(intent, DISPLAY_RESULT_CODE);
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
        if (requestCode == DISPLAY_RESULT_CODE && resultCode == SetupActivity.STATUS_OK) {
            Intent intent = new Intent();
            setResult(STATUS_OK, intent);
            finish();
        }
        refreshButtonStatus();
    }
}
