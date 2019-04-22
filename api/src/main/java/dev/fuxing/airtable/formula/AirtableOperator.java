package dev.fuxing.airtable.formula;

import java.util.StringJoiner;

/**
 * Airtable operator interface.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:04
 */
@FunctionalInterface
public interface AirtableOperator {

    /**
     * Operator function to implement
     *
     * @param left   value
     * @param right  value
     * @param others other values on the right
     * @return String formula
     */
    String apply(AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others);

    /**
     * @param operator symbol
     * @param left     value
     * @param right    value
     * @param others   other values on the right
     * @return left + ' operator ' + [right + ' operator ', ...others]
     */
    default String operator(String operator, AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
        StringJoiner joiner = new StringJoiner(operator);
        joiner.add(left.toString());
        joiner.add(right.toString());
        for (Object other : others) {
            joiner.add(other.toString());
        }
        return joiner.toString();
    }
}
