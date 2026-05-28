/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 */
package sapphire.util;

import com.labvantage.sapphire.BaseClass;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;

public class ForwardUtil
extends BaseClass {
    private HashMap _forwardprops = new HashMap();
    private HttpServletRequest request;

    public ForwardUtil() {
    }

    public ForwardUtil(ServletRequest request) {
        this.request = (HttpServletRequest)request;
    }

    public void setProperty(String propertyid, String value) {
        this._forwardprops.put(propertyid, value);
    }

    public void setProperties(HashMap props) {
        this._forwardprops.putAll(props);
    }

    public String getForm(String formid, String action, String method, boolean autosubmit) {
        return this.getForm(formid, action, method, autosubmit, false);
    }

    public String getForm(String formid, String action, String method, boolean autosubmit, boolean parseURLToForm) {
        int rci;
        int pos;
        StringBuffer form = new StringBuffer(500);
        if (method.equalsIgnoreCase("get") && (pos = action.indexOf(63)) > -1) {
            String[] params = StringUtil.split(action.substring(pos + 1), "&");
            for (int i = 0; i < params.length; ++i) {
                pos = params[i].indexOf(61);
                if (pos <= -1) continue;
                this._forwardprops.put(params[i].substring(0, pos), params[i].substring(pos + 1));
            }
        }
        if (formid == null || formid.length() == 0) {
            formid = "__formdata";
        }
        String querystring = "";
        if (parseURLToForm && (rci = action.indexOf("rc?")) > -1) {
            querystring = action.substring(rci + 3);
            action = action.substring(0, rci + 2);
        }
        form.append("<div style=\"display:none\"><form id=\"").append(SafeHTML.encodeForHTMLAttribute(formid) + "\" name=\"").append(SafeHTML.encodeForHTMLAttribute(formid)).append("\" action=\"").append(SafeHTML.encodeForHTMLAttribute(action)).append("\" method=\"").append(method).append("\">\n");
        if (querystring.length() > 0) {
            String[] queryparts;
            for (String querypart : queryparts = StringUtil.split(querystring, "&")) {
                String[] values = StringUtil.split(querypart, "=");
                if (values.length == 2) {
                    form.append("<input type=\"hidden\" name=\"").append(values[0]).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(HttpUtil.encodeURIComponent(values[1]))).append("\">\n");
                    continue;
                }
                if (values.length != 1) continue;
                form.append("<input type=\"hidden\" name=\"").append(values[0]).append("\" value=\"").append("").append("\">\n");
            }
        }
        form.append("<input type=\"hidden\" name=\"").append("_dummyprop").append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(HttpUtil.encodeURIComponent("_dummyvalue"))).append("\">\n");
        Set keyset = this._forwardprops.keySet();
        for (String propertyid : keyset) {
            if (method.equalsIgnoreCase("get")) {
                form.append("<input type=\"hidden\" name=\"").append(propertyid).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(HttpUtil.encodeURIComponent((String)this._forwardprops.get(propertyid)))).append("\">\n");
                continue;
            }
            String val = this._forwardprops.get(propertyid).toString();
            if (val.startsWith("{") || val.endsWith("}") || val.contains("\"")) {
                form.append("<textarea name=\"").append(SafeHTML.encodeForHTMLAttribute(propertyid)).append("\">");
                form.append(SafeHTML.encodeForHTML(val));
                form.append("</textarea>");
                continue;
            }
            form.append("<input type=\"hidden\" name=\"").append(propertyid).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(val)).append("\">\n");
        }
        if (this.request != null) {
            form.append("<input type=\"hidden\" name=\"csrftoken\" value=\"").append(this.request.getSession().getAttribute("csrftoken") == null ? "" : SafeHTML.encodeForHTMLAttribute((String)this.request.getSession().getAttribute("csrftoken"))).append("\">\n");
        }
        form.append("</form></div>\n");
        if (autosubmit) {
            form.append("<script language=\"JavaScript\">\n");
            form.append("function doSubmit(){var f = document.getElementById('").append(SafeHTML.encodeForJavaScript(formid)).append("');f.submit();}\n");
            form.append("window.onload=doSubmit;\n");
            form.append("</script>\n");
        }
        return form.toString();
    }

    public String getURLParams() {
        StringBuffer urlparams = new StringBuffer(500);
        boolean first = true;
        Set keyset = this._forwardprops.keySet();
        for (String propertyid : keyset) {
            if (!first) {
                urlparams.append("&");
            }
            urlparams.append(propertyid + "=" + (String)this._forwardprops.get(propertyid));
            first = false;
        }
        return urlparams.toString();
    }
}

