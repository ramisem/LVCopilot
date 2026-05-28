/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.tagext.BaseTagInfo;

public class SDIFormFailureTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "sdiformfailureinfo";
    private String lastError;
    private String errorString;

    @Override
    public String getLastError() {
        return this.lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    public String getLastErrorParsed() {
        return this.getLastErrorParsed(false);
    }

    public String getLastErrorParsed(boolean forErrorHandler) {
        String msg = this.lastError;
        int firstBar = msg.indexOf("|");
        int lastBar = msg.lastIndexOf("|");
        if (firstBar > 0 && firstBar != lastBar) {
            ErrorHandler handler;
            msg = msg.substring(firstBar + 1, lastBar);
            if (!forErrorHandler && msg.indexOf("::") > -1 && (handler = new ErrorHandler(msg)).size() == 1) {
                msg = ((ErrorDetail)handler.get(0)).getMessage();
            }
        }
        return msg;
    }

    public boolean hasErrors() {
        return this.errorString != null && this.errorString.length() > 0 && new ErrorHandler(this.errorString).size() > 0;
    }

    public ErrorHandler getErrorHandler() {
        return new ErrorHandler(this.errorString != null && this.errorString.length() > 0 ? this.errorString : this.getLastErrorParsed(true));
    }
}

