/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListMaintUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 76319 $";
    private String position = "";
    private boolean embedded = false;
    private String initiallWidth = "0%";
    private String initialHeight = "0%";
    private RequestContext requestContext;
    private PageContext pageContext;
    private PropertyList pagedata;
    private HttpServletRequest request;
    private String listpageid = "";

    public ListMaintUtil(PageContext pageContext) {
        this.pageContext = pageContext;
        this.requestContext = RequestContext.getRequestContext(pageContext);
        this.pagedata = this.requestContext.getPropertyList("pagedata");
        this.listpageid = this.pagedata.getProperty("webpageid");
        PropertyList detailpageprops = this.pagedata.getPropertyList("detailpageprops");
        if (detailpageprops != null) {
            this.initiallWidth = detailpageprops.getProperty("initialwidth", "53%").replaceAll("50%", "53%");
            this.initialHeight = detailpageprops.getProperty("initialheight", "50%");
            this.position = detailpageprops.getProperty("position", "Right");
            Browser browser = new Browser(pageContext);
            if ("desktop".equalsIgnoreCase(browser.getGUIMode().getId())) {
                this.updateToolbarButtons(this.requestContext.getPropertyList("advancedtoolbar"));
            }
        }
    }

    private void updateToolbarButtons(PropertyList advancedtoolbar) {
        if (advancedtoolbar != null) {
            PropertyListCollection listpagebuttons = advancedtoolbar.getCollection("buttons");
            for (PropertyList button : listpagebuttons) {
                PropertyList standardbuttonprops;
                String page;
                if (!"Standard".equalsIgnoreCase(button.getProperty("buttontype")) || button.getPropertyList("commonprops").getProperty("show", "Y").equalsIgnoreCase("N") || !button.getPropertyList("commonprops").getProperty("showindetailpanel", "N").equalsIgnoreCase("Y") || (page = (standardbuttonprops = button.getPropertyList("standardbuttonprops")).getProperty("page", "")).length() <= 0) continue;
                standardbuttonprops.setProperty("page", page + "&listpageid=" + this.listpageid + "&embeddedinlist=N");
                button.setProperty("primaryvalidation", "checkTBMappingToDP('" + button.getProperty("id", "") + "','NS');" + button.getProperty("primaryvalidation", ""));
            }
        }
    }

    public ListMaintUtil(PageContext pageContext, HttpServletRequest request) {
        this.pageContext = pageContext;
        this.request = request;
        this.requestContext = RequestContext.getRequestContext(pageContext);
        this.pagedata = this.requestContext.getPropertyList("pagedata");
        this.listpageid = this.pagedata.getProperty("listpageid");
        String embeddedinlist = request.getParameter("embeddedinlist");
        if (embeddedinlist != null && "Y".equalsIgnoreCase(embeddedinlist)) {
            RequestProcessor rp = new RequestProcessor(pageContext);
            try {
                PropertyList listPageProps = rp.getWebPageProperties(this.listpageid, this.requestContext);
                PropertyList detailpageprops = listPageProps.getPropertyList("pagedata").getPropertyList("detailpageprops");
                this.position = detailpageprops.getProperty("position", "Right");
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.embedded = "Y".equals(embeddedinlist);
        }
    }

    public String getPosition() {
        return this.position;
    }

    public boolean isPositionRight() {
        return this.position.equalsIgnoreCase("Right");
    }

    public boolean isPositionRightInline() {
        return this.position.equalsIgnoreCase("Right Inline");
    }

    public boolean isPositionBottm() {
        return this.position.equalsIgnoreCase("Bottom");
    }

    public boolean isEmbeddedMaint() {
        return this.embedded;
    }

    public String getInitiallWidth() {
        return this.initiallWidth;
    }

    public String getInitiallHeight() {
        return this.initialHeight;
    }

    public String getContentcellwidth() {
        if (this.isPositionRight()) {
            return 100 - Integer.parseInt(this.initiallWidth.substring(0, this.initiallWidth.length() - 1)) + "%";
        }
        if (this.isPositionRightInline()) {
            return 100 - Integer.parseInt(this.initiallWidth.substring(0, this.initiallWidth.length() - 1)) + "%";
        }
        return "";
    }

    public String getContentcellheight() {
        if (this.isPositionBottm()) {
            return 100 - Integer.parseInt(this.initialHeight.substring(0, this.initialHeight.length() - 1)) + "%";
        }
        return "";
    }

    public String getListWidthRightInline(boolean showNotes, boolean showNoteInitially) {
        String width = "";
        if (showNotes && showNoteInitially) {
            width = "80%";
        } else if (showNotes) {
            width = "100%";
        }
        return width;
    }

    public void removeLayout() {
        PropertyList layout = this.requestContext.getPropertyList("layout");
        String blankLayout = "WEB-OPAL/layouts/blank/blank.jsp";
        layout.setProperty("objectname", blankLayout);
    }

    public void updatePagedata() {
        this.pagedata.setProperty("embeddedinlist", this.request.getParameter("embeddedinlist"));
    }

    public void modifyMaintToolbarButtons() {
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        PropertyList advancedtoolbar = this.requestContext.getPropertyList("advancedtoolbar");
        PropertyList pagedata = this.requestContext.getPropertyList("pagedata");
        String nexturl = pagedata.getProperty("nexturl", "");
        String sdcid = pagedata.getProperty("sdcid", "");
        HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
        bindMap.put("element", advancedtoolbar);
        bindMap.put("pagedata", pagedata);
        bindMap.put("sdc", new SDCProcessor(this.pageContext).getSDCProperties(sdcid));
        if (nexturl.trim().length() > 0) {
            pagedata.setProperty("add_nexturl", nexturl);
            pagedata.setProperty("nexturl", "");
        }
        PropertyListCollection maintButtons = advancedtoolbar.getCollection("buttons");
        if (!this.isPositionRight()) {
            advancedtoolbar.setProperty("displaystyle", "Compact Ribbon");
        }
        for (PropertyList button : maintButtons) {
            String linkUrl;
            if (button.getProperty("id", "").equalsIgnoreCase("ReturnToList") || button.getProperty("id", "").equalsIgnoreCase("Close") || button.getProperty("id", "").equalsIgnoreCase("Details")) {
                button.getPropertyList("commonprops").setProperty("show", "N");
            }
            if (button.getProperty("id", "").equalsIgnoreCase("AddNote") || button.getProperty("id", "").equalsIgnoreCase("ShowNotes")) {
                button.getPropertyList("commonprops").setProperty("show", "N");
                PropertyList sdinotes = RequestContext.getRequestContext(this.pageContext).getPropertyList("sdinotes");
                if (sdinotes != null) {
                    sdinotes.setProperty("show", "N");
                }
            }
            if (button.getProperty("id", "").equalsIgnoreCase("shownext") || button.getProperty("id", "").equalsIgnoreCase("showprev")) {
                button.getPropertyList("commonprops").setProperty("show", "Y");
                String text = button.getPropertyList("commonprops").getProperty("text", "");
                String tip = button.getPropertyList("commonprops").getProperty("tip", text);
                button.getPropertyList("commonprops").setProperty("text", tp.translate(text));
                button.getPropertyList("commonprops").setProperty("tip", tp.translate(tip));
            }
            if (button.getProperty("id", "").equals("Add") && (linkUrl = button.getPropertyList("standardbuttonprops").getProperty("page", "")).length() > 0) {
                button.getPropertyList("standardbuttonprops").setProperty("page", linkUrl + "&embeddedinlist=Y");
            }
            String showProperty = button.getPropertyList("commonprops").getProperty("show", "Y");
            if (!"Action".equalsIgnoreCase(button.getProperty("buttontype")) || showProperty.equalsIgnoreCase("N")) continue;
            String show = "";
            if (showProperty.indexOf("$G{") == 0) {
                try {
                    show = GroovyUtil.getInstance(this.pageContext).evaluateSecure(showProperty, bindMap);
                }
                catch (SapphireException se) {
                    Logger.logError(se.getMessage());
                }
                if (show == null || !show.equalsIgnoreCase("true")) continue;
                pagedata.setProperty("actionbuttonexist", "Y");
                continue;
            }
            pagedata.setProperty("actionbuttonexist", "Y");
        }
    }
}

