/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.instrument;

import com.labvantage.sapphire.ejb.InstrumentManagerLocal;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class InstrumentRequest
extends BaseAjaxRequest {
    String logName = "Instrument Request:";
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        PropertyList props = new PropertyList();
        String instrumentid = request.getParameter("instrumentid");
        String commandid = request.getParameter("commandid");
        String dataitemkey = request.getParameter("dataitemkey");
        if (instrumentid != null && instrumentid.length() > 0) {
            props.setProperty("instrumentid", instrumentid);
            props.setProperty("commandid", commandid);
            if (request.getParameter("hostname") != null) {
                props.setProperty("hostname", request.getParameter("hostname"));
                props.setProperty("hostport", request.getParameter("hostport"));
            }
        } else if (request.getParameter("hostname") != null) {
            String hostName = request.getParameter("hostname");
            props.setProperty("hostname", hostName);
            props.setProperty("hostport", request.getParameter("hostport"));
            props.setProperty("commandcode", request.getParameter("commandcode"));
            props.setProperty("responseterminationstring", request.getParameter("responseterminationstring"));
            props.setProperty("instrumentmodelid", request.getParameter("instrumentmodelid"));
            props.setProperty("instrumenttypeid", request.getParameter("instrumenttypeid"));
        }
        if (dataitemkey != null && dataitemkey.length() > 0) {
            props.setProperty("dataitemkey", dataitemkey);
        }
        String connectionid = this.getRequestContext().getConnectionId();
        try {
            InstrumentManagerLocal instrumentManager = ServiceLocator.getInstance().getInstrumentManager();
            this.logInfo(this.logName + "Sending Command " + commandid);
            HashMap instrumentResponse = instrumentManager.executeCommand(connectionid, props);
            PropertyList result = new PropertyList((HashMap)instrumentResponse.get("result"));
            this.logInfo(this.logName + "Response received " + result);
            String parsingRule = props.getProperty("parsingrule");
            if (parsingRule.indexOf("$G:") != 0) {
                PropertyList pl = new PropertyList();
                pl.setPropertyList(parsingRule);
                PropertyList splitRule = pl.getPropertyListNotNull("splitrule");
                String decimalSeparator = splitRule.getProperty("decimalseparator", ".");
                String userDecimalSeparator = "" + FormatUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid())).getDecimalSeparator();
                if (!decimalSeparator.equals(userDecimalSeparator)) {
                    Pattern numberPattern = Pattern.compile("(((-|\\+)?[0-9]+(" + decimalSeparator + "[0-9]+)?)+(e|E)(-|\\+)[0-9]+)|((-|\\+)?[0-9]+(" + decimalSeparator + "[0-9]+)?)+");
                    for (String paramid : result.keySet()) {
                        String value = result.getProperty(paramid);
                        Matcher matcher = numberPattern.matcher(value);
                        String matched = "";
                        while (matcher.find()) {
                            matched = matcher.group();
                            String replaceMatched = StringUtil.replaceAll(matched, decimalSeparator, userDecimalSeparator);
                            value = StringUtil.replaceAll(value, matched, replaceMatched);
                        }
                        result.setProperty(paramid, value);
                        this.logInfo(this.logName + "Converted number to user locale " + paramid + "=" + result);
                    }
                }
            }
            result.setProperty("(Raw Response)", (String)instrumentResponse.get("response"));
            if (dataitemkey != null && dataitemkey.length() > 0) {
                result.setProperty("__dataitemkey", dataitemkey);
            }
            JSONObject jsonResponseObj = result.toJSONObject();
            jsonResponseObj.write(response.getWriter());
            this.logInfo(this.logName + "Response done" + jsonResponseObj);
        }
        catch (Exception se) {
            this.write("Failed to get instrument response:" + se.getMessage().substring(se.getMessage().lastIndexOf("Exception:") + 10));
            this.logError("Failed to get instrument response:" + se.getMessage(), se);
        }
    }
}

