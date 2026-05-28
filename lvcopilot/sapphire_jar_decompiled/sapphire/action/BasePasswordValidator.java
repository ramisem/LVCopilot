/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public abstract class BasePasswordValidator
extends BaseCustom {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    private DBUtil _dbutil;
    private boolean reset = false;
    protected DBAccess database;

    protected void logError(String errormsg) {
        Trace.log("PASSWORD", "ERROR: " + errormsg);
    }

    protected void logError(String errormsg, Exception exception) {
        Trace.log("PASSWORD", "ERROR: " + errormsg);
    }

    protected void logTrace(String tracemsg) {
        Trace.log("PASSWORD", tracemsg);
    }

    public void startPasswordHandler() {
        this.reset = false;
    }

    public void startPasswordHandler(SapphireConnection sapphireConnection) throws SapphireException {
        this._dbutil = new DBUtil();
        this._dbutil.setConnection(sapphireConnection);
        this.database = this._dbutil;
        this.reset = true;
    }

    public void startPasswordHandler(DBUtil dbutil) throws SapphireException {
        this._dbutil = dbutil;
        this.database = this._dbutil;
        this.reset = false;
    }

    public void endPasswordHandler() {
        if (this.reset) {
            this._dbutil.reset();
        }
    }

    @Deprecated
    public String encrypt(String password) {
        return this.encrypt(password, false);
    }

    @Deprecated
    public String encrypt(String password, boolean isCaseSensitive) {
        return EncryptDecrypt.encryptJCE(password, isCaseSensitive);
    }

    public String encodePassword(String password) {
        return EncryptDecrypt.encodePassword(password);
    }

    public boolean passwordMatches(String password, String encodedPassword, boolean isCaseSensitive) {
        return EncryptDecrypt.passwordMatches(password, encodedPassword, isCaseSensitive);
    }

    public boolean passwordNeedUpgrade(String encodedPassword) {
        return EncryptDecrypt.passwordNeedUpgrade(encodedPassword);
    }

    public abstract void checkPasswordFormat(String var1, String var2, PropertyList var3) throws SapphireException;

    public String generatePassword(PropertyList properties) {
        return "";
    }
}

