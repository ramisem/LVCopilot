/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Trace;
import java.io.File;
import javax.servlet.jsp.PageContext;

public class SequenceProcessor
extends BaseAccessor {
    public SequenceProcessor(String connectionid) {
        super(connectionid);
    }

    public SequenceProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public SequenceProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public SequenceProcessor(PageContext pageContext) {
        super(pageContext);
    }

    public int getSequence(String sdcid, String sequenceid) {
        return this.getSequence(sdcid, sequenceid, 0, 1);
    }

    public int getSequence(String sdcid, String sequenceid, int incrementby) {
        return this.getSequence(sdcid, sequenceid, 0, incrementby);
    }

    public int getSequence(String sdcid, String sequenceid, int startsequencenumber, int incrementby) {
        try {
            return local ? this.getLocalAccessManager().getSequence(this.getConnectionid(), sdcid, sequenceid, startsequencenumber, incrementby) : this.getRemoteAccessManager().getSequence(this.getConnectionid(), sdcid, sequenceid, startsequencenumber, incrementby);
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            return -1;
        }
    }

    public String getUUID() {
        try {
            return local ? this.getLocalAccessManager().getUUID(this.getConnectionid()) : this.getRemoteAccessManager().getUUID(this.getConnectionid());
        }
        catch (Exception e) {
            Trace.log("ERROR", "Exception: " + e.getMessage());
            return null;
        }
    }
}

