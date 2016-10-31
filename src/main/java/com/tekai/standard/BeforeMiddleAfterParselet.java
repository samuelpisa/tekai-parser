package com.tekai.standard;

import com.tekai.Expression;
import com.tekai.Parselet;

import static com.tekai.javaexpansions.Strings.coalesce;

public class BeforeMiddleAfterParselet extends Parselet {

    private final String startingRegularExpression;
    private final String interpolationRegularExpression;
    private final String endingRegularExpression;
    private final String value;
    private final String type;

    public BeforeMiddleAfterParselet(int precedence, String startingRegularExpression, String interpolationRegularExpression, String endingRegularExpression, String type) {
        this(precedence, startingRegularExpression, interpolationRegularExpression, endingRegularExpression, null, type);
    }

    public BeforeMiddleAfterParselet(int precedence, String startingRegularExpression, String interpolationRegularExpression, String endingRegularExpression, String value, String type) {
        this.setPrecedence(precedence);
        this.startingRegularExpression = startingRegularExpression;
        this.interpolationRegularExpression = interpolationRegularExpression;
        this.endingRegularExpression = endingRegularExpression;
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean isLeftAssociativity() {
      return startingRegularExpression == null;
    }

    @Override
    public boolean isPrefixParselet() {
        return startingRegularExpression != null;
    }

    @Override
    public String startingRegularExpression() {
        return startingRegularExpression == null
            ? interpolationRegularExpression
            : startingRegularExpression;
    }

    @Override
    protected Expression parse() {
        Expression result = new Expression(type, coalesce(value, lastMatch()));

        if (endingRegularExpression != null && canConsume(endingRegularExpression)) return result;

        if (startingRegularExpression == null) result.addChildren(left());

        if (interpolationRegularExpression == null) {
            result.addChildren(nextExpression());
        } else {
            do {
                result.addChildren(nextExpression());
            } while (canConsume(interpolationRegularExpression));
        }

        if (endingRegularExpression != null) consumeIf(endingRegularExpression);

        return result;
    }
}
