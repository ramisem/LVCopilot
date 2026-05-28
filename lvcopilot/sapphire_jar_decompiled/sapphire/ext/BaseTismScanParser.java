/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.BaseCustom;
import sapphire.xml.PropertyList;

public abstract class BaseTismScanParser
extends BaseCustom {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    public BaseTismScanParser(String connectionid) {
        this.setConnectionId(connectionid);
    }

    public abstract PropertyList parseScannedString(String var1, String var2);
}

