package com.tekai;

import static com.tekai.Expression.e;

import java.util.LinkedList;
import java.util.List;

public abstract class Transformation {

    public Expression applyOn(Expression expression) {
        LinkedList<Expression> transformedChildren = applyOn(expression.getChildren());

        if (when(expression))
            return then(expression.printValue(), expression.getType(), transformedChildren);
        else
            return e(expression.printValue(), expression.getType(), transformedChildren);
    }

    private LinkedList<Expression> applyOn(List<Expression> children) {
        LinkedList<Expression> transformedChildren = new LinkedList<Expression>();
        for (Expression child : children) {
            Expression newChild = applyOn(child);
            if (newChild != null) transformedChildren.add(newChild);
        }
        return transformedChildren;
    }

    public abstract boolean when(Expression expression);

    public abstract Expression then(String value, String type, List<Expression> children);
}
