/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.pagetype.maint;

import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;

public class MasterDetailProcessor {
    public String processMasterDetail(PageContext pageContext) {
        HashMap<String, String> props;
        StringBuffer msg = new StringBuffer();
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        String strFrmCount = req.getParameter("frmcount");
        int frmCount = Integer.parseInt(strFrmCount);
        int count = 0;
        ActionProcessor ap = new ActionProcessor(pageContext);
        for (count = 0; count < frmCount; ++count) {
            String frmsdcId = req.getParameter("frm" + count + "sdcid");
            String frmfkcol = req.getParameter("frm" + count + "fkcol");
            String frmfkval = req.getParameter("frm" + count + "fkval");
            String frmkeyid1 = req.getParameter("frm" + count + "keyid1");
            String frmkeyid2 = req.getParameter("frm" + count + "keyid2");
            String frmkeyid3 = req.getParameter("frm" + count + "keyid3");
            String validationmsg = this.validate(frmsdcId, frmkeyid1, frmfkcol, frmfkval);
            if (!validationmsg.equals("")) {
                msg.append("Form: " + count + " has errors. Check log for more details <br/>");
                Trace.logDebug("Form: " + count + " errors. " + validationmsg);
                continue;
            }
            props = new HashMap<String, String>();
            props.put("sdcid", frmsdcId);
            props.put("keyid1", frmkeyid1);
            props.put("keyid2", frmkeyid2);
            props.put("keyid3", frmkeyid3);
            props.put(frmfkcol, frmfkval);
            try {
                ap.processAction("EditSDI", "1", props);
                continue;
            }
            catch (ActionException e) {
                msg.append("Form: " + count + "Action Exception caught.  Check log for more details <br/>");
                Trace.logError("Formcount: " + count + "Action Exception: " + e.toString(), e);
            }
        }
        props = null;
        return msg.toString();
    }

    private String validate(String frmsdcId, String frmkeyid1, String frmfkcol, String frmfkval) {
        StringBuffer msg = new StringBuffer();
        if (frmsdcId == null || frmsdcId.equals("")) {
            msg.append("SDCId not found <br/>");
        } else if (frmkeyid1 == null || frmkeyid1.equals("")) {
            msg.append("Keyid1 not found <br/>");
        } else if (frmfkcol == null || frmfkcol.equals("")) {
            msg.append("Foreign key column not found <br/>");
        } else if (frmfkval == null) {
            msg.append("Foreign key value column not found <br/>");
        }
        return msg.toString();
    }
}

