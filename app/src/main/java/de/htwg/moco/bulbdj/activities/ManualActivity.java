package de.htwg.moco.bulbdj.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import de.htwg.moco.bulbdj.R;
import de.htwg.moco.bulbdj.detector.AudioManager;
import de.htwg.moco.bulbdj.detector.Types;

public class ManualActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SeekBar minFreqBar;
    private SeekBar maxFreqBar;

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

        SeekBar sensitivityBar = (SeekBar) findViewById(R.id.sensitivityBar);
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

        sensitivityBar.setProgress(settings.getInt("sensitivity", 0));
        modeSwitch.setChecked(settings.getInt("mode", 0) != 0);

        minFreqBar = (SeekBar) findViewById(R.id.minFreq);
        maxFreqBar = (SeekBar) findViewById(R.id.maxFreq);
        minFreqBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setFrequency();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        maxFreqBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setFrequency();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setMode(Types.FREQUENCY);
                else
                    setMode(Types.BEAT);
            }
        });
    }

    private void setFrequency()
    {
        AudioManager.getInstance().setFrequencyRange(minFreqBar.getProgress(), maxFreqBar.getProgress());
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
     * @param mode of visualization
     */
    public void setMode(Types mode) {
        AudioManager.getInstance().setBeatDetectorOn(mode == Types.BEAT);
        editor.putInt("mode", mode.compareTo(Types.BEAT));
        editor.commit();
    }

}
