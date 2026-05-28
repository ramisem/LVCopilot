/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server.command;

import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.pageelements.gwt.shared.CommandConstants;
import com.labvantage.sapphire.util.json.JSONUtil;
import com.labvantage.sapphire.util.logger.LogUtil;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;

public class CommandResponse
implements CommandConstants {
    private String status = "ok";
    private String statusMessage = "";
    private Throwable exception;
    private HashMap<String, Object> responseData = new HashMap();

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(String status, String statusMessage) {
        this.status = status;
        this.statusMessage = statusMessage;
        if (status.equals("fail")) {
            this.addErrorHandler(statusMessage);
        }
    }

    public void setStatusFail(String statusMessage) {
        this.status = "fail";
        this.statusMessage = statusMessage;
        this.addErrorHandler(statusMessage);
    }

    public void setStatusFail(String statusMessage, Throwable e) {
        this.status = "fail";
        this.statusMessage = statusMessage;
        this.exception = e;
        this.addErrorHandler(statusMessage);
    }

    public void addErrorHandler(ErrorHandler errorHandler) {
        this.set("ERRORHANDLER", errorHandler);
    }

    public void addErrorHandler(String errorMsg) {
        ErrorHandler errorHandler = (ErrorHandler)this.responseData.get("ERRORHANDLER");
        if (errorHandler == null) {
            errorHandler = new ErrorHandler();
            this.responseData.put("ERRORHANDLER", errorHandler);
        }
        errorHandler.add(errorHandler.createErrorDetail("", "", "", "FAILURE", errorMsg));
    }

    public void set(String name, String value) {
        this.responseData.put(name, value);
    }

    public void set(String name, JSONable JSONable2) {
        this.responseData.put(name, JSONable2);
    }

    public boolean containsKey(String name) {
        return this.responseData.containsKey(name);
    }

    public Object get(String name) {
        return this.responseData.get(name);
    }

    public String getString(String name) {
        return (String)this.responseData.get(name);
    }

    public String getStatus() {
        return this.status;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public boolean hasException() {
        return this.exception != null;
    }

    public Throwable getException() {
        return this.exception;
    }

    public String toJSONString() {
        JSONObject job = new JSONObject();
        try {
            job.put("status", this.status);
            job.put("statusmessage", this.statusMessage);
            job.put("exceptionmessage", this.exception != null ? LogUtil.getStackTraceMessages(this.exception, "<br/>", true, true) : "");
            JSONObject jsonData = new JSONObject();
            for (String name : this.responseData.keySet()) {
                Object value = this.responseData.get(name);
                if (value instanceof DataSet) {
                    jsonData.put(name, JSONUtil.toJSONObject((DataSet)value, false).toString());
                } else if (value instanceof JSONable) {
                    jsonData.put(name, ((JSONable)value).toJSONString());
                } else if (value instanceof String) {
                    jsonData.put(name, (String)value);
                }
                jsonData.put(name + "_type", value.getClass().getSimpleName());
            }
            job.put("responsedata", jsonData);
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        return job.toString();
    }
}

