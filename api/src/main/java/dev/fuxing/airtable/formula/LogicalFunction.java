package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#logical">Logical operators and functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface LogicalFunction extends AirtableFunction {

    /**
     * Returns true if all the arguments are true, returns false otherwise.
     */
    LogicalFunction AND = new LogicalFunction() {
        @Override
        public String apply(AirtableFormula.Object... objects) {
            return function("AND", objects);
        }
    };
}
