/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.http;

import java.io.Serializable;
import sapphire.util.DataSet;

public class WebPageRequest
implements Serializable {
    public String requestwebpageid;
    public String sysuserid;
    public String[] webpageid;
    public boolean[] webpageaccess;
    public boolean showhistory = true;
    public boolean showbulletins = true;
    public boolean showfavorites = true;
    public DataSet history = null;
    public DataSet bulletins = null;
    public DataSet favorites = null;
}

