/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.DefaultAuthentication;
import com.labvantage.sapphire.Trace;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DefaultSSOAuthentication
extends DefaultAuthentication {
    @Override
    public void createUser(String username, String password, PropertyList properties) throws SapphireException {
        Trace.logInfo("Attempting to create new SSO user " + username);
        PropertyList ssoAttributes = properties.getPropertyList("ssoattributes");
        this.addUser(username, password, properties, ssoAttributes);
    }
}

