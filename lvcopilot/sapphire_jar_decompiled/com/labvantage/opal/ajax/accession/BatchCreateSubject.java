/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.AddSDIAlias;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BatchCreateSubject
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String sampleid = StringUtil.replaceAll(ajaxResponse.getRequestParameter("sampleid", ""), "%3B", ";");
            JSONObject subjectprop = new JSONObject(ajaxResponse.getRequestParameter("subjectprops"));
            String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct samplefamilyid from s_sample where s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
            if (ds.size() > 0) {
                String subjectalias = "";
                PropertyList props = new PropertyList();
                Iterator keys = subjectprop.keys();
                while (keys.hasNext()) {
                    String key = (String)keys.next();
                    String value = subjectprop.getString(key);
                    if (!"subjectalias".equals(key)) {
                        props.setProperty(key, value);
                        continue;
                    }
                    subjectalias = value;
                }
                props.setProperty("sdcid", "LV_Subject");
                props.setProperty("copies", String.valueOf(ds.size()));
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                String subjectid = props.getProperty("newkeyid1");
                if (OpalUtil.isNotEmpty(subjectid)) {
                    if (subjectalias != null && subjectalias.trim().length() > 0) {
                        String[] aliasArray;
                        for (String alias : aliasArray = StringUtil.split(subjectalias, "|")) {
                            String[] s = StringUtil.split(alias, ";");
                            String aliasid = s[0];
                            String aliastype = s[1];
                            if (aliasid == null || aliasid.length() <= 0 || aliastype == null || aliastype.length() <= 0) continue;
                            props.clear();
                            props.setProperty("sdcid", "LV_Subject");
                            props.setProperty("keyid1", subjectid);
                            props.setProperty("aliasid", aliasid);
                            props.setProperty("aliastype", aliastype);
                            this.getActionProcessor().processActionClass(AddSDIAlias.class.getName(), props);
                        }
                    }
                    props.clear();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", ds.getColumnValues("samplefamilyid", ";"));
                    props.setProperty("subjectid", subjectid);
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
        }
        catch (JSONException | SapphireException e) {
            message = this.getTranslationProcessor().translate("Error while adding Subjects");
            message = message + "<br>" + e.getMessage();
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

