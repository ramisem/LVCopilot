/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.platform.Configuration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class GenerateComponentKey
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String compcode = properties.getProperty("compcode").toUpperCase();
        if (compcode.length() == 0) {
            throw new SapphireException("Component code property missing!");
        }
        if (!compcode.matches("[A-Z]{3}")) {
            throw new SapphireException("Component code must be 3 capital characters (A-Z)!");
        }
        File out = new File(properties.getProperty("dir", Configuration.getInstance().getSapphireHome()), "Comp-" + compcode + ".props");
        FileOutputStream fos = null;
        Properties comp = new Properties();
        comp.setProperty("key", EncryptDecrypt.encodeComponentKey(compcode, this.getClass().getSimpleName()));
        try {
            fos = new FileOutputStream(out);
            comp.store(fos, compcode + " Component Key");
            properties.setProperty("file", out.getAbsolutePath());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to write component key!", e);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (IOException iOException) {}
            }
        }
    }
}

