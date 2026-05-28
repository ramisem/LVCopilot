/*
 * Decompiled with CFR 0.152.
 */
package sapphire.error;

import sapphire.error.ErrorDetail;

public class ErrorHandler
extends com.labvantage.sapphire.gwt.shared.error.ErrorHandler {
    public ErrorHandler() {
    }

    public ErrorHandler(String errorString) {
        super(errorString);
    }

    public ErrorHandler(String errorId, String errorType, String errorString) {
        super(errorId, errorType, errorString);
    }

    @Override
    public ErrorDetail createErrorDetail(String sdcid, String event, String errorid, String errorType, String message) {
        return new ErrorDetail(sdcid, event, errorid, errorType, message);
    }
}

