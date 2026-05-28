/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.owasp.esapi.ESAPI
 *  org.owasp.esapi.Encoder
 */
package sapphire.util;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import sapphire.util.StringUtil;

public class SafeHTML {
    private static Encoder encoder = ESAPI.encoder();
    private static String[] allowedHtmlTags = new String[]{"<br>", "<br/>", "<BR>", "<BR/>", "<p>", "</p>", "<strong>", "</strong>", "<b>", "</b>", "<i>", "</i>", "<u>", "</u>", "<pre>", "</pre>", "<hr>", "<h1>", "</h1>", "<h2>", "<h2/>", "<h3>", "</h3>", "<h4>", "</h4>", "<h5>", "</h5>", "<h6>", "</h6>", "<ul>", "</ul>", "<li>", "</li>", "<sub>", "</sub>", "<sup>", "</sup>"};

    public static String[] getAllowedHtmlTags() {
        return allowedHtmlTags;
    }

    public static String encodeForJavaScript(String input) {
        return encoder.encodeForJavaScript(input);
    }

    public static String encodeForHTMLAttribute(String input) {
        return encoder.encodeForHTMLAttribute(input);
    }

    public static String encodeForHTML(String input) {
        return encoder.encodeForHTML(input);
    }

    public static String encodeForHTML(String input, boolean allowBasicFormatTags) {
        String encoded = "";
        if (allowBasicFormatTags) {
            String[] fonttokens = StringUtil.getTokens(input, "<font ", "</font>");
            if (fonttokens != null && fonttokens.length > 0) {
                for (int t = 0; t < fonttokens.length; ++t) {
                    input = StringUtil.replaceAll(input, "<font " + fonttokens[t] + "</font>", "fonttag" + t);
                }
            }
            encoded = SafeHTML.encodeForHTML(input);
            for (String tags : allowedHtmlTags) {
                encoded = StringUtil.replaceAll(encoded, SafeHTML.encodeForHTML(tags), tags);
            }
            if (fonttokens != null && fonttokens.length > 0) {
                for (int t = 0; t < fonttokens.length; ++t) {
                    encoded = StringUtil.replaceAll(encoded, "fonttag" + t, "<font " + fonttokens[t] + "</font>");
                }
            }
        } else {
            encoded = SafeHTML.encodeForHTML(input);
        }
        return encoded;
    }

    public static String encodeForURL(String input) throws Exception {
        return encoder.encodeForURL(input);
    }

    public static String encodeForCSS(String input) {
        return encoder.encodeForCSS(input);
    }
}

