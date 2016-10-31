package com.tekai;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Expression {

    private final String type;
    private final String value;
    private final List<Expression> children = new LinkedList<Expression>();

    // == Construction

    public Expression(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public void addChildren(Expression... expressions) {
        for(Expression expression : expressions)
            children.add(expression);
    }

    // == Accessors ==

    public String getValue() {
        return value;
    }

    // == Helpers ==

    @Override
    public String toString() {
        if (children.isEmpty())
            return "[" + value + "]:" + type;
        else
            return "([" + value + "]:" + type + " " + joinChildren() + ")";
    }

    public String joinChildren() {
        if (children == null || children.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        Iterator<Expression> iterator = children.iterator();
        result.append(iterator.next().toString());

        while(iterator.hasNext()) {
            Expression element = iterator.next();
            result.append(" ");
            result.append(element.toString());
        }

        return result.toString();
    }
}
