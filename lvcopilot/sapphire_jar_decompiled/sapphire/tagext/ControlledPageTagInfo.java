/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import java.util.HashMap;
import java.util.Set;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseTagInfo;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class ControlledPageTagInfo
extends BaseTagInfo {
    public static final String TAG_VARIABLE_NAME = "requestinfo";
    private RequestContext requestContext;

    public ControlledPageTagInfo(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public boolean isCopyrequestparameters() {
        return this.requestContext.copyRequestParameters();
    }

    public void setCopyrequestparameters(boolean copy) {
        this.requestContext.setCopyRequestParameters(copy);
    }

    public void setConnectionid(String connectionid) {
        this.requestContext.setConnectionId(connectionid);
    }

    public String getConnectionId() {
        return this.requestContext.getConnectionId();
    }

    public String getConnectionid() {
        return this.requestContext.getConnectionId();
    }

    public void setControlledpage(boolean controlledpage) {
        this.requestContext.setControlledPage(controlledpage);
    }

    public boolean isControlledpage() {
        return this.requestContext.isControlledPage();
    }

    public void setRequestProperty(String propertyid, String propertyvalue) {
        this.requestContext.setProperty(propertyid, propertyvalue);
    }

    public void setPageProperty(String propertyid, String propertyvalue) {
        this.requestContext.setProperty(propertyid, propertyvalue);
    }

    public void setSessionProperty(String propertyid, String propertyvalue) {
        this.requestContext.setProperty(propertyid, propertyvalue);
    }

    public String getProperty(String propertyid) {
        return this.requestContext.getProperty(propertyid);
    }

    public String getEncodedProperty(String propertyid) {
        return HttpUtil.encodeURIComponent(this.getProperty(propertyid));
    }

    public String getDecodedProperty(String propertyid) {
        return HttpUtil.decodeURIComponent(this.getProperty(propertyid));
    }

    public String getRequestProperty(String propertyid) {
        return this.getProperty(propertyid);
    }

    public String getPageProperty(String propertyid) {
        return this.getProperty(propertyid);
    }

    public String getSessionProperty(String propertyid) {
        return this.getProperty(propertyid);
    }

    public boolean isProperty(String propertyid) {
        return this.isRequestProperty(propertyid) || this.isPageProperty(propertyid) || this.isSessionProperty(propertyid);
    }

    public boolean isRequestProperty(String propertyid) {
        return this.requestContext.getPropertyList().containsKey(propertyid);
    }

    public boolean isPageProperty(String propertyid) {
        return this.requestContext.getPropertyList().containsKey(propertyid);
    }

    public boolean isSessionProperty(String propertyid) {
        return this.requestContext.getPropertyList().containsKey(propertyid);
    }

    public HashMap getProperties() {
        HashMap newmap = new HashMap();
        PropertyList oldmap = this.requestContext.getPropertyList();
        if (oldmap != null) {
            Set keys = oldmap.keySet();
            for (String propertyid : keys) {
                Object o = oldmap.get(propertyid);
                if (!(o instanceof String)) continue;
                newmap.put(propertyid, o);
            }
        }
        return newmap;
    }

    public HashMap getRequestProperties() {
        return this.getProperties();
    }

    public HashMap getPageProperties() {
        return this.getProperties();
    }

    public HashMap getSessionProperties() {
        return this.getProperties();
    }

    public void resetProperties() {
        this.requestContext.getPropertyList().clear();
    }

    public void resetRequestProperties() {
        this.requestContext.getPropertyList().clear();
    }

    public void resetPageProperties() {
        this.requestContext.getPropertyList().clear();
    }

    public void resetSessionProperties() {
        this.requestContext.getPropertyList().clear();
    }
}

