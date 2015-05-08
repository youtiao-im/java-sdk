package im.youtiao.java_sdk;

import im.youtiao.java_sdk.http.HttpRequestor;
import im.youtiao.java_sdk.http.StandardHttpRequestor;

public class YTRequestConfig {

    public static String defaultLocale = "en_US";

    public String clientIdentifier = null;
    public String userLocale = null;
    public HttpRequestor httpRequestor = null;

    public YTRequestConfig(String clientIdentifier, String userLocale, HttpRequestor httpRequestor) {
        if (clientIdentifier == null) {
            throw new IllegalArgumentException("'clientIdentifier' should not be null");
        }
        if (httpRequestor == null) {
            throw new IllegalArgumentException("'httpRequestor' should not be null");
        }

        this.clientIdentifier = clientIdentifier;
        this.userLocale = userLocale;
        this.httpRequestor = httpRequestor;
    }

    public YTRequestConfig(String clientIdentifier, String userLocale) {
        this(clientIdentifier, userLocale, StandardHttpRequestor.getInstance());
    }

    public YTRequestConfig(String clientIdentifier) {
        this(clientIdentifier, defaultLocale);
    }
}
