package com.tekai.standard;

import com.tekai.Expression;
import com.tekai.Parselet;

public class GroupingParselet extends Parselet {

    private final String startingRegularExpression;
    private final String endingRegularExpression;

    public GroupingParselet(int precedence, String startingRegularExpression, String endingRegularExpression) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.endingRegularExpression = endingRegularExpression;
    }

    @Override
    public boolean isPrefixParselet() {
        return true;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = right();
        consumeIf(endingRegularExpression);
        return result;
    }
}
