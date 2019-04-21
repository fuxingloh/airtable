package dev.fuxing.airtable.formula;

import java.util.function.Supplier;

/**
 * A fluent interface used to filter records.
 * If the result is not {@code 0}, {@code false}, {@code ""}, {@code NaN}, {@code []}, or @{code #Error!} the record will be included in the response.
 * <p>
 * Note that there are too many formula that Airtable supports. Only the most common formula are implemented.
 *
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference">Airtable: Filtering Records</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:04
 */
public interface AirtableFormula {

    /**
     * Formulas may include parentheses () to change the order of operations:
     */
    class Parentheses implements Object {
        private final Supplier<String> supplier;

        private Parentheses(Supplier<String> supplier) {
            this.supplier = supplier;
        }

        @Override
        public String toString() {
            return supplier.get();
        }
    }

    /**
     * Field name, surrounded by '{', '}'
     */
    class Field implements Object {
        private final String name;

        private Field(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "{" + name + "}";
        }
    }

    /**
     * Numeric value
     */
    class Numeric implements Object {
        private final Number value;

        private Numeric(Number value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Text value, surrounded by: '
     */
    class Text implements Object {
        private final String value;

        private Text(String value) {
            this.value = value;
        }


        @Override
        public String toString() {
            return "'" + value + "'";
        }
    }

    /**
     * Airtable Formula Object
     */
    interface Object {

        @Override
        String toString();

        /**
         * @param name airtable field name
         * @return field name wrapped Object
         */
        static Object field(String name) {
            return new Field(name);
        }

        /**
         * @param operator to add parentheses
         * @param left     object value
         * @param right    object value
         * @param others   object values on the right
         * @return parentheses wrapped Object
         */
        static Object parentheses(AirtableOperator operator, Object left, Object right, Object... others) {
            return new Parentheses(() -> operator.apply(left, right, others));
        }

        /**
         * @param function to add parentheses
         * @param objects  in the function
         * @return parentheses wrapped Object
         */
        static Object parentheses(AirtableFunction function, Object... objects) {
            return new Parentheses(() -> function.apply(objects));
        }

        /**
         * @param value text value
         * @return text value wrapped Object
         */
        static Object value(String value) {
            return new Text(value);
        }

        /**
         * @param value float value
         * @return float value wrapped Object
         */
        static Object value(float value) {
            return new Numeric(value);
        }

        /**
         * @param value double value
         * @return double value wrapped Object
         */
        static Object value(double value) {
            return new Numeric(value);
        }

        /**
         * @param value int value
         * @return int value wrapped Object
         */
        static Object value(int value) {
            return new Numeric(value);
        }

        /**
         * @param value long value
         * @return long value wrapped Object
         */
        static Object value(long value) {
            return new Numeric(value);
        }
    }
}
