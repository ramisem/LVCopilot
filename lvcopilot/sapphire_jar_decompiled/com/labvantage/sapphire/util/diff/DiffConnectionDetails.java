/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import sapphire.servlet.AjaxResponse;
import sapphire.tagext.PageTagInfo;
import sapphire.xml.PropertyList;

public class DiffConnectionDetails {
    String type;
    String username;
    String password;
    String database;
    String dbservername;
    String dbport;
    String dbsid;
    String dbusername;
    String dbpassword;
    String filename;

    public void setPageInfo(PageTagInfo pageinfo, String prefix) {
        this.type = pageinfo.getProperty(prefix + "type");
        this.username = pageinfo.getProperty(prefix + "username");
        this.password = pageinfo.getProperty(prefix + "password");
        this.database = pageinfo.getProperty(prefix + "database");
        this.dbservername = pageinfo.getProperty(prefix + "dbservername");
        this.dbport = pageinfo.getProperty(prefix + "dbport");
        this.dbsid = pageinfo.getProperty(prefix + "dbsid");
        this.dbusername = pageinfo.getProperty(prefix + "dbusername");
        this.dbpassword = pageinfo.getProperty(prefix + "dbpassword");
        this.filename = pageinfo.getProperty(prefix + "filename");
    }

    public void setPageInfo(PageTagInfo pageinfo, PropertyList defaults, String prefix) {
        this.type = pageinfo.getProperty(prefix + "type", defaults.getProperty(prefix + "type"));
        this.username = pageinfo.getProperty(prefix + "username", defaults.getProperty(prefix + "username"));
        this.password = pageinfo.getProperty(prefix + "password", defaults.getProperty(prefix + "password"));
        this.database = pageinfo.getProperty(prefix + "database", defaults.getProperty(prefix + "database"));
        this.dbservername = pageinfo.getProperty(prefix + "dbservername", defaults.getProperty(prefix + "dbservername"));
        this.dbport = pageinfo.getProperty(prefix + "dbport", defaults.getProperty(prefix + "dbport"));
        this.dbsid = pageinfo.getProperty(prefix + "dbsid", defaults.getProperty(prefix + "dbsid"));
        this.dbusername = pageinfo.getProperty(prefix + "dbusername", defaults.getProperty(prefix + "dbusername"));
        this.dbpassword = pageinfo.getProperty(prefix + "dbpassword", defaults.getProperty(prefix + "dbpassword"));
        this.filename = pageinfo.getProperty(prefix + "filename", defaults.getProperty(prefix + "filename"));
    }

    public void setAjaxResponse(AjaxResponse ajaxResponse, String prefix) {
        this.type = ajaxResponse.getRequestParameter(prefix + "type");
        this.username = ajaxResponse.getRequestParameter(prefix + "username");
        this.password = ajaxResponse.getRequestParameter(prefix + "password");
        this.database = ajaxResponse.getRequestParameter(prefix + "database");
        this.dbservername = ajaxResponse.getRequestParameter(prefix + "dbservername");
        this.dbport = ajaxResponse.getRequestParameter(prefix + "dbport");
        this.dbsid = ajaxResponse.getRequestParameter(prefix + "dbsid");
        this.dbusername = ajaxResponse.getRequestParameter(prefix + "dbusername");
        this.dbpassword = ajaxResponse.getRequestParameter(prefix + "dbpassword");
        this.filename = ajaxResponse.getRequestParameter(prefix + "filename");
    }

    public String getTitle() {
        return this.type.equals("current") ? "Current Database" : (this.type.equals("other") ? this.username + " on " + this.database : (this.type.equals("jdbc") ? this.dbusername + " on " + this.dbservername : this.filename));
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDbservername() {
        return this.dbservername;
    }

    public void setDbservername(String dbservername) {
        this.dbservername = dbservername;
    }

    public String getDbport() {
        return this.dbport;
    }

    public void setDbport(String dbport) {
        this.dbport = dbport;
    }

    public String getDbsid() {
        return this.dbsid;
    }

    public void setDbsid(String dbsid) {
        this.dbsid = dbsid;
    }

    public String getDbusername() {
        return this.dbusername;
    }

    public void setDbusername(String dbusername) {
        this.dbusername = dbusername;
    }

    public String getDbpassword() {
        return this.dbpassword;
    }

    public void setDbpassword(String dbpassword) {
        this.dbpassword = dbpassword;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

