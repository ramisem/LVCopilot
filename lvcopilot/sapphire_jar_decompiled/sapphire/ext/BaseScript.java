/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Binding
 *  groovy.lang.Script
 */
package sapphire.ext;

import com.labvantage.sapphire.Trace;
import groovy.lang.Binding;
import groovy.lang.Script;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public abstract class BaseScript {
    protected String LOGHEADER = "GROOVYSCRIPT";
    protected Script callingScript;
    protected HashMap bindings;
    protected ConnectionInfo connectionInfo;

    public static BaseScript getInstance(Class theclass, Script callingScript) throws SapphireException {
        return BaseScript.getInstance(theclass.getName(), callingScript);
    }

    public static BaseScript getInstance(String classname, Script callingScript) throws SapphireException {
        BaseScript scriptClass = null;
        try {
            Class<?> c = Class.forName(classname);
            scriptClass = (BaseScript)c.newInstance();
            scriptClass.callingScript = callingScript;
            Binding binding = callingScript.getBinding();
            scriptClass.setContext(binding != null ? (HashMap)binding.getVariables() : null);
            return scriptClass;
        }
        catch (Exception e) {
            Trace.logError("GROOVY", (Object)("Failed to create Groovy Script class " + classname), e);
            throw new SapphireException("Failed to create Groovy Script class " + classname);
        }
    }

    protected void setContext(HashMap bindings) {
        this.bindings = bindings;
        HashMap user = (HashMap)bindings.get("user");
        if (user != null) {
            this.connectionInfo = new ConnectionInfo(user);
        } else {
            Trace.logWarn(this.LOGHEADER, "BIND_VAR_USER not found in bindings");
        }
    }

    protected ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }
}

