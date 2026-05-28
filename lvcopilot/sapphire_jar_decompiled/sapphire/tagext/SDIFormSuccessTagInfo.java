/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import sapphire.error.ErrorHandler;
import sapphire.tagext.BaseTagInfo;

public class SDIFormSuccessTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "sdiformsuccessinfo";
    private String infoErrorString;

    public String getInfoErrorString() {
        return this.infoErrorString;
    }

    public void setInfoErrorString(String infoErrorString) {
        this.infoErrorString = infoErrorString;
    }

    public boolean hasInfoErrors() {
        return this.infoErrorString != null && this.infoErrorString.length() > 0 && new ErrorHandler(this.infoErrorString).size() > 0;
    }

    public boolean hasErrors() {
        return false;
    }

    public ErrorHandler getErrorHandler() {
        return this.infoErrorString != null && this.infoErrorString.length() > 0 ? new ErrorHandler(this.infoErrorString) : null;
    }
}

