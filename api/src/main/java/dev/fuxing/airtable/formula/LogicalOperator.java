package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#logical">Logical operators and functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:04
 */
public interface LogicalOperator extends AirtableOperator {

    /**
     * Greater than
     */
    LogicalOperator GT = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator(">", left, right);
        }
    };

    /**
     * Less than
     */
    LogicalOperator LT = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("<", left, right);
        }
    };

    /**
     * Greater than or equal to
     */
    LogicalOperator GTE = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator(">=", left, right);
        }
    };

    /**
     * Less than or equal to
     */
    LogicalOperator LTE = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("<=", left, right);
        }
    };

    /**
     * Equal to
     */
    LogicalOperator EQ = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("=", left, right);
        }
    };

    /**
     * Not equal to
     */
    LogicalOperator NEQ = new LogicalOperator() {
        @Override
        public String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return operator("!=", left, right);
        }
    };
}
