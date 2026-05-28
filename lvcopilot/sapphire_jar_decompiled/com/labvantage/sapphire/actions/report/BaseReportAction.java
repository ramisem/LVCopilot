/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class BaseReportAction
extends BaseAction {
    protected String getPrinter(String addressid, String addresstype) throws SapphireException {
        String printer = "";
        if (addressid.length() > 0 && addresstype.length() > 0) {
            try {
                this.database.createPreparedResultSet("GetPrinter", "SELECT printerid FROM address WHERE addressid = ? AND addresstype = ?", new Object[]{addressid, addresstype});
                if (this.database.getNext("GetPrinter")) {
                    printer = this.database.getString("GetPrinter", "printerid");
                }
                if (printer == null || printer.length() == 0) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Could not determine the report printer using addressid " + addressid + " and addresstype " + addresstype);
                }
            }
            catch (SapphireException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Error found looking up printer for addressid " + addressid + " and addresstype " + addresstype, e);
            }
            finally {
                this.database.closeResultSet("GetPrinter");
            }
        }
        return printer;
    }

    protected String getSMTPHost() throws SapphireException {
        try {
            String smtphost = ConfigService.getConfigProperty("com.labvantage.sapphire.server.smtphost");
            if (smtphost.length() == 0) {
                throw new SapphireException("PROCESSACTION_FAILED", "Failed to get SMTP host to email report");
            }
            return smtphost;
        }
        catch (ServiceException e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Failed to get SMTP host to email report", e);
        }
    }

    protected String buildQueryString(PropertyList properties) {
        StringBuffer queryString = new StringBuffer();
        for (String key : properties.keySet()) {
            queryString.append("&").append(key).append("=").append(HttpUtil.encodeURIComponent(properties.getProperty(key)));
        }
        return queryString.toString();
    }

    protected void processReportViaWebApp(String jspURL, String queryString) throws SapphireException {
        block6: {
            try {
                String message;
                int i;
                URL url = new URL(jspURL);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(queryString.getBytes());
                outputStream.flush();
                urlConnection.getContentType();
                urlConnection.connect();
                InputStream stream = urlConnection.getInputStream();
                BufferedInputStream in = new BufferedInputStream(stream);
                StringBuffer sb = new StringBuffer();
                byte[] b = new byte[1024];
                while ((i = in.read(b, 0, b.length)) != -1) {
                    sb.append(new String(b, 0, i));
                }
                in.close();
                String returnVal = sb.toString().trim();
                String ENDOFACTIONMARKER = "Action processing result rc=";
                int idx = returnVal.indexOf(ENDOFACTIONMARKER);
                if (idx == -1) break block6;
                String rcValue = returnVal.substring(idx += 28, idx + 7);
                try {
                    message = returnVal.substring(idx + 7);
                }
                catch (StringIndexOutOfBoundsException strExp) {
                    message = "";
                }
                if (!rcValue.equalsIgnoreCase("SUCCESS")) {
                    throw new SapphireException("PROCESSACTION_FAILED", message);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to process report using " + jspURL, e);
            }
        }
    }
}

