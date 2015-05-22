package im.youtiao.java_sdk;

import im.youtiao.java_sdk.core.ErrorResponse;
import im.youtiao.java_sdk.http.HttpRequestor;
import im.youtiao.java_sdk.util.IOUtil;
import im.youtiao.java_sdk.util.StringUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class YTRequestUtil {

    public static String encodeUrlParam(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            AssertionError ae = new AssertionError("UTF-8 not supported?  Should never happen, right?");
            ae.initCause(ex);
            throw ae;
        }
    }

    private static String encodeUrlParams(String token, String userLocale, Map<String, String> params) {
        StringBuilder buf = new StringBuilder();
        String sep = "";
//		if (token != null) {
//			buf.append(sep);
//			sep = "&";
//			buf.append("token=").append(token);
//		}
        if (userLocale != null) {
            buf.append(sep);
            sep = "&";
            buf.append("locale=").append(userLocale);
        }

        if (params != null) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = params.get(key);
                if (value != null) {
                    buf.append(sep);
                    sep = "&";
                    buf.append(encodeUrlParam(key));
                    buf.append("=");
                    buf.append(encodeUrlParam(value));
                }
            }
        }
        return buf.toString();
    }

    public static String buildUri(String host, String path) {
        return "http://" + host + "/" + path;
    }

    public static String buildUrlWithParams(String host, String path, String token, String userLocale,
                                            Map<String, String> params) {
        String url = buildUri(host, path) + "?" + encodeUrlParams(token, userLocale, params);
        return url;
    }

    public static List<HttpRequestor.Header> addUserAgentHeader(List<HttpRequestor.Header> headers,
                                                                YTRequestConfig requestConfig, String token) {
        if (headers == null) {
            headers = new ArrayList<HttpRequestor.Header>();
        }
        headers.add(buildUserAgentHeader(requestConfig));
        if (token != null) {
            headers.add(buildAuthorizationHeader(token));
        }
        return headers;
    }

    public static HttpRequestor.Header buildUserAgentHeader(YTRequestConfig requestConfig) {
        return new HttpRequestor.Header("User-Agent", requestConfig.clientIdentifier + " YT-Java-SDK/"
                + YTSdkVersion.Version);
    }

    public static HttpRequestor.Header buildAuthorizationHeader(String token) {
        return new HttpRequestor.Header("Authorization", token);
    }

    public static HttpRequestor.Response startGet(YTRequestConfig requestConfig, String host, String path,
                                                  String token, Map<String, String> params, List<HttpRequestor.Header> headers) throws YTException.NetworkIO {
        headers = addUserAgentHeader(headers, requestConfig, token);

        String url = buildUrlWithParams(host, path, token, requestConfig.userLocale, params);
        try {
            return requestConfig.httpRequestor.doGet(url, headers);
        } catch (IOException ex) {
            throw new YTException.NetworkIO(ex);
        }
    }

    public static HttpRequestor.Response startDelete(YTRequestConfig requestConfig, String host, String path,
                                                     String token, Map<String, String> params, List<HttpRequestor.Header> headers) throws YTException.NetworkIO {
        headers = addUserAgentHeader(headers, requestConfig, token);

        String url = buildUrlWithParams(host, path, token, requestConfig.userLocale, params);
        try {
            return requestConfig.httpRequestor.doDelete(url, headers);
        } catch (IOException ex) {
            throw new YTException.NetworkIO(ex);
        }
    }

    public static HttpRequestor.Response startPost(YTRequestConfig requestConfig, String host, String path,
                                                   String token, Map<String, String> params, String body, List<HttpRequestor.Header> headers)
            throws YTException {
        String url = buildUrlWithParams(host, path, token, requestConfig.userLocale, params);
        headers = addUserAgentHeader(headers, requestConfig, token);
        headers.add(new HttpRequestor.Header("Content-Type", "application/json"));

        try {
            HttpRequestor.Uploader uploader = requestConfig.httpRequestor.startPost(url, headers);
            try {
                if (body != null) {
                    uploader.getBody().write(body.getBytes());
                }
                return uploader.finish();
            } finally {
                uploader.close();
            }
        } catch (IOException ex) {
            throw new YTException.NetworkIO(ex);
        }
    }

    public static byte[] loadErrorBody(HttpRequestor.Response response) throws YTException.NetworkIO {
        // Slurp the body into memory (up to 4k; anything past that is probably
        // not useful).
        try {
            return IOUtil.slurp(response.body, 4096);
        } catch (IOException ex) {
            throw new YTException.NetworkIO(ex);
        }
    }

    public static String parseErrorBody(int statusCode, byte[] body) throws YTException.BadResponse {
        // Read the error message from the body.
        try {
            return StringUtil.utf8ToString(body);
        } catch (CharacterCodingException e) {
            throw new YTException.BadResponse("Got non-UTF8 response body: " + statusCode + ": " + e.getMessage());
        }
    }

    public static YTException unexpectedStatus(HttpRequestor.Response response) throws YTException.NetworkIO,
            YTException.BadResponse {
        System.out.println(response.statusCode);
        byte[] body = loadErrorBody(response);
        String message = parseErrorBody(response.statusCode, body);
        BufferedReader br = new BufferedReader(new InputStreamReader(response.body));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new YTException.BadResponse(message, e);
        }
        ErrorResponse errorResponse = readJsonFromResponse(ErrorResponse.class, new ByteArrayInputStream(body));
        if (errorResponse != null) {
            return new YTException.YTServerResponseException(errorResponse);
        }

        if (response.statusCode == 400) {
            return new YTException.BadRequest(message);
        }
        if (response.statusCode == 401) {
            return new YTException.InvalidAccessToken(message);
        }
        if (response.statusCode == 403) {
            return new YTException.AccessDenied(message);
        }
        if (response.statusCode == 404) {
            return new YTException.NotFound(message);
        }
        if (response.statusCode == 409) {
            return new YTException.AlreadyExists(message);
        }
        if (response.statusCode == 422) {
            return new YTException.UnacceptableRequest(message);
        }
        if (response.statusCode == 424) {
            return new YTException.OperationNotAllowed(message);
        }
        if (response.statusCode == 500) {
            return new YTException.ServerError(message);
        }
        if (response.statusCode == 503) {
            return new YTException.RetryLater(message);
        }
        if (response.statusCode == 507) {
            return new YTException.QuotaOutage(message);
        }

        return new YTException.BadResponseCode("unexpected HTTP status code: " + response.statusCode + ": " + message,
                response.statusCode);
    }

    public static <T> T readJsonFromResponse(Class<T> objectClass, InputStream body) throws YTException.BadResponse,
            YTException.NetworkIO {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, objectClass);
        } catch (JsonGenerationException e) {
            throw new YTException.BadResponse("error in response JSON: " + e.getMessage(), e);
        } catch (JsonMappingException e) {
            throw new YTException.BadResponse("error in response JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new YTException.NetworkIO(e);
        }
    }

    public static <T> T readJsonFromResponse(TypeReference<T> typeRef, InputStream body)
            throws YTException.BadResponse, YTException.NetworkIO {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, typeRef);
        } catch (JsonGenerationException e) {
            throw new YTException.BadResponse("error in response JSON: " + e.getMessage(), e);
        } catch (JsonMappingException e) {
            throw new YTException.BadResponse("error in response JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new YTException.NetworkIO(e);
        }
    }

    public static abstract class ResponseHandler<T> {
        public abstract T handle(HttpRequestor.Response response) throws YTException;
    }

    public static Map<String, String> parseAsQueryString(InputStream in) throws YTException {
        // TODO: Maybe just slurp string up to a max limit.
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(in).useDelimiter("&");
        Map<String, String> result = new HashMap<String, String>();
        try {
            while (scanner.hasNext()) {
                String pair = scanner.next();

                // The 'Scanner' class masks any IOExceptions that happen on
                // '.next()', so we have to check for them explicitly.
                IOException ioe = scanner.ioException();
                if (ioe != null) {
                    throw new YTException.NetworkIO(ioe);
                }

                String[] parts = pair.split("=");
                if (parts.length < 2) {
                    throw new YTException.BadResponse("expecting a name-value pair, but there's no '=': \"" + pair
                            + "\"");
                } else if (parts.length > 2) {
                    throw new YTException.BadResponse(
                            "expecting a single name-value pair, but there's more than one '=': \"" + pair + "\"");
                }
                String displaced = result.put(parts[0], parts[1]);
                if (displaced != null) {
                    throw new YTException.BadResponse("duplicate query parameter name: \"" + parts[0] + "\"");
                }
            }
        } finally {
            scanner.close();
        }

        return result;
    }

    public static <T> T doGet(YTRequestConfig requestConfig, String host, String path, String token,
                              Map<String, String> params, List<HttpRequestor.Header> headers, ResponseHandler<T> handler)
            throws YTException {
        HttpRequestor.Response response = startGet(requestConfig, host, path, token, params, headers);
        try {
            return handler.handle(response);
        } finally {
            try {
                response.body.close();
            } catch (IOException ex) {
                throw new YTException.NetworkIO(ex);
            }
        }
    }

    public static <T> T doDelete(YTRequestConfig requestConfig, String host, String path, String token,
                                 Map<String, String> params, List<HttpRequestor.Header> headers, ResponseHandler<T> handler)
            throws YTException {
        HttpRequestor.Response response = startDelete(requestConfig, host, path, token, params, headers);
        try {
            return handler.handle(response);
        } finally {
            try {
                response.body.close();
            } catch (IOException ex) {
                throw new YTException.NetworkIO(ex);
            }
        }
    }

    public static <T> T doPost(YTRequestConfig requestConfig, String host, String path, String token,
                               Map<String, String> params, String body, List<HttpRequestor.Header> headers, ResponseHandler<T> handler)
            throws YTException {
        HttpRequestor.Response response = startPost(requestConfig, host, path, token, params, body, headers);
        return finishResponse(response, handler);
    }

    public static HttpRequestor.Uploader getUploaderWithPost(YTRequestConfig requestConfig, String host, String path,
                                                             String token, Map<String, String> params, List<HttpRequestor.Header> headers) throws YTException {
        String url = buildUrlWithParams(host, path, token, requestConfig.userLocale, params);
        headers = addUserAgentHeader(headers, requestConfig, token);

        try {
            HttpRequestor.Uploader uploader = requestConfig.httpRequestor.startPost(url, headers);
            return uploader;
        } catch (IOException e) {
            throw new YTException.NetworkIO(e);
        }
    }

    public static <T> T finishResponse(HttpRequestor.Response response, ResponseHandler<T> handler) throws YTException {
        try {
            if (handler != null) {
                return handler.handle(response);
            } else {
                return null;
            }
        } finally {
            IOUtil.closeInput(response.body);
        }
    }

    public static String getFirstHeader(HttpRequestor.Response response, String name) throws YTException {
        List<String> values = response.headers.get(name);
        if (values == null) {
            throw new YTException.BadResponse("missing HTTP header \"" + name + "\"");
        }
        assert !values.isEmpty();
        return values.get(0);
    }

    public static String getFirstHeaderMaybe(HttpRequestor.Response response, String name) throws YTException {
        List<String> values = response.headers.get(name);
        if (values == null) {
            return null;
        }
        assert !values.isEmpty();
        return values.get(0);
    }

    public static abstract class RequestMaker<T, E extends Throwable> {
        public abstract T run() throws YTException, E;
    }

    public static <T, E extends Throwable> T runAndRetry(int maxTries, RequestMaker<T, E> requestMaker)
            throws YTException, E {
        int numTries = 0;
        while (true) {
            try {
                numTries++;
                return requestMaker.run();
            } catch (YTException ex) {
                // If we can't retry, just let this exception through.
                if (!isRetriableException(ex) || numTries >= maxTries) {
                    throw ex;
                }
                // Otherwise, run through the loop again.
            }
        }
    }

    private static boolean isRetriableException(YTException ex) {
        return ex instanceof YTException.RetryLater || ex instanceof YTException.ServerError;
    }
}
