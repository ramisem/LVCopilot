/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class CMISRepository {
    private final String bindingType;
    private final String username;
    private final String password;
    private final String url;
    private final String repositoryid;
    private String sdmsRootFolderName;

    public CMISRepository(PropertyList repositoryProps) {
        this.repositoryid = repositoryProps.getProperty("repositoryid");
        this.bindingType = repositoryProps.getProperty("bindingtype");
        this.username = repositoryProps.getProperty("username");
        this.password = repositoryProps.getProperty("password");
        this.url = repositoryProps.getProperty("url");
        this.sdmsRootFolderName = repositoryProps.getProperty("sdmsrootfolder");
    }

    public CMISRepository(String repositoryid, String bindingType, String username, String password, String url, String sdmsRootFolderName) {
        this.repositoryid = repositoryid;
        this.bindingType = bindingType;
        this.username = username;
        this.password = password;
        this.url = url;
        this.sdmsRootFolderName = sdmsRootFolderName;
    }

    public PropertyList getCMSInfo() {
        PropertyList out = new PropertyList();
        return out;
    }

    public boolean connect() throws SapphireException {
        boolean connected = false;
        HashMap sessionParameters = new HashMap();
        return connected;
    }

    private void setupRootFolder() {
    }

    public String getRepositoryid() {
        return this.repositoryid;
    }
}

