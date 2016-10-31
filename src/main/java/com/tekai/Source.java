package com.tekai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Source {

    private final CharSequence source;
    private int cursor = 0;
    private int newCursor = 0;
    private String lastMatch;
    private String lastSpacing;

    public Source(CharSequence source) {
        this.source = source == null ? "" : source;
    }

    public boolean matches(String regularExpression) {
        if (cursor >= source.length()) return false;

        Pattern pattern = Pattern.compile("^(\\s*?)(" + regularExpression + ")");
        Matcher matcher = pattern.matcher(source);
        matcher.useAnchoringBounds(true);
        matcher.region(cursor, source.length());

        if (!matcher.find()) return false;

        lastSpacing = matcher.group(1);
        lastMatch = lastMatchedGroup(matcher);
        newCursor = matcher.end();

        return true;
    }

    private String lastMatchedGroup(Matcher matcher) {
        for (int i = matcher.groupCount(); i >= 3; i--)
            if (matcher.group(i) != null) return matcher.group(i);

        return matcher.group(2);
    }

    public boolean couldConsume(String regularExpression) {
        return matches(regularExpression);
    }

    public boolean canConsume(String regularExpression) {
        if (matches(regularExpression)) {
            consumeLastMatch();
            return true;
        } else {
            return false;
        }
    }

    public void consumeIf(String regularExpression) {

        if (canConsume(regularExpression)) return;

        if ("".equals(sample()))
            throw new RuntimeException("Expected \"" + regularExpression + "\", but found end of source");
        else
            throw new RuntimeException("Expected \"" + regularExpression + "\", but found \"" + sample() + "\"");
    }

    public void consumeLastMatch() {
        cursor = newCursor;
    }

    /**
     * Returns the last expression matched by {@link #canConsume(String)}, {@link #consumeIf(String)} and {@link #matches(String)}.
     * <p>
     * Never returns null.
     * </p>
     */
    public String lastMatch() {
        return lastMatch == null ? "" : lastSpacing + lastMatch;
    }

    public boolean isEmpty() {
        return source.length() == 0;
    }

    public String sample() {
        int start = cursor;
        int end = cursor + 20 > source.length() ? source.length() : cursor + 20;
        return source.subSequence(start, end).toString();
    }
}
