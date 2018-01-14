package de.htwg.moco.bulbdj.bridge;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.philips.lighting.annotations.Bridge;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
     * Maximum hue value.
     */
    private final float MAX_HUE = 65534;

    /**
     * Maximum saturation value.
     */
    private final float MAX_SATURATION = 254;

    /**
     * Maximum brightness value.
     */
    private final float MAX_BRIGTHNESS = 254;

    /**
     * Default constructor. Private because of singleton pattern.
     */
    private BridgeController() {
        try {
            pHHueSDK = PHHueSDK.getInstance();

        } catch (Exception e) {
        }
        if (context == null) return;
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
        try {
            pHHueSDK.stopPushlinkAuthentication();
            pHHueSDK.destroySDK();
        } catch (Exception e) {

        }
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

        return (connectionProperties != null &&
                connectionProperties.getIpAddress() != null &&
                connectionProperties.getUserName() != null &&
                connectionProperties.getMacAddress() != null);
    }

    /**
     * Method registers instance of {@link PHSDKListener} listener.
     * @param phsdkListener listener that is registered
     */
    public void registerPhsdkListener(PHSDKListener phsdkListener) {
        try {
            bridgeController.pHHueSDK.getNotificationManager().registerSDKListener(phsdkListener);
        } catch (Exception e) {
        }
    }

    /**
     * Getter method.
     *
     * @return count of lights connected to selected bridge
     */
    public boolean isLightsEmpty() {
        if (!connected || pHHueSDK == null || pHHueSDK.getSelectedBridge() == null) return true;
        try {
            return pHHueSDK.getSelectedBridge().getResourceCache().getAllLights().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Getter method.
     *
     * @return list of all lights connected to selected bridge
     */
    public List<String> getAllLights() {
        if (!connected || pHHueSDK == null || pHHueSDK.getSelectedBridge() == null) return null;
        try {
            return new ArrayList<>(pHHueSDK.getSelectedBridge().getResourceCache().getLights().keySet());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Getter method.
     *
     * @param idn light bulbs' identifier
     * @return light bulb with given identifier if exists, otherwise <<code>null</code>
     */
    @Nullable
    private PHLight getLight(String idn) {
        if (!connected || pHHueSDK == null || pHHueSDK.getSelectedBridge() == null) return null;
        try {
            return pHHueSDK.getSelectedBridge().getResourceCache().getLights().get(idn);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Method sets light bulb's color.
     *
     * @param idn light bulbs' identifier
     * @param color value set for color
     */
    public void setLightColor(String idn, int color) {
        PHLight light = getLight(idn);
        if (!connected || pHHueSDK == null || pHHueSDK.getSelectedBridge() == null || light == null) return;

        float[] HSV = new float[3];
        Color.colorToHSV(color, HSV);

        PHLightState lightState = new PHLightState();

        lightState.setHue(Math.round(HSV[0] / 360 * MAX_HUE));
        lightState.setSaturation(Math.round(HSV[1] * MAX_SATURATION));
        lightState.setTransitionTime(0);

        try {
            pHHueSDK.getSelectedBridge().updateLightState(light, lightState);
        } catch (Exception e) {
        }
    }

    /**
     * Method sets light bulb's brightness.
     *
     * @param idn light bulbs' identifier
     * @param brightness value set for brightness
     */
    public void setLightBrightness(String idn, int brightness) {
        PHLight light = getLight(idn);
        if (!connected || pHHueSDK == null || pHHueSDK.getSelectedBridge() == null || light == null ||
                brightness < 0 || brightness > MAX_BRIGTHNESS) return;

        PHLightState lightState = new PHLightState();
        lightState.setBrightness(brightness);
        lightState.setTransitionTime(0);

        try {
            pHHueSDK.getSelectedBridge().updateLightState(light, lightState);
        } catch (Exception e) {
        }
    }
}
