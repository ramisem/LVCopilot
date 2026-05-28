/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.XML;

public class XMLTokener
extends JSONTokener {
    public static final HashMap entity = new HashMap(8);

    public XMLTokener(String s) {
        super(s);
    }

    public String nextCDATA() throws JSONException {
        int i;
        StringBuffer sb = new StringBuffer();
        do {
            char c;
            if ((c = this.next()) == '\u0000') {
                throw this.syntaxError("Unclosed CDATA");
            }
            sb.append(c);
        } while ((i = sb.length() - 3) < 0 || sb.charAt(i) != ']' || sb.charAt(i + 1) != ']' || sb.charAt(i + 2) != '>');
        sb.setLength(i);
        return sb.toString();
    }

    public Object nextContent() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        if (c == '\u0000') {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        StringBuffer sb = new StringBuffer();
        while (true) {
            if (c == '<' || c == '\u0000') {
                this.back();
                return sb.toString().trim();
            }
            if (c == '&') {
                sb.append(this.nextEntity(c));
            } else {
                sb.append(c);
            }
            c = this.next();
        }
    }

    public Object nextEntity(char a) throws JSONException {
        char c;
        StringBuffer sb = new StringBuffer();
        while (Character.isLetterOrDigit(c = this.next()) || c == '#') {
            sb.append(Character.toLowerCase(c));
        }
        if (c != ';') {
            throw this.syntaxError("Missing ';' in XML entity: &" + sb);
        }
        String s = sb.toString();
        Object e = entity.get(s);
        return e != null ? e : a + s + ";";
    }

    public Object nextMeta() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        switch (c) {
            case '\u0000': {
                throw this.syntaxError("Misshaped meta tag");
            }
            case '<': {
                return XML.LT;
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"': 
            case '\'': {
                char q = c;
                do {
                    if ((c = this.next()) != '\u0000') continue;
                    throw this.syntaxError("Unterminated string");
                } while (c != q);
                return Boolean.TRUE;
            }
        }
        while (!Character.isWhitespace(c = this.next())) {
            switch (c) {
                case '\u0000': 
                case '!': 
                case '\"': 
                case '\'': 
                case '/': 
                case '<': 
                case '=': 
                case '>': 
                case '?': {
                    this.back();
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public Object nextToken() throws JSONException {
        char c;
        while (Character.isWhitespace(c = this.next())) {
        }
        switch (c) {
            case '\u0000': {
                throw this.syntaxError("Misshaped element");
            }
            case '<': {
                throw this.syntaxError("Misplaced '<'");
            }
            case '>': {
                return XML.GT;
            }
            case '/': {
                return XML.SLASH;
            }
            case '=': {
                return XML.EQ;
            }
            case '!': {
                return XML.BANG;
            }
            case '?': {
                return XML.QUEST;
            }
            case '\"': 
            case '\'': {
                char q = c;
                StringBuffer sb = new StringBuffer();
                while (true) {
                    if ((c = this.next()) == '\u0000') {
                        throw this.syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return sb.toString();
                    }
                    if (c == '&') {
                        sb.append(this.nextEntity(c));
                        continue;
                    }
                    sb.append(c);
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        while (true) {
            sb.append(c);
            c = this.next();
            if (Character.isWhitespace(c)) {
                return sb.toString();
            }
            switch (c) {
                case '\u0000': 
                case '!': 
                case '/': 
                case '=': 
                case '>': 
                case '?': 
                case '[': 
                case ']': {
                    this.back();
                    return sb.toString();
                }
                case '\"': 
                case '\'': 
                case '<': {
                    throw this.syntaxError("Bad character in a name");
                }
            }
        }
    }

    static {
        entity.put("amp", XML.AMP);
        entity.put("apos", XML.APOS);
        entity.put("gt", XML.GT);
        entity.put("lt", XML.LT);
        entity.put("quot", XML.QUOT);
    }
}

