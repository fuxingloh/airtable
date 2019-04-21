package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#date_and_time_functions">Date and time functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface DateTimeFunction extends AirtableFunction {

    /**
     * Returns the second of a datetime as an integer between 0 and 59.
     */
    DateTimeFunction SECOND = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("SECOND", objects);
        }
    };

    /**
     * Returns the minute of a datetime as an integer between 0 and 59.
     */
    DateTimeFunction MINUTE = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("MINUTE", objects);
        }
    };

    /**
     * Returns the day of the month of a datetime in the form of a number between 1-31.
     */
    DateTimeFunction DAY = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("DAY", objects);
        }
    };

    /**
     * Returns the hour of a datetime as a number between 0 (12:00am) and 23 (11:00pm).
     */
    DateTimeFunction HOUR = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("HOUR", objects);
        }
    };

    /**
     * Returns the month of a datetime as a number between 1 (January) and 12 (December).
     */
    DateTimeFunction MONTH = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("MONTH", objects);
        }
    };

    /**
     * Returns the four-digit year of a datetime.
     */
    DateTimeFunction YEAR = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("YEAR", objects);
        }
    };

    /**
     * Returns the current date and time.
     * (Note that the results of these functions change only when the formula is recalculated or a base is loaded. They are not updated continuously.)
     */
    DateTimeFunction NOW = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("NOW", objects);
        }
    };

    /**
     * Returns the current date and time.
     * (Note that the results of these functions change only when the formula is recalculated or a base is loaded. They are not updated continuously.)
     */
    DateTimeFunction TODAY = NOW;

    /**
     * Calculates the number of days between the current date and another date.
     */
    DateTimeFunction TONOW = new DateTimeFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("TONOW", objects);
        }
    };

    /**
     * Calculates the number of days between the current date and another date.
     */
    DateTimeFunction FROMNOW = TONOW;
}
