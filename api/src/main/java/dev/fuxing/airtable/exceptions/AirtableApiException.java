package dev.fuxing.airtable.exceptions;

/**
 * Exceptions that originated from https://api.airtable.com.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 23:51
 */
public class AirtableApiException extends AirtableException {
    private int code;
    private String type;

    /**
     * <pre>
     *  {
     *      "error": {
     *          "type": "UNKNOWN_FIELD_NAME",
     *          "message": "Unknown field name: \" Name\""
     *      }
     *  }
     * </pre>
     *
     * <pre>
     *  {
     *      "error": "NOT_FOUND"
     *  }
     * </pre>
     *
     * @param code    from http status
     * @param type    from node: 'error.type'
     * @param message from node: 'error.message'
     */
    public AirtableApiException(int code, String type, String message) {
        super(message);
        this.type = type;
        this.code = code;
    }

    /**
     * These errors generally indicate a problem on the client side.
     * If you are getting one of these, check your code and the request details.
     * <p>
     * User Error Code:
     * <p>
     * 400: Bad Request<br>
     * The request encoding is invalid; the request can't be parsed as a valid JSON.
     * <p>
     * 401: Unauthorized <br>
     * Accessing a protected resource without authorization or with invalid credentials.
     * <p>
     * 402: Payment Required<br>
     * The account associated with the API key making requests hits a quota that can be increased by upgrading the Airtable account plan.
     * <p>
     * 403: Forbidden<br>
     * Accessing a protected resource with API credentials that don't have access to that resource.
     * <p>
     * 404: Not Found<br>
     * Route or resource is not found.
     * This error is returned when the request hits an undefined route, or if the resource doesn't exist (e.g. has been deleted).
     * <p>
     * 413: Request Entity Too Large<br>
     * The request exceeded the maximum allowed payload size.
     * You shouldn't encounter this under normal use.
     * <p>
     * 422: Invalid Request<br>
     * The request data is invalid.
     * This includes most of the base-specific validations.
     * You will receive a detailed error message and code pointing to the exact issue.
     * <p>
     * Server Error Code:
     * <p>
     * 500: Internal Server Error<br>
     * The server encountered an unexpected condition.
     * <p>
     * 502: Bad Gateway<br>
     * Airtable's servers are restarting or an unexpected outage is in progress.
     * You should generally not receive this error, and requests are safe to retry.
     * <p>
     * 503: Service Unavailable<br>
     * The server could not process your request in time.
     * The server could be temporarily unavailable, or it could have timed out processing your request.
     * You should retry the request with backoffs.
     *
     * @return status code, refer to airtable api document.
     */
    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
