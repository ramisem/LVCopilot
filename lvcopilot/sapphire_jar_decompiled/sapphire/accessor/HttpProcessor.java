/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import java.io.File;
import javax.servlet.jsp.PageContext;

public class HttpProcessor
extends BaseAccessor {
    public HttpProcessor(String connectionid) {
        super(connectionid);
    }

    public HttpProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
    }

    public HttpProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
    }

    public HttpProcessor(PageContext pageContext) {
        super(pageContext);
    }
}

