package com.tekai.standard;

import com.tekai.Expression;
import com.tekai.Parselet;

public class PostfixParselet extends Parselet {

    private final String startingRegularExpression;
    private final String type;

    public PostfixParselet(int precedence, String startingRegularExpression, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.type = type;
    }

    @Override
    public boolean isPrefixParselet() {
        return false;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = new Expression(type, lastMatch());
        result.addChildren(left());
        return result;
    }
}
