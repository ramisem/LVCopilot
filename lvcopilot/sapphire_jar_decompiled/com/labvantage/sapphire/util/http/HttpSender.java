/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.http;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.http.MultipartOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class HttpSender {
    static final String LABVANTAGE_CVS_ID = "$Revision: 83187 $";
    private boolean isConnected;
    private HttpURLConnection connection;
    private String multipartBoundary;

    public boolean doConnect(String url, String method) {
        return this.doConnect(url, method, false);
    }

    public boolean doConnect(String url, String method, boolean isMultiPart) {
        try {
            return this.doIntConnect(url, method, isMultiPart);
        }
        catch (Exception e) {
            Trace.logError(this.getClass().getName(), (Object)e.getMessage(), e);
            return false;
        }
    }

    public void addRequestHeader(String key, String value) {
        if (this.connection != null && this.isConnected) {
            this.connection.setRequestProperty(key, value);
        }
    }

    private boolean doIntConnect(String url, String method, boolean isMultiPart) throws Exception {
        boolean theReturn = false;
        if (url.length() > 0) {
            Runtime.getRuntime().gc();
            URL address = new URL(null, url);
            theReturn = this.doConnect(address, method, isMultiPart);
        } else {
            throw new Exception("No URL provided.");
        }
        return theReturn;
    }

    public boolean doConnect(URL address, String method) {
        return this.doConnect(address, method, false);
    }

    public boolean doConnect(URL address, String method, boolean isMultiPart) {
        boolean theReturn = false;
        Runtime.getRuntime().gc();
        try {
            URLConnection urlConnection = address.openConnection();
            this.connection = (HttpURLConnection)urlConnection;
            this.connection.setDoOutput(true);
            this.connection.setDoInput(true);
            this.connection.setRequestMethod(method.toUpperCase());
            if (isMultiPart) {
                Trace.logDebug(this.getClass().getName(), "Setting up for multipart...");
                this.connection.setUseCaches(false);
                this.connection.setDefaultUseCaches(false);
                this.multipartBoundary = MultipartOutputStream.createBoundary();
                this.connection.setRequestProperty("Accept", "*/*");
                this.connection.setRequestProperty("Content-Type", MultipartOutputStream.getContentType(this.multipartBoundary));
            }
            Trace.logDebug(this.getClass().getName(), "Connected.");
            this.isConnected = true;
            theReturn = true;
        }
        catch (IOException eException2) {
            Trace.logError(this.getClass().getName(), eException2.getMessage());
        }
        return theReturn;
    }

    public void doDisconnect() {
        this.connection.disconnect();
        this.isConnected = false;
        Trace.logDebug(this.getClass().getName(), "Disconnected");
    }

    public void doSend(HashMap multipartDataMap) {
        try {
            this.doIntSend(multipartDataMap);
        }
        catch (Exception e) {
            Trace.logError(this.getClass().getName(), (Object)e.getMessage(), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doIntSend(HashMap multipartDataMap) throws Exception {
        block11: {
            if (this.isConnected) {
                if (this.multipartBoundary != null && this.multipartBoundary.length() > 0) {
                    try (MultipartOutputStream output = new MultipartOutputStream(this.connection.getOutputStream(), this.multipartBoundary);){
                        for (String field : multipartDataMap.keySet()) {
                            Object value = multipartDataMap.get(field);
                            if (value instanceof String) {
                                Trace.logDebug(this.getClass().getName(), "writeField " + field + " value " + value);
                                output.writeField(field, (String)value);
                                continue;
                            }
                            if (value instanceof File) {
                                Trace.logDebug(this.getClass().getName(), "File found for field '" + field + "'.");
                                output.writeFile(field, "", (File)value);
                                continue;
                            }
                            if (value instanceof InputStream) {
                                Trace.logDebug(this.getClass().getName(), "File found for field '" + field + "'.");
                                output.writeFile(field, "", "", (InputStream)value);
                                continue;
                            }
                            Trace.logWarn(this.getClass().getName(), "Value found that is not a string or file.");
                        }
                        break block11;
                    }
                    catch (Exception e) {
                        throw new Exception("Could not write MultiPartFormOutputStream.", e);
                    }
                }
                throw new Exception("Connection not set up for multipart.");
            }
            throw new Exception("Not connected!");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String connectAndSendMultipart(String url, HashMap multipartDataMap) throws Exception {
        this.doIntConnect(url, "POST", true);
        try {
            this.doIntSend(multipartDataMap);
            String string = this.getResponse(false, false);
            return string;
        }
        finally {
            this.doDisconnect();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doSend(String text) {
        if (this.isConnected) {
            try (OutputStream oStream = this.connection.getOutputStream();
                 PrintStream pPrint = new PrintStream(oStream);){
                pPrint.print(text);
                pPrint.flush();
            }
            catch (IOException eException) {
                Trace.logError(this.getClass().getName(), eException.getMessage());
            }
        } else {
            Trace.logError(this.getClass().getName(), "Not Connected!");
        }
    }

    public String getResponseType() {
        String out;
        try {
            out = this.connection.getContentType();
        }
        catch (Exception e) {
            Trace.logError(this.getClass().getName(), "Could not obtain content type.");
            out = "";
        }
        return out;
    }

    public int getResponseCode() {
        int out;
        try {
            out = this.connection.getResponseCode();
        }
        catch (Exception e) {
            Trace.logError(this.getClass().getName(), "Could not obtain response code.");
            out = 0;
        }
        return out;
    }

    public InputStream getResponseStream() {
        InputStream stream = null;
        try {
            stream = this.connection.getInputStream();
        }
        catch (Exception e) {
            Trace.logError(this.getClass().getName(), e.getMessage());
        }
        return stream;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getResponse(boolean includeHeader, boolean includeLineNos) {
        StringBuffer out = new StringBuffer();
        try (InputStream inputStream = this.connection.getInputStream();){
            int responseCode = this.connection.getResponseCode();
            if (includeHeader) {
                out.append("\nResponse Code: ").append(responseCode);
                out.append("\nResponse Msg: ").append(this.connection.getResponseMessage());
                out.append("\nType: ").append(this.connection.getContentType());
            }
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader);){
                String line;
                int cout = 1;
                while ((line = bufferedReader.readLine()) != null) {
                    if (includeLineNos) {
                        out.append("\nLine ").append(cout).append(": ").append(line);
                    } else {
                        out.append(line);
                    }
                    ++cout;
                }
            }
        }
        catch (IOException eException) {
            Trace.logError(this.getClass().getName(), "Error retrieving response...." + eException.getMessage());
        }
        if (out.length() > 0) {
            return out.toString();
        }
        return "";
    }
}

