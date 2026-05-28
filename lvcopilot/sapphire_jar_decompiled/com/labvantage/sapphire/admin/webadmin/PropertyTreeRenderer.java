/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.admin.webadmin;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.PropertyListEditor;
import com.labvantage.sapphire.admin.propertytree.PropertyTreeDisplayOptions;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeBuilder;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyTreeRenderer {
    public static StringBuffer drawFullNodeTree(org.w3c.dom.Node node, ArrayList childNodes, int indent, boolean[] lastnode, String selectedpageid, String selectededition, String selectedelementid, String selectedgizmoid, DataSet allPages, DataSet allGizmos, String ptreetype, String ptreeid, String topNodeid, PropertyList userConfig, boolean expanded, boolean isDevMode, String compCode, String matchCategoryid, TranslationProcessor tp, DataSet allChangeLogDS) throws TransformerException {
        StringBuffer output = new StringBuffer();
        QueryProcessor qp = new QueryProcessor(tp.getConnectionid());
        SDCProcessor sdcProcessor = new SDCProcessor(tp.getConnectionid());
        ConnectionProcessor connectionProcessor = new ConnectionProcessor(tp.getConnectionid());
        if (node != null) {
            String parentnodeid = ((Element)node).getAttribute("id");
            boolean isParentProductOrComponent = parentnodeid.endsWith(" Product") || parentnodeid.contains(" Comp ") && !parentnodeid.endsWith(" Comp Custom");
            String changeControlFlag = CMTPolicy.getPolicy(tp.getConnectionid(), "PropertyTree").getChangeControlledFlag();
            HashMap<String, String> filter = new HashMap<String, String>();
            if (childNodes != null && childNodes.size() > 0) {
                for (int i = 0; i < childNodes.size(); ++i) {
                    Element childNode = (Element)childNodes.get(i);
                    String nodeid = childNode.getAttribute("id");
                    ArrayList childNodeChildNodes = PropertyTreeRenderer.getChildNodes(childNode);
                    boolean hasChildren = childNodeChildNodes != null && childNodeChildNodes.size() > 0;
                    String categoryList = childNode.getAttribute("categorylist");
                    if (categoryList == null) {
                        categoryList = "";
                    }
                    boolean showCategory = matchCategoryid.length() > 0 && (";" + categoryList + ";").indexOf(";" + matchCategoryid + ";") > 0 || matchCategoryid.length() == 0 && categoryList.length() > 0;
                    boolean locked = "Y".equals(childNode.getAttribute("locked"));
                    int newIndent = indent;
                    String subTopNodeid = topNodeid;
                    boolean subExpanded = expanded;
                    ++newIndent;
                    output.append("<tr style=\"display: ").append(expanded ? "block" : "none").append("\" id=\"topnode__").append(topNodeid).append("\"><td>");
                    output.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr>");
                    output.append("<td><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\"></td>");
                    for (int j = 0; j < indent; ++j) {
                        output.append("<td width=\"18\">");
                        output.append(lastnode[j] ? "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\">" : "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_bar" + (tp.isRTL() ? ".rtl" : "") + ".gif\">");
                        output.append("</td>");
                    }
                    lastnode[indent] = i + 1 == childNodes.size();
                    output.append("<td nowrap width=\"18\"><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_").append(lastnode[indent] ? "corner" : "tee").append((tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
                    String icon = PropertyTreeRenderer.getNodeIcon(nodeid, locked, isParentProductOrComponent, showCategory);
                    String label = PropertyTreeRenderer.getNodeLabel(nodeid, locked, isParentProductOrComponent);
                    if (indent == 0) {
                        output.append("<td nowrap width=\"8\">");
                        String localExpand = userConfig.getProperty("wa_nodes_" + StringUtil.replaceAll(nodeid, " ", "_"));
                        boolean bl = subExpanded = !localExpand.equals("plus");
                        if (hasChildren) {
                            output.append("<span onclick=\"showNodes( this, '").append(nodeid).append("')\"><img src=\"WEB-CORE/modules/webadmin/images/").append(subExpanded ? "minus" : "plus").append(".gif\"></span>");
                        }
                        output.append("</td>");
                        subTopNodeid = nodeid;
                    }
                    String changeLockIconHTML = "";
                    boolean isChangeControlLockOk = true;
                    if (("Y".equals(changeControlFlag) || "R".equals(changeControlFlag)) && allChangeLogDS != null) {
                        isChangeControlLockOk = CMTUtil.isChangeControlLockOk(allChangeLogDS, "PropertyTree", ptreeid, "", "", nodeid, connectionProcessor.getConnectionid());
                        changeLockIconHTML = CMTUtil.getChangeLockIconHTML(allChangeLogDS, "PropertyTree", ptreeid, "", "", nodeid, connectionProcessor.getConnectionid());
                    }
                    output.append("<td><a href=\"#\" " + (categoryList.length() > 0 ? "title=\"" + categoryList + "\"" : "") + "onClick=\"showNodeMaintenance( '").append(nodeid).append("'").append(isChangeControlLockOk ? ", 'Y'" : ", 'N'").append(");sapphire.events.cancelEvent(event, false);\">");
                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/").append(icon).append("\">");
                    output.append("</a></td>");
                    output.append("<td nowrap valign=\"middle\"><a style=\"text-decoration:none\" href=\"javascript:showNodeMaintenance( '").append(nodeid).append("'").append(isChangeControlLockOk ? ", 'Y'" : ", 'N'").append(");sapphire.events.cancelEvent(event, false);\"><span title=\"" + nodeid + "\" id=\"label__").append(nodeid).append("\" categorylist=\"").append(categoryList).append("\" locked=\"").append(locked ? "Y" : "N").append("\" parentproductorcomponent=\"").append(isParentProductOrComponent ? "Y" : "N").append("\">");
                    output.append("&nbsp;" + label);
                    output.append("</span></a>");
                    output.append(changeLockIconHTML);
                    filter.put("extendnodeid", nodeid);
                    DataSet pages = allPages.getFilteredDataSet(filter);
                    int pageCount = pages.size();
                    boolean expand = false;
                    if (pageCount > 0) {
                        HashMap<String, String> findMap = new HashMap<String, String>();
                        findMap.put("webpageid", selectedpageid);
                        findMap.put("productedition", selectededition);
                        findMap.put("elementid", selectedelementid);
                        expand = pages.findRow(findMap) >= 0;
                        output.append("&nbsp;&nbsp;<span pageexpander=\"Y\" onclick=\"showPages( this, 'pagerow__").append(nodeid).append("')\"><img src=\"WEB-CORE/modules/webadmin/images/").append(expand ? "minus" : "plus").append(".gif\"></span> (").append(pageCount).append(")");
                    }
                    DataSet gizmos = allGizmos.getFilteredDataSet(filter);
                    int gizmoCount = gizmos.size();
                    output.append("</td>");
                    output.append("</tr></table>");
                    output.append("</td></tr>");
                    if (pageCount > 0) {
                        output.append(PropertyTreeRenderer.drawPageTree(childNode, childNodeChildNodes, nodeid, newIndent, lastnode, pages, ptreetype, ptreeid, subTopNodeid, isDevMode, compCode, expand, tp, allChangeLogDS));
                    }
                    if (gizmoCount > 0) {
                        output.append(PropertyTreeRenderer.drawGizmoTree(childNode, childNodeChildNodes, nodeid, newIndent, lastnode, gizmos, subTopNodeid, tp, allChangeLogDS));
                    }
                    output.append(PropertyTreeRenderer.drawFullNodeTree(childNode, childNodeChildNodes, newIndent, lastnode, selectedpageid, selectededition, selectedelementid, selectedgizmoid, allPages, allGizmos, ptreetype, ptreeid, subTopNodeid, userConfig, subExpanded, isDevMode, compCode, matchCategoryid, tp, allChangeLogDS));
                }
            }
        }
        return output;
    }

    public static String drawPageTree(org.w3c.dom.Node node, ArrayList subnodes, String nodeid, int indent, boolean[] lastnode, DataSet pages, String ptreetype, String ptreeid, String topnodeid, boolean isDevMode, String compCode, boolean expand, TranslationProcessor tp, DataSet allChangeLogDS) throws TransformerException {
        int pagecount;
        SDCProcessor sdcProcessor = new SDCProcessor(tp.getConnectionid());
        String changeControlFlag = sdcProcessor.getProperty("WebPage", "changecontrolledflag");
        StringBuffer output = new StringBuffer();
        if (node != null && (pagecount = pages.getRowCount()) > 0) {
            boolean showElementid = false;
            for (int i = 0; i < pages.size(); ++i) {
                if (!pages.getValue(i, "elementid").equals(pages.getValue(0, "elementid"))) {
                    showElementid = true;
                }
                pages.setString(i, "__sort", pages.getValue(i, "extendwebpageid", pages.getValue(i, "webpageid")));
            }
            pages.sort("__sort,extendwebpageid");
            lastnode[indent] = subnodes == null || subnodes.size() == 0;
            for (int p = 0; p < pagecount; ++p) {
                String pageid;
                output.append("<tr style=\"display:").append(expand ? "block" : "none").append("\" name=\"topnode__" + topnodeid + "\" id=\"pagerow__").append(nodeid).append("\"><td>");
                output.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr>");
                output.append("<td><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\"></td>");
                for (int j = 0; j < indent; ++j) {
                    output.append("<td nowrap width=\"18\">");
                    output.append(lastnode[j] ? "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\">" : "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_bar" + (tp.isRTL() ? ".rtl" : "") + ".gif\">");
                    output.append("</td>");
                }
                output.append("<td nowrap width=\"18\"><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_").append(p == pagecount - 1 ? "corner" : "tee").append((tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
                String pageidurl = pageid = pages.getString(p, "webpageid");
                String edition = pages.getString(p, "productedition");
                String elementid = pages.getString(p, "elementid");
                String extendwebpageid = pages.getValue(p, "extendwebpageid");
                String extendproductedition = pages.getValue(p, "extendproductedition");
                String componentValueTree = pages.getValue(p, "componentvaluetree");
                String pageCompCode = pages.getValue(p, "compcode");
                boolean isCompPage = pageCompCode.length() > 0 && pageCompCode.equals(compCode);
                String changeLockIconHTML = "";
                boolean isChangeControlLockOk = true;
                if ("Y".equals(changeControlFlag) && allChangeLogDS != null) {
                    isChangeControlLockOk = CMTUtil.isChangeControlLockOk(allChangeLogDS, "WebPage", pageid, edition, "", "", tp.getConnectionid());
                    changeLockIconHTML = CMTUtil.getChangeLockIconHTML(allChangeLogDS, "WebPage", pageid, edition, "", "", tp.getConnectionid());
                }
                String pageMaint = "showPageMaintenance( '" + pageid + "', '" + edition + "','" + elementid + "', 'N', '" + (isChangeControlLockOk ? "Y" : "N") + "' );sapphire.events.cancelEvent(event, false);";
                String productPageMaint = "showPageMaintenance( '" + pageid + "', '" + edition + "','" + elementid + "', 'Y', '" + (isChangeControlLockOk ? "Y" : "N") + "' );sapphire.events.cancelEvent(event, false);";
                String compOverridePageMaint = "showPageMaintenance( '" + pageid + "', '" + edition + "','" + elementid + "', 'CO;[compcode]', '" + (isChangeControlLockOk ? "Y" : "N") + "' );sapphire.events.cancelEvent(event, false);";
                String compPageMaint = "showPageMaintenance( '" + pageid + "', '" + edition + "','" + elementid + "', 'C', '" + (isChangeControlLockOk ? "Y" : "N") + "' );sapphire.events.cancelEvent(event, false);";
                String compOverrideLink = "";
                output.append("<td nowrap>");
                if (extendwebpageid.length() > 0) {
                    output.append("<img height=\"22\" width=\"12\" src=\"WEB-CORE/modules/webadmin/images/menu_line.gif\">");
                }
                if (isCompPage) {
                    output.append("<a title=\"Component Properties\" href=\"javascript:" + compPageMaint + "\">");
                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/pagelocked.gif\">");
                    output.append("</a>");
                } else {
                    output.append("<a title=\"Product Properties\" href=\"javascript:" + productPageMaint + "\">");
                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/pagelocked.gif\">");
                    output.append("</a>");
                    if (componentValueTree.length() > 0) {
                        try {
                            PropertyList componentValueTreePL = new PropertyList();
                            componentValueTreePL.setPropertyList(componentValueTree);
                            PropertyListCollection components = componentValueTreePL.getCollectionNotNull("components");
                            if (components.size() > 0) {
                                for (int i = 0; i < components.size(); ++i) {
                                    PropertyList cpl = components.getPropertyList(i);
                                    String comp = cpl.getProperty("compcode");
                                    output.append("<a title=\"Component " + comp + " Properties\" href=\"javascript:" + StringUtil.replaceAll(compOverridePageMaint, "[compcode]", comp) + "\">");
                                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/componentpageoverride.jpg\">");
                                    output.append("</a>");
                                    if (!comp.equals(compCode)) continue;
                                    compOverrideLink = StringUtil.replaceAll(compOverridePageMaint, "[compcode]", comp);
                                }
                            }
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!isDevMode && !isCompPage) {
                    output.append("<a href=\"javascript:" + pageMaint + "\">");
                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/page").append(edition.equals("V3") ? "v3" : (edition.equals("R5") ? "r5" : "")).append(".gif\">");
                    output.append("</a>");
                }
                if (isDevMode || isCompPage) {
                    output.append("<a title=\"Custom Page Overrides - should be empty\" href=\"javascript:" + pageMaint + "\">");
                    output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/page").append(edition.equals("V3") ? "v3" : (edition.equals("R5") ? "r5" : "")).append(".gif\">");
                    output.append("</a>");
                }
                String editionLabel = showElementid && elementid != null || "Element".equals(ptreetype) ? "(" + elementid + ")" : "";
                output.append("<a style=\"color: indigo; text-decoration:none\" href=\"" + (isDevMode ? productPageMaint : (isCompPage ? compPageMaint : (compOverrideLink.length() > 0 ? compOverrideLink : pageMaint))) + "\" onClick=\"" + (isDevMode ? productPageMaint : (isCompPage ? compPageMaint : (compOverrideLink.length() > 0 ? compOverrideLink : pageMaint))) + "\">");
                output.append("<span id=\"label__").append(pageid).append("__").append(edition).append("__").append(elementid).append("\">");
                output.append(pageid).append(" ").append(editionLabel);
                output.append("</span></a>");
                output.append(changeLockIconHTML);
                output.append("</td>");
                output.append("</tr></table>");
                output.append("</td></tr>");
            }
        }
        return output.toString();
    }

    public static String drawGizmoTree(org.w3c.dom.Node node, ArrayList subnodes, String nodeid, int indent, boolean[] lastnode, DataSet gizmos, String topnodeid, TranslationProcessor tp, DataSet allChangeLogDS) throws TransformerException {
        int gizmoCount;
        SDCProcessor sdcProcessor = new SDCProcessor(tp.getConnectionid());
        String changeControlFlag = sdcProcessor.getProperty("LV_GizmoDef", "changecontrolledflag");
        StringBuffer output = new StringBuffer();
        if (node != null && (gizmoCount = gizmos.getRowCount()) > 0) {
            lastnode[indent] = subnodes == null || subnodes.size() == 0;
            for (int p = 0; p < gizmoCount; ++p) {
                output.append("<tr style=\"display:block\" name=\"topnode__" + topnodeid + "\"><td>");
                output.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr>");
                output.append("<td><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\"></td>");
                for (int j = 0; j < indent; ++j) {
                    output.append("<td nowrap width=\"18\">");
                    output.append(lastnode[j] ? "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_blank.gif\">" : "<img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_bar" + (tp.isRTL() ? ".rtl" : "") + ".gif\">");
                    output.append("</td>");
                }
                output.append("<td nowrap valign=\"middle\" width=\"18\"><img height=\"22\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_").append(p == gizmoCount - 1 ? "corner" : "tee").append((tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
                String gizmoid = gizmos.getString(p, "gizmodefid");
                String changeLockIconHTML = "";
                if ("Y".equals(changeControlFlag) && allChangeLogDS != null) {
                    changeLockIconHTML = CMTUtil.getChangeLockIconHTML(allChangeLogDS, "LV_GizmoDef", gizmoid, "", "", "", tp.getConnectionid());
                }
                String gizmoMaint = "showGizmoMaintenance( '" + gizmoid + "' );sapphire.events.cancelEvent(event, false);";
                output.append("<td nowrap valign=\"middle\" >");
                output.append("<a title=\"Component Properties\" href=\"javascript:" + gizmoMaint + "\">");
                output.append("<img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/images/gif/CheckIn.gif\">");
                output.append("</a>");
                output.append("</td>");
                output.append("<td nowrap valign=\"middle\" >");
                output.append("<a style=\"color: indigo; text-decoration:none\" href=\"javascript: " + gizmoMaint + "\">");
                output.append("<span id=\"label__").append(gizmoid).append("\">");
                output.append("&nbsp;" + gizmoid);
                output.append("</span></a>");
                output.append(changeLockIconHTML);
                output.append("</td>");
                output.append("</tr></table>");
                output.append("</td></tr>");
            }
        }
        return output.toString();
    }

    public static ArrayList getChildNodes(org.w3c.dom.Node node) {
        NodeList childNodes = node.getChildNodes();
        ArrayList<org.w3c.dom.Node> returnNodes = new ArrayList<org.w3c.dom.Node>();
        for (int i = 0; i < childNodes.getLength() && returnNodes.size() == 0; ++i) {
            org.w3c.dom.Node n = childNodes.item(i);
            if (!"nodelist".equals(n.getNodeName())) continue;
            NodeList childNodes2 = n.getChildNodes();
            for (int j = 0; j < childNodes2.getLength(); ++j) {
                org.w3c.dom.Node tempNode = childNodes2.item(j);
                if (!"node".equals(tempNode.getNodeName())) continue;
                returnNodes.add(tempNode);
            }
        }
        return returnNodes;
    }

    private static String getNodeLabel(String nodeid, boolean locked, boolean isParentProductOrComponent) {
        String label = nodeid;
        if (isParentProductOrComponent && (nodeid.endsWith(" Custom") || nodeid.endsWith(" ImplCustom"))) {
            label = "(Custom)";
        } else if (locked && nodeid.endsWith(" Product")) {
            label = nodeid.substring(0, label.length() - 8) + " (P)&#8206;";
        } else if (locked && nodeid.contains(" Comp ")) {
            String nodeCompName = "(" + nodeid.substring(label.lastIndexOf(" ") + 1) + ")";
            label = isParentProductOrComponent ? nodeCompName : nodeid.substring(0, nodeid.indexOf(" Comp ")) + " " + nodeCompName;
        } else if (locked && nodeid.endsWith(" Impl")) {
            label = nodeid.substring(0, label.length() - 5) + " (I)&#8206;";
        }
        return label;
    }

    private static String getNodeIcon(String nodeid, boolean locked, boolean isParentProductOrComponent, boolean showCategory) {
        String icon = locked ? "lockednode.gif" : (isParentProductOrComponent && (nodeid.endsWith(" Custom") || nodeid.endsWith(" ImplCustom")) ? "customnode.gif" : "node.gif");
        if (showCategory) {
            icon = "category" + icon;
        }
        return icon;
    }

    public static String drawNodeTree(String uniqueid, String ptreetype, String selectednodeid, org.w3c.dom.Node node, int indent, boolean[] lastnode, TranslationProcessor tp) throws TransformerException {
        StringBuffer output = new StringBuffer();
        output.append("<tr><td><table cellspacing=0 cellpadding=0><tr><td height=\"27\" width=\"18\"><img src=\"WEB-CORE/modules/webadmin/images/ptree_").append(ptreetype.toLowerCase()).append(".gif\"></td><td colspan=\"502\"><b>&nbsp;").append(ptreetype).append(": ").append(uniqueid).append("</b></td></tr></table></td></tr>");
        output.append("<tr><td><table cellspacing=0 cellpadding=0><tr><td width=\"18\"><img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_corner" + (tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
        output.append("<td width=\"3\">");
        output.append("<input type=\"radio\" ").append(selectednodeid == null || selectednodeid.equals("__root") ? "checked" : "").append(" name=\"").append(uniqueid).append("selector\" id=\"").append(uniqueid).append("__root\" nodeid=\"__root\">");
        output.append("</td>");
        output.append("<td height=\"27\" width=\"18\"><img src=\"WEB-CORE/modules/webadmin/images/node.gif\"></td><td><label for=\"").append(uniqueid).append("__root\"><i>&lt;Root&gt;</i></label></td>");
        output.append("<td width=\"8\">&nbsp;</td></tr></td></table></tr>");
        output.append(PropertyTreeRenderer.drawSubNodeTree(uniqueid, selectednodeid, node, indent, lastnode, tp));
        return output.toString();
    }

    private static StringBuffer drawSubNodeTree(String uniqueid, String selectednodeid, org.w3c.dom.Node node, int indent, boolean[] lastnode, TranslationProcessor tp) throws TransformerException {
        StringBuffer output = new StringBuffer();
        String parentnodeid = ((Element)node).getAttribute("id");
        boolean isParentProductOrComponent = parentnodeid.endsWith(" Product") || parentnodeid.contains(" Comp ") && !parentnodeid.endsWith("Custom");
        ArrayList subnodes = PropertyTreeRenderer.getChildNodes(node);
        if (subnodes.size() > 0) {
            for (int i = 0; i < subnodes.size(); ++i) {
                Element childnode = (Element)subnodes.get(i);
                String nodeid = childnode.getAttribute("id");
                boolean locked = "Y".equals(childnode.getAttribute("locked"));
                output.append("<tr><td><table cellspacing=0 cellpadding=0><tr>");
                output.append("<td width=\"18\"><img width=\"18\" src=\"WEB-CORE/images/blank.gif\"></td>");
                for (int j = 0; j < indent; ++j) {
                    output.append("<td width=\"18\">");
                    output.append(lastnode[j] ? "<img width=\"18\" src=\"WEB-CORE/images/blank.gif\">" : "<img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_bar" + (tp.isRTL() ? ".rtl" : "") + ".gif\">");
                    output.append("</td>");
                }
                lastnode[indent] = i + 1 == subnodes.size();
                output.append("<td width=\"18\"><img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_").append(lastnode[indent] ? "corner" : "tee").append((tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
                if (!locked) {
                    output.append("<td width=\"3\">");
                    output.append("<input type=\"radio\" ").append(selectednodeid != null && selectednodeid.equals(nodeid) ? "checked" : "").append(" name=\"").append(uniqueid).append("selector\" id=\"").append(uniqueid).append("__").append(nodeid).append("\" nodeid=\"").append(nodeid).append("\">");
                    output.append("</td>");
                }
                output.append("<td width=\"16\"><img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/").append(locked ? "lockednode.gif" : "node.gif").append("\"></td>");
                output.append("<td nowrap>");
                String label = PropertyTreeRenderer.getNodeLabel(nodeid, locked, isParentProductOrComponent);
                output.append("<label for=\"").append(uniqueid).append("__").append(nodeid).append("\">&nbsp;").append(label).append("</label>");
                output.append("</td></tr></table></td></tr>");
                output.append(PropertyTreeRenderer.drawSubNodeTree(uniqueid, selectednodeid, childnode, indent + 1, lastnode, tp));
            }
        }
        return output;
    }

    public static String drawExportNodeTree(String uniqueid, String ptreetype, ArrayList nodes, org.w3c.dom.Node node, boolean multi, TranslationProcessor tp) throws TransformerException {
        return PropertyTreeRenderer.drawExportNodeTree(uniqueid, ptreetype, nodes, node, multi, tp, false);
    }

    public static String drawExportNodeTree(String uniqueid, String ptreetype, ArrayList nodes, org.w3c.dom.Node node, boolean multi, TranslationProcessor tp, boolean allowRootNodeSelection) throws TransformerException {
        boolean[] lastnode = new boolean[100];
        StringBuffer output = new StringBuffer();
        StringBuffer subnodeOutput = new StringBuffer();
        output.append("<tr><td height=\"27\" width=\"18\"><img src=\"WEB-CORE/modules/webadmin/images/ptree_").append(ptreetype.toLowerCase()).append(".gif\"></td><td colspan=\"502\"><b>&nbsp;").append(ptreetype).append(": ").append(uniqueid).append("</b></td></tr>");
        output.append("<td width=\"18\"><img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_corner" + (tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
        output.append("<td height=\"30\" width=\"3\">");
        if (allowRootNodeSelection) {
            output.append("<input type=\"").append(multi ? "checkbox" : "radio").append("\" ").append(nodes.contains("__root") ? "checked" : "").append(" name=\"").append(uniqueid).append("selector\"").append(" id=\"").append(uniqueid).append("__").append("__root").append("\" ").append(multi ? "ondblclick=\"nodeDblClicked( this )\"" : "").append(" nodeid=\"__root\"></td>");
        }
        output.append("<td width='16'><img src=\"WEB-CORE/modules/webadmin/images/node.gif\"></td><td colspan=\"502\"><label for=\"").append(uniqueid).append("__root\"><i>&lt;Root&gt;</i></label></td>");
        output.append("</tr>");
        ArrayList subnodelist = new ArrayList();
        output.append(PropertyTreeRenderer.drawSubMultiNodeTree(uniqueid, nodes, node, 0, lastnode, subnodelist, subnodeOutput, multi, tp));
        output.append("<script>");
        output.append("var subnodes = new Array();");
        output.append(subnodeOutput);
        output.append("</script>");
        return output.toString();
    }

    private static StringBuffer drawSubMultiNodeTree(String uniqueid, ArrayList nodes, org.w3c.dom.Node node, int indent, boolean[] lastnode, ArrayList subnodelist, StringBuffer subnodeOutput, boolean multi, TranslationProcessor tp) throws TransformerException {
        StringBuffer output = new StringBuffer();
        String parentnodeid = ((Element)node).getAttribute("id");
        boolean isParentProductOrComponent = parentnodeid.endsWith(" Product") || parentnodeid.contains(" Comp ") && !parentnodeid.endsWith(" Comp Custom");
        ArrayList subnodes = PropertyTreeRenderer.getChildNodes(node);
        if (subnodes.size() > 0) {
            for (int i = 0; i < subnodes.size(); ++i) {
                Element childnode = (Element)subnodes.get(i);
                String nodeid = childnode.getAttribute("id");
                boolean locked = "Y".equals(childnode.getAttribute("locked"));
                subnodelist.add(nodeid);
                output.append("<tr><td>&nbsp;</td>");
                for (int j = 0; j < indent; ++j) {
                    output.append("<td width=\"18\">");
                    output.append(lastnode[j] ? "&nbsp;" : "<img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_bar" + (tp.isRTL() ? ".rtl" : "") + ".gif\">");
                    output.append("</td>");
                }
                lastnode[indent] = i + 1 == subnodes.size();
                output.append("<td width=\"18\"><img height=\"27\" width=\"18\" src=\"WEB-CORE/modules/webadmin/images/menu_").append(lastnode[indent] ? "corner" : "tee").append((tp.isRTL() ? ".rtl" : "") + ".gif\"></td>");
                output.append("<td width=\"3\">");
                output.append("<input type=\"").append(multi ? "checkbox" : "radio").append("\" ").append(nodes.contains(nodeid) ? "checked" : "").append(" name=\"").append(uniqueid).append("selector\" id=\"").append(uniqueid).append("__").append(nodeid).append("\" ").append(multi ? "ondblclick=\"nodeDblClicked( this )\"" : "").append(" nodeid=\"").append(nodeid).append("\"></td>");
                output.append("<td width=\"16\"><img border=\"0\" width=\"16\" height=\"16\" src=\"WEB-CORE/modules/webadmin/images/").append(locked ? "lockednode.gif" : "node.gif").append("\"></td>");
                output.append("<td nowrap colspan=\"").append(500 - indent).append("\">");
                String label = PropertyTreeRenderer.getNodeLabel(nodeid, locked, isParentProductOrComponent);
                output.append("<label for=\"").append(uniqueid).append("__").append(nodeid).append("\">").append(label).append("</label>");
                output.append("</td></tr>");
                ArrayList thissubnodelist = new ArrayList();
                output.append(PropertyTreeRenderer.drawSubMultiNodeTree(uniqueid, nodes, childnode, indent + 1, lastnode, thissubnodelist, subnodeOutput, multi, tp));
                subnodeOutput.append("subnodes['").append(nodeid).append("'] = '");
                Iterator iterator = thissubnodelist.iterator();
                while (iterator.hasNext()) {
                    String subnodeid = (String)iterator.next();
                    subnodeOutput.append(subnodeid);
                    if (!iterator.hasNext()) continue;
                    subnodeOutput.append(";");
                }
                subnodeOutput.append("';\n");
            }
            if (parentnodeid.length() == 0) {
                subnodeOutput.append("subnodes['__root'] = '");
                Iterator iterator = subnodelist.iterator();
                while (iterator.hasNext()) {
                    String subnodeid = (String)iterator.next();
                    subnodeOutput.append(subnodeid);
                    if (!iterator.hasNext()) continue;
                    subnodeOutput.append(";");
                }
                subnodeOutput.append("';\n");
            }
        }
        return output;
    }

    public static String drawPropertyListidEditorTree(String uniqueid, PropertyTree pt, int indent, boolean[] lastnode, int showlevel) throws TransformerException, SapphireException {
        StringBuffer output = new StringBuffer();
        output.append("<tr>");
        output.append("<td>Node</td>");
        output.append("<td>Level</td>");
        output.append("<td>Property</td>");
        output.append("<td>Title</td>");
        output.append("<td>Parent</td>");
        output.append("<td>Current Name</td>");
        output.append("<td>#</td>");
        output.append("<td>New Name</td>");
        output.append("</tr>");
        com.labvantage.sapphire.xml.NodeList subnodes = pt.getNodeList();
        Counter counter = new Counter();
        output.append(PropertyTreeRenderer.drawPropertyListidEditorSubTree(pt, subnodes, indent, lastnode, showlevel, counter));
        output.append("<input type=\"hidden\" name=\"newnamecount\" value=\"").append(counter.counter + 1).append("\" />");
        output.append("<script>");
        output.append("var idcount=").append(counter.counter + 1).append(";");
        output.append("var idlist=';;");
        for (String propertylistid : counter.idlist) {
            output.append(propertylistid).append(";;");
        }
        output.append("';</script>");
        return output.toString();
    }

    private static StringBuffer drawPropertyListidEditorSubTree(PropertyTree pt, com.labvantage.sapphire.xml.NodeList subnodes, int indent, boolean[] lastnode, int showlevel, Counter counter) throws TransformerException, SapphireException {
        StringBuffer output = new StringBuffer();
        if (subnodes != null && subnodes.size() > 0) {
            for (int i = 0; i < subnodes.size(); ++i) {
                Node node = (Node)subnodes.get(i);
                String nodeid = node.getId();
                PropertyList pl = pt.getNodePropertyList(nodeid, true);
                output.append(PropertyTreeRenderer.drawPropertyListidEditorSubTreePropertyList(pt.getPropertyDefinitionList(), pl, nodeid, 1, showlevel, counter));
                output.append(PropertyTreeRenderer.drawPropertyListidEditorSubTree(pt, pt.getNodeDescendantList(nodeid), indent + 1, lastnode, showlevel, counter));
            }
        }
        return output;
    }

    private static StringBuffer drawPropertyListidEditorSubTreePropertyList(PropertyDefinitionList propertyDefinitionList, PropertyList masterPropertyList, String nodeid, int level, int showlevel, Counter counter) {
        StringBuffer output = new StringBuffer();
        for (PropertyDefinition propertyDefinition : propertyDefinitionList) {
            String propertyid = propertyDefinition.getId();
            if (!propertyDefinition.getType().equals("collection")) continue;
            PropertyDefinitionList subpropertydeflist = propertyDefinition.getPropertyDefinitionList();
            PropertyListCollection collection = masterPropertyList.getCollection(propertyid);
            if (collection == null) continue;
            for (PropertyList itemPropertyList : collection) {
                String titlepropertyid = subpropertydeflist.getTitlePropertyId();
                String title = itemPropertyList.getProperty(titlepropertyid);
                String currentnodeid = itemPropertyList.getPropertyTreeNodeId();
                if (!currentnodeid.equals(nodeid)) continue;
                output.append("\n<tr ").append(level == showlevel ? " class=\"highlightrow\"" : "").append(">");
                output.append("<td>").append(nodeid).append("</td>");
                output.append("<td>").append(level).append("</td>");
                output.append("<td>").append(propertyid).append("</td>");
                output.append("<td>").append(title).append("</td>");
                if (level <= showlevel) {
                    output.append("<td>").append(masterPropertyList.getId()).append("</td>");
                } else {
                    output.append("<td></td>");
                }
                output.append("<td>").append(itemPropertyList.getId()).append("</td>");
                counter.idlist.add(itemPropertyList.getId());
                if (level == showlevel) {
                    ++counter.counter;
                    output.append("<td>").append(counter.counter + 1).append("</td>");
                    output.append("<td><input index=\"").append(counter.counter).append("\" onkeypress=\"keyPress( this )\" onfocus=\"setFocus( this )\" onchange=\"checkId( this )\" id=\"newname_").append(counter.counter).append("\"  name=\"newname_").append(counter.counter).append("\" style=\"width: 200px\" value=\"\"/>");
                    output.append("<input type=\"hidden\" name=\"oldname_").append(counter.counter).append("\" value=\"").append(itemPropertyList.getId()).append("\"/>");
                    output.append("<input type=\"hidden\" name=\"node_").append(counter.counter).append("\" value=\"").append(nodeid).append("\"/>");
                    output.append("</td>");
                }
                output.append("</tr>");
                output.append(PropertyTreeRenderer.drawPropertyListidEditorSubTreePropertyList(subpropertydeflist, itemPropertyList, nodeid, level + 1, showlevel, counter));
            }
        }
        return output;
    }

    public static String getPropertyEditorPage(PageContext pageContext, PropertyTree tree, String nodeid, String formAction) {
        return PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, nodeid, formAction, null);
    }

    public static String getPropertyEditorPage(PageContext pageContext, PropertyTree tree, String nodeid, String formAction, HashMap<String, String> formInputs) {
        PropertyTreeDisplayOptions options = new PropertyTreeDisplayOptions();
        options.showAdvanced = false;
        options.collectionitemcopy = false;
        options.collectionitempaste = false;
        return PropertyTreeRenderer.getPropertyEditorPage(pageContext, tree, nodeid, formAction, formInputs, null);
    }

    public static String getPropertyEditorPage(PageContext pageContext, PropertyTree tree, String nodeid, String formAction, HashMap<String, String> formInputs, PropertyTreeDisplayOptions options) {
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        boolean readOnly = request.getParameter("readonly") != null ? "Y".equalsIgnoreCase(request.getParameter("readonly")) : (options != null ? options.readonly : false);
        StringBuffer output = new StringBuffer();
        try {
            String dothis;
            Node node = tree.getNode(nodeid);
            if (node == null) {
                throw new Exception("Unable to locate node " + nodeid + " in property tree");
            }
            PropertyDefinitionList propertyDefinitionList = tree.getPropertyDefinitionList();
            if (propertyDefinitionList == null) {
                throw new Exception("Property tree does not have definition list defined.");
            }
            HashMap<String, Object> requestParams = PropertyTreeBuilder.getRequestParams((ServletRequest)request);
            String string = requestParams == null ? request.getParameter("dothis") : (dothis = requestParams.get("dothis") == null ? null : requestParams.get("dothis").toString());
            if (dothis != null) {
                PropertyList parentPropertyList = tree.getNodePropertyList(nodeid, true);
                Document document = DOMUtil.getNewDocument("", false);
                ArrayList extraPropertyLists = new ArrayList();
                if (dothis.equals("pastepropertylist")) {
                    String TEMP_NODE = "tempnode";
                    PropertyListCollection dummyCollection = new PropertyListCollection();
                    Element dummyRoot = document.createElement("root");
                    PropertyTreeBuilder.buildPropertyValueTree(pageContext, null, dummyCollection, false, propertyDefinitionList, dummyRoot, "root", System.currentTimeMillis(), false);
                    Element newPropertyListElement = (Element)XPathAPI.selectSingleNode((org.w3c.dom.Node)dummyRoot, (String)"propertylist");
                    PropertyList newPropertyList = new PropertyList();
                    newPropertyList.setUsePropertyValues(true);
                    newPropertyList.addPropertyList(newPropertyListElement, false, TEMP_NODE);
                    Node tempNode = tree.createNode(TEMP_NODE, node);
                    tempNode.setPropertyList(newPropertyList);
                    dummyCollection = new PropertyListCollection();
                    dummyCollection.add(tree.getNodePropertyList(TEMP_NODE, true));
                    EditorUtil.loadExtraPropertyLists(extraPropertyLists, pageContext, document, dummyCollection);
                } else if (dothis.equals("copypropertylist")) {
                    pageContext.getSession().setAttribute("collectionitempastecontext", (Object)"maintpage");
                }
                Element newroot = document.createElement("root");
                PropertyListCollection collection = new PropertyListCollection();
                collection.add(parentPropertyList);
                PropertyTreeBuilder.buildPropertyValueTree(pageContext, null, collection, false, propertyDefinitionList, newroot, "root", System.currentTimeMillis(), false, null, null, extraPropertyLists);
                Element newPropertyListElement = (Element)XPathAPI.selectSingleNode((org.w3c.dom.Node)newroot, (String)"propertylist");
                PropertyList newPropertyList = new PropertyList();
                newPropertyList.setUsePropertyValues(true);
                newPropertyList.addPropertyList(newPropertyListElement, false, nodeid);
                node.setPropertyList(newPropertyList);
            }
            PropertyList propertyList = tree.getNodePropertyList(nodeid, true);
            output.append("<script language=\"JavaScript\" src=\"WEB-CORE/modules/webadmin/scripts/editors.js\"></script>");
            output.append("<script type=\"text/javascript\" src=\"WEB-CORE/scripts/tags.js\"></script>");
            output.append("<script language=\"JavaScript\" src=\"WEB-CORE/modules/webadmin/scripts/propertyeditor.js\"></script>");
            output.append("<script language=\"JavaScript\" src=\"WEB-CORE/scripts/lookup.js\"></script>");
            output.append("<form id=\"propertytreeform\" method=\"post\" action=\"").append(formAction).append("\">");
            if (formInputs != null && formInputs.size() > 0) {
                for (String key : formInputs.keySet()) {
                    String val = formInputs.get(key);
                    if (val.contains("{") || val.contains("\"")) {
                        output.append("<textarea style=\"display:none;\" name=\"").append(key).append("\">").append(formInputs.get(key)).append("\"</textarea>");
                        continue;
                    }
                    output.append("<input type=\"hidden\" name=\"").append(key).append("\" value=\"").append(formInputs.get(key)).append("\"/>");
                }
            }
            output.append("<input type=\"hidden\" name=\"refreshelement\" value=\"Y\" />");
            output.append("<input type=\"hidden\" name=\"nodeid\" value=\"").append(nodeid).append("\" />");
            output.append("<input type=\"hidden\" name=\"propertytreeid\" value=\"").append(tree.getId()).append("\" />");
            output.append(PropertyTreeRenderer.getStandardFormFields());
            output.append("<div style=\"overflow:auto\">");
            output.append("<table border=\"1\" class=\"propertytable\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin-left: 5px\">");
            if (options == null) {
                options = new PropertyTreeDisplayOptions();
            }
            options.readonly = readOnly;
            output.append(new PropertyListEditor().getEditor(nodeid, propertyDefinitionList, propertyList, propertyList, "root", 0, pageContext, options));
            output.append("</table >");
            output.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>");
            output.append("</div >");
            output.append("</form >");
            output.append("<script>");
            output.append(PropertyTreeRenderer.getScrollJavaScript(request));
            output.append("document.body.onkeydown = bodykeydown;");
            output.append("document.body.onbeforeunload = checkunload;");
            output.append("</script>");
        }
        catch (Exception e) {
            output.append("Exception: ").append(e.getMessage());
            Logger.logStackTrace(e);
        }
        return output.toString();
    }

    public static StringBuffer getStandardFormFields() {
        StringBuffer output = new StringBuffer();
        output.append("<input type = \"hidden\" name = \"root_0__PROPERTYLISTID\" value = \"root\" / >");
        output.append("<input type = \"hidden\" name = \"root_0__ANCESTOR\" value = \"N\" / >");
        output.append("<input type = \"hidden\" name = \"root__PROPERTYLISTCOUNT\" value = \"1\" / >");
        output.append("<input type = \"hidden\" name = \"dothis\" id = \"dothis\" / >");
        output.append("<input type = \"hidden\" name = \"args1\" id = \"args1\" / >");
        output.append("<input type = \"hidden\" name = \"args2\" id = \"args2\" / >");
        output.append("<input type = \"hidden\" name = \"args3\" id = \"args3\" / >");
        output.append("<input type = \"hidden\" name = \"args4\" id = \"args4\" / >");
        output.append("<input id = \"xoffset\" name = \"xoffset\" type = \"hidden\" / >");
        output.append("<input id = \"yoffset\" name = \"yoffset\" type = \"hidden\" / >");
        output.append("<input type=\"hidden\" name=\"nm_showadvanced\" value=\"\" />");
        output.append("<input type=\"hidden\" name=\"changesmade\" value=\"\" />\n");
        output.append("<input type=\"hidden\" id=\"requiresrefresh\" name=\"requiresrefresh\" value=\"N\" />");
        return output;
    }

    public static String getScrollJavaScript(HttpServletRequest request) {
        String xoffset = request.getParameter("xoffset");
        String yoffset = request.getParameter("yoffset");
        return "function moveScroll() { window.scrollTo( " + xoffset + ", " + yoffset + ");} sapphire.events.attachEvent( window, \"onload\", moveScroll );";
    }

    static class Counter {
        int counter = -1;
        ArrayList idlist = new ArrayList();

        Counter() {
        }
    }
}

