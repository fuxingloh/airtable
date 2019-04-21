package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#array_functions">Array functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface ArrayFunction extends AirtableFunction {

    /**
     * Removes empty strings and null values from the array. Keeps "false" and strings that contain one or more blank characters.
     */
    ArrayFunction ARRAYCOMPACT = new ArrayFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("ARRAYCOMPACT", objects);
        }
    };

    /**
     * Flattens the array by removing any array nesting. All items become elements of a single array.
     */
    ArrayFunction ARRAYFLATTEN = new ArrayFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("ARRAYFLATTEN", objects);
        }
    };


    /**
     * Join the array of items into a string with a separator.
     */
    ArrayFunction ARRAYJOIN = new ArrayFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("ARRAYJOIN", objects);
        }
    };

    /**
     * Returns only unique items in the array.
     */
    ArrayFunction ARRAYUNIQUE = new ArrayFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("ARRAYUNIQUE", objects);
        }
    };
}
