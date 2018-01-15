package de.htwg.moco.bulbdj.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.bridge.BridgeController;
import de.htwg.moco.bulbdj.data.AppProperties;
import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.renderers.LEDRenderer;

/**
 * Class represents activity for app adjustments.
 * <br>Provided features:
 * <br>Setting delay of beat (frequency of detection)
 * <br>Switch for beat detection mode
 * <br>Setting sensitivity of microphone
 * <br>Setting brightness of bulbs
 *
 * @author Mislav Jurić & Daniel Steidinger
 * @version 1.0
 */
public class AdjustActivity extends AppCompatActivity {
    /**
     * Sensitivity text view reference.
     */
    @BindView(R.id.text_view_sensitivity)
    TextView textViewSensitivity;

    /**
     * Mode switch reference.
     */
    @BindView(R.id.mode_switch)
    Switch modeSwitch;

    /**
     * Sensitivity seek bar reference.
     */
    @BindView(R.id.sensitivity_bar)
    SeekBar sensitivityBar;

    /**
     * Delay text view reference.
     */
    @BindView(R.id.delay)
    TextView delayText;

    /**
     * Delay seek bar reference.
     */
    @BindView(R.id.delay_bar)
    SeekBar delayBar;

    /**
     * Sensitivity seek bar reference.
     */
    @BindView(R.id.brightness_Bar)
    SeekBar brightnessBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);
        ButterKnife.bind(this);
        initToolbar();

        initSensitivityBar();

        initDelayBar();

        initModeSwitch();

        initBrightnessBar();
    }

    /**
     * Method initializes activity's toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.adjust_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.action_adjust);
    }

    /**
     * Methods initializes sensitivity seek bar.
     */
    private void initSensitivityBar() {
        sensitivityBar.setProgress(AppProperties.getInstance().getSensitivity());

        sensitivityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AppProperties.getInstance().setSensitivity(progress);
                AppProperties.getInstance().saveProperties();
                AudioManager.getInstance().setSettings(progress);
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
     * Methods initializes delay seek bar.
     */
    private void initDelayBar() {
        int delayValue = AppProperties.getInstance().getDelay();
        int processValue = (delayValue - 20) / 5;
        delayBar.setProgress(processValue);
        delayText.setText(String.valueOf(delayValue));

        delayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int delayValue = progress * 5 + 20;
                delayText.setText(String.valueOf(delayValue));
                AppProperties.getInstance().setDelay(delayValue);
                AppProperties.getInstance().saveProperties();
                LEDRenderer.getInstance().setDelay(delayValue);
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
     * Methods initializes mode switch component.
     */
    private void initModeSwitch() {
        boolean isChecked = AppProperties.getInstance().isModeSwitch();
        modeSwitch.setChecked(isChecked);
        modeSwitchChanged(isChecked);

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppProperties.getInstance().setModeSwitch(isChecked);
                AppProperties.getInstance().saveProperties();
                modeSwitchChanged(isChecked);
                AudioManager.getInstance().setBeatDetectorOn(isChecked);
            }
        });
    }

    /**
     * Methods sets components when mode switch value changed.
     *
     * @param isChecked value of mode switch
     */
    private void modeSwitchChanged(boolean isChecked) {
        if (isChecked) {
            sensitivityBar.setVisibility(View.VISIBLE);
            textViewSensitivity.setVisibility(View.VISIBLE);
        } else {
            AppProperties.getInstance().setModeSwitch(false);
            sensitivityBar.setVisibility(View.INVISIBLE);
            textViewSensitivity.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Methods initializes brightness seek bar.
     */
    private void initBrightnessBar() {
        brightnessBar.setProgress((int)(AppProperties.getInstance().getBrightness() / 2.54));
        setLightBrightness(AppProperties.getInstance().getBrightness());

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int brightnessValue = (int)(seekBar.getProgress() * 2.54);
                AppProperties.getInstance().setBrightness(brightnessValue);
                AppProperties.getInstance().saveProperties();
                setLightBrightness(brightnessValue);
            }
        });
    }

    /**
     * Methods sets brightness of lights
     *
     * @param brightnessValue value of lights' brightness
     */
    private void setLightBrightness(int brightnessValue) {
        if (BridgeController.getInstance().isConnected() && !BridgeController.getInstance().isLightsEmpty()) {
            for (String idn : BridgeController.getInstance().getAllLights()) {
                BridgeController.getInstance().setLightBrightness(idn, brightnessValue);
            }
        }
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
