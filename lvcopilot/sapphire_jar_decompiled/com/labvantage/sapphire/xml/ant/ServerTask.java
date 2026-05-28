/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.Task;
import sapphire.SapphireException;

public class ServerTask
extends Task {
    private String file;
    private String connectionurl;
    private String username;
    private String password;

    public void setFile(String file) {
        this.file = file;
    }

    public void setConnectionurl(String connectionurl) {
        this.connectionurl = connectionurl;
    }

    public String getConnectionurl() {
        return this.connectionurl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public String getFile() {
        return this.file;
    }

    public void loadFileProps() throws SapphireException {
        File propsFile = new File(this.file);
        if (propsFile.exists()) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(propsFile));
                this.connectionurl = props.getProperty("connectionurl");
                this.username = props.getProperty("username");
                this.password = props.getProperty("password");
            }
            catch (IOException e) {
                throw new SapphireException("Failed to load properies file '" + this.file + "'");
            }
        } else {
            throw new SapphireException("Properties file '" + this.file + "' does not exist");
        }
    }
}

