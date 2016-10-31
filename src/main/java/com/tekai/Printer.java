package com.tekai;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author SFPISA
 */
public abstract class Printer {

    protected String printChildren(List<Expression> e) {
        return printChildren(e, ",");
    }

    protected String printChildren(List<Expression> e, String separator) {
        StringBuilder result = new StringBuilder();

        String lastValue = null;
        Iterator<Expression> iterator = e.iterator();
        if (iterator.hasNext()) {
            lastValue = print(iterator.next());
            result.append(lastValue);
        }

        while (iterator.hasNext()) {
            if (separator.matches("\\w+"))
                result.append(" " + separator);
            else
                result.append(separator);

            lastValue = print(iterator.next());
            result.append(lastValue);
        }

        return result.toString();
    }

    public abstract String print(Expression e);
}
