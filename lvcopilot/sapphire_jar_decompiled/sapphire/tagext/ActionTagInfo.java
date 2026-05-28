/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import java.util.HashMap;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorHandler;
import sapphire.tagext.BaseTagInfo;
import sapphire.util.ActionBlock;

public class ActionTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "actioninfo";
    private ActionBlock actionblock;
    private ErrorHandler errorHandler;

    public ActionTagInfo(ActionBlock actionblock) {
        this.actionblock = actionblock;
    }

    public int getActionCount() {
        return this.actionblock.getActionCount();
    }

    public String getActionName(int actionindex) {
        String value = "";
        try {
            value = this.actionblock.getActionName(actionindex);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getActionid(int actionindex) {
        String value = "";
        try {
            value = this.actionblock.getActionid(actionindex);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getActionid(String name) {
        String value = "";
        try {
            value = this.actionblock.getActionid(name);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getActionClass(int actionindex) throws ActionException {
        return this.actionblock.getActionClass(actionindex);
    }

    public String getActionClass(String name) throws ActionException {
        return this.actionblock.getActionClass(name);
    }

    public String getVersionid(int actionindex) {
        String value = "";
        try {
            value = this.actionblock.getVersionid(actionindex);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getVersionid(String name) throws ActionException {
        String value = "";
        try {
            value = this.actionblock.getVersionid(name);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public HashMap getActionProperties(String name) {
        HashMap value = new HashMap();
        try {
            value = this.actionblock.getActionProperties(name);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public HashMap getActionProperties(int actionindex) {
        HashMap value = new HashMap();
        try {
            value = this.actionblock.getActionProperties(actionindex);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getActionProperty(String name, String propertyid) {
        String value = "";
        try {
            value = this.actionblock.getActionProperty(name, propertyid);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public String getActionProperty(int actionindex, String propertyid) {
        String value = "";
        try {
            value = this.actionblock.getActionProperty(actionindex, propertyid);
        }
        catch (ActionException actionException) {
            // empty catch block
        }
        return value;
    }

    public HashMap getBlockProperties() {
        return this.actionblock.getBlockProperties();
    }

    public String getBlockProperty(String propertyid) {
        return this.actionblock.getBlockProperty(propertyid);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public boolean hasErrors() {
        return this.errorHandler.hasErrors();
    }

    public boolean hasInfoErrors() {
        return this.errorHandler.hasInfoErrors();
    }
}

