package de.htwg.moco.bulbdj.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;

/**
 * Activity that provides functionality for adding new lights to selected bridge.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class FindLightActivity extends AppCompatActivity {
    /**
     * Progress bar which is displayed while searching for new access points.
     */
    @BindView(R.id.progress_bar_find)
    ProgressBar progressBar;

    /**
     * Input for serial number.
     */
    @BindView(R.id.input)
    EditText serial;

    /**
     * Status light found identificator.
     */
    public static final int LIGHT_FOUND = 108;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_light);
        ButterKnife.bind(this);
        initToolbar();

        progressBar.setVisibility(View.GONE);
    }

    /**
     * Method initializes activity's toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.find_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.find);
    }

    /**
     * Method is executed when search button clicked.
     * Bridge searches for new light.
     */
    @OnClick(R.id.find)
    void onClickFind() {
        hideKeyboard();
        if (serial.getText() == null ||
                serial.getText().toString().equals("")) {
            Toast.makeText(this.getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!BridgeController.getInstance().isConnected() ||
                BridgeController.getInstance().getPHHueSDK() == null) {
            Toast.makeText(this.getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
        if (bridge == null) {
            Toast.makeText(this.getApplicationContext(), "Bridge not available", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> serials = new ArrayList<>();
        serials.add(serial.getText().toString());
        try {
            bridge.findNewLightsWithSerials(serials, getLightListener());
        } catch (Exception e) {
        }
    }

    /**
     * Method for initializing light listener.
     *
     * @return new instance of light listener
     */
    private PHLightListener getLightListener() {
        return new PHLightListener() {
            boolean found = false;

            @Override
            public void onSuccess() {
            }

            @Override
            public void onStateUpdate(Map<String, String> arg0, List<PHHueError> arg1) {
            }

            @Override
            public void onError(int arg0, String arg1) {
            }

            @Override
            public void onReceivingLightDetails(PHLight arg0) {
            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> arg0) {
                if (found)
                    return;
                found = true;
                Intent intent = new Intent();
                setResult(LIGHT_FOUND, intent);
                finish();
            }

            @Override
            public void onSearchComplete() {
            }
        };
    }

    /**
     * Method hides the keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}