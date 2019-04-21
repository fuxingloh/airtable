package dev.fuxing.airtable.formula;

import java.util.StringJoiner;

/**
 * Airtable function interface.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:04
 */
public interface AirtableFunction {

    /**
     * @param objects in the function
     * @return String formula
     */
    String apply(AirtableFormula.Object... objects);

    /**
     * @param name    of the function
     * @param objects values to join inside
     * @return left + ' operator ' + [right + ' operator ', ...objects]
     */
    default String function(String name, AirtableFormula.Object... objects) {
        StringJoiner joiner = new StringJoiner(",", name + "(", ")");
        for (Object other : objects) {
            joiner.add(other.toString());
        }
        return joiner.toString();
    }
}
