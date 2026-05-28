/*
 * Decompiled with CFR 0.152.
 */
package sapphire.servlet;

import com.labvantage.sapphire.modules.sdms.collector.SDMSCollector;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppException;
import com.labvantage.sapphire.util.file.FileTransfer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class ExternalHandlerProcessor
implements ExternalAppConstants {
    private String token;
    private String sapphireControllerURL;

    public ExternalHandlerProcessor(String token, String sapphireControllerURL) {
        this.token = token;
        this.sapphireControllerURL = sapphireControllerURL;
    }

    public JSONObject sendCommandToLIMS(String command, JSONObject jsonRequest) throws SapphireException {
        return this.sendCommandToLIMS(null, command, jsonRequest);
    }

    public JSONObject sendCommandToLIMS(String processAs, String command, JSONObject jsonRequest) throws SapphireException {
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
            if (jsonResponse.has("_exception")) {
                throw new SapphireException(jsonResponse.getString("_exception"));
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send command to LIMS: " + e.getMessage(), e);
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
                throw new SapphireException("Bad response from sending command to LIMS: " + errortype + " : " + message);
            }
        }
        return jsonResponse == null ? new JSONObject() : jsonResponse;
    }

    public JSONObject sendFileCommandToLIMS(String command, Path file, JSONObject jsonRequest) throws SapphireException {
        return this.sendFileCommandToLIMS("", command, file, jsonRequest);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public JSONObject sendFileCommandToLIMS(String processAs, String command, Path file, JSONObject jsonRequest) throws SapphireException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        PrintWriter writer = null;
        JSONObject jsonResponse = new JSONObject();
        Object response = null;
        StringBuilder requestBody = new StringBuilder();
        boolean performHashing = jsonRequest.optString("performhash", "Y").equalsIgnoreCase("Y");
        try {
            conn = this.getHttpURLConnection(processAs);
            String boundary = "====TEST";
            String linefeed = "\r\n";
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("charset", StandardCharsets.UTF_8.name());
            conn.setRequestProperty("LVCommandType", "file");
            out = conn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
            HashMap<String, Object> formFields = new HashMap<String, Object>();
            formFields.put("filename", file.toFile().getName());
            formFields.put("command", command);
            Iterator keys = jsonRequest.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                String value = jsonRequest.getString(key);
                formFields.put(key, value);
            }
            MessageDigest messageDigest = null;
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
                    int bytesRead;
                    messageDigest = MessageDigest.getInstance(FileTransfer.getHashAlgorithmName(SDMSCollector.defaultHashingAlgorithm));
                    upload = new FileInputStream(file.toFile());
                    byte[] buffer = new byte[4096];
                    while ((bytesRead = upload.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        messageDigest.update(buffer, 0, bytesRead);
                        out.flush();
                        ((Writer)writer).flush();
                    }
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
                finally {
                    if (upload != null) {
                        try {
                            upload.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                ((Writer)writer).append(linefeed).flush();
                ((Writer)writer).append("--" + boundary + "--").append(linefeed);
            }
            finally {
                ((Writer)writer).close();
            }
            jsonResponse = this.getJsonObject(conn, jsonResponse);
            if (jsonResponse.has("_exception")) {
                throw new SapphireException(jsonResponse.getString("_exception"));
            }
            String filehashvalue = "";
            if (performHashing && messageDigest != null) {
                byte[] checksum = messageDigest.digest();
                BigInteger bigInt = new BigInteger(1, checksum);
                filehashvalue = "" + bigInt.longValue();
            }
            jsonResponse.put("filehashvalue", filehashvalue);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send command to LIMS: " + e.getMessage(), e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException iOException) {}
            }
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
                throw new SapphireException("Bad response from sending command to LIMS: " + errortype + " : " + message);
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

    public HttpURLConnection getHttpURLConnection(String processAs) throws Exception {
        URL url = new URL(this.sapphireControllerURL);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HostnameVerifier appserverValid = new HostnameVerifier(){

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            ((HttpsURLConnection)conn).setHostnameVerifier(appserverValid);
            ((HttpsURLConnection)conn).setSSLSocketFactory(sc.getSocketFactory());
        }
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

    public String getToken() {
        return this.token;
    }

    public String requestToken(String authorizationcode, String requestReason) throws SapphireException {
        return this.requestToken(authorizationcode, requestReason, "");
    }

    public String requestToken(String authorizationCode, String requestReason, String externalUserid) throws SapphireException {
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
        String token = jsonResponse.optString("token");
        return token;
    }

    public boolean isTokenActive() throws SapphireException {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", this.getToken());
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        JSONObject jsonResponse = this.sendCommandToLIMS("IsTokenActive", jsonRequest);
        return jsonResponse.optString("istokenactive").equals("Y");
    }

    public PropertyList sendCommandToLIMS(String command, PropertyList commandRequest) throws SapphireException {
        return this.sendCommandToLIMS(null, command, commandRequest);
    }

    public PropertyList sendCommandToLIMS(String processas, String command, PropertyList commandRequest) throws SapphireException {
        String propertylistresponse;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("propertylistrequest", commandRequest.toJSONString());
        }
        catch (JSONException e) {
            throw new SapphireException("Failed to handle commandrequest: " + e.getMessage(), e);
        }
        JSONObject jsonResponse = this.sendCommandToLIMS(processas, command, jsonRequest);
        PropertyList commandResponse = new PropertyList();
        if (jsonResponse != null && (propertylistresponse = jsonResponse.optString("propertylistresponse")).length() > 0) {
            try {
                commandResponse.setJSONString(propertylistresponse);
            }
            catch (JSONException e) {
                throw new SapphireException("Malformed json response: " + propertylistresponse + ": " + e.getMessage());
            }
        }
        return commandResponse;
    }

    public String getConnectionId(String username, String password, String database) throws ExternalAppException {
        JSONObject json = new JSONObject();
        JSONObject response = null;
        try {
            json.put("username", username);
            json.put("password", password);
            json.put("database", database);
            response = this.sendCommandToLIMS("GetConnectionId", json);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        String connectionid = response.optString("connectionid");
        return connectionid;
    }

    public String checkConnection(String connectionid) throws ExternalAppException, SapphireException {
        JSONObject json = new JSONObject();
        try {
            json.put("connectionid", connectionid);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        JSONObject response = this.sendCommandToLIMS("CheckConnection", json);
        return response.optString("CheckConnection");
    }

    public void clearConnection(String connectionid) throws ExternalAppException, SapphireException {
        JSONObject json = new JSONObject();
        try {
            json.put("connectionid", connectionid);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        JSONObject response = this.sendCommandToLIMS("ClearConnection", json);
    }

    public void sendDownloadFileCommandToLIMS(String command, JSONObject commandRequest, Path targetFile) throws SapphireException {
        this.sendDownloadFileCommandToLIMS(null, command, commandRequest, targetFile);
    }

    public void sendDownloadFileCommandToLIMS(String processAs, String command, JSONObject jsonRequest, Path targetFile) throws SapphireException {
        block8: {
            try {
                HttpURLConnection conn = null;
                OutputStream out = null;
                OutputStreamWriter writer = null;
                JSONObject jsonResponse = new JSONObject();
                Object response = null;
                try {
                    conn = this.getHttpURLConnection(processAs);
                    out = conn.getOutputStream();
                    writer = new OutputStreamWriter(out, "UTF-8");
                    jsonRequest.put("downloadrequest", "Y");
                    writer.write("command=" + URLEncoder.encode(command, "UTF-8") + "&");
                    writer.write("jsonrequest=" + URLEncoder.encode(jsonRequest.toString(), "UTF-8"));
                    ((Writer)writer).close();
                    out.close();
                    InputStreamReader isr = null;
                    BufferedReader rd = null;
                    int responseCode = conn.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        InputStream is = conn.getInputStream();
                        Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        is.close();
                        break block8;
                    }
                    System.out.println("Error detected. HTTP ResponseCode=" + responseCode);
                    InputStream is = conn.getErrorStream();
                    try {
                        String line;
                        isr = new InputStreamReader(is);
                        rd = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                        throw new SapphireException(sb.toString());
                    }
                    catch (Throwable throwable) {
                        rd.close();
                        isr.close();
                        throw throwable;
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to send command to LIMS: " + e.getMessage(), e);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to handle commandrequest: " + e.getMessage(), e);
            }
        }
    }
}

