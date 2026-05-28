/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.PageContext
 *  org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager
 */
package sapphire.util;

import com.labvantage.sapphire.BaseClass;
import com.labvantage.sapphire.Trace;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

public class JstlUtil
extends BaseClass {
    public static Object evaluateExpression(String expression, PageContext pageContext) {
        Object value = null;
        if (expression != null && expression.length() > 0) {
            if (expression.indexOf("${") >= 0) {
                try {
                    value = ExpressionEvaluatorManager.evaluate((String)"", (String)expression, Object.class, (PageContext)pageContext);
                }
                catch (JspException jspe) {
                    Trace.log("JSTL", "ERROR: Evaluating expression: " + expression + ", Exception: " + jspe.getMessage());
                }
            } else {
                value = expression;
            }
        }
        return value;
    }

    public static Object evaluateExpression(String expression, PageContext pageContext, Object nullValue) {
        Object value = JstlUtil.evaluateExpression(expression, pageContext);
        return value != null ? value : nullValue;
    }
}

