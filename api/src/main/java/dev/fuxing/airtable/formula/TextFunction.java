package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#text">Text operators and functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface TextFunction extends AirtableFunction {

    /**
     * Returns the length of a string.
     */
    TextFunction LEN = new TextFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("LEN", objects);
        }
    };

    /**
     * Makes a string lowercase.
     */
    TextFunction LOWER = new TextFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("LOWER", objects);
        }
    };

    /**
     * Makes string uppercase.
     */
    TextFunction UPPER = new TextFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("UPPER", objects);
        }
    };

    /**
     * Removes whitespace at the beginning and end of string.
     */
    TextFunction TRIM = new TextFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("TRIM", objects);
        }
    };

    /**
     * Joins together the text arguments into a single text value.
     * <p>
     * To concatenate static text, surround it with double quotation marks. To concatenate double quotation marks, you need to use a backslash (\) as an escape character.
     * <p>
     * Equivalent to use of the & operator.
     */
    TextFunction CONCATENATE = new TextFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("CONCATENATE", objects);
        }
    };
}
