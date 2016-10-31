package com.tekai;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expression {

    private final String spacing;
    private final String type;
    private final String value;
    private List<Expression> children = new LinkedList<Expression>();

    // == Construction

    public Expression(String type, String value) {
        Pattern pattern = Pattern.compile("^(\\s*)(.*)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            this.spacing = matcher.group(1);
            this.value = matcher.group(2);
        } else {
            this.spacing = "";
            this.value = matcher.group(2);
        }
        this.type = type;
    }

    public void addChildren(Expression... expressions) {
        for(Expression expression : expressions)
            children.add(expression);
    }

    public void addChildren(List<Expression> expressions) {
        children = expressions;
    }

    public void addFirstChild(Expression expression){
        addChildAt(expression, 0);
    }

    public void addChildAt(Expression expression, int index){
        children.add(index, expression);
    }

    public static Expression e(String value, String type, Expression ... expressions) {
        return e(value, type, new LinkedList<Expression>(asList(expressions)));
    }

    public static Expression e(String value, String type, List<Expression> expressions) {
        Expression result = new Expression(type, value);
        result.addChildren(expressions);
        return result;
    }

    // == Accessors ==

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getSpacing() {
        return spacing;
    }

    /**
     * {@link #getSpacing()} + {@link #getValue()}
     */
    public String printValue() {
        return getSpacing() + getValue();
    }

    public List<Expression> getChildren() {
      return children;
    }

    public Expression getChild(int i) {
        return children.get(i);
    }

    // == Inspection ==

    public boolean isType(String type) {
      return this.type != null && this.type.equals(type);
    }

    public boolean hasValue(String regex) {
        return value.matches(regex);
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
