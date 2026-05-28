/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.ajax.operations.ImageHandler;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import java.util.Random;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class UpdateProfilePicture
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54860 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String image = ajaxResponse.getRequestParameter("image");
        String url = "";
        String sql = "";
        QueryProcessor qp = this.getQueryProcessor();
        try {
            String userid = ajaxResponse.getRequestParameter("userid");
            if (userid.length() == 0) {
                userid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
            }
            if (image.length() > 0) {
                String attNummber = ImageHandler.storeAsAttachment(image, "User", userid, "", "", "", "ProfilePicture", "ProfilePicture", "ProfilePicture", "Profile Picture", false, "", "", qp, new AttachmentProcessor(this.getConnectionId()), new ConfigurationProcessor(this.getConnectionId()));
                if (attNummber.length() > 0) {
                    int r = new Random().nextInt(100) + 1;
                    url = "rc?command=image&attachment=User;" + userid + ";;;ProfilePicture&width=46&_r" + r;
                }
            } else {
                String[] users = StringUtil.split(userid, ";");
                for (int u = 0; u < users.length; ++u) {
                    String user = users[u];
                    SafeSQL safeSQL = new SafeSQL();
                    String sql1 = "SELECT attachmentnum, attachmentclass FROM sdiattachment WHERE sdcid='User' AND keyid1=" + safeSQL.addVar(user) + " AND attachmentclass='ProfilePicture'";
                    DataSet atts = qp.getPreparedSqlDataSet(sql1, safeSQL.getValues());
                    int attNum = -1;
                    if (atts != null && atts.size() > 0) {
                        attNum = atts.getBigDecimal(0, "attachmentnum").intValue();
                    }
                    if (attNum <= -1) continue;
                    qp.execPreparedUpdate("DELETE sdiattachment WHERE sdcid='User' AND keyid1=? AND attachmentclass='ProfilePicture' AND attachmentnum =?", new Object[]{user, attNum});
                }
            }
        }
        catch (Exception e) {
            this.logError("Exception: " + e);
        }
        finally {
            ajaxResponse.addCallbackArgument("url", url);
            ajaxResponse.print();
        }
    }
}

