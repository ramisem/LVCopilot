/*
 * Decompiled with CFR 0.152.
 */
package sapphire;

public class SapphireException
extends Exception {
    private String errorid = "GENERAL_ERROR";
    private String errorType = "FAILURE";
    public static final String TYPE_FAILURE = "FAILURE";
    public static final String TYPE_CONFIRM = "CONFIRM";
    public static final String TYPE_VALIDATION = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";

    public SapphireException() {
    }

    public SapphireException(String message) {
        super(message);
    }

    public SapphireException(String message, Throwable e) {
        super(message, e);
    }

    public SapphireException(String errorid, String message, Throwable e) {
        super(message, e);
        this.errorid = errorid;
    }

    public SapphireException(String errorid, String message) {
        super(message);
        this.errorid = errorid;
    }

    public SapphireException(String errorid, String errorType, String message) {
        super(message);
        this.errorid = errorid;
        this.errorType = errorType;
    }

    public SapphireException(String errorid, String errorType, String message, Throwable e) {
        super(message, e);
        this.errorid = errorid;
        this.errorType = errorType;
    }

    public SapphireException(Throwable e) {
        super(e);
    }

    public String getErrorid() {
        return this.errorid;
    }

    public String getErrorType() {
        return this.errorType;
    }
}

