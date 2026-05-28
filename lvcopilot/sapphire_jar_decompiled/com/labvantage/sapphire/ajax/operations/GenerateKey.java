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

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SequenceService;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GenerateKey
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block11: {
            ajaxResponse = new AjaxResponse(request, response, "generateKeyHandler");
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String keycolid1 = ajaxResponse.getRequestParameter("keycolid1");
            String keyrule = ajaxResponse.getRequestParameter("keyrule");
            if (sdcid.length() > 0) {
                if (keycolid1.length() > 0) {
                    if (keyrule.length() > 0) {
                        DataSet ds = new DataSet();
                        int newRow = ds.addRow();
                        ds.setString(newRow, keycolid1, "");
                        try {
                            JSONObject data = new JSONObject(ajaxResponse.getRequestParameter("data"));
                            Iterator it = data.keys();
                            while (it.hasNext()) {
                                String columnid = (String)it.next();
                                String[] parts = StringUtil.split(data.getString(columnid), ";");
                                if (parts.length != 2) continue;
                                if (parts[0].equals("C") || parts[0].equals("S")) {
                                    ds.setString(newRow, columnid, parts[1]);
                                    continue;
                                }
                                if (!parts[0].equals("N")) continue;
                                ds.setNumber(newRow, columnid, parts[1]);
                            }
                            boolean testMode = "true".equals(ajaxResponse.getRequestParameter("testmode"));
                            this.logInfo("info message");
                            if (testMode) {
                                SapphireConnection sapphireConnection = new SapphireConnection();
                                sapphireConnection.setConnectionId(this.getConnectionId());
                                SequenceService seqService = new SequenceService(sapphireConnection);
                                seqService.generateKeys(sdcid, keycolid1, keyrule, ds, true);
                                String id = ds.getString(newRow, keycolid1);
                                ajaxResponse.addCallbackArgument("keyid1", id);
                                break block11;
                            }
                            ajaxResponse.setError("Test mode property not set to true - this is required and the only option supported");
                        }
                        catch (Exception e) {
                            ajaxResponse.setError("Failed to generate sequence. Reason: " + e.getMessage());
                        }
                    } else {
                        ajaxResponse.setError("Key rule property not defined for service!");
                    }
                } else {
                    ajaxResponse.setError("Key column 1 property not defined for service!");
                }
            } else {
                ajaxResponse.setError("SDC property not defined for service!");
            }
        }
        ajaxResponse.print();
    }
}

