package org.xblackcat.sjpu.storage.impl;

/**
 * 25.02.2016 17:27
 *
 * @author xBlackCat
 */
public class ArgumentCounter {
    private int totalAmount = 0;
    /**
     * Stores a open quote character. Space means a quote are closed or not yet open.
     */
    private char quote = ' ';

    /**
     * Scans prepared statement (or it part) and returns amount of argument placeholders
     *
     * @param sqlPart sql part to examine
     * @return amount of found argument placeholders
     */
    public static int getArgumentCount(String sqlPart) {
        return new ArgumentCounter().argsInPart(sqlPart);
    }

    /**
     * Scans prepared statement (or it part) and returns amount of argument placeholders
     *
     * @param sqlPart sql part to examine
     * @return amount of found argument placeholders
     */
    public int argsInPart(String sqlPart) {
        int amount = 0;

        boolean wasEscape = false;

        for (char c : sqlPart.toCharArray()) {
            if (quote == ' ') {
                if (c == '?') {
                    amount++;
                    totalAmount++;
                } else if (c == '`' || c == '"' || c == '\'') {
                    quote = c;
                    wasEscape = false;
                }
            } else if (wasEscape) {
                wasEscape = false;
            } else if (c == quote) {
                quote = ' ';
            } else if (c == '\\') {
                wasEscape = true;
            }
        }

        return amount;
    }

    public boolean isQuoteOpen() {
        return quote != ' ';
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
