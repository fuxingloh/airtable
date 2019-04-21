package dev.fuxing.airtable.exceptions;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 17:43
 */
public abstract class AirtableException extends RuntimeException {

    public AirtableException(String message) {
        super(message);
    }

    public AirtableException(Throwable cause) {
        super(cause);
    }
}
