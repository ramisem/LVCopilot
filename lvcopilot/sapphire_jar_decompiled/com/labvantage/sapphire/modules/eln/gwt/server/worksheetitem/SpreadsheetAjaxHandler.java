/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;

public class SpreadsheetAjaxHandler
extends BaseAjaxRequest
implements ELNConstants,
CacheNames {
    @Override
    public boolean acceptContentType(String contentType) {
        return contentType == null || contentType.equalsIgnoreCase("application/x-www-form-urlencoded") || contentType.equalsIgnoreCase("application/json") || contentType.equalsIgnoreCase("text/javascript");
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String ajaxCommand = request.getParameter("ajaxcommand");
        if ("loadspreadsheettranslations".equals(ajaxCommand)) {
            Throwable throwable;
            StringBuilder trans;
            block50: {
                TranslationProcessor tp = this.getTranslationProcessor();
                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                String languageid = connectionInfo.getLanguage();
                if (languageid != null && languageid.length() > 0) {
                    trans = (StringBuilder)CacheUtil.get(connectionInfo.getDatabaseId(), "TranslationELNSpreadsheet", languageid);
                    if (trans == null || trans.length() <= 2) {
                        trans = new StringBuilder();
                        try {
                            throwable = null;
                            try (InputStream inputStream = this.getClass().getResourceAsStream("/com/labvantage/sapphire/modules/eln/gwt/server/worksheetitem/spreadsheet_transmaster.json");){
                                if (inputStream != null) {
                                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));){
                                        String line;
                                        while ((line = bufferedReader.readLine()) != null) {
                                            int pos1 = line.indexOf(":");
                                            int pos2 = line.indexOf("\"");
                                            if (pos1 > 0 && pos2 > 0 && pos1 < pos2) {
                                                int pos3 = line.lastIndexOf("\"");
                                                String sub = line.substring(pos2 + 1, pos3);
                                                String subtrans = tp.translate(sub);
                                                line = line.substring(0, pos2 + 1) + subtrans + line.substring(pos3);
                                            }
                                            trans.append(line);
                                        }
                                    }
                                    inputStream.close();
                                    try {
                                        new JSONObject(trans.toString());
                                        CacheUtil.put(connectionInfo.getDatabaseId(), "TranslationELNSpreadsheet", languageid, trans);
                                    }
                                    catch (JSONException e) {
                                        this.logger.error("Failed to build spreadsheet_translation: " + e.getMessage());
                                        trans = new StringBuilder("{}");
                                    }
                                    break block50;
                                }
                                trans = new StringBuilder("{}");
                            }
                            catch (Throwable e) {
                                throwable = e;
                                throw e;
                            }
                        }
                        catch (IOException e) {
                            this.logger.error("Unable to locate translation file");
                            trans = new StringBuilder("{}");
                        }
                    }
                } else {
                    trans = new StringBuilder("{}");
                }
            }
            try {
                throwable = null;
                try (ServletOutputStream outputStream = response.getOutputStream();){
                    response.setHeader("Cache-Control", "max-age=2592000");
                    response.setDateHeader("Last-Modified", System.currentTimeMillis() - 10000000000L);
                    response.setHeader("Pragma", "");
                    response.setContentType("text/javascript");
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter((OutputStream)outputStream));
                    bw.append("try{spreadsheet_translation = ");
                    bw.append(trans);
                    bw.append(";");
                    bw.append("} catch(e){sapphire.alert( e );} ");
                    bw.flush();
                    bw.close();
                }
                catch (Throwable throwable2) {
                    throwable = throwable2;
                    throw throwable2;
                }
            }
            catch (IOException e) {
                throw new ServletException("Failed to translate spreadsheet toolbar", (Throwable)e);
            }
        }
    }

    public void translate(TranslationProcessor tp, JSONObject jso) throws JSONException {
        Iterator keys = jso.keys();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            JSONObject o = jso.optJSONObject(key);
            if (o == null) continue;
            if (o instanceof String) {
                jso.put(key, tp.translate((String)((Object)o)));
                continue;
            }
            if (!(o instanceof JSONObject)) continue;
            this.translate(tp, o);
        }
    }
}

