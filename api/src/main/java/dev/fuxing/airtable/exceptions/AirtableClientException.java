package dev.fuxing.airtable.exceptions;

import java.util.List;

/**
 * Exceptions that are caused by the client.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 23:44
 */
public class AirtableClientException extends AirtableException {

    /**
     * Exceptions that are caused by the client.
     *
     * @param cause internal java client exception
     */
    public AirtableClientException(Throwable cause) {
        super(cause);
    }

    public AirtableClientException(String message) {
        super(message);
    }

    public static void assert10Records(List<?> list) {
        if (list.size() > 10) {
            throw new AirtableClientException("Your request body should include an array of up to 10 record objects.");
        }
    }
}
