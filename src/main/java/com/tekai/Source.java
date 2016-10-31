package com.tekai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Source {

    private final CharSequence source;
    private int cursor = 0;
    private int newCursor = 0;
    private String lastMatch;

    public Source(CharSequence source) {
        this.source = source == null ? "" : source;
    }

    public boolean matches(String regularExpression) {
        if (cursor >= source.length()) return false;

        Pattern pattern = Pattern.compile("^\\s*" + regularExpression);
        Matcher matcher = pattern.matcher(source);
        matcher.useAnchoringBounds(true);
        matcher.region(cursor, source.length());

        if (!matcher.find()) return false;

        lastMatch = matcher.group(0);
        newCursor = matcher.end();

        return true;
    }

    public boolean canConsume(String regularExpression) {
        if (!matches(regularExpression)) return false;

        consumeLastMatch();
        return true;
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

    public String sample() {
        int start = cursor;
        int end = cursor + 20 > source.length() ? source.length() : cursor + 20;
        return source.subSequence(start, end).toString();
    }

    public String lastMatch() {
        return lastMatch;
    }
}
