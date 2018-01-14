package de.htwg.moco.bulbdj.bridge;

import android.content.Context;
import android.graphics.Color;

import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.ArrayList;
import java.util.List;

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
     * Maximum value of hue.
     */
    private final float MAX_HUE = 65534;

    /**
     * Maximum value of saturation.
     */
    private final float MAX_SATURATION = 254;

    /**
     * Maximum value of brightness.
     */
    private final int MAX_BRIGTHNESS = 254;

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

    /**
     * Getter method.
     * @return number of light bulbs connected to bridge
     */
    public int getLightCount() {
        if (bridgeController == null) return 0;
        try {
            PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
            return bridge.getResourceCache().getAllLights().size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Getter method.
     * @return light bulb's identifiers
     */
    public List<String> getLightIdentifiers() {
        if (bridgeController == null) return null;
        try {
            PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
            return new ArrayList<>(bridge.getResourceCache().getLights().keySet());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Getter method.
     * @param idn identifier of light bulb
     * @return light bulb with provided identifier
     */
    private PHLight getLight(String idn) {
        if (bridgeController == null) return null;
        try {
            PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
            return bridge.getResourceCache().getLights().get(idn);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Method sets the brightness of certain light bulb, defined by identifier.
     * @param idn identifier of light bulb
     * @param brightness value of brightness
     */
    public void setLightBrightness(String idn, int brightness) {
        if (bridgeController == null ||idn == null) return;
        try {
            if (getLight(idn) == null) return;
            PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
            PHLightState lightState = new PHLightState();
            lightState.setBrightness(brightness > MAX_BRIGTHNESS ? MAX_BRIGTHNESS : brightness);
            bridge.updateLightState(getLight(idn), lightState);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Method sets the color of certain light bulb, defined by identifier.
     * @param idn identifier of light bulb
     * @param hue value of color
     * @param saturation value of color
     */
    private void setLightColor(String idn, int hue, int saturation) {
        if (bridgeController == null ||idn == null) return;
        try {
            if (getLight(idn) == null) return;
            PHBridge bridge = BridgeController.getInstance().getPHHueSDK().getSelectedBridge();
            PHLightState lightState = new PHLightState();
            lightState.setHue(hue);
            lightState.setSaturation(saturation);
            bridge.updateLightState(getLight(idn), lightState);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Method sets the color of certain light bulb, defined by identifier.
     * @param idn identifier of light bulb
     * @param color color to be set
     */
    public void satLightColor(String idn, int color) {
        float[] HSV = new float[3];
        Color.colorToHSV(color, HSV);

        setLightColor(
                idn,
                Math.round(HSV[0] * MAX_HUE),
                Math.round(HSV[0] * MAX_SATURATION)
                );
    }
}
