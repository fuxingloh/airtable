package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#numeric">Numeric operators and functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface NumericFunction extends AirtableFunction {

    /**
     * Returns the absolute value.
     */
    NumericFunction ABS = new NumericFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("ABS", objects);
        }
    };

    /**
     * Returns the average of the numbers.
     */
    NumericFunction AVERAGE = new NumericFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("AVERAGE", objects);
        }
    };
}
