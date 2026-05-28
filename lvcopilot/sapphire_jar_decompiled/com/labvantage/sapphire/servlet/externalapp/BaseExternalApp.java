/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.externalapp;

import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class BaseExternalApp
implements ExternalAppConstants {
    private static final String PROPERTY_TOKEN = "token";
    private static Properties bootstrapProps = new Properties();
    private static String bootstrapPropsFile;
    protected static String sapphireControllerURL;
    private static boolean createBootstrapIfMissing;

    public BaseExternalApp(String bootstrapPropsFile) throws SapphireException {
        this.loadBootstrapProps(bootstrapPropsFile);
        sapphireControllerURL = this.getBootstrapProperty("url");
        if (sapphireControllerURL.length() == 0) {
            throw new SapphireException("Could not find a url property in " + bootstrapPropsFile + ".");
        }
    }

    public static void setCreateBootstrapIfMissing(String sapphireControllerURL) {
        createBootstrapIfMissing = true;
        BaseExternalApp.sapphireControllerURL = sapphireControllerURL;
    }

    private void loadBootstrapProps(String bootstrapPropsFile) throws SapphireException {
        BaseExternalApp.bootstrapPropsFile = bootstrapPropsFile;
        File file = new File(bootstrapPropsFile);
        if (!file.exists() && createBootstrapIfMissing) {
            bootstrapProps.put("url", sapphireControllerURL);
            this.saveBootstrapProps(bootstrapPropsFile);
            System.out.println("Unable to locate bootstrap file " + bootstrapPropsFile + ". Skeleton file created. You will need to enter information into this file.");
        }
        if (file.exists()) {
            FileInputStream input = null;
            try {
                input = new FileInputStream(file);
                bootstrapProps.load(input);
            }
            catch (Exception ex) {
                throw new SapphireException("Unable to bootstrap: " + ex.getMessage(), ex);
            }
            finally {
                if (input != null) {
                    try {
                        input.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        throw new SapphireException("Unable to locate bootstrap file " + bootstrapPropsFile);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void saveBootstrapProps(String bootstrapPropsFile) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(bootstrapPropsFile);
            bootstrapProps.store(output, "Adding new token");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public PropertyList sendCommandToLIMS(String command, PropertyList commandRequest) throws ExternalAppException {
        return this.sendCommandToLIMS(null, command, commandRequest);
    }

    public PropertyList sendCommandToLIMS(String processas, String command, PropertyList commandRequest) throws ExternalAppException {
        String propertylistresponse;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("propertylistrequest", commandRequest.toJSONString());
        }
        catch (JSONException e) {
            throw new ExternalAppException(100, "", "Failed to handle commandrequest");
        }
        JSONObject jsonResponse = this.sendCommandToLIMS(processas, command, jsonRequest);
        PropertyList commandResponse = new PropertyList();
        if (jsonResponse != null && (propertylistresponse = jsonResponse.optString("propertylistresponse")).length() > 0) {
            try {
                commandResponse.setJSONString(propertylistresponse);
            }
            catch (JSONException e) {
                throw new ExternalAppException(100, "", "Malformed json response: " + propertylistresponse + ": " + e.getMessage());
            }
        }
        return commandResponse;
    }

    public JSONObject sendCommandToLIMS(String command, JSONObject jsonRequest) throws ExternalAppException {
        return this.sendCommandToLIMS(null, command, jsonRequest);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JSONObject sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest) throws ExternalAppException, SapphireException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        PrintWriter writer = null;
        JSONObject jsonResponse = new JSONObject();
        Object response = null;
        StringBuilder requestBody = new StringBuilder();
        try {
            conn = this.getHttpURLConnection(processAs);
            String boundary = "====TEST";
            String linefeed = "\r\n";
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("charset", StandardCharsets.UTF_8.name());
            conn.setRequestProperty("LVCommandType", "file");
            out = conn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            HashMap<Object, String> formFields = new HashMap<Object, String>();
            formFields.put("filename", file.toFile().getName());
            formFields.put("command", command);
            Iterator keys = jsonRequest.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                String value = jsonRequest.getString(key);
                formFields.put(key, value);
            }
            try {
                for (String key : formFields.keySet()) {
                    ((Writer)writer).append("--" + boundary).append(linefeed);
                    ((Writer)writer).append("Content-Disposition: form-data; name=\"" + key + "\"").append(linefeed);
                    ((Writer)writer).append("Content-Type: text/plain; charset=" + StandardCharsets.UTF_8.name()).append(linefeed);
                    ((Writer)writer).append(linefeed);
                    ((Writer)writer).append((CharSequence)formFields.get(key)).append(linefeed);
                    ((Writer)writer).flush();
                }
                String multipartfilefieldname = "file";
                String fileName = formFields.containsKey("filename") ? (String)formFields.get("filename") : "noname";
                ((Writer)writer).append("--" + boundary).append(linefeed);
                ((Writer)writer).append("Content-Disposition: form-data; name=\"" + multipartfilefieldname + "\"; filename=\"" + fileName + "\"").append(linefeed);
                String contenttype = URLConnection.guessContentTypeFromName(fileName);
                if (contenttype == null) {
                    try {
                        contenttype = Files.probeContentType(Paths.get(fileName, new String[0]));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                ((Writer)writer).append("Content-Type: " + (contenttype == null ? "text/plain; charset=" + StandardCharsets.UTF_8.name() : contenttype)).append(linefeed);
                ((Writer)writer).append("Content-Transfer-Encoding: binary").append(linefeed);
                ((Writer)writer).append(linefeed);
                ((Writer)writer).flush();
                FileInputStream upload = null;
                try {
                    upload = new FileInputStream(file.toFile());
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = upload.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                    upload.close();
                    ((Writer)writer).flush();
                }
                catch (Exception e) {
                    if (upload != null) {
                        try {
                            upload.close();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                    throw new SapphireException(e);
                }
                ((Writer)writer).append(linefeed).flush();
                ((Writer)writer).append("--" + boundary + "--").append(linefeed);
            }
            finally {
                ((Writer)writer).close();
            }
            out.close();
            jsonResponse = this.getJsonObject(conn, jsonResponse);
        }
        catch (Exception e) {
            throw new ExternalAppException(400, "Unexpected server error", e.getMessage());
        }
        if (conn != null) {
            int code = 500;
            try {
                code = conn.getResponseCode();
            }
            catch (IOException linefeed) {
                // empty catch block
            }
            if (code != 200) {
                String errortype = jsonResponse.optString("error");
                String message = jsonResponse.optString("message");
                throw new ExternalAppException(code, errortype, message);
            }
        }
        return jsonResponse == null ? new JSONObject() : jsonResponse;
    }

    public JSONObject sendCommandToLIMS(String processAs, String command, JSONObject jsonRequest) throws ExternalAppException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        OutputStreamWriter writer = null;
        JSONObject jsonResponse = new JSONObject();
        Object response = null;
        try {
            conn = this.getHttpURLConnection(processAs);
            out = conn.getOutputStream();
            writer = new OutputStreamWriter(out, "UTF-8");
            writer.write("command=" + URLEncoder.encode(command, "UTF-8") + "&");
            writer.write("jsonrequest=" + URLEncoder.encode(jsonRequest.toString(), "UTF-8"));
            ((Writer)writer).close();
            out.close();
            jsonResponse = this.getJsonObject(conn, jsonResponse);
        }
        catch (Exception e) {
            throw new ExternalAppException(400, "Unexpected server error", e.getMessage());
        }
        if (conn != null) {
            int code = 500;
            try {
                code = conn.getResponseCode();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (code != 200) {
                String errortype = jsonResponse.optString("error");
                String message = jsonResponse.optString("message");
                throw new ExternalAppException(code, errortype, message);
            }
        }
        return jsonResponse == null ? new JSONObject() : jsonResponse;
    }

    public JSONObject getJsonObject(HttpURLConnection conn, JSONObject jsonResponse) throws IOException, SapphireException {
        String response = this.readConnectionInput(conn);
        if (response != null && response.length() > 0) {
            if (response.startsWith("{")) {
                try {
                    jsonResponse = new JSONObject(response);
                }
                catch (JSONException e) {
                    throw new SapphireException("Malformed json response: " + response + ": " + e.getMessage());
                }
            } else {
                if (response.contains("HTTP Status")) {
                    int pos = response.indexOf("HTTP Status");
                    throw new SapphireException(response.substring(pos, pos + 15));
                }
                throw new SapphireException("Unknown response from the server.");
            }
        }
        return jsonResponse;
    }

    public HttpURLConnection getHttpURLConnection(String processAs) throws IOException {
        URL url = new URL(sapphireControllerURL);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Accept", "application/json&v=1");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        if (this.getToken().length() > 0) {
            conn.setRequestProperty("Authorization", "Token " + this.getToken());
        }
        if (processAs != null && processAs.length() > 0) {
            conn.setRequestProperty("ProcessAs", processAs);
        }
        return conn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String readConnectionInput(HttpURLConnection conn) throws IOException {
        InputStreamReader isr = null;
        BufferedReader rd = null;
        InputStream is = conn.getResponseCode() >= 200 && conn.getResponseCode() < 300 ? conn.getInputStream() : conn.getErrorStream();
        try {
            String line;
            isr = new InputStreamReader(is);
            rd = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            String string = sb.toString();
            return string;
        }
        finally {
            rd.close();
            isr.close();
        }
    }

    protected String getBootstrapProperty(String propertyid) {
        return this.getBootstrapProperty(propertyid, "");
    }

    protected String getBootstrapProperty(String propertyid, String defaultValue) {
        String value = (String)bootstrapProps.get(propertyid);
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    protected void setBootstrapProperty(String propertyid, String value) {
        bootstrapProps.put(propertyid, value);
    }

    protected boolean hasToken() {
        return this.getBootstrapProperty(PROPERTY_TOKEN).length() > 0;
    }

    protected String getToken() {
        return this.getBootstrapProperty(PROPERTY_TOKEN);
    }

    public String requestToken(String authorizationcode, String requestReason) throws ExternalAppException {
        return this.requestToken(authorizationcode, requestReason, "");
    }

    public String requestToken(String authorizationCode, String requestReason, String externalUserid) throws ExternalAppException {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("authorizationcode", authorizationCode);
            jsonRequest.put("reason", requestReason);
            jsonRequest.put("externaluserid", externalUserid);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        JSONObject jsonResponse = this.sendCommandToLIMS("RequestToken", jsonRequest);
        String token = jsonResponse.optString(PROPERTY_TOKEN);
        this.setBootstrapProperty(PROPERTY_TOKEN, token);
        this.saveBootstrapProps(bootstrapPropsFile);
        return token;
    }

    public boolean isTokenActive() throws ExternalAppException {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put(PROPERTY_TOKEN, this.getToken());
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        JSONObject jsonResponse = this.sendCommandToLIMS("IsTokenActive", jsonRequest);
        return jsonResponse.optString("istokenactive").equals("Y");
    }

    static {
        sapphireControllerURL = "";
        createBootstrapIfMissing = false;
    }
}

