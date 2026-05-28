/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package sapphire.ext;

import com.labvantage.sapphire.Trace;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public abstract class BaseWebMFAHandler
implements Serializable {
    private PropertyList authenticationProps = null;
    protected String username = null;
    protected String database = null;
    protected String connectionid = null;
    protected String secretKey = null;
    private Timer timer = null;

    public void init(PropertyList authenticationProps, String username, String database, final String connectionid, String secretKey) {
        this.authenticationProps = authenticationProps;
        this.connectionid = connectionid;
        this.username = username;
        this.database = database;
        this.secretKey = secretKey;
        this.timer = new Timer();
        TimerTask task = new TimerTask(){

            @Override
            public void run() {
                new ConnectionProcessor(connectionid).clearConnection(connectionid);
            }
        };
        this.timer.schedule(task, 900000L);
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    public String getUsername() {
        return this.username;
    }

    public void completeLogin(HttpServletRequest request) {
        BaseWebMFAHandler baseWebMFAHandler;
        if (request != null && (baseWebMFAHandler = (BaseWebMFAHandler)request.getSession().getAttribute("baseWebMFAHandler")) != null) {
            request.getSession().removeAttribute("baseWebMFAHandler");
        }
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    public void completeLogin(HttpSession httpSession) {
        BaseWebMFAHandler baseWebMFAHandler = (BaseWebMFAHandler)httpSession.getAttribute("baseWebMFAHandler");
        if (baseWebMFAHandler != null) {
            httpSession.removeAttribute("baseWebMFAHandler");
        }
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    protected ConnectionInfo getConnectionInfo() throws SapphireException {
        ConnectionInfo connectionInfo = null;
        try {
            connectionInfo = new ConnectionProcessor(this.connectionid).getConnectionInfo(this.connectionid);
            if (connectionInfo == null) {
                throw new SapphireException("ConnectionInfo not found for connectionid: " + this.connectionid);
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to get Connection Info", e);
            throw new SapphireException("Connection Expired. Please try again.");
        }
        return connectionInfo;
    }

    public abstract void renderPrompt(HttpServletRequest var1, HttpServletResponse var2) throws Exception;

    public abstract boolean verifyResponse(HttpServletRequest var1, HttpServletResponse var2) throws Exception;

    public PropertyList getAuthenticationProps() {
        return this.authenticationProps;
    }

    public void setAuthenticationProps(PropertyList authenticationProps) {
        this.authenticationProps = authenticationProps;
    }
}

