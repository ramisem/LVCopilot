/*
 * Decompiled with CFR 0.152.
 */
package sapphire.servlet;

import com.labvantage.sapphire.servlet.rest.RestConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONObject;
import sapphire.SapphireException;

public class RestClient
implements RestConstants {
    private String restContext;
    private URL url;
    private String connectionid;
    private String tokenvalue = "";

    public RestClient(String restContext) {
        this.restContext = restContext.endsWith("/") ? restContext.substring(0, restContext.length() - 1) : restContext;
    }

    public String status(String databaseid) throws SapphireException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(this.restContext + "?databaseid=" + databaseid);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            if (code != 200) {
                throw new Exception("Failed to GET status. Reason: " + conn.getResponseMessage());
            }
            String string = this.readConnectionInput(conn);
            return string;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            conn.disconnect();
        }
    }

    public boolean connect(String databaseid, String username, String password) {
        try {
            this.connectionid = this.getConnection(databaseid, username, password);
            return this.connectionid.length() > 0;
        }
        catch (SapphireException e) {
            return false;
        }
    }

    public boolean connect(String tokenvalue) {
        this.tokenvalue = tokenvalue;
        return true;
    }

    public boolean isConnected() {
        return this.connectionid != null && this.connectionid.length() > 0 || this.tokenvalue.length() > 0;
    }

    public void disconnect() {
        try {
            this.clearConnection(this.connectionid);
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
    }

    public String getConnection() {
        return this.connectionid;
    }

    public String getConnection(String databaseid, String username, String password) throws SapphireException {
        try {
            JSONObject request = new JSONObject();
            request.put("databaseid", databaseid);
            request.put("username", username);
            request.put("password", password);
            JSONObject response = this.post("connections", request);
            JSONObject connections = response.getJSONObject("connections");
            return connections.getString("connectionid");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get connection. Reason: " + e.getMessage(), e);
        }
    }

    public void clearConnection(String connectionid) throws SapphireException {
        try {
            this.connectionid = connectionid;
            this.delete("connections/" + connectionid);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public JSONObject get(String resource) throws SapphireException {
        HttpURLConnection conn = null;
        try {
            resource = this.sanitze(resource);
            URL url = new URL(this.restContext + "/" + (resource != null && resource.length() > 0 ? resource : ""));
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json&v=1");
            conn.setRequestProperty("Content-Type", "application/json");
            if (this.isConnected()) {
                conn.setRequestProperty("Authorization", this.tokenvalue.length() > 0 ? "Token " + this.tokenvalue : "Token " + this.connectionid);
            }
            String response = this.readConnectionInput(conn);
            JSONObject jSONObject = resource != null && response.startsWith("{") ? new JSONObject(response) : new JSONObject();
            return jSONObject;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            conn.disconnect();
        }
    }

    public JSONObject post(String resource, JSONObject jsonParams) throws SapphireException {
        return (JSONObject)this.request(resource, "POST", null, jsonParams);
    }

    public JSONObject put(String resource, JSONObject jsonParams) throws SapphireException {
        return (JSONObject)this.request(resource, "PUT", null, jsonParams);
    }

    private Object request(String resource, String method, HashMap<String, String> hashParams, JSONObject jsonParams) throws SapphireException {
        HttpURLConnection conn = null;
        OutputStream out = null;
        Writer writer = null;
        try {
            resource = this.sanitze(resource);
            URL url = new URL(this.restContext + "/" + (resource != null && resource.length() > 0 ? resource : ""));
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(method);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Accept", "application/json&v=1");
            conn.setRequestProperty("Content-Type", jsonParams == null ? "application/x-www-form-urlencoded" : "application/json");
            if (this.isConnected()) {
                conn.setRequestProperty("Authorization", "Token " + (this.tokenvalue.length() > 0 ? this.tokenvalue : this.connectionid));
            }
            out = conn.getOutputStream();
            writer = new OutputStreamWriter(out, "UTF-8");
            if (hashParams != null && hashParams.size() > 0) {
                Iterator<String> iterator = hashParams.keySet().iterator();
                while (iterator.hasNext()) {
                    String paramid = iterator.next();
                    writer.write(paramid + "=" + URLEncoder.encode(hashParams.get(paramid), "UTF-8") + (iterator.hasNext() ? "&" : ""));
                }
            } else if (jsonParams != null) {
                writer.write(jsonParams.toString());
            }
            writer.close();
            out.close();
            String response = this.readConnectionInput(conn);
            JSONObject jSONObject = resource != null && response.startsWith("{") ? new JSONObject(response) : new JSONObject();
            return jSONObject;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            try {
                writer.close();
                out.close();
            }
            catch (Exception exception) {}
            conn.disconnect();
        }
    }

    public JSONObject delete(String resource) throws SapphireException {
        HttpURLConnection conn = null;
        try {
            int code;
            resource = this.sanitze(resource);
            URL url = new URL(this.restContext + "/" + (resource != null && resource.length() > 0 ? resource : ""));
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("DELETE");
            if (this.isConnected()) {
                conn.setRequestProperty("Authorization", "Token " + (this.tokenvalue.length() > 0 ? this.tokenvalue : this.connectionid));
            }
            if ((code = conn.getResponseCode()) != 200) {
                throw new Exception("Failed to DELETE resource '" + resource + "'. Reason: " + conn.getResponseMessage());
            }
            String response = this.readConnectionInput(conn);
            JSONObject jSONObject = resource != null && response.startsWith("{") ? new JSONObject(response) : new JSONObject();
            return jSONObject;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            conn.disconnect();
        }
    }

    private String sanitze(String input) throws UnsupportedEncodingException {
        if (input == null) {
            return null;
        }
        input = input.startsWith("/") ? input.substring(1) : input;
        input = input.endsWith("/") ? input.substring(0, input.length() - 1) : input;
        return input.replaceAll("\\|", "%7C").replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll(" ", "%20");
    }

    public static String getFieldsParam(String[] fields) {
        StringBuffer fieldParam = new StringBuffer();
        for (int i = 0; i < fields.length; ++i) {
            fieldParam.append(",").append(fields[i]);
        }
        return "fields=(" + RestClient.encode(fieldParam.substring(1)) + ")";
    }

    public static String encode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return input;
        }
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
}

