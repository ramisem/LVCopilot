/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector;

import java.nio.file.Path;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public interface SDMSCollectorHolder {
    public void log(String var1, String var2);

    public void log(String var1, String var2, Throwable var3);

    public void log(String var1, String var2, String var3);

    public void log(String var1, String var2, String var3, Throwable var4);

    public JSONObject sendFileCommandToLIMS(String var1, String var2, Path var3, JSONObject var4) throws SapphireException;

    public PropertyList sendCommandToLIMS(String var1, PropertyList var2) throws SapphireException;

    public void executeRebootCommand();

    public void upgrade(String var1) throws SapphireException;

    public String getActualHostname();

    public void setStartupStateProperties(PropertyList var1);
}

