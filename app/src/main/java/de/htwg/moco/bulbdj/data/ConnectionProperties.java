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
 * Class represents properties for app's connection to the bridge.
 * <p>Stores user's name, ip address and value of auto start flag.
 *
 * @author Mislav Jurić
 * @version 1.0
 */
public class ConnectionProperties {

    /**
     * Value of ip address.
     */
    private String ipAddress;

    /**
     * Value of username.
     */
    private String userName;

    /**
     * Value of auto start flag.
     */
    private boolean autoStart;

    /**
     * Reference to context of main activity.
     */
    private Context context;

    /**
     * Name of property file.
     */
    private String fileName = "Connection.properties";

    /**
     * Default constructor. Sets properties' context.
     * @param context sets value of context
     */
    public ConnectionProperties(Context context) {
        this.context = context;
        if (fileExists()) {
            loadProperties();
        }
    }

    /**
     * Getter method.
     * @return value of ip address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Getter method.
     * @return value of username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Getter method.
     * @return value of auto start flag
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * Setter method.
     * @param ipAddress sets value of ip address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Setter method.
     * @param userName sets value of user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Setter method.
     * @param autoStart sets value of auto start flag
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * Method reads properties' values from file.
     */
    private void loadProperties() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(context.openFileInput(fileName), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("ip_address") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    ipAddress = pairs[1];
                }
                if (line.startsWith("user_name") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    userName = pairs[1];
                }
                if (line.startsWith("auto_start") && line.contains("=")) {
                    String[] pairs = line.split("=");
                    if (pairs.length != 2) continue;
                    autoStart = pairs[1].equals("true");
                }
            }

        } catch (IOException e) {
            Log.e("Error", "Could not open properties file");
        }
    }

    /**
     * Method saves properties' values.
     */
    public void saveProperties() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("ip_address=");
            if (ipAddress != null) {
                sb.append(ipAddress);
            }
            writer.println(sb.toString());

            sb = new StringBuilder();
            sb.append("user_name=");
            if (userName != null) {
                sb.append(userName);
            }
            writer.println(sb.toString());

            sb = new StringBuilder();
            sb.append("auto_start=");
            sb.append(autoStart ? "true" : "false");
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