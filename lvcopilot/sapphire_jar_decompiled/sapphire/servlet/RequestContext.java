/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package sapphire.servlet;

import com.labvantage.sapphire.servlet.BaseContext;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;

public class RequestContext
extends BaseContext
implements Serializable {
    public static final String NAME = "RequestContext";
    public static final String REGISTEREDPAGE_PROPERTYID = "__registeredpage";
    public static final String NEXTPAGE_PROPERTYID = "__nextpage";
    public static final String ISURL_PROPERTYID = "__isurl";
    public static final String CACHED_PROPERTYID = "__cached";
    public static final String LAYOUT_DATABLOCK = "layout";
    public static final String PAGE_DATABLOCK = "pagedata";
    public static final String ELEMENTS = "elements";
    public static final String PAGEDIRECTIVES_DATABLOCK = "pagedirectives";
    public static final String USER_CONFIG = "userconfig";
    public static final String CURRENT_LAYOUT = "currentlayout";
    public static final String CURRENT_LAYOUT_NODE = "currentlayoutnode";
    public static final String CURRENT_LAYOUT_REPLACE = "currentlayoutreplace";
    public static final String CACHED_REQUEST_CONTEXT = "__crc";
    public static final String CACHED_REQUESTID_PREFIX = "crc_";
    public static final String ACTION_TRANSACTIONID = "actiontransactionid";
    private String requestId;
    private String connectionId;
    private boolean rtl = false;
    private boolean copyRequestParameters = true;
    private boolean controlledPage = false;

    public RequestContext(PropertyList propertyList) {
        super(propertyList);
    }

    public static RequestContext getInstance(HttpServletRequest request) {
        RequestContext requestContext = (RequestContext)request.getAttribute(NAME);
        return requestContext != null ? requestContext : new RequestContext(new PropertyList());
    }

    public static RequestContext getRequestContext(HttpServletRequest request) {
        return (RequestContext)request.getAttribute(NAME);
    }

    public static RequestContext getRequestContext(PageContext pageContext) {
        return (RequestContext)pageContext.getRequest().getAttribute(NAME);
    }

    public boolean copyRequestParameters() {
        return this.copyRequestParameters;
    }

    public void setCopyRequestParameters(boolean copy) {
        this.copyRequestParameters = copy;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public String getConnectionid() {
        return this.getConnectionId();
    }

    public void setControlledPage(boolean controlledPage) {
        this.controlledPage = controlledPage;
    }

    public boolean isControlledPage() {
        return this.controlledPage;
    }

    public boolean isRtl() {
        return this.rtl;
    }

    public void setRtl(boolean rtl) {
        this.rtl = rtl;
    }
}

