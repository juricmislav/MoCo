package de.htwg.moco.bulbdj.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.detector.Types;
import de.htwg.moco.bulbdj.renderers.LEDRenderer;

public class ManualActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        Toolbar toolbar = (Toolbar) findViewById(R.id.manual_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.action_manual);

        SharedPreferences settings = getSharedPreferences("beatDetection", MODE_PRIVATE);
        editor = settings.edit();

        Switch modeSwitch = (Switch) findViewById(R.id.modeSwitch);

        final TextView textViewSensitivity = (TextView) findViewById(R.id.textViewSensitivity);

        final SeekBar sensitivityBar = (SeekBar) findViewById(R.id.sensitivityBar);
        sensitivityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveSensitivity("sensitivity", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final TextView delayTest = (TextView) findViewById(R.id.delay);
        final SeekBar delayBar = (SeekBar) findViewById(R.id.delayBar);
        delayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int delayValue = progress * 5 + 20;
                delayTest.setText(String.valueOf(delayValue));
                editor.putInt("delay", delayValue);
                editor.commit();
                LEDRenderer.getInstance().setDelay(delayValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        int delayValue = settings.getInt("delay", 50);
        int processValue = (delayValue - 20) / 5;
        delayBar.setProgress(processValue);
        sensitivityBar.setProgress(settings.getInt("sensitivity", 0));
        boolean isChecked = settings.getInt("mode", 0) != 1;
        modeSwitch.setChecked(isChecked);
        if (isChecked) {
            setMode(Types.BEAT);
            sensitivityBar.setVisibility(View.VISIBLE);
            textViewSensitivity.setVisibility(View.VISIBLE);
        } else {
            setMode(Types.FREQUENCY);
            sensitivityBar.setVisibility(View.INVISIBLE);
            textViewSensitivity.setVisibility(View.INVISIBLE);
        }

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setMode(Types.BEAT);
                    sensitivityBar.setVisibility(View.VISIBLE);
                    textViewSensitivity.setVisibility(View.VISIBLE);
                } else {
                    setMode(Types.FREQUENCY);
                    sensitivityBar.setVisibility(View.INVISIBLE);
                    textViewSensitivity.setVisibility(View.INVISIBLE);
                }
            }
        });
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

    private void saveSensitivity(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
        AudioManager.getInstance().setSettings(value);
    }

    /**
     * Set the mode of visualization.
     * @param type of visualization
     */
    public void setMode(Types type) {
        AudioManager.getInstance().setBeatDetectorOn(type == Types.BEAT);
        editor.putInt("mode", type.compareTo(Types.BEAT));
        editor.commit();
    }

}
