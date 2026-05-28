/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childbox;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetBoxTransferMethod
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        String sourceboxid;
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String boxid = sourceboxid = ajaxResponse.getRequestParameter("boxid", "");
        JSONObject jsonObject = new JSONObject();
        if (boxid.contains(";")) {
            boxid = boxid.substring(0, boxid.indexOf(";"));
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet("select atm.arraytransfermethodid, atm.arraytransfermethodversionid, atm.targetvolume, atm.targetvolumeunits, atm.targetarraytypeid, atm.numtargetarrays, atm.sourcearraytypeid, atm.numsourcearrays, atm.targetconcentration, atm.targetconcentrationunits, su.arraylayoutid, su.arraylayoutversionid, atm.sampletypeid from arraytransfermethod atm, storageunit su where atm.sourcearraytypeid = {fn concat('ASL ', su.storageunittype)} and su.linksdcid = 'LV_Box' and su.linkkeyid1 = ? order by atm.arraytransfermethodid", (Object[])new String[]{boxid})) != null && ds.size() > 0) {
            try {
                JSONArray transfermethods = new JSONArray();
                for (int i = 0; i < ds.size(); ++i) {
                    String sourceboxtype;
                    String arraytransfermethodid = ds.getString(i, "arraytransfermethodid", "");
                    if (arraytransfermethodid.length() <= 0) continue;
                    String targetboxtype = ds.getString(i, "targetarraytypeid", "");
                    if (targetboxtype.startsWith("ASL ")) {
                        targetboxtype = targetboxtype.substring(4);
                    }
                    if ((sourceboxtype = ds.getString(i, "sourcearraytypeid", "")).startsWith("ASL ")) {
                        sourceboxtype = sourceboxtype.substring(4);
                    }
                    int numchildboxes = 1;
                    int numsourcearrays = ds.getInt(i, "numsourcearrays", 1);
                    int numtargetarrays = ds.getInt(i, "numtargetarrays", 1);
                    int numparentboxes = StringUtil.split(sourceboxid, ";").length;
                    if (numsourcearrays == numtargetarrays) {
                        numchildboxes = numparentboxes;
                    } else if (numtargetarrays > numsourcearrays) {
                        numchildboxes = numparentboxes * numtargetarrays;
                    } else if (numsourcearrays > numtargetarrays) {
                        numchildboxes = numparentboxes / numsourcearrays + numparentboxes % numsourcearrays;
                    }
                    JSONObject transfermethod = new JSONObject();
                    transfermethod.put("arraytransfermethodid", arraytransfermethodid);
                    transfermethod.put("arraytransfermethodversionid", ds.getString(i, "arraytransfermethodversionid", ""));
                    transfermethod.put("sampletypeid", ds.getString(i, "sampletypeid", ""));
                    transfermethod.put("sourceboxtype", sourceboxtype);
                    transfermethod.put("numsourceboxtype", ds.getValue(i, "numsourcearrays", ""));
                    transfermethod.put("targetboxtype", targetboxtype);
                    transfermethod.put("numtargetboxtype", ds.getValue(i, "numtargetarrays", ""));
                    transfermethod.put("numchildboxes", String.valueOf(numchildboxes));
                    transfermethod.put("targetvolume", ds.getValue(i, "targetvolume"));
                    transfermethod.put("targetvolumeunits", ds.getValue(i, "targetvolumeunits"));
                    transfermethod.put("targetconcentration", ds.getValue(i, "targetconcentration"));
                    transfermethod.put("targetconcentrationunits", ds.getValue(i, "targetconcentrationunits"));
                    transfermethod.put("havereagents", ds.getInt(i, "reagentcount", 0) > 0 ? "Y" : "N");
                    transfermethod.put("haveinstruments", ds.getInt(i, "instrumentcount", 0) > 0 ? "Y" : "N");
                    transfermethod.put("haveservices", ds.getInt(i, "instrumentcount", 0) > 0 ? "Y" : "N");
                    transfermethods.put(transfermethod);
                }
                jsonObject.put("transfermethods", transfermethods);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            message = this.getTranslationProcessor().translate("No Transfer Method found for selected parent Box") + " (" + sourceboxid + ")";
        }
        String boxsampletypeid = "";
        SafeSQL safeSQL = new SafeSQL();
        DataSet sampleTypeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT distinct(s.sampletypeid) sampletypeid FROM s_sample s, trackitem t, storageunit su WHERE s.sampletypeid IS NOT NULL      AND t.linksdcid = 'Sample'      AND t.linkkeyid1 = s.s_sampleid      AND t.currentstorageunitid = su.storageunitid      AND su.parentid IN ( SELECT b.storageunitid                           FROM storageunit b                           WHERE b.LINKSDCID = 'LV_Box' AND b.linkkeyid1 IN ( " + safeSQL.addIn(sourceboxid, ";") + " ) )", safeSQL.getValues());
        if (sampleTypeDS != null && sampleTypeDS.size() == 1) {
            boxsampletypeid = sampleTypeDS.getString(0, "sampletypeid", "");
        }
        try {
            jsonObject.put("boxsampletypeid", boxsampletypeid);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("data", jsonObject.toString());
        ajaxResponse.print();
    }
}

