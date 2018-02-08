package org.iot.ste;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    protected void onResume() {
        super.onResume();

        Map<String, Connection> connections = Connections.getInstance(this).getConnections();
        Connection ctouse = null;

        if (connections.values().isEmpty()) {
            ctouse = createAndConnectInActivity();
        } else {
            for (Connection connection : connections.values()) {
                connection.getClient().registerResources(this);
                connection.getClient().setCallback(new MqttCallbackHandler(this, connection.getClient().getServerURI() + connection.getClient().getClientId()));
                connection.getClient().setTraceEnabled(false);

                ctouse = connection;
            }
        }

        Intent intent = new Intent();
        intent.setClassName(getApplicationContext().getPackageName(),
                "org.iot.ste.MainActivity");
        intent.putExtra("handle", ctouse.handle());
        startActivity(intent);


    }


    private Connection connectAction(Bundle data) {
        MqttConnectOptions conOpt = new MqttConnectOptions();
    /*
     * SSLContext context = SSLContext.getDefault();
     * context.init({new CustomX509KeyManager()},null,null); //where CustomX509KeyManager proxies calls to keychain api
     * SSLSocketFactory factory = context.getSSLSocketFactory();
     *
     * MqttConnectOptions options = new MqttConnectOptions();
     * options.setSocketFactory(factory);
     *
     * client.connect(options);
     *
     */


        String server = (String) data.get(ActivityConstants.server);
        String clientId = (String) data.get(ActivityConstants.clientId);
        int port = (int) data.get(ActivityConstants.port);
        boolean cleanSession = (Boolean) data.get(ActivityConstants.cleanSession);

        boolean ssl = (Boolean) data.get(ActivityConstants.ssl);
        String ssl_key = (String) data.get(ActivityConstants.ssl_key);
        String uri = null;
        if (ssl) {
            Log.e("SSLConnection", "Doing an SSL Connect");
            uri = "ssl://";

        } else {
            uri = "tcp://";
        }

        uri = uri + server + ":" + port;

        MqttAndroidClient client;
        client = Connections.getInstance(this).createClient(this, uri, clientId);

        if (ssl) {
            try {
                if (ssl_key != null && !ssl_key.equalsIgnoreCase("")) {
                    FileInputStream key = new FileInputStream(ssl_key);
                    conOpt.setSocketFactory(client.getSSLSocketFactory(key,
                            "mqtttest"));
                }

            } catch (MqttSecurityException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException Occured: ", e);
            } catch (FileNotFoundException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException Occured: SSL Key file not found", e);
            }
        }


        String clientHandle = uri + clientId;


        String message = (String) data.get(ActivityConstants.message);
        String topic = (String) data.get(ActivityConstants.topic);
        Integer qos = (Integer) data.get(ActivityConstants.qos);
        Boolean retained = (Boolean) data.get(ActivityConstants.retained);


        String username = (String) data.get(ActivityConstants.username);
        String password = (String) data.get(ActivityConstants.password);

        int timeout = (Integer) data.get(ActivityConstants.timeout);
        int keepalive = (Integer) data.get(ActivityConstants.keepalive);

        Connection connection = new Connection(clientHandle, clientId, server, port, this, client, ssl);


        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(timeout);
        conOpt.setKeepAliveInterval(keepalive);
        if (!username.equals(ActivityConstants.empty)) {
            conOpt.setUserName(username);
        }
        if (!password.equals(ActivityConstants.empty)) {
            conOpt.setPassword(password.toCharArray());
        }

        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);

        boolean doConnect = true;

        if ((!message.equals(ActivityConstants.empty))
                || (!topic.equals(ActivityConstants.empty))) {

            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(),
                        retained.booleanValue());
            } catch (Exception e) {
                Log.e(this.getClass().getCanonicalName(), "Exception Occured", e);
                doConnect = false;
                callback.onFailure(null, e);
            }
        }
        client.setCallback(new MqttCallbackHandler(this, clientHandle));

        client.setTraceEnabled(false);

        client.setTraceCallback(new MqttTraceCallback());

        connection.addConnectionOptions(conOpt);
        Connections.getInstance(this).addConnection(connection);
        if (doConnect) {
            try {
                client.connect(conOpt, null, callback);
            } catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException Occured", e);
            }
        }

        return connection;

    }

    public Connection createAndConnectInActivity() {

        Bundle dataBundle = new Bundle();

        dataBundle.putString(ActivityConstants.server, ActivityConstants.defaultServer);
        dataBundle.putInt(ActivityConstants.port, ActivityConstants.defaultPort);
        dataBundle.putString(ActivityConstants.clientId, UUID.randomUUID().toString());
        dataBundle.putInt(ActivityConstants.action, ActivityConstants.connect);
        dataBundle.putBoolean(ActivityConstants.cleanSession, true);
        dataBundle.putString(ActivityConstants.message, ActivityConstants.empty);
        dataBundle.putString(ActivityConstants.topic, ActivityConstants.empty);
        dataBundle.putInt(ActivityConstants.qos, ActivityConstants.defaultQos);
        dataBundle.putBoolean(ActivityConstants.retained, ActivityConstants.defaultRetained);
        dataBundle.putString(ActivityConstants.username, ActivityConstants.defaultUsername);
        dataBundle.putString(ActivityConstants.password, ActivityConstants.defaultPassword);
        dataBundle.putInt(ActivityConstants.timeout, ActivityConstants.defaultTimeOut);
        dataBundle.putInt(ActivityConstants.keepalive, ActivityConstants.defaultKeepAlive);
        dataBundle.putBoolean(ActivityConstants.ssl, ActivityConstants.defaultSsl);

        return connectAction(dataBundle);

    }


}
