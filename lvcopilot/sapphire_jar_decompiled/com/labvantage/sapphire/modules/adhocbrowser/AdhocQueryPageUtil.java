/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdhocQueryPageUtil {
    public static String getDefaultSdcId(ServletRequest request, QueryProcessor qp) throws SapphireException {
        PropertyListCollection sdcs;
        PropertyList pagedata;
        RequestContext requestContext;
        String sdcid = request.getParameter("sdcid");
        if (sdcid == null && (requestContext = (RequestContext)request.getAttribute("RequestContext")) != null && requestContext.getPropertyList() != null && (pagedata = requestContext.getPropertyList().getPropertyList("pagedata")) != null && (sdcs = pagedata.getCollection("searchablesdcs")).size() > 0) {
            sdcid = sdcs.getPropertyList(0).getProperty("sdcid");
        }
        if (sdcid == null || sdcid.trim().length() == 0) {
            DataSet ds = qp.getSqlDataSet("select sdcid from sdc where searchableflag='Y' order by sdcid");
            if (ds == null || ds.getRowCount() == 0) {
                throw new SapphireException("No searchable SDCs defined.");
            }
            sdcid = ds.findRow("sdcid", "Sample") >= 0 ? "Sample" : ds.getString(0, "sdcid");
        }
        return sdcid;
    }

    public static String getRestrictiveWhere(String sdcid, PropertyList pagedata, PageContext pageContext) {
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        String restrictiveWhere = "";
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                restrictiveWhere = searchablesdcs.getPropertyList(i).getProperty("restrictivewhere");
                ConnectionInfo connectionInfo = new ConnectionProcessor(pageContext).getConnectionInfo(HttpUtil.getConnectionId(pageContext));
                if (restrictiveWhere.indexOf("[currentuser]") >= 0) {
                    restrictiveWhere = StringUtil.replaceAll(restrictiveWhere, "[currentuser]", connectionInfo.getSysuserId());
                }
                if (restrictiveWhere.indexOf("$G{") != 0) break;
                HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
                bindMap.put("user", connectionInfo.getUserAttributeMap());
                bindMap.put("pagedata", pagedata);
                try {
                    restrictiveWhere = GroovyUtil.getInstance(connectionInfo).evaluateSecure(restrictiveWhere, bindMap);
                    break;
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return restrictiveWhere;
    }

    public static PropertyList getDataEntryPropertyList(String sdcid, PropertyList pagedata) {
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        PropertyList dataentryPL = null;
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                dataentryPL = searchablesdcs.getPropertyList(i).getPropertyList("dataentrysearch");
                break;
            }
        }
        return dataentryPL;
    }

    public static PropertyListCollection getSDCColumnPLCollection(String sdcid, PropertyList pagedata, PropertyListCollection ddtcolumns, TranslationProcessor tp) {
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        PropertyListCollection columns = null;
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                columns = searchablesdcs.getPropertyList(i).getCollection("searchablecolumns");
                break;
            }
        }
        ElementUtil.setColumnDefaultTitle(columns, ddtcolumns, tp);
        return columns;
    }

    public static boolean isShowAllSearchableColumns(String sdcid, PropertyList pagedata) {
        boolean isShowAllSearchableColumns = false;
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        if (searchablesdcs != null) {
            for (int i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                isShowAllSearchableColumns = "Y".equals(searchablesdcs.getPropertyList(i).getProperty("showallsearchablecolumns"));
                break;
            }
        }
        return isShowAllSearchableColumns;
    }

    public static PropertyList getColumnPropertyList(String sdcid, String columnid, PropertyList pagedata, PropertyListCollection ddtcolumns, TranslationProcessor tp) {
        PropertyListCollection columns = AdhocQueryPageUtil.getSDCColumnPLCollection(sdcid, pagedata, ddtcolumns, tp);
        PropertyList colPL = null;
        if (columns != null) {
            for (int c = 0; c < columns.size(); ++c) {
                if (!columnid.equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                colPL = columns.getPropertyList(c);
                break;
            }
        }
        return colPL;
    }

    public static String getDetailGroupJsVariables(String sdcid, PropertyList pagedata) {
        int i;
        StringBuffer addgroupbuttons = new StringBuffer("\nvar addgroupbuttons='");
        StringBuffer addgroupprefixes = new StringBuffer("\nvar addgroupprefixes='");
        StringBuffer addgrouptitles = new StringBuffer("\nvar addgrouptitles='");
        PropertyListCollection searchablesdcs = null;
        if (pagedata != null) {
            searchablesdcs = pagedata.getCollection("searchablesdcs");
        }
        ArrayList buttons = null;
        if (searchablesdcs != null) {
            for (i = 0; i < searchablesdcs.size(); ++i) {
                if (!sdcid.equals(searchablesdcs.getPropertyList(i).getProperty("sdcid"))) continue;
                buttons = searchablesdcs.getPropertyList(i).getCollection("addgroupbuttons");
                break;
            }
        }
        if (buttons != null) {
            for (i = 0; i < buttons.size(); ++i) {
                PropertyList buttonPL = ((PropertyListCollection)buttons).getPropertyList(i);
                if (buttonPL.getProperty("sdidetail").length() <= 0) continue;
                String delimiter = i == 0 ? "" : ";";
                addgroupprefixes.append(delimiter + buttonPL.getProperty("sdidetail"));
                addgroupbuttons.append(delimiter + buttonPL.getProperty("text"));
                addgrouptitles.append(delimiter + buttonPL.getProperty("grouptitle"));
            }
        }
        return addgroupbuttons + "';" + addgroupprefixes + "';" + addgrouptitles + "';";
    }

    public static String getToolBarHtml(PropertyList pagedata, PageContext pageContext, TranslationProcessor tp) {
        StringBuffer buttonhtml = new StringBuffer();
        PropertyListCollection buttons = null;
        buttons = "popup".equals(pagedata.getProperty("mode")) || "Y".equals(pageContext.getRequest().getParameter("fromsearch")) ? AdhocQueryPageUtil.getPopupButtonCollection(tp) : AdhocQueryPageUtil.getDefaultButtonCollection(tp);
        if (pagedata.getCollection("buttons") != null && pagedata.getCollection("buttons").size() > 0) {
            buttons.addAll(pagedata.getCollection("buttons"));
        }
        for (int i = 0; i < buttons.size(); ++i) {
            PropertyList buttonprops = buttons.getPropertyList(i);
            AdhocQueryPageUtil.fillDefaultButtonProps(buttonprops, tp);
        }
        AdvancedToolbar toolbar = new AdvancedToolbar();
        PropertyList element = new PropertyList();
        toolbar.setPageContext(pageContext);
        element.setProperty("buttons", buttons);
        toolbar.setElementProperties(element);
        element.setProperty("rendermode", "Button");
        element.setProperty("displaystyle", "Modern");
        element.setProperty("pagetitle", "&nbsp;&nbsp;&nbsp;" + tp.translate("Adhoc Query") + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        return toolbar.getHtml();
    }

    private static void fillDefaultButtonProps(PropertyList buttonprops, TranslationProcessor tp) {
        PropertyList commonprops = new PropertyList();
        commonprops.setProperty("show", "Y");
        commonprops.setProperty("text", buttonprops.getProperty("text"));
        commonprops.setProperty("image", buttonprops.getProperty("img"));
        commonprops.setProperty("tip", buttonprops.getProperty("tip"));
        commonprops.setProperty("group", buttonprops.getProperty("group"));
        commonprops.setProperty("dropdowngroup", buttonprops.getProperty("dropdowngroup"));
        buttonprops.setProperty("commonprops", commonprops);
        PropertyList userbuttonprops = new PropertyList();
        userbuttonprops.setProperty("action", buttonprops.getProperty("js"));
        buttonprops.setProperty("userbuttonprops", userbuttonprops);
    }

    private static PropertyListCollection getDefaultButtonCollection(TranslationProcessor tp) {
        PropertyListCollection pc = new PropertyListCollection();
        pc.add(AdhocQueryPageUtil.getSearchButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getCountButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getSaveButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getDeleteButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getExportExcelButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getExportPDFButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getExportAllButtonPL(tp));
        return pc;
    }

    private static PropertyListCollection getPopupButtonCollection(TranslationProcessor tp) {
        PropertyListCollection pc = new PropertyListCollection();
        pc.add(AdhocQueryPageUtil.getSearchButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getCountButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getSaveButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getDeleteButtonPL(tp));
        pc.add(AdhocQueryPageUtil.getCloseButtonPL(tp));
        return pc;
    }

    private static PropertyList getSearchButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "searchNow();");
        pl.setProperty("text", tp.translate("Search Now"));
        pl.setProperty("tip", tp.translate("Search Now"));
        pl.setProperty("id", "searchbutton");
        pl.setProperty("appearance", "");
        pl.setProperty("img", "WEB-CORE/images/gif/Search.gif");
        pl.setProperty("group", "Search");
        return pl;
    }

    private static PropertyList getCustomButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "alert( dataview_iframe.getSelected() );");
        pl.setProperty("text", "View Ingredients");
        pl.setProperty("tip", "");
        pl.setProperty("id", "customid");
        pl.setProperty("appearance", "");
        pl.setProperty("img", "");
        pl.setProperty("group", "Custom Operations");
        pl.setProperty("dropdowngroup", "Other Operations");
        return pl;
    }

    private static PropertyList getCustomButtonPL1(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "alert( dataview_iframe.getSelected() );");
        pl.setProperty("text", "Do Custom Stuff");
        pl.setProperty("tip", "");
        pl.setProperty("id", "customid");
        pl.setProperty("appearance", "");
        pl.setProperty("img", "");
        pl.setProperty("group", "Custom Operations");
        pl.setProperty("dropdowngroup", "Other Operations");
        return pl;
    }

    private static PropertyList getCountButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "countNow();");
        pl.setProperty("text", tp.translate("Count Results"));
        pl.setProperty("tip", tp.translate("Show total results count met the criteria without the max results limit"));
        pl.setProperty("id", "countbutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/Help.gif");
        pl.setProperty("group", "Search");
        return pl;
    }

    private static PropertyList getSaveButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "saveAdhocQueryNow()");
        pl.setProperty("text", tp.translate("Save Query"));
        pl.setProperty("tip", tp.translate("Save the current query definition for later use"));
        pl.setProperty("id", "savebutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/Save.gif");
        pl.setProperty("group", "Save");
        return pl;
    }

    private static PropertyList getDeleteButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "deleteTheQueryNow()");
        pl.setProperty("text", tp.translate("Delete Query"));
        pl.setProperty("tip", tp.translate("Delete the current query"));
        pl.setProperty("id", "deletebutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/Delete.gif");
        pl.setProperty("group", "Save");
        return pl;
    }

    private static PropertyList getExportExcelButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "exportExcel()");
        pl.setProperty("text", tp.translate("Export To Excel"));
        pl.setProperty("tip", tp.translate("Export the search result to Excel spread sheet view"));
        pl.setProperty("id", "exportxlsbutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/ExporttoExcel.gif");
        pl.setProperty("group", "Export");
        return pl;
    }

    private static PropertyList getExportAllButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "exportAll()");
        pl.setProperty("text", tp.translate("Search And Export All"));
        pl.setProperty("tip", tp.translate("Export all query results without the max results limit"));
        pl.setProperty("id", "exportallbutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/ExporttoExcel.gif");
        pl.setProperty("group", "Export");
        return pl;
    }

    private static PropertyList getExportPDFButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "exportPDF()");
        pl.setProperty("text", tp.translate("Export To PDF"));
        pl.setProperty("tip", tp.translate("Export the search result to PDF view"));
        pl.setProperty("id", "exportpdfbutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/ExporttoPDF.gif");
        pl.setProperty("group", "Export");
        return pl;
    }

    private static PropertyList getCloseButtonPL(TranslationProcessor tp) {
        PropertyList pl = new PropertyList();
        pl.setProperty("js", "window.close()");
        pl.setProperty("text", tp.translate("Close"));
        pl.setProperty("tip", tp.translate("Close the window"));
        pl.setProperty("id", "closebutton");
        pl.setProperty("appearance", "standard");
        pl.setProperty("img", "WEB-CORE/images/gif/Close.gif");
        pl.setProperty("group", "Close");
        return pl;
    }
}

