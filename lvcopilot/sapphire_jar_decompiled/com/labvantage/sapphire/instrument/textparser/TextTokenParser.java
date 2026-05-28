/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Script
 */
package com.labvantage.sapphire.instrument.textparser;

import com.labvantage.sapphire.instrument.textparser.TextToken;
import groovy.lang.Script;
import sapphire.util.StringUtil;

public abstract class TextTokenParser
extends Script {
    String[] tokens = null;
    String tokenDelimiter = "";
    TextToken rootToken = null;
    String decimalSeparator = ".";

    public void setInput(String s) {
        this.rootToken = new TextToken(s, 0, null);
        if (s.indexOf("\n") > 0 && s.indexOf("\r\n") < 0) {
            this.rootToken.setLINE_SEPARATOR("\n");
        } else if (s.indexOf("\r") > 0 && s.indexOf("\r\n") < 0) {
            this.rootToken.setLINE_SEPARATOR("\r");
        }
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.rootToken.setDecimalSeparator(decimalSeparator);
        this.decimalSeparator = decimalSeparator;
    }

    public void resetRootToken() {
        this.rootToken.reset();
    }

    public void splitby(String delimiter) {
        this.tokenDelimiter = delimiter;
        if (this.rootToken != null) {
            this.tokens = StringUtil.split(this.rootToken.getText(), delimiter);
        }
    }

    public TextToken token(int tknNo) {
        --tknNo;
        int startindex = 0;
        TextToken tk = null;
        if (this.tokens != null) {
            if (tknNo > 0) {
                for (int t = 0; t < tknNo; ++t) {
                    startindex += this.tokens[t].length() + this.tokenDelimiter.length();
                }
            }
            tk = new TextToken(this.tokens[tknNo], startindex, this.rootToken);
        } else {
            tk = new TextToken("", -1, this.rootToken);
        }
        tk.setDecimalSeparator(this.decimalSeparator);
        return tk;
    }

    public TextToken line(int lineNo) {
        return this.rootToken.line(lineNo);
    }

    public TextToken line(int lineNo, int noOfline) {
        return this.rootToken.line(lineNo, noOfline);
    }

    public TextToken pos(int start, int end) {
        return this.rootToken.pos(start, end);
    }

    public TextToken pos(int start) {
        return this.rootToken.pos(start);
    }

    public TextToken col(int colNo) {
        return this.rootToken.pos(colNo, colNo + 1);
    }

    public TextToken col(int colNo, int endNo) {
        return this.rootToken.pos(colNo, endNo);
    }

    public TextToken fromline(int fromlineNo) {
        return this.rootToken.fromline(fromlineNo);
    }

    public TextToken toline(int tolineNo) {
        return this.rootToken.toline(tolineNo);
    }

    public TextToken linecol(int fromlineNo, int fromcol) {
        return this.rootToken.linecol(fromlineNo, fromcol);
    }

    public TextToken fromlinecol(int fromlineNo, int fromcol) {
        return this.rootToken.fromlinecol(fromlineNo, fromcol);
    }

    public TextToken tolinecol(int tolineNo, int tocol) {
        return this.rootToken.tolinecol(tolineNo, tocol);
    }

    public TextToken linecoltolinecol(int fromlineNo, int tolineNo) {
        return this.rootToken.linecoltolinecol(fromlineNo, tolineNo);
    }

    public TextToken linecoltolinecol(int fromlineNo, int fromcol, int tolineNo, int tocol) {
        return this.rootToken.linecoltolinecol(fromlineNo, fromcol, tolineNo, tocol);
    }

    public TextToken before(String token) {
        return this.rootToken.before(token);
    }

    public TextToken after(String token) {
        return this.rootToken.after(token);
    }

    public TextToken extractByRegex(String regex) {
        return this.rootToken.extractByRegex(regex);
    }

    public TextToken extract() {
        return this.rootToken.extract();
    }

    public TextToken extract(int n) {
        return this.rootToken.extract(n);
    }

    public TextToken extractLast(int n) {
        return this.rootToken.extractLast(n);
    }

    public TextToken extractByRegexLast(String regex) {
        return this.rootToken.extractByRegexLast(regex);
    }

    public TextToken extractNumber() {
        return this.rootToken.extractNumber();
    }

    public TextToken extractNumberLast() {
        return this.rootToken.extractNumberLast();
    }

    public TextToken extractNumber(String format) {
        return this.rootToken.extractNumber(format);
    }

    public TextToken extractNumberLast(String format) {
        return this.rootToken.extractNumberLast(format);
    }

    public TextToken extractDate() {
        return this.rootToken.extractDate();
    }

    public TextToken extractDateLast() {
        return this.rootToken.extractDateLast();
    }

    public TextToken extractDate(String format) {
        return this.rootToken.extractDate(format);
    }

    public TextToken extractDateLast(String format) {
        return this.rootToken.extractDateLast(format);
    }
}

