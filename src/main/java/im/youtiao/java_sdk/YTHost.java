package im.youtiao.java_sdk;


public class YTHost {

    public static final YTHost DEFAULT = new YTHost("192.168.200.195:5000");
    public final String api;

    public YTHost(String api) {
        this.api = api;
    }
}
