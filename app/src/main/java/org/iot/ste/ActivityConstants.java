package org.iot.ste;


public class ActivityConstants {

    static final int defaultPort = 1883;
    static final String defaultServer = "MQTT_SERVER";
    static final String defaultUsername = "MQTT_ACCOUNT_USERNAME";
    static final String defaultPassword = "MQTT_ACCOUNT_PASSWORD";
    static final String topicred = "/MQTT_BASE_TOPIC/red";
    static final String topicgreen = "/QTT_BASE_TOPIC/green";

    public static final String INTENT_FILTER = "STEMQTT";
    public static final String STATUS_KEY = "STATUS_KEY";
    public static final String STATUS_ARRIVED = "ARRIVED";
    public static final String STATUS_LOST = "LOST";
    public static final String PAYLOAD_KEY = "VALUE_KEY";


    static final int defaultQos = 0;
    static final int defaultTimeOut = 1000;
    static final int defaultKeepAlive = 10;
    static final boolean defaultSsl = false;
    static final boolean defaultRetained = false;

    static final int connect = 0;
    static final String server = "server";
    static final String port = "port";
    static final String clientId = "clientId";
    static final String topic = "topic";
    static final String message = "message";
    static final String retained = "retained";
    static final String qos = "qos";
    static final String username = "username";
    static final String password = "password";
    static final String keepalive = "keepalive";
    static final String timeout = "timeout";
    static final String ssl = "ssl";
    static final String ssl_key = "ssl_key";
    static final String cleanSession = "cleanSession";
    static final String action = "action";
    static final String ConnectionStatusProperty = "connectionStatus";
    static final String empty = new String();


}
