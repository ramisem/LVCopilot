/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.util.UtilityRequestHandler;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;

public class UtilityProcessor
extends BaseAccessor {
    public UtilityProcessor() {
    }

    public UtilityProcessor(String connectionid) {
        super(connectionid);
    }

    public UtilityProcessor(File rakFile) throws SapphireException {
        this.setRakFile(rakFile);
    }

    public UtilityProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public UtilityProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public String evaluate(String expression, HashMap params) throws SapphireException {
        try {
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("operation", "evaluateexpression");
            props.put("expression", expression);
            props.put("params", params);
            HashMap returnMap = this.getRequestManager().processRequest(this.getConnectionid(), UtilityRequestHandler.class.getName(), props);
            String ret = (String)returnMap.get("value");
            return ret;
        }
        catch (Exception e) {
            throw new SapphireException("Unable to evaluate", e);
        }
    }

    public BigDecimal convertUnits(BigDecimal number, String fromUnit, String toUnit) throws SapphireException {
        try {
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put("operation", "convertunits");
            props.put("number", number);
            props.put("fromunit", fromUnit);
            props.put("tounit", toUnit);
            HashMap returnMap = this.getRequestManager().processRequest(this.getConnectionid(), UtilityRequestHandler.class.getName(), props);
            BigDecimal ret = (BigDecimal)returnMap.get("value");
            return ret;
        }
        catch (Exception e) {
            throw new SapphireException("Unable to convert units", e);
        }
    }
}

