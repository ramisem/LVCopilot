/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.pageelements;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.xml.PropertyList;

public abstract class BaseElement
extends BaseCustom {
    public static final String PROPERTYHANDLER_PREFIX = "__propertyhandler_";
    public static final String PROPERTYHANDLER_POST_PREFIX = "__postpropertyhandler_post_";
    public static final String PROPERTYHANDLER_PRE_PREFIX = "__propertyhandler_pre_";
    public static final String PROPERTYHANDLER_ELEMENTID = "__propertyhandler_elementid";
    protected PageContext pageContext;
    protected String prefix;
    protected String suffix;
    protected PropertyList element = new PropertyList();
    protected SDITagInfo sdiInfo;
    protected ConnectionInfo connectionInfo;
    protected String sdiFormId;
    protected RequestContext requestContext;
    private TranslationProcessor translationProcessor;
    protected String elementid;
    protected String elementType;
    protected String elementClass;
    protected Browser browser;
    protected String debugErrorMsg;

    public void setElementClass(String elementClass) {
        this.elementClass = elementClass;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public void setElementid(String elementid) {
        this.elementid = elementid;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
        if (pageContext != null) {
            this.requestContext = RequestContext.getRequestContext(pageContext);
            String connectionid = HttpUtil.getConnectionId(pageContext);
            if (connectionid != null && connectionid.length() > 0) {
                this.setConnectionId(connectionid);
                if (this.getConnectionProcessor() != null) {
                    this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                }
            }
            this.browser = new Browser(pageContext);
        }
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public void setSDIInfo(SDITagInfo sdiInfo) {
        this.sdiInfo = sdiInfo;
    }

    public SDITagInfo getSDIInfo() {
        return this.sdiInfo;
    }

    public String getSDIFormId() {
        return this.sdiFormId;
    }

    public void setSDIFormId(String sdiFormId) {
        this.sdiFormId = sdiFormId;
    }

    protected String getJavaScriptAPI() {
        return JavaScriptAPITag.getJavaScriptAPI(this.pageContext, this.requestContext, this.connectionInfo, true);
    }

    public void setElementProperties(String properties) {
        this.element = (PropertyList)JstlUtil.evaluateExpression(properties, this.pageContext);
        this.setElementResolution(this.element);
    }

    public void setElementProperties(PropertyList properties) {
        this.element = properties;
        this.setElementResolution(this.element);
    }

    public PropertyList getElementProperties() {
        return this.element;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    protected void logError(String errormsg) {
        this.debugErrorMsg = errormsg;
        this.logger.error(errormsg);
    }

    protected void logError(String errormsg, Throwable exception) {
        this.debugErrorMsg = errormsg;
        this.logger.error(errormsg, exception);
    }

    protected void logTrace(String tracemsg) {
        this.logger.info(tracemsg);
    }

    @Override
    protected TranslationProcessor getTranslationProcessor() {
        if (this.translationProcessor == null) {
            this.translationProcessor = this.pageContext == null ? new TranslationProcessor(this.getConnectionId()) : new TranslationProcessor(this.pageContext);
            if (this.element.getProperty("sdcid").length() > 0) {
                this.translationProcessor.setTextType(this.element.getProperty("sdcid"));
            } else if (this.sdiInfo != null && this.sdiInfo.getSdcid() != null) {
                this.translationProcessor.setTextType(this.sdiInfo.getSdcid());
            }
        }
        return this.translationProcessor;
    }

    public abstract String getHtml();

    protected String getError() {
        StringBuffer bufferReturn = new StringBuffer();
        if (this.debugErrorMsg != null && this.debugErrorMsg.length() > 0) {
            bufferReturn.append("<p><div style='margin:10px'><font color='red'><b>").append(this.debugErrorMsg);
            bufferReturn.append("<p></b></font><p></div>");
        }
        String stringReturn = bufferReturn.length() > 0 ? bufferReturn.toString() : "";
        return stringReturn;
    }

    protected void logDebug(String message) {
        this.logger.debug(message);
    }

    protected void logInfo(String message) {
        this.logger.info(message);
    }

    protected void logWarn(String message) {
        this.logger.warn(message);
    }

    public boolean isVisibleInAddMode() {
        return false;
    }

    private void setElementResolution(PropertyList element) {
        if (this.browser != null && this.browser.getGUIMode() != null) {
            element.setGuiMode(this.browser.getGUIMode().getId());
        }
    }

    public static void setElementResolution(PropertyList element, Browser browser) {
        if (browser != null && browser.getGUIMode() != null) {
            element.setGuiMode(browser.getGUIMode().getId());
        }
    }
}

