package de.htwg.moco.bulbdj.bridge;

import android.content.Context;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;

import de.htwg.moco.bulbdj.data.ConnectionProperties;

/**
 * Class represents singleton for obtaining single connection to bridge.
 *
 * @author Mislav Jurić
 * @version 1.0
 */

public class BridgeController {

    /**
     * Singleton instance of {@link BridgeController} class.
     */
    private static BridgeController bridgeController = null;

    /**
     * Reference to context of main activity.
     */
    private static Context context;

    /**
     * Reference to {@link PHHueSDK} instance.
     */
    private PHHueSDK pHHueSDK;

    /**
     * Instance of {@link ConnectionProperties} class.
     */
    private ConnectionProperties connectionProperties;

    /**
     * Flag for bridge connection. True if connected.
     */
    private boolean connected;

    /**
     * Default constructor. Private because of singleton pattern.
     */
    private BridgeController() {
        pHHueSDK = PHHueSDK.getInstance();
        connectionProperties = new ConnectionProperties(context);
    }

    /**
     * Method gets singleton instance of {@link BridgeController} class.
     *
     * @return singleton instance
     */
    public static BridgeController getInstance() {
        if (bridgeController == null) {
            bridgeController = new BridgeController();
        }
        return bridgeController;
    }

    /**
     * Method terminates connection to the bridge.
     */
    public void terminate() {
        pHHueSDK.stopPushlinkAuthentication();
        pHHueSDK.destroySDK();
        bridgeController = null;
        connected = false;
    }

    /**
     * Setter method.
     * @param context sets value of context
     */
    public static void setContext(Context context) {
        BridgeController.context = context;
    }

    /**
     * Setter method.
     * @param connected sets value of connection flag
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Getter method.
     * @return value of connection flag, true if bridge is connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Getter method.
     * @return connection properties
     */
    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * Getter method.
     * @return reference to {@link PHHueSDK} instance
     */
    public PHHueSDK getPHHueSDK() {
        return pHHueSDK;
    }

    /**
     * Method checks whether property values are defined.
     * @return true if property values are defined, otherwise false
     */
    public boolean propertiesDefined() {
        return !(connectionProperties.getIpAddress() == null || connectionProperties.getUserName() == null);
    }

    /**
     * Method registers instance of {@link PHSDKListener} listener.
     * @param phsdkListener listener that is registered
     */
    public void registerPhsdkListener(PHSDKListener phsdkListener) {
        bridgeController.pHHueSDK.getNotificationManager().registerSDKListener(phsdkListener);
    }
}
