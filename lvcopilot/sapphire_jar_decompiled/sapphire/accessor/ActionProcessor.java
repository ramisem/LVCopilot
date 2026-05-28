/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import java.io.File;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;

public class ActionProcessor
extends BaseAccessor {
    public ActionProcessor(String connectionid) {
        super(connectionid);
    }

    public ActionProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public ActionProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public ActionProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public void reset() {
        this.resetErrorStack();
    }

    public void processActionClass(String actionClass, PropertyList properties) throws ActionException {
        this.processActionClass(actionClass, properties, false);
    }

    public void processActionClass(String actionClass, HashMap properties, boolean newtransaction) throws ActionException {
        ActionBlock actionblock = new ActionBlock();
        actionblock.setActionClass("singleaction", actionClass, properties);
        this.processActionBlock(actionblock, newtransaction);
        properties.putAll(actionblock.getActionProperties("singleaction"));
    }

    public void processAction(String actionid, String versionid, HashMap properties) throws ActionException {
        this.processAction(actionid, versionid, properties, false);
    }

    public void processAction(String actionid, String versionid, HashMap properties, boolean newtransaction) throws ActionException {
        this.processAction(actionid, versionid, properties, newtransaction, false);
    }

    public void processAction(String actionid, String versionid, HashMap properties, boolean newtransaction, boolean processasynchronous) throws ActionException {
        ActionBlock actionblock = new ActionBlock();
        actionblock.setAction("singleaction", actionid, versionid, properties);
        if (properties.containsKey("_nolog")) {
            actionblock.setBlockProperty("_nolog", (String)properties.get("_nolog"));
        }
        this.processActionBlock(actionblock, newtransaction, processasynchronous);
        properties.putAll(actionblock.getActionProperties("singleaction"));
    }

    public void processActionBlock(ActionBlock actionblock) throws ActionException {
        this.processActionBlock(actionblock, false);
    }

    public void processActionBlock(ActionBlock actionblock, boolean newtransaction) throws ActionException {
        this.processActionBlock(actionblock, newtransaction, false);
    }

    public void processActionBlock(ActionBlock actionblock, boolean newtransaction, boolean processasynchronous) throws ActionException {
        int actionCount = actionblock.getActionCount();
        if (actionCount > 0) {
            ActionBlock returnactionblock;
            try {
                returnactionblock = local ? this.getLocalAccessManager().processActionBlock(this.getConnectionid(), actionblock, newtransaction, processasynchronous) : this.getRemoteAccessManager().processActionBlock(this.getConnectionid(), actionblock, newtransaction, processasynchronous);
            }
            catch (Exception e) {
                Throwable[] suppressedExceptions;
                Throwable rootcause;
                this.setError(e.getMessage(), e);
                for (rootcause = e.getCause(); rootcause != null && rootcause.getCause() != null; rootcause = rootcause.getCause()) {
                }
                Throwable[] throwableArray = suppressedExceptions = rootcause == null ? null : rootcause.getSuppressed();
                if (suppressedExceptions != null && suppressedExceptions.length > 0) {
                    Throwable rootcauseThrowable = suppressedExceptions[0].getCause();
                    String rootcauseString = rootcauseThrowable.getMessage();
                    if (rootcauseThrowable.getCause() != null) {
                        rootcauseString = rootcauseString + ":" + rootcauseThrowable.getCause().getMessage();
                    }
                    throw new ActionException("An unexpected error occurred Caused by " + rootcauseString, e);
                }
                if (rootcause != null && rootcause.getMessage() != null && rootcause.getMessage().length() > 0) {
                    throw new ActionException("An unexpected error occurred Caused by " + rootcause.getMessage(), e);
                }
                throw new ActionException("An unexpected error occurred possibly due to RI - check log for details", e);
            }
            actionblock.setDebugLog(returnactionblock.getDebugLog());
            ErrorHandler errorHandler = returnactionblock.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors()) {
                this.setErrorString(errorHandler.getEncodedString());
                String errorActionName = returnactionblock.getErrorActionName();
                String reason = " Reason: " + errorHandler.getLastErrorMessage();
                this.setError(errorHandler.getLastErrorMessage());
                if (errorHandler.getLastErrorType().equals("CONFIRM")) {
                    throw new ActionException(errorActionName, returnactionblock.getErrorAction(), "CONFIRM", errorHandler.getLastErrorMessage(), errorHandler.getEncodedString());
                }
                throw new ActionException(errorActionName, returnactionblock.getErrorAction(), "Failed to process action '" + errorActionName + "'." + reason, errorHandler.getEncodedString());
            }
            if (errorHandler != null && errorHandler.hasInfoErrors()) {
                this.setInfoErrorString(errorHandler.getEncodedString());
            }
            actionblock.setBlockProperties(returnactionblock.getBlockProperties());
            actionblock.setReturnProperties(returnactionblock.getReturnProperties());
            actionblock.synchronizeProperties(returnactionblock);
        }
    }
}

