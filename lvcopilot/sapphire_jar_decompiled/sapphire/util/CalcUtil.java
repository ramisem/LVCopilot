/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class CalcUtil
implements Serializable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private QueryProcessor queryProcessor;

    public CalcUtil(ConnectionInfo connectionInfo) {
        this.queryProcessor = new QueryProcessor(connectionInfo.getConnectionId());
    }

    public CalcUtil(PageContext pageContext) {
        this.queryProcessor = new QueryProcessor(pageContext);
    }

    public CalcUtil(File rakFile, String connectionid) {
        this.queryProcessor = new QueryProcessor(rakFile, connectionid);
    }

    public String evaluate(String expression, HashMap params) throws SapphireException {
        return ExpressionUtil.evaluate(expression, params, false, false);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public BigDecimal convertUnits(BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        if (value == null) {
            return null;
        }
        if (fromUnit.equals(toUnit)) {
            return value;
        }
        BigDecimal returnValue = null;
        String sql = "select expression from unitconversion where unitsid = ? and tounits= ? ";
        DataSet expressions = this.queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{fromUnit, toUnit});
        if (expressions == null) throw new SapphireException("Unable to find conversion rule from " + fromUnit + " to " + toUnit);
        if (expressions.getRowCount() <= 0) throw new SapphireException("Unable to find conversion rule from " + fromUnit + " to " + toUnit);
        String expression = expressions.getString(0, "expression");
        try {
            if (expression == null) return returnValue;
            if (expression.length() <= 0) return returnValue;
            HashMap<String, BigDecimal> params = new HashMap<String, BigDecimal>();
            params.put("this", value);
            String returnStr = ExpressionUtil.evaluate(expression, params);
            if (returnStr == null) throw new SapphireException("Unknown calculation error");
            if (returnStr.length() <= 0) throw new SapphireException("Unknown calculation error");
            return new BigDecimal(returnStr);
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to convert " + value + " from " + fromUnit + " to " + toUnit + " using expresson:" + expression, e);
        }
    }
}

