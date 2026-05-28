/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.tagext;

import com.labvantage.sapphire.services.SecurityService;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseTagInfo;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;

public class PageTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "pageinfo";
    private RequestContext requestContext;
    private TaskContext taskContext;
    private boolean taskPage = false;

    public PageTagInfo(PageContext pageContext, RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public String getConnectionId() {
        return this.requestContext.getConnectionId();
    }

    public String getConnectionid() {
        return this.requestContext.getConnectionId();
    }

    public boolean isRTL() {
        return this.requestContext.isRtl();
    }

    public String getNameServerList() {
        return "";
    }

    public String getNameserverlist() {
        return "";
    }

    public String getDatabaseid() {
        return SecurityService.getDatabaseId(this.getConnectionId());
    }

    public ActionProcessor getActionProcessor() {
        return new ActionProcessor(this.getConnectionId());
    }

    public ConnectionProcessor getConnectionProcessor() {
        return new ConnectionProcessor(this.getConnectionId());
    }

    public SDCProcessor getSDCProcessor() {
        return new SDCProcessor(this.getConnectionId());
    }

    public SDIProcessor getSDIProcessor() {
        return new SDIProcessor(this.getConnectionId());
    }

    public QueryProcessor getQueryProcessor() {
        return new QueryProcessor(this.getConnectionId());
    }

    public String getProperty(String propertyId) {
        return this.requestContext.getProperty(propertyId);
    }

    public String getProperty(String propertyId, String defaultValue) {
        String value = this.requestContext.getProperty(propertyId);
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    public PropertyList getPropertyList() {
        return this.requestContext.getPropertyList();
    }

    public PropertyList getPropertyList(String propertyId) {
        return this.requestContext.getPropertyList().getPropertyList(propertyId);
    }

    public boolean isTaskPage() {
        return this.taskPage;
    }

    public void setTaskPage(boolean taskPage) {
        this.taskPage = taskPage;
        if (taskPage) {
            PropertyList requestProps = this.requestContext.getPropertyList();
            this.taskContext = new TaskContext(requestProps);
            requestProps.setProperty("stepprops", this.taskContext.getStepProps());
        }
    }

    public TaskContext getTaskContext() {
        if (this.taskPage) {
            return this.taskContext;
        }
        return new TaskContext();
    }
}

