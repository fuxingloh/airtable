package dev.fuxing.airtable.exceptions;

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
}
