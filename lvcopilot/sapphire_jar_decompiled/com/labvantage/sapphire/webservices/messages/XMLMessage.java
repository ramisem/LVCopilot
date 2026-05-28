/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.messages;

import com.labvantage.sapphire.webservices.messages.BaseSECMessage;

public class XMLMessage
extends BaseSECMessage {
    private String xml = "";

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getXml() {
        return this.xml;
    }

    @Override
    public void fromMessage(String message) {
        this.setXml(message);
    }

    @Override
    public String toMessage() {
        return this.getXml();
    }
}

