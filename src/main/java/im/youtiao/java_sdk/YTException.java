package im.youtiao.java_sdk;

import im.youtiao.java_sdk.core.ErrorResponse;

import java.io.IOException;

public class YTException extends Exception {

    private static final long serialVersionUID = 1L;

    public YTException(String message) {
        super(message);
    }

    public YTException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class YTServerResponseException extends YTException {
        private static final long serialVersionUID = 1L;

        private ErrorResponse response = null;

        public YTServerResponseException(ErrorResponse response) {
            super("Server Error Response");
            this.response = response;
        }

        public ErrorResponse getErrorResponse() {
            return this.response;
        }
    }

    // 403
    public static class AccessDenied extends YTException {

        private static final long serialVersionUID = 1L;

        public AccessDenied(String message) {
            super(message);
        }
    }

    // 404
    public static class NotFound extends YTException {
        private static final long serialVersionUID = 1L;

        public NotFound(String message) {
            super(message);
        }
    }

    // 409
    public static class AlreadyExists extends YTException {
        private static final long serialVersionUID = 1L;

        public AlreadyExists(String message) {
            super(message);
        }
    }

    // 422
    public static class UnacceptableRequest extends YTException {
        private static final long serialVersionUID = 1L;

        public UnacceptableRequest(String message) {
            super(message);
        }
    }

    // 424
    public static class OperationNotAllowed extends YTException {
        private static final long serialVersionUID = 1L;

        public OperationNotAllowed(String message) {
            super(message);
        }
    }

    // 500
    public static class ServerError extends YTException {
        public static final long serialVersionUID = 1L;

        public ServerError(String message) {
            super(message);
        }
    }

    // 503
    public static class RetryLater extends YTException {
        public static final long serialVersionUID = 1L;

        public RetryLater(String message) {
            super(message);
        }
    }

    // 507
    public static class QuotaOutage extends YTException {
        public static final long serialVersionUID = 1L;

        public QuotaOutage(String message) {
            super(message);
        }
    }

    public static abstract class ProtocolError extends YTException {
        public static final long serialVersionUID = 1L;

        public ProtocolError(String message) {
            super(message);
        }

        public ProtocolError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // 400
    public static class BadRequest extends ProtocolError {
        public static final long serialVersionUID = 1L;

        public BadRequest(String message) {
            super(message);
        }
    }

    public static class BadResponse extends ProtocolError {
        public static final long serialVersionUID = 1L;

        public BadResponse(String message) {
            super(message);
        }

        public BadResponse(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BadResponseCode extends BadResponse {
        public static final long serialVersionUID = 1L;

        private int statusCode;

        public BadResponseCode(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public BadResponseCode(String message, int statusCode, Throwable cause) {
            super(message, cause);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return this.statusCode;
        }
    }

    public static class NetworkIO extends YTException {
        public static final long serialVersionUID = 1L;

        private IOException underlying;

        public NetworkIO(IOException underlying) {
            super(underlying.toString(), underlying);
            this.underlying = underlying;
        }

        @Override
        public IOException getCause() {
            return this.underlying;
        }
    }

    // 401
    public static class InvalidAccessToken extends YTException {
        public static final long serialVersionUID = 1L;

        public InvalidAccessToken(String message) {
            super(message);
        }
    }
}
