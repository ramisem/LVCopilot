/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared.error;

import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.error.ErrorDetail;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import java.util.ArrayList;

public class ErrorHandler
extends ArrayList
implements JSONable {
    public static final String NAME = "ERRORHANDLER";
    public static final String ERROR_SEPARATOR = "|";
    public static final String LINE_SEPARATOR = "|!|";
    public static final String PART_SEPARATOR = "::";

    public static boolean isErrorHandlerFormat(String text) {
        ErrorHandler eh = new ErrorHandler(text);
        return eh.size() > 0;
    }

    public ErrorHandler() {
    }

    public ErrorHandler(String errorString) {
        if (errorString != null && errorString.length() > 0) {
            if (errorString.startsWith(ERROR_SEPARATOR)) {
                errorString = errorString.substring(1);
            }
            if (errorString.endsWith(ERROR_SEPARATOR)) {
                errorString = errorString.substring(0, errorString.lastIndexOf(ERROR_SEPARATOR));
            }
            String[] parts = StringUtil.split(errorString, LINE_SEPARATOR);
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                String[] bits = StringUtil.split(part, PART_SEPARATOR);
                if (bits.length == 5) {
                    this.add(this.createErrorDetail(bits[0], bits[1], bits[2], bits[3], bits[4]));
                    continue;
                }
                this.add(this.createErrorDetail("", "", "", "", part));
            }
        }
    }

    public ErrorHandler(String errorId, String errorType, String errorString) {
        this.add(this.createErrorDetail("", "", errorId, errorType, errorString));
    }

    public ErrorDetail createErrorDetail(String sdcid, String event, String errorid, String errorType, String message) {
        return new ErrorDetail(sdcid, event, errorid, errorType, message);
    }

    public ErrorHandler add(String sdcid, String event, String errorid, String errorType, String message) {
        if (message != null && message.length() > 0) {
            this.add(this.createErrorDetail(sdcid, event, errorid, errorType, message));
        }
        return this;
    }

    public ErrorHandler addFailureError(String message) {
        if (message != null && message.length() > 0) {
            this.add(this.createErrorDetail("", "", "", "FAILURE", message));
        }
        return this;
    }

    public ErrorHandler addValidationError(String message) {
        if (message != null && message.length() > 0) {
            this.add(this.createErrorDetail("", "", "", "VALIDATION", message));
        }
        return this;
    }

    public ErrorHandler addInformation(String message) {
        if (message != null && message.length() > 0) {
            this.add(this.createErrorDetail("", "", "", "INFORMATION", message));
        }
        return this;
    }

    public String getEncodedString() {
        return this.getEncodedString(true);
    }

    public String getEncodedString(boolean addErrorSeparators) {
        if (this.size() > 0) {
            StringBuffer out = new StringBuffer();
            for (ErrorDetail error : this) {
                out.append(LINE_SEPARATOR).append(error);
            }
            out.delete(0, LINE_SEPARATOR.length());
            return addErrorSeparators ? ERROR_SEPARATOR + out.toString() + ERROR_SEPARATOR : out.toString();
        }
        return "";
    }

    public boolean hasErrors() {
        for (ErrorDetail error : this) {
            if (!error.getErrorType().equals("VALIDATION") && !error.getErrorType().equals("FAILURE") && !error.getErrorType().equals("CONFIRM")) continue;
            return true;
        }
        return false;
    }

    public String getLastErrorMessage() {
        for (int i = this.size() - 1; i >= 0; --i) {
            ErrorDetail error = (ErrorDetail)this.get(i);
            if (!error.getErrorType().equals("VALIDATION") && !error.getErrorType().equals("FAILURE") && !error.getErrorType().equals("CONFIRM")) continue;
            return error.getMessage();
        }
        return "";
    }

    public String getLastErrorType() {
        for (int i = this.size() - 1; i >= 0; --i) {
            ErrorDetail error = (ErrorDetail)this.get(i);
            if (!error.getErrorType().equals("VALIDATION") && !error.getErrorType().equals("FAILURE") && !error.getErrorType().equals("CONFIRM")) continue;
            return error.getErrorType();
        }
        return "";
    }

    public boolean hasInfoErrors() {
        for (ErrorDetail error : this) {
            if (!error.getErrorType().equals("INFORMATION")) continue;
            return true;
        }
        return false;
    }

    @Override
    public String toJSONString() {
        return this.getEncodedString(false);
    }
}

