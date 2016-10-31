package com.tekai.standard;

import java.util.LinkedList;
import java.util.List;

import com.tekai.Expression;
import com.tekai.Transformation;

public class MultiTransformation extends Transformation {

    private Transformation matchedTransformation = null;
    private final List<Transformation> transformations = new LinkedList<Transformation>();

    public MultiTransformation register(Transformation transformation) {
        transformations.add(transformation);
        return this;
    }

    @Override
    public boolean when(Expression expression) {

        for (Transformation transformation : transformations) {
            if (transformation.when(expression)) {
                matchedTransformation = transformation;
                return true;
            }
        }

        return false;
    }

    @Override
    public Expression then(String value, String type, List<Expression> children) {
        if (matchedTransformation == null) return null;
        return matchedTransformation.then(value, type, children);
    }

}
