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

import com.labvantage.sapphire.Trace;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;

public class ClientTranslation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public boolean acceptContentType(String contentType) {
        return contentType == null || contentType.equalsIgnoreCase("application/x-www-form-urlencoded") || contentType.equalsIgnoreCase("application/json");
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String update = ajaxResponse.getRequestParameter("update");
        if (update != null && update.length() > 0) {
            ClientTranslation.saveToTransmasterTemp(update, this.getQueryProcessor(), this.getTranslationProcessor());
            ajaxResponse.setCallback("sapphire.translate");
            ajaxResponse.addCallbackArgument("hello", "");
            ajaxResponse.print();
        } else {
            response.setHeader("Cache-Control", "max-age=2592000");
            response.setDateHeader("Last-Modified", System.currentTimeMillis() - 10000000000L);
            response.setHeader("Pragma", "");
            response.setContentType("text/javascript");
            String languageid = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getLanguage();
            String translationJSON = this.generateJSTranslations(languageid);
            this.write(translationJSON);
            Trace.logInfo("***********Client JS Translation Response");
        }
    }

    public String generateJSTranslations(String languageid) {
        DataSet translanguageDs = this.getQueryProcessor().getPreparedSqlDataSet("select tm.textid, tl.transtext from transmaster tm, translanguage tl where tm.transmasterid=tl.transmasterid and tm.clientsideflag='Y' and tl.languageid=? order by textid", new Object[]{languageid});
        return ClientTranslation.generateJSTranslations(languageid, translanguageDs, "Y".equals(this.getConfigurationProcessor().getProfileProperty("showtranslations")));
    }

    public static String generateJSTranslations(String languageid, DataSet translanguageDs, boolean showtranslations) {
        StringBuffer sb;
        block9: {
            sb = new StringBuffer();
            if (languageid != null && languageid.length() > 0 && translanguageDs != null) {
                try {
                    for (int i = 0; i < translanguageDs.getRowCount(); ++i) {
                        if (i == 0) {
                            sb.append("\nvar translations = {");
                        }
                        sb.append("\n\"" + SafeHTML.encodeForJavaScript(translanguageDs.getValue(i, "textid")) + "\":\"" + SafeHTML.encodeForJavaScript(translanguageDs.getValue(i, "transtext")) + "\"");
                        if (i == translanguageDs.getRowCount() - 1) continue;
                        sb.append(",");
                    }
                    if (translanguageDs.getRowCount() > 0) {
                        sb.append("\n}");
                        break block9;
                    }
                    sb.append("\nvar translations = {};\n");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append("\nvar translations = {};\n");
            }
        }
        if (showtranslations) {
            sb.append("\nvar showtranslations=true;\n");
        } else {
            sb.append("\nvar showtranslations=false;\n");
        }
        return sb.toString();
    }

    public static void saveToTransmasterTemp(String textidlist, QueryProcessor queryProcessor, TranslationProcessor translationProcessor) {
        String[] textids = StringUtil.split(textidlist, "{{");
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < textids.length; ++i) {
            if (set.contains(textids[i])) continue;
            translationProcessor.translate(textids[i]);
            set.add(textids[i]);
            queryProcessor.execPreparedUpdate("UPDATE transmaster set clientsideflag='Y' WHERE textid=? AND texttype='W'", new Object[]{textids[i]});
            queryProcessor.execPreparedUpdate("UPDATE transmastertemp set clientsideflag='Y' WHERE textid=? AND texttype='W'", new Object[]{textids[i]});
        }
    }
}

