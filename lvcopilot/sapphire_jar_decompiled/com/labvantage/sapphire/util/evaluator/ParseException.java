/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.evaluator.Token;

public class ParseException
extends Exception {
    protected boolean specialConstructor;
    public Token currentToken;
    public int[][] expectedTokenSequences;
    public String[] tokenImage;
    protected String eol = System.getProperty("line.separator", "\n");

    public ParseException(Token currentTokenVal, int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
        super("");
        this.specialConstructor = true;
        this.currentToken = currentTokenVal;
        this.expectedTokenSequences = expectedTokenSequencesVal;
        this.tokenImage = tokenImageVal;
    }

    public ParseException() {
        this.specialConstructor = false;
    }

    public ParseException(String message) {
        super(message);
        this.specialConstructor = false;
    }

    @Override
    public String getMessage() {
        if (!this.specialConstructor) {
            return super.getMessage();
        }
        String expected = "";
        int maxSize = 0;
        for (int i = 0; i < this.expectedTokenSequences.length; ++i) {
            if (maxSize < this.expectedTokenSequences[i].length) {
                maxSize = this.expectedTokenSequences[i].length;
            }
            for (int j = 0; j < this.expectedTokenSequences[i].length; ++j) {
                expected = expected + this.tokenImage[this.expectedTokenSequences[i][j]] + " ";
            }
            if (this.expectedTokenSequences[i][this.expectedTokenSequences[i].length - 1] != 0) {
                expected = expected + "...";
            }
            expected = expected + this.eol + "    ";
        }
        String retval = "Encountered \"";
        Token tok = this.currentToken.next;
        for (int i = 0; i < maxSize; ++i) {
            if (i != 0) {
                retval = retval + " ";
            }
            if (tok.kind == 0) {
                retval = retval + this.tokenImage[0];
                break;
            }
            retval = retval + this.add_escapes(tok.image);
            tok = tok.next;
        }
        retval = retval + "\" at line " + this.currentToken.next.beginLine + ", column " + this.currentToken.next.beginColumn;
        retval = retval + "." + this.eol;
        retval = this.expectedTokenSequences.length == 1 ? retval + "Was expecting:" + this.eol + "    " : retval + "Was expecting one of:" + this.eol + "    ";
        retval = retval + expected;
        return retval;
    }

    protected String add_escapes(String str) {
        StringBuffer retval = new StringBuffer();
        block11: for (int i = 0; i < str.length(); ++i) {
            switch (str.charAt(i)) {
                case '\u0000': {
                    continue block11;
                }
                case '\b': {
                    retval.append("\\b");
                    continue block11;
                }
                case '\t': {
                    retval.append("\\t");
                    continue block11;
                }
                case '\n': {
                    retval.append("\\n");
                    continue block11;
                }
                case '\f': {
                    retval.append("\\f");
                    continue block11;
                }
                case '\r': {
                    retval.append("\\r");
                    continue block11;
                }
                case '\"': {
                    retval.append("\\\"");
                    continue block11;
                }
                case '\'': {
                    retval.append("\\'");
                    continue block11;
                }
                case '\\': {
                    retval.append("\\\\");
                    continue block11;
                }
                default: {
                    char ch = str.charAt(i);
                    if (ch < ' ' || ch > '~') {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                        continue block11;
                    }
                    retval.append(ch);
                }
            }
        }
        return retval.toString();
    }
}

