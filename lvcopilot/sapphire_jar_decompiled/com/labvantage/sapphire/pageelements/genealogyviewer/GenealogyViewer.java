/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.genealogyviewer;

import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenealogyViewer
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 70389 $";
    public static String PARENT_LEVEL = "parentlevel";
    public static String CHILD_LEVEL = "childlevel";
    public static final String CACHE_GENEALOGYMODEL = "genealogy_model_";
    public static final String TABLE_NAME = "table";
    public static final String CHILD_COLUMN = "childcolumn";
    public static final String PARENT_COLUMN = "parentcolumn";
    public static final String DISPLAY_VALUE = "displayvalue";
    public static final String HREF_URL = "hrefURL";
    public static final String RETURN_URL = "returntolistpage";
    public static final String MAX_PARENT_COUNT = "maxparentcount";
    public static final String MAX_CHILD_COUNT = "maxchildcount";
    public static final String PARENT_SDC = "parentsdc";
    public static final String PARENT_TABLE = "parenttable";
    public static final String LINK_COL = "linkcolumn";
    public static final String NODE_WIDTH = "nodewidth";
    public static final String NODE_HEIGHT = "nodeheight";
    public static final String COLOR_CODE_COLUMN = "basedoncolumn";
    public static final String CURR_NODE_PROPS = "currentnodeprops";
    public static final String CURR_NODE_HIGHLIGHT_FLAG = "currentnodehighlightflag";
    public static final String CURR_NODE_TEXT_FLAG = "currentnodetextflag";
    public static final String CURR_NODE_TEXT = "currentnodetext";
    public static final String CURR_NODE_COLOR = "currentnodecolor";
    public static final String NODE_COLOR = "nodecolor";
    public static final String LEGEND_TITLE = "legendtitle";
    public static final String LEGEND_TEXT = "legendtext";
    public static final String NODE_PROPS = "nodeprops";
    public static final String COLOR_PROPS = "colorcodeprops";
    public static final String POPUP_MENU = "inkpages";
    public static final String COLORNODE_TYPE = "nodetype";
    public static final String COLORNODE_COLOR = "color";

    private String getUpdatedBatchSequence(String oldBS, String rootBatchId) {
        String newBatchSequence = oldBS;
        int rootPos = oldBS.indexOf(rootBatchId);
        newBatchSequence = rootPos > -1 ? ((oldBS = oldBS.substring(0, oldBS.indexOf(rootBatchId))).length() > 0 ? oldBS + rootBatchId : rootBatchId) : oldBS + ";" + rootBatchId;
        return newBatchSequence;
    }

    @Override
    public String getHtml() {
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        try {
            String yPos;
            String position;
            String rootBatchId = this.requestContext.getProperty("keyid1");
            String newBatchSequence = this.requestContext.getProperty("batchsequence");
            String newBatchSequenceURL = this.requestContext.getProperty("batchsequrl");
            if (newBatchSequence.length() > 0) {
                String bs = this.requestContext.getProperty("batchsequence");
                newBatchSequence = this.getUpdatedBatchSequence(bs, rootBatchId);
            } else {
                newBatchSequence = rootBatchId;
            }
            String table = this.element.getProperty(TABLE_NAME);
            String parentSDC = this.element.getProperty(PARENT_SDC);
            String linkCol = this.element.getProperty(LINK_COL, "");
            String childColumn = this.element.getProperty(CHILD_COLUMN);
            String parentColumn = this.element.getProperty(PARENT_COLUMN);
            String parentLevel = this.element.getProperty(PARENT_LEVEL);
            String childLevel = this.element.getProperty(CHILD_LEVEL);
            String maxParentCount = this.element.getProperty(MAX_PARENT_COUNT);
            String maxChildCount = this.element.getProperty(MAX_CHILD_COUNT);
            String parentTableId = "";
            if (parentSDC != null && parentSDC.trim().length() > 0) {
                parentTableId = this.getSDCProcessor().getProperty(parentSDC, "tableid");
            }
            PropertyList nodeProps = this.element.getPropertyList(NODE_PROPS);
            String displayValue = nodeProps.getProperty(DISPLAY_VALUE);
            String hrefURL = nodeProps.getProperty(HREF_URL);
            String returnToURL = nodeProps.getProperty(RETURN_URL);
            String returnToListUrl = nodeProps.getProperty(RETURN_URL);
            String nodeWidth = nodeProps.getProperty(NODE_WIDTH);
            String nodeHeight = nodeProps.getProperty(NODE_HEIGHT);
            PropertyListCollection links = nodeProps.getCollection(POPUP_MENU);
            PropertyList currentNodeProps = nodeProps.getPropertyListNotNull(CURR_NODE_PROPS);
            String currNodeHighlightFlag = StringUtil.getYN(currentNodeProps.getProperty(CURR_NODE_HIGHLIGHT_FLAG), "Y");
            String currNodeTextFlag = StringUtil.getYN(currentNodeProps.getProperty(CURR_NODE_TEXT_FLAG), "N");
            String currNodeText = currentNodeProps.getProperty(CURR_NODE_TEXT, "CURRENT NODE");
            String currNodeColor = currentNodeProps.getProperty(CURR_NODE_COLOR, "#FFFFFF");
            if (currNodeColor.startsWith("#")) {
                currNodeColor = currNodeColor.substring(1);
            }
            PropertyList colorProps = this.element.getPropertyList(COLOR_PROPS);
            String colorCodeColumn = colorProps.getProperty(COLOR_CODE_COLUMN, "");
            String legendTitle = colorProps.getProperty(LEGEND_TITLE, "");
            String nodeColors = "";
            String legendText = "";
            if (colorCodeColumn != null && colorCodeColumn.trim().length() > 0) {
                PropertyListCollection nodeColorsProp = colorProps.getCollection(NODE_COLOR);
                for (int i = 0; i < nodeColorsProp.size(); ++i) {
                    PropertyList lColorProps = nodeColorsProp.getPropertyList(i);
                    String nodeType = lColorProps.getProperty(COLORNODE_TYPE);
                    String color = lColorProps.getProperty(COLORNODE_COLOR);
                    String legendDisplay = lColorProps.getProperty(LEGEND_TEXT);
                    if (color.startsWith("#")) {
                        color = color.substring(1);
                    }
                    nodeColors = nodeColors + ";" + nodeType + "-" + color;
                    legendText = legendText + ";" + nodeType + "-" + legendDisplay;
                }
                if (nodeColors.length() > 0) {
                    nodeColors = nodeColors.substring(1);
                }
                if (legendText.length() > 0) {
                    legendText = legendText.substring(1);
                }
            }
            String xPos = (position = this.requestContext.getProperty("xPos")).trim().length() > 0 ? position + 125 : "0";
            position = this.requestContext.getProperty("yPos");
            if (position.trim().length() > 0) {
                int y = Integer.parseInt(position.trim());
                yPos = String.valueOf(y += 70);
            } else {
                yPos = "0";
            }
            String webAppRoot = HttpUtil.getWebAppRoot(this.pageContext.getServletContext());
            html.append("<style>\n\t.menuholder {\n\t\tcursor: default;\n\t\twidth: 100px;\n\t\tbackground-color: gainsboro;\n\t\tborder: black solid 1px;\n\t}\n\n\t.menu {\n\t\twidth: 100%;\n\t\tpadding: 2px;\n\t\tbackground-color: white;\n\t}\n\n\t.menuselected {\n\t\twidth: 100%;\n\t\tpadding: 2px;\n\t\tbackground-color: #a9a9a9;\n\t\tcolor: white;\n\t}\n.bredcrumb:link {\n\ttext-decoration: none; \n\tcolor: blue; \n}\n.bredcrumb:visited {\n\ttext-decoration: none; \n\tcolor: blue; \n}\n.bredcrumb:hover {\n\ttext-decoration: underline; \n\tcolor: blue; \n\tfont-weight:bold;}\n.bredcrumb:focus {\n\ttext-decoration: none; \n\tcolor: blue; \n}\n.bredcrumb:active {\n\ttext-decoration: underline; \n\tcolor: blue; \n}</style>");
            html.append("<table><tr><td>");
            html.append("<div id=\"batchsequence\" style=\"background-color:#C0C0C0;\"></div>");
            html.append("</td></tr><tr><td>");
            html.append("<div style=\"overflow:auto;position:relative;\">");
            html.append("<img id=\"loadimage\" src=\"WEB-CORE/images/spinners/flat_blue_spinner.svg\" border=\"0\">\n");
            html.append("<img id=\"image\" src=\"WEB-CORE/elements/images/arwdown.gif\"  border=\"0\" usemap=\"#batchmap\" style='display:none'>\n");
            html.append("<div width=\"10%\" id=\"float\" STYLE=\"position:absolute; display:none;;z-index:1000\" onmouseover=\"showCoDiv();\">");
            html.append("<a href=\"javascript:openMenuList()\"><img src=\"WEB-CORE/elements/images/down.gif\"></a></div>");
            html.append("<div width=\"10%\" id=\"menuDiv\" style=\"display:none;position:absolute;z-index:1000;background:white\">");
            html.append("<div class=\"menuholder\" style=\"position:absolute; top:0px; left:0px\">");
            if (links != null && links.size() > 0) {
                for (int i = 0; i < links.size(); ++i) {
                    PropertyList linkProps = links.getPropertyList(i);
                    String linkId = linkProps.getProperty("linkid");
                    String pageId = linkProps.getProperty("pageid");
                    String target = linkProps.getProperty("target");
                    html.append("<div class='menu' onclick=\"popupEvent(event,this,'").append(pageId).append("','").append(target).append("');\"");
                    html.append(" onmouseover=\"this.className='menuselected'\"").append(" onmouseout=\"this.className='menu'\" id='").append(linkId).append("'>");
                    html.append(" &nbsp;").append(linkId).append("</div>");
                }
            }
            html.append("</div></div>");
            html.append("</div>");
            html.append("</td></tr></table>");
            html.append("<a id=print class='bredcrumb' href='#' onclick='openPrinterView()'>").append(tp.translate("Printer Friendly View")).append("</a><br>");
            html.append("<a id=fullview class='bredcrumb' href='#' onclick='showFullView()'>").append(tp.translate("Show Full View")).append("</a>");
            html.append("<div id=\"truncatemsgdiv\" style=\"display:none;color:red;\">* This is a truncated view </div>");
            html.append("<map id=\"batchmap\" name=\"batchmap\">");
            html.append("</map>");
            html.append("<br>");
            html.append("<script src=\"WEB-CORE/scripts/sapphirecore.js\"></script>\n");
            html.append("<script>\n");
            if ("Y".equals(this.element.getProperty("heterogeneous"))) {
                html.append("var heterogeneous = 'Y';");
                PropertyListCollection collection = this.element.getCollectionNotNull("additionalsdc");
                for (int i = 0; i < collection.size(); ++i) {
                    String displayvalue;
                    PropertyList pl = collection.getPropertyList(i);
                    if (pl == null || (displayvalue = pl.getProperty(DISPLAY_VALUE)).length() <= 0) continue;
                    pl.setProperty(DISPLAY_VALUE, StringUtil.replaceAll(displayvalue, "\n", "\\n"));
                }
                html.append("var additionalsdc = ").append(collection.toJSONString()).append(";");
            } else {
                html.append("var heterogeneous = 'N';");
                html.append("var additionalsdc = {};");
            }
            PropertyList nodeprops = this.element.getPropertyListNotNull(NODE_PROPS);
            String displayvalue = nodeprops.getProperty(DISPLAY_VALUE).trim();
            if (displayvalue.length() > 0) {
                nodeprops.setProperty(DISPLAY_VALUE, StringUtil.replaceAll(displayvalue, "\n", "\\n"));
            }
            html.append("\nrootBatchId =\"").append(rootBatchId).append("\";\n").append("     childLevel =").append(childLevel).append(";\n").append("     parentLevel =").append(parentLevel).append(";\n").append("     batchSequence =\"").append(newBatchSequence).append("\";\n").append("     batchsequrl =\"").append(newBatchSequenceURL).append("\";\n").append("     returnToURL =\"").append(returnToURL).append("\";\n").append("     dbtable =\"").append(table).append("\";\n").append("     childColumn =\"").append(childColumn).append("\";\n").append("     parentColumn =\"").append(parentColumn).append("\";\n").append("     displayValue =\"").append(StringUtil.replaceAll(displayValue, "\n", "\\n")).append("\";\n").append("     returnToListUrl =\"").append(returnToListUrl).append("\";\n").append("     webAppRoot =\"").append(webAppRoot.replaceAll("\\\\", "/")).append("\";\n").append("     maxParentCount =\"").append(maxParentCount).append("\";\n").append("     maxChildCount =\"").append(maxChildCount).append("\";\n").append("     xPos = ").append(xPos).append(";\n").append("     yPos =").append(yPos).append(";\n").append("     currentnodehighlightflag =\"").append(currNodeHighlightFlag).append("\";\n").append("     currentnodetextflag =\"").append(currNodeTextFlag).append("\";\n").append("     currentnodetext =\"").append(currNodeText).append("\";\n").append("     currentnodecolor =\"").append(currNodeColor).append("\";\n").append("     nodeWidth =").append(nodeWidth).append(";\n").append("     nodeHeight =").append(nodeHeight).append(";\n").append("     colorCodeColumn =\"").append(colorCodeColumn).append("\";\n").append("     nodeColors =\"").append(nodeColors).append("\";\n").append("     legendText =\"").append(legendText).append("\";\n").append("     parentSDC =\"").append(parentSDC).append("\";\n").append("     parentTableId =\"").append(parentTableId).append("\";\n").append("     linkCol =\"").append(linkCol).append("\";\n").append("     legendTitle =\"").append(legendTitle).append("\";\n").append("     hrefURL =\"").append(hrefURL).append("\";\n").append("\t   var parentSDCColumn = \"").append(this.element.getProperty("parentsdccolumn")).append("\";\n").append("     var childSDCColumn = \"").append(this.element.getProperty("childsdccolumn")).append("\";");
            if (links != null && links.size() > 0) {
                html.append("showPopup=true;\n");
            } else {
                html.append("showPopup=false;\n");
            }
            html.append("</script>\n");
            html.append("<script src=\"WEB-CORE/elements/genealogyviewer/genealogyscript.js\"></script>\n");
        }
        catch (Exception e) {
            html = new StringBuffer();
            html.append("<font color=red>");
            html.append("Error in rendering element");
            html.append("</font>");
        }
        return html.toString();
    }
}

