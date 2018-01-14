package de.htwg.moco.bulbdj.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Class represents properties for application, designed as singleton.
 * <p>Stores app's brightness settings.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class AppProperties {

    private static AppProperties appProperties = null;

    /**
     * Value of ip address.
     */
    private int brightness = 150;

    /**
     * Value of delay (beat frequency detection).
     */
    private int delay = 200;

    /**
     * Value of delay (beat frequency detection).
     */
    private int sensitivity = 40;

    /**
     * Flag for mode switch component
     */
    private boolean modeSwitch = true;

    /**
     * Reference to context of main activity.
     */
    private Context context;

    /**
     * Name of property file.
     */
    private String fileName = "AppProp.properties";

    /**
     * Default constructor.
     */
    public AppProperties() {
    }

    /**
     * Setter method.
     *
     * @param context sets value of context
     */
    public void setContext(Context context) {
        this.context = context;
        if (fileExists()) {
            loadProperties();
        }
    }

    /**
     * Getter method for singleton type of class.
     *
     * @return singleton instance of class
     */
    public static AppProperties getInstance() {
        if (appProperties == null) {
            appProperties = new AppProperties();
        }
        return appProperties;
    }

    /**
     * Getter method.
     * @return value of brightness
     */
    public int getBrightness() {
        return brightness;
    }

    /**
     * Getter method.
     * @return value of delay
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Getter method.
     * @return value of sensitivity
     */
    public int getSensitivity() {
        return sensitivity;
    }

    /**
     * Getter method.
     * @return value of mode switch component flag
     */
    public boolean isModeSwitch() {
        return modeSwitch;
    }

    /**
     * Setter method.
     * @param brightness sets value of brightness
     */
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    /**
     * Setter method.
     * @param delay sets value of delay
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Setter method.
     * @param sensitivity sets value of sensitivity
     */
    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Setter method.
     * @param modeSwitch sets value of mode switch flag
     */
    public void setModeSwitch(boolean modeSwitch) {
        this.modeSwitch = modeSwitch;
    }

    /**
     * Method reads properties' values from file.
     */
    private void loadProperties() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(context.openFileInput(fileName), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("brightness") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    brightness = Integer.valueOf(pairs[1]);
                }
                if (line.startsWith("delay") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    delay = Integer.valueOf(pairs[1]);
                }
                if (line.startsWith("sensitivity") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    sensitivity = Integer.valueOf(pairs[1]);
                }
                if (line.startsWith("mode_switch") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    modeSwitch = Boolean.valueOf(pairs[1]);
                }
            }

        } catch (Exception e) {
            Log.e("Error", "Could not load App properties");
        }
    }

    /**
     * Method saves properties' values.
     */
    public void saveProperties() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("brightness=");
            sb.append(brightness);
            writer.println(sb.toString());

            sb = new StringBuilder();
            sb.append("delay=");
            sb.append(delay);
            writer.println(sb.toString());

            sb = new StringBuilder();
            sb.append("sensitivity=");
            sb.append(sensitivity);
            writer.println(sb.toString());

            sb = new StringBuilder();
            sb.append("mode_switch=");
            sb.append(modeSwitch);
            writer.print(sb.toString());
        } catch (IOException e) {
            Log.e("Error", "Could not save properties");
        }
    }

    /**
     * Method checks whether properties file exists.
     * @return true if file exists, otherwise false
     */
    private boolean fileExists() {
        File file = context.getFileStreamPath(fileName);
        return !(file == null || !file.exists());
    }
}