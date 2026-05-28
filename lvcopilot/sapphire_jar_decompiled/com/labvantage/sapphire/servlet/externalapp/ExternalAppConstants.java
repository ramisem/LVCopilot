/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.externalapp;

import com.labvantage.sapphire.servlet.rest.RestConstants;

public interface ExternalAppConstants
extends RestConstants {
    public static final String CONNECTION_DEVICEID = "ExternalApp";
    public static final String ERROR_EXTERNALAPP_DISABLED = "External App is not Active";
    public static final String ERROR_EXTERNALAPP_NOTFOUND = "External App not found";
    public static final String ERROR_UNRECOGNIZED_TOKEN = "Failed to process token";
    public static final String ERROR_TOKEN_EXPIRED = "Token has expired";
    public static final String ERROR_PROCESSAS_INVALID = "Unable to execute ProcessAs user";
    public static final String ERROR_INVALID_HANDLER = "Invalid Handler class";
    public static final String COMMAND_REQUESTTOKEN = "RequestToken";
    public static final String COMMAND_ISTOKENACTIVE = "IsTokenActive";
    public static final String COMMAND_GETCONNECTIONID = "GetConnectionId";
    public static final String COMMAND_CHECKCONNECTION = "CheckConnection";
    public static final String COMMAND_CLEARCONNECTION = "ClearConnection";
    public static final String COMMANDRESPONSE_EXCEPTION = "_exception";
    public static final String EXTERNALAPP_STATUS_ACTIVE = "Active";
    public static final String EXTERNALAPP_STATUS_DISABLED = "Disabled";
    public static final String PROCESSAS_NEVER = "N";
    public static final String PROCESSAS_ANY = "A";
    public static final String PROCESSAS_USER = "U";
    public static final String PROCESSAS_ROLE = "R";
    public static final String PROCESSAS_DEPARTMENT = "D";
}

