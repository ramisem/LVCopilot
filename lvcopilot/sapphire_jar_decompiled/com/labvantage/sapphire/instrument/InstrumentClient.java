/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.instrument;

public interface InstrumentClient {
    public void connect() throws Exception;

    public void disconnect() throws Exception;

    public String sendMessage(String var1, boolean var2, int var3) throws Exception;
}

