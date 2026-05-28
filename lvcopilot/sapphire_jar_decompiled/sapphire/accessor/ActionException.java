/*
 * Decompiled with CFR 0.152.
 */
package sapphire.accessor;

import sapphire.SapphireException;
import sapphire.error.ErrorHandler;

public class ActionException
extends SapphireException {
    private int actionindex = -1;
    private String actionname = "";
    private ErrorHandler errorHandler;

    public ActionException(String msg) {
        super(msg);
    }

    public ActionException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.errorHandler = new ErrorHandler(throwable.getMessage());
    }

    public ActionException(String msg, String errorString) {
        super(msg);
        this.errorHandler = new ErrorHandler(errorString);
    }

    public ActionException(String actionname, int actionindex, String msg) {
        super(msg);
        this.actionname = actionname;
        this.actionindex = actionindex;
    }

    public ActionException(String actionname, int actionindex, String msg, String errorString) {
        super(msg);
        this.actionname = actionname;
        this.actionindex = actionindex;
        this.errorHandler = new ErrorHandler(errorString);
    }

    public ActionException(String actionname, int actionindex, String type, String msg, String errorString) {
        super("GENERAL_ERROR", type, msg);
        this.actionname = actionname;
        this.actionindex = actionindex;
        this.errorHandler = new ErrorHandler(errorString);
    }

    public String getActionName() {
        return this.actionname;
    }

    public int getActionIndex() {
        return this.actionindex;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }
}

