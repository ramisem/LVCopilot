/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.DataAccessService;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class RSetPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String mode = (String)props.get("dothis");
        String rsetidlist = props.get("rsetidlist") != null ? (String)props.get("rsetidlist") : "";
        DataAccessService das = new DataAccessService(this.sapphireConnection);
        String[] rsets = StringUtil.split(rsetidlist, ";");
        for (int i = 0; i < rsets.length; ++i) {
            String rsetid = rsets[i];
            try {
                das.clearRSet(new RSet(rsetid));
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

