package com.tekai.standard;

import com.tekai.Transformation;
import java.util.LinkedList;
import java.util.List;
import com.tekai.Expression;
import static com.tekai.Expression.e;

public abstract class SpecificTransformation extends Transformation{

    private Expression specificExpression;

    @Override
    public Expression applyOn(Expression expression){
        Expression exp = applying(expression);
        if(specificExpression == null)
            return exp;
        else
            return then(exp, specificExpression);
    }

    private Expression applying(Expression expression){
        LinkedList<Expression> transformedChildren = applying(expression.getChildren());

        if (when(expression)){
            specificExpression = expression;
            return null;
        }
        else
            return e(expression.printValue(), expression.getType(), transformedChildren);
    }

    private LinkedList<Expression> applying(List<Expression> children) {
        LinkedList<Expression> transformedChildren = new LinkedList<Expression>();
        for (Expression child : children) {
            Expression newChild = applying(child);
            if (newChild != null) transformedChildren.add(newChild);
        }
        return transformedChildren;
    }

    @Override
    public Expression then(String value, String type, List<Expression> children) {
        return null;
    }

    public abstract Expression then(Expression expression, Expression specific);

}
