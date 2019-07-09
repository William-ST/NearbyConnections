package com.cursoandroid.mobile;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BalizaAdapter.OnItemSelect {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    // Consejo: utiliza como SERVICE_ID el nombre de tu paquete
    private final static String ACTION_ON_LED = "ON", ACTION_OFF_LED = "OFF", REMOTE_ACTION = "REMOTE_ACTION",
            ACTION_GET_CURRENT_NETWORK_INFO = "ACTION_GET_CURRENT_NETWORK_INFO",
            ACTION_RESPONSE_CURRENT_NETWORK_INFO = "ACTION_RESPONSE_CURRENT_NETWORK_INFO";

    private static final String SERVICE_ID = "com.cursoandroid.things";
    private static final String TAG = "Mobile:";
    private Button btnScan, btnConnect, btnOn, btnOff, btnDisconnect, btnConfigWifi, btnCleanConfigWifi;
    private TextView tvDisplay, tvBalizaSelect;
    private BalizaAdapter balizaAdapter;
    private RecyclerView rvList;
    private BalizaModel selectBalizaModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDisplay = findViewById(R.id.tv_display);
        tvBalizaSelect = findViewById(R.id.tv_baliza_select);
        btnScan = findViewById(R.id.btn_scan);
        btnConnect = findViewById(R.id.btn_connect);
        btnOn = findViewById(R.id.btn_on);
        btnOff = findViewById(R.id.btn_off);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnConfigWifi = findViewById(R.id.btn_config_wifi);
        btnCleanConfigWifi = findViewById(R.id.btn_clean_config_wifi);
        rvList = findViewById(R.id.rv_list);

        btnScan.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
        btnOn.setOnClickListener(this);
        btnOff.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnConfigWifi.setOnClickListener(this);
        btnCleanConfigWifi.setOnClickListener(this);
        restartBtn();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        balizaAdapter = new BalizaAdapter();
        balizaAdapter.setOnItemSelect(this);
        rvList.setAdapter(balizaAdapter);
        rvList.setLayoutManager(new LinearLayoutManager(this));
    }

    // Gestión de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permisos concedidos");
                } else {
                    Log.i(TAG, "Permisos denegados");
                    tvDisplay.append("Debe aceptar los permisos para comenzar\n");
                    restartBtn();
                }
                return;
            }
            default:
                break;
        }
    }

    private void startDiscovery() {
        btnConnect.setEnabled(false);
        btnOn.setEnabled(false);
        btnOff.setEnabled(false);
        btnConfigWifi.setEnabled(false);
        btnCleanConfigWifi.setEnabled(false);
        btnDisconnect.setEnabled(false);
        tvDisplay.setText("Buscando...\n");
        Nearby.getConnectionsClient(this).startDiscovery(SERVICE_ID, mEndpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Estamos en modo descubrimiento!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Modo descubrimiento no iniciado.", e);
            }
        });
    }

    private void stopDiscovery() {
        Nearby.getConnectionsClient(this).stopDiscovery();
        Log.i(TAG, "Se ha detenido el modo descubrimiento.");
    }

    private void restartBtn() {
        btnScan.setEnabled(true);
        btnConnect.setEnabled(false);
        btnOn.setEnabled(false);
        btnOff.setEnabled(false);
        btnConfigWifi.setEnabled(false);
        btnCleanConfigWifi.setEnabled(false);
        btnDisconnect.setEnabled(false);
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {

            Log.i(TAG, "Descubierto dispositivo con Id: " + endpointId);
            tvDisplay.append("Descubierto: " + discoveredEndpointInfo.getEndpointName()+"\n");

            BalizaModel balizaModel = new BalizaModel();
            balizaModel.setId(endpointId);
            balizaModel.setName(discoveredEndpointInfo.getEndpointName());
            balizaAdapter.addBaliza(balizaModel);
        }

        @Override
        public void onEndpointLost(String endpointId) {
        }
    };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) { // Aceptamos la conexión automáticamente en ambos lados.
            Log.i(TAG, "Aceptando conexión entrante sin autenticación");
            Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, mPayloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Log.i(TAG, "Estamos conectados!");
                    tvDisplay.append("Conectado\n");
                    btnConnect.setEnabled(false);
                    btnOn.setEnabled(true);
                    btnOff.setEnabled(true);
                    btnConfigWifi.setEnabled(true);
                    btnCleanConfigWifi.setEnabled(true);
                    btnDisconnect.setEnabled(true);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.i(TAG, "Conexión rechazada por uno o ambos lados");
                    tvDisplay.append("Desconectado\n");
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    Log.i(TAG, "Conexión perdida antes de poder ser " + "aceptada");
                    tvDisplay.append("Desconectado\n");
                    break;
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.i(TAG, "Desconexión del endpoint, no se pueden " + "intercambiar más datos.");
            tvDisplay.append("Desconectado\n");
        }
    };

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        // En este ejemplo, el móvil no recibirá transmisiones de la RP3
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) { // Payload recibido
            String message = new String(payload.asBytes());
            Log.i(TAG, "Se ha recibido una transferencia desde (" + endpointId + ") con el siguiente contenido: " + message);
            switch (message) {
                case ACTION_RESPONSE_CURRENT_NETWORK_INFO:

                    break;
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) { // Actualizaciones sobre el proceso de transferencia
        }
    };

    private void sendData(String endpointId, String mensaje) {
        Payload data = null;
        try {
            data = Payload.fromBytes(mensaje.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error en la codificación del mensaje.", e);
        }
        Nearby.getConnectionsClient(this).sendPayload(endpointId, data);
        Log.i(TAG, "Mensaje enviado.");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                startDiscovery();
                break;
            case R.id.btn_connect:
                connectNearby(selectBalizaModel.getId());
                break;
            case R.id.btn_on:
                onLed();
                break;
            case R.id.btn_off:
                offLed();
                break;
            case R.id.btn_disconnect:
                disconnectNearby();
                break;
            case R.id.btn_config_wifi:
                openConfigWifi();
                break;
            case R.id.btn_clean_config_wifi:
                cleanConfigWifi();
                break;
            default:
                break;
        }
    }

    private void connectNearby(String endpointId) {
        stopDiscovery();
        tvDisplay.append("Conectando...\n");
        // Iniciamos la conexión con al anunciante "Nearby LED"
        Log.i(TAG, "Conectando...");
        Nearby.getConnectionsClient(getApplicationContext()).requestConnection("Nearby LED", endpointId, mConnectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        Log.i(TAG, "Solicitud lanzada, falta que ambos " + "lados acepten");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error en solicitud de conexión", e);
                tvDisplay.append("Desconectado\n");
            }
        });
    }

    private void onLed() {
        tvDisplay.append("Encendiendo led...\n");
        sendData(selectBalizaModel.getId(), ACTION_ON_LED);
    }

    private void offLed() {
        tvDisplay.append("Apgando led...\n");
        sendData(selectBalizaModel.getId(), ACTION_OFF_LED);
    }

    private void remoteAction() {
        sendData(selectBalizaModel.getId(), REMOTE_ACTION);
    }

    private void disconnectNearby() {
        Nearby.getConnectionsClient(this).disconnectFromEndpoint(selectBalizaModel.getId());
        restartBtn();
        tvBalizaSelect.setText("");
        selectBalizaModel = null;
    }

    private void actionRemote() {
        tvDisplay.append("Acción remota WifiConfig...\n");
        sendData(selectBalizaModel.getId(), REMOTE_ACTION);
    }

    @Override
    public void balizaItemSelect(BalizaModel balizaModel) {
        btnConnect.setEnabled(true);
        selectBalizaModel = balizaModel;
        tvBalizaSelect.setText(selectBalizaModel.getName());
    }

    private void openConfigWifi() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(ConfigWifiDialogFragment.newInstance(), ConfigWifiDialogFragment.TAG)
                .commitAllowingStateLoss();
    }

    private void cleanConfigWifi() {

    }

}
