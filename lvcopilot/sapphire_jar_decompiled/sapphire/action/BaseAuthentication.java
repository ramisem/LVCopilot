/*
 * Decompiled with CFR 0.152.
 */
package sapphire.action;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import java.util.HashMap;
import javax.naming.directory.Attributes;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public abstract class BaseAuthentication
extends BaseCustom {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final String AUTHENTICATION_FAIL_MESSAGE = "Authentication failed. Incorrect username or password for username";
    private DBUtil _dbutil;
    protected DBAccess database;
    protected String databaseid;
    protected Attributes ldapUserAttributes;
    protected HashMap ldapUserAttributesMap = new HashMap();

    protected void logError(String errormsg) {
        this.logger.error(errormsg);
    }

    protected void logError(String errormsg, Exception exception) {
        this.logger.error(errormsg, exception);
    }

    protected void logTrace(String tracemsg) {
        this.logger.info(tracemsg);
    }

    public void startAuthenticate(String databaseid, DBUtil dbutil) throws SapphireException {
        this._dbutil = dbutil;
        this.database = this._dbutil;
        this.databaseid = databaseid;
    }

    public void endAuthenticate() {
        this._dbutil.reset();
    }

    public abstract void authenticateUser(String var1, String var2, PropertyList var3) throws SapphireException;

    public void synchronizeUser(String userid, String password, PropertyList properties) throws SapphireException {
    }

    public void createUser(String userid, String password, PropertyList properties) throws SapphireException {
    }

    public void secondaryAuthentication(PropertyList properties) throws SapphireException {
    }
}

