package com.cursoandroid.things;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.imgprocesadondk.interaction.NetworkInfoResponse;

import java.util.List;

public class WifiUtils {

    WifiManager wifiManager;
    WifiConfiguration wifiConfig;
    private static final String TAG = "WifiUtils";
    Context context;

    public WifiUtils(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context
                .WIFI_SERVICE);
        wifiConfig = new WifiConfiguration();

    }

    public void listNetworks() {
        List<WifiConfiguration> redes = wifiManager.getConfiguredNetworks();
        Log.i(TAG, "Lista de redes configuradas:\n " + redes.toString());
    }

    public NetworkInfoResponse getConnectionInfo() {
        NetworkInfoResponse response = new NetworkInfoResponse();
        response.setSsid(wifiManager.getConnectionInfo().getSSID());
        response.setLinkSpeed(wifiManager.getConnectionInfo().getLinkSpeed()+" Mbps");
        response.setRssi(wifiManager.getConnectionInfo().getRssi()+"");
        return response;
    }

    public void removeAllAPs() {
        // Solo se pueden eliminar las redes que haya creado esta app!!
        // Si el resultado es false, no se ha eliminado
        List<WifiConfiguration> redes = wifiManager.getConfiguredNetworks();
        wifiManager.disconnect();
        for (WifiConfiguration red : redes) {
            Log.i(TAG, "Intento de eliminar red " + red.SSID + " con " + "resultado " + wifiManager.removeNetwork(red.networkId));
        }
        wifiManager.reconnect();
    }

    public int connectToAP(String networkSSID, String networkPasskey) {
        Log.i(TAG, "connectToAP... "+networkSSID+" with: "+networkPasskey);
        WifiManager wifiManager = (WifiManager) context.getSystemService
                (Context.WIFI_SERVICE);
        for (ScanResult result : wifiManager.getScanResults()) {
            if (result.SSID.equals(networkSSID)) {
                String securityMode = getScanResultSecurity(result);
                WifiConfiguration wifiConfiguration = createAPConfiguration
                        (networkSSID, networkPasskey, securityMode);
                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.i(TAG, "Intento de añadir red: " + res);
                boolean b = wifiManager.enableNetwork(res, true);
                Log.i(TAG, "Intento de activar red: " + b);
                wifiManager.setWifiEnabled(true);
                boolean changeHappen = wifiManager.saveConfiguration();
                if (res != -1 && changeHappen) {
                    Log.i(TAG, "Cambio de red correcto: " + networkSSID);
                } else {
                    Log.i(TAG, "Cambio de red erróneo.");
                }
                return res;
            } else {
                Log.i(TAG, "SSID no encontrado");
            }
        }
        return -1;
    }

    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return "OPEN";
    }

    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        if (securityMode.equalsIgnoreCase("OPEN")) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration
                    .KeyMgmt.NONE);
        } else if (securityMode.equalsIgnoreCase("WEP")) {
            wifiConfiguration.wepKeys[0] = "\"" + networkPasskey + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration
                    .KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration
                    .GroupCipher.WEP40);
        } else if (securityMode.equalsIgnoreCase("PSK")) {
            wifiConfiguration.preSharedKey = "\"" + networkPasskey + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration
                    .GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration
                    .GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration
                    .KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration
                    .PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration
                    .PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol
                    .RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol
                    .WPA);
        } else {
            Log.i(TAG, "Modo de seguridad no soportado: " + securityMode);
            return null;
        }
        return wifiConfiguration;
    }

}
