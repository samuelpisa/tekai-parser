package com.tekai;


public abstract class Parselet {

    private int precedence;
    private Parser parser;
    private String match = "";
    private Expression left;
    private Expression right;

    // == Construction ==

    public Parselet() {
        this(0);
    }

    public Parselet(int precedence) {
        this.precedence = precedence;
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    protected boolean isLeftAssociativity() {
        return false;
    }

    // == Accessors to be used by Parse Engine ==

    public int getPrecedence() {
        return precedence;
    }

    // == Commands to be used inside #parse() ==

    /**
     * Value matched by the {@link #startingRegularExpression()}.
     * <p>
     * Never returns null.
     * </p>
     */
    protected String originalMatch() {
        return match == null ? "" : match;
    }

    /**
     * Value matched by {@link #startingRegularExpression()} with no spaces around
     * @return
     */
    protected String originalMatchTrimmed() {
        return originalMatch().trim();
    }

    protected String lastMatch() {
        assert parser != null;
        return parser.lastMatch();
    }

    protected Expression left() {
        if (isPrefixParselet()) throw new RuntimeException("There is no \"left\" expression in a prefix parser");
        return left;
    }

    protected Expression right() {
        return isLeftAssociativity() ? right(precedence) : right(0);
    }

    protected Expression right(int precedence) {
        if (right == null) right = nextExpression(precedence);
        return right;
    }

    protected Expression nextExpression() {
        return isLeftAssociativity() ? nextExpression(precedence) : nextExpression(0);
    }

    protected Expression nextExpression(int precedence) {
        assert parser != null;
        return parser.parse(precedence);
    }

    protected void consumeIf(String regularExpression) {
        assert parser != null;
        parser.consumeIf(regularExpression);
    }

    protected boolean cannotConsume(String regularExpression) {
        return !canConsume(regularExpression);
    }

    protected boolean couldConsume(String regularExpression) {
        assert parser != null;
        return parser.couldConsume(regularExpression);
    }

    protected boolean canConsume(String regularExpression) {
        assert parser != null;
        return parser.canConsume(regularExpression);
    }

    // == Calling method for #parse() that prepares the "environment"

    public Expression executeParsing(Parser parser) {
        return executeParsing(parser, null);
    }

    public Expression executeParsing(Parser parser, Expression left) {
        this.parser = parser;
        this.match = parser.lastMatch();
        this.left = left;
        this.right = null;
        return parse();
    }

    // == To be implemented

    public abstract boolean isPrefixParselet();

    public abstract String startingRegularExpression();

    protected abstract Expression parse();
}
