package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#numeric">Numeric operators and functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:04
 */
public interface NumericOperator extends AirtableOperator {

    /**
     * Add together two numeric values
     */
    NumericOperator ADD = new NumericOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("+", left, right, others);
        }
    };

    /**
     * Subtract two numeric values
     */
    NumericOperator SUBTRACT = new NumericOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("-", left, right, others);
        }
    };

    /**
     * Multiply two numeric values
     */
    NumericOperator MULTIPLY = new NumericOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("*", left, right, others);
        }
    };

    /**
     * Divide two numeric values
     */
    NumericOperator DIVIDE = new NumericOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("/", left, right, others);
        }
    };
}
