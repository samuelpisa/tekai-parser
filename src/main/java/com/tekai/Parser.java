package com.tekai;

import java.util.LinkedList;
import java.util.List;


public class Parser {

    private final Source source;

    private List<Parselet> prefixParselets = new LinkedList<Parselet>();
    private List<Parselet> parselets = new LinkedList<Parselet>();

    // == Construction

    public Parser(CharSequence source) {
        this.source = new Source(source);
    }

    public void register(Parselet parselet) {
        if (parselet.isPrefixParselet()) {
            prefixParselets.add(parselet);
        } else {
            parselets.add(parselet);
        }
    }

    // == Parse Engine ==

    public Expression parse() {
        return parse(0);
    }

    public Expression parse(int currentPrecedence) {

        Parselet currentParselet = findParseletIn(prefixParselets);

        if (currentParselet == null)
            if ("".equals(source.sample()))
                throw new RuntimeException("Expected something to parse, but found end of source");
            else
                throw new RuntimeException("Could not find a expression to parse \"" + source.sample() + "\"");


        consumeLastMatch();
        Expression left = currentParselet.executeParsing(this);

        Parselet nextParselet = findParseletIn(parselets);

        while (nextParselet != null && currentPrecedence < nextParselet.getPrecedence()) {
            consumeLastMatch();
            left = nextParselet.executeParsing(this, left);
            nextParselet = findParseletIn(parselets);
        }

        return left;
    }

    // == Helpers to Parse Engine ==

    private Parselet findParseletIn(List<Parselet> parselets) {
        for (Parselet parselet : parselets)
            if (sourceMatches(parselet))
                return parselet;
        return null;
    }

    private void consumeLastMatch() {
        source.consumeLastMatch();
    }

    private boolean sourceMatches(Parselet parselet) {
        return source.matches(parselet.startingRegularExpression());
    }

    // == Helpers to Parselets

    public String lastMatch() {
        return source.lastMatch();
    }

    public boolean canConsume(String regularExpression) {
        return source.canConsume(regularExpression);
    }

    public void consumeIf(String regularExpression) {
        source.consumeIf(regularExpression);
    }
}
