package org.iot.ste;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import android.content.Context;
import org.eclipse.paho.android.service.MqttAndroidClient;

public class Connection {

    private String clientHandle = null;
    private String clientId = null;
    private String host = null;
    private int port = 0;
    private ConnectionStatus status = ConnectionStatus.NONE;
    private MqttAndroidClient client = null;
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private Context context = null;
    private MqttConnectOptions conOpt;
    private boolean sslConnection = false;
    private long persistenceId = -1;

    enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        NONE
    }

    public static Connection createConnection(String clientId, String host,
                                              int port, Context context, boolean sslConnection) {
        String handle = null;
        String uri = null;
        if (sslConnection) {
            uri = "ssl://" + host + ":" + port;
            handle = uri + clientId;
        } else {
            uri = "tcp://" + host + ":" + port;
            handle = uri + clientId;
        }
        MqttAndroidClient client = new MqttAndroidClient(context, uri, clientId);
        return new Connection(handle, clientId, host, port, context, client, sslConnection);

    }

    public Connection(String clientHandle, String clientId, String host,
                      int port, Context context, MqttAndroidClient client, boolean sslConnection) {
        this.clientHandle = clientHandle;
        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.context = context;
        this.client = client;
        this.sslConnection = sslConnection;

    }


    public String handle() {
        return clientHandle;
    }


    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }

    public void changeConnectionStatus(ConnectionStatus connectionStatus) {
        status = connectionStatus;
        notifyListeners((new PropertyChangeEvent(this, ActivityConstants.ConnectionStatusProperty, null, null)));
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Connection)) {
            return false;
        }

        Connection c = (Connection) o;

        return clientHandle.equals(c.clientHandle);

    }

    public String getId() {
        return clientId;
    }

    public String getHostName() {

        return host;
    }

    public MqttAndroidClient getClient() {
        return client;
    }

    public void addConnectionOptions(MqttConnectOptions connectOptions) {
        conOpt = connectOptions;

    }


    public MqttConnectOptions getConnectionOptions() {
        return conOpt;
    }

    public void registerChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }


    private void notifyListeners(PropertyChangeEvent propertyChangeEvent) {
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(propertyChangeEvent);
        }
    }

    public int getPort() {
        return port;
    }

    public int isSSL() {
        return sslConnection ? 1 : 0;
    }

    public void assignPersistenceId(long id) {
        persistenceId = id;
    }

}
