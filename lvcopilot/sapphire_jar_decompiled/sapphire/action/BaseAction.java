/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public abstract class BaseAction
extends BaseCustom {
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String NULL = "(null)";
    public static final String POLL = "(poll)";
    public static final String DEFAULT_SEPARATOR = ";";
    public static final String RETURN = "(return)";
    public static final String RETURN_SUCCESS = "1";
    public static final String RETURN_FAILURE = "2";
    public static final String VERSION = "1";
    public static final String ESC_SEMICOLON = "#semicolon#";
    public static final String TYPE_FAILURE = "FAILURE";
    public static final String TYPE_CONFIRM = "CONFIRM";
    public static final String TYPE_VALIDATION = "VALIDATION";
    public static final String TYPE_INFORMATION = "INFORMATION";
    private ErrorHandler errorHandler;
    private DBUtil dbUtil;
    private String actionid;
    private long startTime = 0L;
    protected DBAccess database;
    protected ConnectionInfo connectionInfo;

    public void startAction(String actionid, SapphireConnection sapphireConnection, ErrorHandler errorHandler, boolean nolog) throws SapphireException {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.connectionInfo = sapphireConnection;
        this.errorHandler = errorHandler;
        this.actionid = actionid;
        if (this.isDatabaseRequired()) {
            this.dbUtil = new DBUtil(sapphireConnection.getConnectionId());
            this.dbUtil.setConnection(sapphireConnection);
            this.database = this.dbUtil;
        }
        this.logger.setLoggerName(actionid != null && !actionid.equals("Unspecified") ? actionid.toUpperCase() : this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase());
        this.logger.noLog(nolog);
        this.startTime = System.currentTimeMillis();
    }

    public boolean isDatabaseRequired() {
        return true;
    }

    public void processAction(PropertyList properties) throws SapphireException {
    }

    public int processAction(String actionid, String actionversionid, HashMap properties) {
        try {
            PropertyList pl = (PropertyList)properties;
            pl.setLocale(I18nUtil.getConnectionLocale(this.connectionInfo));
            pl.setTimeZone(I18nUtil.getConnectionTimeZone(this.connectionInfo));
            this.processAction((PropertyList)properties);
        }
        catch (SapphireException e) {
            return this.setError(e.getErrorid(), e.getErrorType(), e.getMessage(), e);
        }
        return 1;
    }

    public void endAction() {
        this.logger.info(new StringBuffer("End action: ").append(this.actionid).append(". Took ").append(System.currentTimeMillis() - this.startTime).append(" ms").toString());
        if (this.isDatabaseRequired()) {
            this.dbUtil.reset();
        }
    }

    protected void logTrace(String message) {
        this.logger.info(message);
    }

    protected void logError(String message) {
        this.logger.error(message);
    }

    protected int setError(String message) {
        return this.setError("API_ACTION_ERROR", TYPE_FAILURE, message);
    }

    protected int setError(String errorid, String message) {
        return this.setError(errorid, TYPE_FAILURE, message);
    }

    protected int setError(String message, Exception e) {
        return this.setError("API_ACTION_ERROR", TYPE_FAILURE, message, e);
    }

    protected int setError(String errorid, String message, Exception e) {
        return this.setError(errorid, TYPE_FAILURE, message);
    }

    public int setError(String errorid, String errorType, String message, Throwable e) {
        this.logger.error(errorid + ": " + message, e);
        return this.addError(errorid, message, errorType);
    }

    public int setError(String errorid, String errorType, String message) {
        this.logger.error(errorid + ": " + message);
        return this.addError(errorid, message, errorType);
    }

    private int addError(String errorid, String message, String errorType) {
        if (!message.startsWith("|") || !message.endsWith("|")) {
            this.errorHandler.add("", this.actionid, errorid, errorType, ErrorUtil.extractMessage(message, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        return 2;
    }

    public void setErrors(ErrorHandler errorHandler) {
        this.errorHandler.addAll(errorHandler);
    }

    public void setInfoError(String message) {
        this.errorHandler.add("", this.actionid, "API_ACTION_ERROR", TYPE_INFORMATION, message);
    }

    public void setInfoError(String errorid, String message) {
        this.errorHandler.add("", this.actionid, errorid, TYPE_INFORMATION, message);
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    protected String createDatabaseTransactionLogId(PropertyList properties) throws SapphireException {
        return this.createDatabaseTransactionLogId(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), properties);
    }

    protected String createDatabaseTransactionLogId(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList properties) throws SapphireException {
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase(NO) && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? NO : YES;
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals(YES));
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        return traceLogId;
    }
}

