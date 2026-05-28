/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.opal.util.EvaluateExpression;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.LockedImage;
import com.labvantage.sapphire.pageelements.maint.MaintAttribute;
import com.labvantage.sapphire.pageelements.maint.MaintColumn;
import com.labvantage.sapphire.pageelements.maint.MultiMaintGrid;
import com.labvantage.sapphire.pageelements.maint.RegexConverter;
import com.labvantage.sapphire.tagext.QueryData;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MultiSDIAttributeMaint
extends MultiMaintGrid {
    public static final String JS_CLASS = "multiSDIAttributeMaint";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_FORCEPRIMARY = "forceprimary";
    public static final String PROPERTY_MANDATORY = "mandatory";
    public static final String PROPERTY_HIDETOOLBAR = "hidetoolbar";
    public static final String PROPERTY_COLLAPSEGROUPS = "collapsegroups";
    public static final String PROPERTY_SCROLLGRID = "scrollgrid";
    public static final String RECORD_SEPARATOR = "~;~";
    public static final String VALUE_SEPARATOR = "#;#";
    private boolean isSortByKeyOrder = false;
    private boolean isAttributeAcross = false;
    private boolean allowUserChangeView = false;
    String expandedgroups = "";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String sdcid;
    private String rsetid;
    private String cornerText;
    private boolean forceprimary;
    private String columnHeadText;
    private int colheaderIndex = -1;
    private int colheaderIndex1 = -1;
    private int yheaderIndex = -1;
    private String[] columnHeadTextTokens;
    private SDIData sdiData;
    private boolean mandatory;
    private boolean hidetoolbar;
    private PropertyListCollection attributesDefs;
    private PropertyList sdcprops;
    private PropertyList parentsdcprops = new PropertyList();
    private String parentSDCId = "";
    private DataSet sortedPrimary = new DataSet();
    PropertyListCollection parentSDCColumns = new PropertyListCollection();
    PropertyListCollection sdiwiSDCColumns = new PropertyListCollection();
    PropertyListCollection datasetSDCColumns = new PropertyListCollection();
    DataSet dsSdiData = new DataSet();
    DataSet dsSdiDataAttributes = new DataSet();
    DataSet dsSdiDataAttributesOld = new DataSet();
    DataSet dsSDCAttributeOld = new DataSet();
    DataSet dsSdiWorkItem = new DataSet();
    DataSet dsSdiWorkItemItem = new DataSet();
    DataSet dsSdiWorkItemAttributes = new DataSet();
    DataSet dsSdiWorkItemAttributesOld = new DataSet();
    DataSet dsChildSdiWorkItem = new DataSet();
    DataSet dsChildSdiData = new DataSet();
    private PropertyList pagedata;
    private HashMap<String, String> validations = new HashMap();
    private JSONArray clientCells = new JSONArray();
    private ArrayList<String> nonduplicates = new ArrayList();
    private StringBuffer dropdownComboValues = new StringBuffer();
    private HashSet readOnlySet = new HashSet();
    private HashSet informationOnlySet = new HashSet();
    private HashMap<String, LockedImage> lockedImageMap = new HashMap();
    private HashSet templateSet = new HashSet();
    private String changecontrolledflag = "";
    private ArrayList<String> keyorder = new ArrayList();
    PropertyList primarymaintelement = new PropertyList();
    boolean divRow = false;
    ArrayList<ArrayList<String>> cellsarray = new ArrayList();
    ArrayList<ArrayList<String>> primarycolumncellsarray = new ArrayList();
    ArrayList<ArrayList<String>> childSDICellArray = new ArrayList();
    ArrayList<ArrayList<String>> subchildSDICellArray = new ArrayList();
    String prevYKey = "";
    String prevYKeyPrimaryColumn = "";
    String prevYKey1 = "";
    String prevYKey2 = "";

    private PropertyList getAttributeDef(String attributeId) {
        PropertyListCollection attributesDefs = this.sdcprops != null ? this.sdcprops.getCollection("attributes") : null;
        return attributesDefs != null ? attributesDefs.find("attributeid", attributeId) : null;
    }

    private PropertyList getAttributeDef(String attributeId, String sdcId) {
        PropertyList sdcprops = this.getSDCProcessor().getPropertyList(sdcId);
        PropertyListCollection attributesDefs = sdcprops != null ? sdcprops.getCollection("attributes") : null;
        return attributesDefs != null ? attributesDefs.find("attributeid", attributeId) : null;
    }

    private String getAttributeTitle(String attributeId) {
        PropertyList def = this.getAttributeDef(attributeId);
        return def != null ? def.getProperty("attributetitle", attributeId) : attributeId;
    }

    @Override
    public int compareY(String o1, String o2) {
        if (!this.isSortByKeyOrder || !this.isAttributeAcross || this.keyorder.size() == 0) {
            return super.compareY(o1, o2);
        }
        return this.compareKeys(o1, o2);
    }

    @Override
    public int compareGroupEntry(String o1, String o2) {
        if (!this.isSortByKeyOrder || !this.isAttributeAcross || this.keyorder.size() == 0) {
            return super.compareGroupEntry(o1, o2);
        }
        return this.compareKeys(o1, o2);
    }

    private int compareKeys(String o1, String o2) {
        int index1 = -1;
        int index2 = -1;
        for (int i = 0; i < this.keyorder.size(); ++i) {
            if (this.keyorder.get(i).equals(o1)) {
                index1 = i;
            }
            if (this.keyorder.get(i).equals(o2)) {
                index2 = i;
            }
            if (index1 > -1 && index2 > -1) break;
        }
        if (index1 > index2) {
            return 1;
        }
        if (index1 < index2) {
            return -1;
        }
        return 0;
    }

    @Override
    public int compareX(String o1, String o2) {
        if (!this.isAttributeAcross && !this.isSortByKeyOrder && this.keyorder.size() > 0) {
            return this.compareKeys(o1, o2);
        }
        if (!this.isSortByKeyOrder || this.isAttributeAcross || this.keyorder.size() == 0) {
            return super.compareX(o1, o2);
        }
        return this.compareKeys(o1, o2);
    }

    public MultiSDIAttributeMaint(PageContext pageContext, PropertyList pageproperties) {
        this.requestContext = RequestContext.getInstance((HttpServletRequest)pageContext.getRequest());
        PropertyList pagedatatemp = this.requestContext.getPropertyList("pagedata");
        String ptitle = pagedatatemp.getProperty("title", "");
        ptitle = EvaluateExpression.evaluate(ptitle, "maint", pageContext);
        pagedatatemp.setProperty("title", ptitle);
        this.requestContext.setProperty("pagedata", pagedatatemp);
        this.setPageContext(pageContext);
        String newlyaddedStr = "";
        String changeafterAdd = "";
        this.primarymaintelement.setProperty("style", "grid");
        this.sdcid = pageproperties.getProperty(PROPERTY_SDCID, "");
        String requestsdcid = pageContext.getRequest().getParameter(PROPERTY_SDCID);
        if (requestsdcid != null && requestsdcid.length() > 0 && !requestsdcid.equals(this.sdcid)) {
            throw new RuntimeException("Configured page primary sdcid '" + this.sdcid + "' does not match the actual sdcid '" + requestsdcid + "'");
        }
        this.logger.debug("sdcid = " + this.sdcid);
        this.keyid1 = pageproperties.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pageproperties.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.keyid3 = pageproperties.getProperty(PROPERTY_KEYID3, "");
        this.logger.debug("keyid3 = " + this.keyid3);
        this.pagedata = pageproperties.getPropertyList("pagedata");
        if (this.pagedata != null) {
            this.allowUserChangeView = "Y".equals(this.pagedata.getProperty("allowuserchangeview"));
            String gridlayout = pageContext.getRequest().getParameter("gridlayout");
            this.isAttributeAcross = "Attribute Across".equals(gridlayout) ? true : ("SDI Across".equals(gridlayout) ? false : "Attribute Across".equals(this.pagedata.getProperty("gridlayout")));
            if ("Y".equals(this.pagedata.getProperty("inkeyidorder"))) {
                this.isSortByKeyOrder = true;
            }
            this.hidetoolbar = pageproperties.getProperty(PROPERTY_HIDETOOLBAR, this.pagedata.getProperty(PROPERTY_HIDETOOLBAR, "n")).equalsIgnoreCase("Y");
            boolean isTaskPage = pageproperties.getProperty("taskpage", "N").equalsIgnoreCase("Y");
            if (isTaskPage) {
                this.hidetoolbar = true;
            }
            this.logger.debug("hidetoolbar = " + this.hidetoolbar);
            this.expandedgroups = pageproperties.getProperty("expandedgroups", this.pagedata.getProperty("expandedgroups", ""));
            this.logger.debug("expandedgroups = " + this.expandedgroups);
            this.expandedGroupsArray = StringUtil.split(this.expandedgroups, ";");
            this.viewonly = this.pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
            this.logger.debug("viewonly = " + this.viewonly);
            this.esigEnabled = "Y".equalsIgnoreCase(this.pagedata.getProperty("esigreqd", "N"));
            this.logger.debug("esigenabled = " + this.esigEnabled);
            this.forceprimary = pageproperties.getProperty(PROPERTY_FORCEPRIMARY, this.pagedata.getProperty(PROPERTY_FORCEPRIMARY, "n")).equalsIgnoreCase("Y");
            this.logger.debug("forceprimary = " + this.forceprimary);
            this.mandatory = this.pagedata.getProperty(PROPERTY_MANDATORY, "n").equalsIgnoreCase("y");
            this.logger.debug("mandatory = " + this.mandatory);
            this.collapseGroups = this.pagedata.getProperty(PROPERTY_COLLAPSEGROUPS, "N").equalsIgnoreCase("Y");
            this.scrollGrid = !this.pagedata.getProperty(PROPERTY_SCROLLGRID, "Y").equalsIgnoreCase("N");
            newlyaddedStr = this.pagedata.getProperty("newlyaddedArr");
            changeafterAdd = this.pagedata.getProperty("changesafterAdd");
            PropertyList headers = this.pagedata.getPropertyList("headers");
            if (headers != null) {
                this.cornerText = pageproperties.getProperty("cornerheader", headers.getProperty("corner", ""));
                this.columnHeadText = pageproperties.getProperty("primaryheader", headers.getProperty("primary", "[keycolid1]"));
                this.columnHeadTextTokens = StringUtil.getExpressionTokens(this.columnHeadText);
            } else {
                this.cornerText = "[plural]";
                this.columnHeadText = "[keycolid1]";
                this.columnHeadTextTokens = new String[]{"keycolid1"};
            }
        }
        this.elementid = "multisdiattribute";
        if (this.isAttributeAcross) {
            this.setEndRow(true);
        } else {
            this.setEndColumn(true);
        }
        this.parentSDCId = this.pagedata.getProperty("parentsdcid");
        if (this.parentSDCId.length() > 0) {
            this.parentsdcprops = this.getSDCProcessor().getPropertyList(this.parentSDCId);
        }
        if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
            this.sdiwiSDCColumns = this.pagedata.getCollectionNotNull("columns");
            this.datasetSDCColumns = this.pagedata.getCollectionNotNull("sdidatacolumns");
            this.parentSDCColumns = this.pagedata.getCollectionNotNull("primarycolumns");
        } else if (this.sdcid.equalsIgnoreCase("DataSet")) {
            this.datasetSDCColumns = this.pagedata.getCollectionNotNull("columns");
            this.sdiwiSDCColumns = this.pagedata.getCollectionNotNull("sdiworkitemcolumns");
            this.parentSDCColumns = this.pagedata.getCollectionNotNull("primarycolumns");
        } else {
            this.parentSDCColumns = this.pagedata.getCollectionNotNull("columns");
            this.sdiwiSDCColumns = this.pagedata.getCollectionNotNull("sdiworkitemcolumns");
            this.datasetSDCColumns = this.pagedata.getCollectionNotNull("sdidatacolumns");
        }
        if (this.keyid1.length() > 0 && this.sdcid.length() > 0) {
            String[] keys1 = StringUtil.split(this.keyid1, ";");
            String[] keys2 = this.keyid2.length() > 0 ? StringUtil.split(this.keyid2, ";") : null;
            String[] keys3 = this.keyid3.length() > 0 ? StringUtil.split(this.keyid3, ";") : null;
            this.sdcprops = this.getSDCProcessor().getPropertyList(this.sdcid);
            this.changecontrolledflag = CMTPolicy.getPolicy(this.connectionInfo.getConnectionId(), this.sdcid).getChangeControlledFlag();
            SDIData sDIData = this.sdiData = this.sdcprops != null ? this.getData() : null;
            if (this.sdiData != null) {
                QueryData queryData = new QueryData("primary", this.sdiData.getDataset("primary"));
                HashMap<String, QueryData> queryDataMap = new HashMap<String, QueryData>();
                queryDataMap.put("primary", queryData);
                SDITagInfo sdiinfo = new SDITagInfo(queryDataMap);
                this.setSDIInfo(sdiinfo);
            }
            String keycolid1 = this.getSDCProcessor().getProperty(this.sdcid, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(this.sdcid, "keycolid2");
            String keycolid3 = this.getSDCProcessor().getProperty(this.sdcid, "keycolid3");
            ArrayList<String> sdiwiKeyCols = new ArrayList<String>();
            sdiwiKeyCols.add("sdiworkitemid");
            sdiwiKeyCols.add("workitemid");
            sdiwiKeyCols.add("workiteminstance");
            sdiwiKeyCols.add(PROPERTY_SDCID);
            sdiwiKeyCols.add(PROPERTY_KEYID1);
            sdiwiKeyCols.add(PROPERTY_KEYID2);
            sdiwiKeyCols.add(PROPERTY_KEYID3);
            ArrayList<String> sdidataKeyCols = new ArrayList<String>();
            sdidataKeyCols.add("sdidataid");
            sdidataKeyCols.add("paramlistid");
            sdidataKeyCols.add("paralistversionid");
            sdidataKeyCols.add("variantid");
            sdidataKeyCols.add("dataset");
            sdidataKeyCols.add(PROPERTY_SDCID);
            sdidataKeyCols.add(PROPERTY_KEYID1);
            sdidataKeyCols.add(PROPERTY_KEYID2);
            sdidataKeyCols.add(PROPERTY_KEYID3);
            if (this.sdiData != null && this.sdiData.getDataset("attribute") != null) {
                String y_key;
                DataSet attributeData = this.sdiData.getDataset("attribute");
                DataSet primaryData = this.sdiData.getDataset("primary");
                PropertyListCollection primaryColumns = this.pagedata.getCollection("columns");
                if (!this.isAttributeAcross) {
                    primaryData = this.sortAdvancedScrollGridRows();
                }
                if (primaryColumns != null && primaryData != null) {
                    for (int i = 0; i < primaryData.getRowCount(); ++i) {
                        int row = i;
                        if (row < 0) continue;
                        HashMap primaryRowMap = (HashMap)primaryData.get(row);
                        String currentPrimaryKey = primaryData.getValue(row, keycolid1) + ";" + primaryData.getValue(row, keycolid2, "(null)") + ";" + primaryData.getValue(row, keycolid3, "(null)");
                        if (!this.keyorder.contains(currentPrimaryKey)) {
                            this.keyorder.add(currentPrimaryKey);
                        }
                        primaryRowMap.put("__primaryRow", row);
                        for (int c = 0; c < primaryColumns.size(); ++c) {
                            String y_title;
                            String x_titleTemp;
                            String mode = primaryColumns.getPropertyList(c).getProperty("mode");
                            String group = " Primary";
                            if ("hidden".equals(mode) || "retrievedata".equals(mode)) continue;
                            String columnid = primaryColumns.getPropertyList(c).getProperty("columnid");
                            if (columnid.indexOf(" ") > 0) {
                                columnid = RequestParser.parseAlias(columnid);
                            }
                            boolean keyColumn = false;
                            if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                                group = " Test";
                                if (sdiwiKeyCols.contains(columnid)) {
                                    keyColumn = true;
                                }
                            } else if (this.sdcid.equalsIgnoreCase("DataSet")) {
                                group = " DataSet";
                                if (sdidataKeyCols.contains(columnid)) {
                                    keyColumn = true;
                                }
                            } else {
                                boolean bl = keyColumn = columnid.equals(this.sdcprops.getProperty("keycolid1")) || columnid.equals(this.sdcprops.getProperty("keycolid2")) || columnid.equals(this.sdcprops.getProperty("keycolid3"));
                            }
                            if (keyColumn) {
                                mode = "readonly";
                            }
                            String title = primaryColumns.getPropertyList(c).getProperty("title");
                            primaryRowMap.put(columnid + "_columnDef", primaryColumns.getPropertyList(c));
                            String x_keyTemp = x_titleTemp = currentPrimaryKey;
                            String y_keyTemp = (c < 10 ? "0" : "") + c + "_" + columnid;
                            String y_titleTemp = title;
                            String x_key = this.isAttributeAcross ? y_keyTemp : x_keyTemp;
                            String x_title = this.isAttributeAcross ? y_titleTemp : x_titleTemp;
                            y_key = this.isAttributeAcross ? x_keyTemp : y_keyTemp;
                            String string = y_title = this.isAttributeAcross ? primaryData.getValue(row, keycolid1) : y_titleTemp;
                            if ("readonly".equals(mode)) {
                                if (this.isAttributeAcross) {
                                    this.readOnlySet.add(x_key);
                                } else {
                                    this.readOnlySet.add(y_key);
                                    if (!this.sdcid.equalsIgnoreCase("SDIWorkItem") && !this.sdcid.equalsIgnoreCase("DataSet")) continue;
                                }
                            }
                            if (this.isAttributeAcross) {
                                this.set(x_key, x_title, y_key, y_title, this.isAttributeAcross ? "NA" : " Primary", primaryRowMap, true);
                                continue;
                            }
                            this.set(x_key, x_title, y_key, y_title, this.isAttributeAcross ? "NA" : group, primaryRowMap);
                        }
                    }
                }
                if (attributeData.getRowCount() > 0) {
                    int i;
                    ArrayList<String> foundKey = new ArrayList<String>();
                    ArrayList<String[]> usedYs = new ArrayList<String[]>();
                    ArrayList<String[]> usedAttrs = new ArrayList<String[]>();
                    attributeData.addColumn("__rownum", 1);
                    for (i = 0; i < attributeData.getRowCount(); ++i) {
                        String def_instructionflag;
                        boolean allowduplicates;
                        attributeData.setNumber(i, "__rownum", i);
                        MaintAttribute.MaintenanceMode attributetype = MaintAttribute.getMode(attributeData.getValue(i, "attributesourcetype"));
                        String attributesdcid = attributeData.getValue(i, "attributesdcid", "");
                        if (attributetype != MaintAttribute.MaintenanceMode.adhoc && (attributetype != MaintAttribute.MaintenanceMode.linkdef || !attributesdcid.equalsIgnoreCase(this.sdcid))) continue;
                        String attributeid = attributeData.getValue(i, "attributeid", "");
                        int attributeinstance = attributeData.getBigDecimal(i, "attributeinstance").intValue();
                        String currkey1 = attributeData.getValue(i, PROPERTY_KEYID1, "");
                        String currkey2 = this.keyid2.length() > 0 ? attributeData.getValue(i, PROPERTY_KEYID2, "") : "";
                        String currkey3 = this.keyid3.length() > 0 ? attributeData.getValue(i, PROPERTY_KEYID3, "") : "";
                        String x_title = currkey1 + (currkey2.length() > 0 ? (currkey3.length() > 0 ? " (" + currkey2 + ") " + currkey3 : " (" + currkey2 + ")") : "");
                        PropertyList attributedef = this.getAttributeDef(attributeid, attributesdcid);
                        String groupid = this.isAttributeAcross ? "NA" : (attributedef != null ? attributedef.getProperty("attributegroup", "NA") : "NA");
                        String y_title = attributedef != null ? attributedef.getProperty("attributetitle", "") : attributeid;
                        y_title = y_title.length() > 0 ? this.getTranslationProcessor().translate(y_title) : attributeid;
                        boolean bl = allowduplicates = attributedef != null ? attributedef.getProperty("allowduplicatesflag", "Y").equalsIgnoreCase("Y") : true;
                        if (attributeinstance > 1) {
                            y_title = y_title + " (" + attributeinstance + ")";
                        }
                        x_title = this.getTranslationProcessor().translate(x_title);
                        y_key = attributeid + ";" + attributeinstance + ";" + attributesdcid;
                        String keyid1 = attributeData.getValue(i, PROPERTY_KEYID1, "");
                        String keyid2 = attributeData.getValue(i, PROPERTY_KEYID2, "");
                        String keyid3 = attributeData.getValue(i, PROPERTY_KEYID3, "");
                        String x_key = keyid1 + ";" + keyid2 + ";" + keyid3;
                        if (!allowduplicates && !this.nonduplicates.contains(attributeid + "|" + x_key)) {
                            this.nonduplicates.add(attributeid + "|" + x_key);
                        }
                        String x_keynew = this.isAttributeAcross ? y_key : x_key;
                        String x_titlenew = this.isAttributeAcross ? y_title : x_title;
                        String y_keynew = this.isAttributeAcross ? x_key : y_key;
                        String y_titlenew = this.isAttributeAcross ? x_title : y_title;
                        PropertyList attributeDef = this.getAttributeDef(attributeid);
                        String string = def_instructionflag = attributeDef != null ? attributeDef.getProperty("instructionflag", attributeid) : "";
                        if ("O".equalsIgnoreCase(def_instructionflag)) {
                            this.informationOnlySet.add(x_keynew);
                        }
                        this.set(x_keynew, x_titlenew, y_keynew, y_titlenew, groupid, (HashMap)attributeData.get(i));
                        foundKey.add(this.isAttributeAcross ? y_keynew : x_keynew);
                        usedYs.add(new String[]{y_keynew, y_titlenew, groupid});
                        usedAttrs.add(new String[]{y_key, y_title, groupid});
                    }
                    if (foundKey.size() != keys1.length) {
                        if (usedYs.size() > 0) {
                            for (i = 0; i < keys1.length; ++i) {
                                String x_key = keys1[i];
                                if (keys2 != null) {
                                    x_key = x_key + ";" + keys2[i];
                                    x_key = keys3 != null ? x_key + ";" + keys3[i] : x_key + ";(null)";
                                } else {
                                    x_key = x_key + ";(null);(null)";
                                }
                                if (foundKey.contains(x_key)) continue;
                                String x_title = keys1[i] + (keys2 != null ? (keys3 != null ? " (" + keys2[i] + ") " + keys3[i] : " (" + keys2[i] + ")") : "");
                                for (int a = 0; a < usedAttrs.size(); ++a) {
                                    String xk = x_key;
                                    String xt = x_title;
                                    String yk = ((String[])usedAttrs.get(a))[0];
                                    String yt = ((String[])usedAttrs.get(a))[1];
                                    String g = ((String[])usedAttrs.get(a))[2];
                                    if (this.isAttributeAcross) {
                                        this.set(yk, yt, xk, xt, g, null);
                                        continue;
                                    }
                                    this.set(xk, xt, yk, yt, g, null);
                                }
                            }
                        } else {
                            this.buildEmptyGrid(keys1, keys2, keys3);
                        }
                    }
                } else {
                    this.buildEmptyGrid(keys1, keys2, keys3);
                }
                if (this.isAttributeAcross) {
                    this.setChildSDIKeys();
                }
            } else {
                this.setError("Attribute data could not be obtained.");
            }
        } else {
            this.setError("No keyid1 or sdcid provided.");
        }
    }

    @Override
    public String getHtml() {
        if (this.viewonly && this.getData().getDataset("attribute") != null && this.getData().getDataset("attribute").getRowCount() == 0 && this.pagedata.getProperty("nodatamessage").length() > 0) {
            StringBuilder html = new StringBuilder();
            html.append("<div style=\"width:100%;height:100%;padding-top: 5px;\">");
            html.append(this.getTranslationProcessor().translate(this.pagedata.getProperty("nodatamessage", "")));
            html.append("</div>");
            return html.toString();
        }
        boolean hasDynamicAudit = this.pagedata.getProperty("dynamicaudit").length() > 0;
        return hasDynamicAudit ? "<script type=\"text/javascript\">sapphire.page.data.dynamicaudit_" + this.elementid + "='" + this.pagedata.getProperty("dynamicaudit") + "';var enableDynamicAudit = true;</script>" + super.getHtml() : super.getHtml();
    }

    private void buildEmptyGrid(String[] keys1, String[] keys2, String[] keys3) {
        this.buildEmptyGrid(keys1, keys2, keys3, false, 0);
    }

    private void buildEmptyGrid(String[] keys1, String[] keys2, String[] keys3, boolean childSDI, int childlevel) {
        for (int i = 0; i < keys1.length; ++i) {
            String x_key = keys1[i];
            if (keys2 != null) {
                x_key = x_key + ";" + keys2[i];
                x_key = keys3 != null ? x_key + ";" + keys3[i] : x_key + ";(null)";
            } else {
                x_key = x_key + ";(null);(null)";
            }
            String x_title = keys1[i] + (keys2 != null ? (keys3 != null ? " (" + keys2[i] + ") " + keys3[i] : " (" + keys2[i] + ")") : "");
            String y_key = null;
            String y_title = null;
            if (this.isAttributeAcross) {
                y_key = x_key;
                y_title = x_title;
                x_key = null;
                x_title = null;
            }
            if (childSDI) {
                this.setChildKey(x_key, x_title, y_key, y_title, null, childlevel);
                continue;
            }
            this.set(x_key, x_title, y_key, y_title, null);
        }
    }

    private StringBuffer getToolBar() {
        String newlyaddedstr = this.pagedata.getProperty("newlyaddedArr");
        StringBuffer html = new StringBuffer();
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        toolbar.setProperty("displaystyle", "Ribbon");
        PropertyListCollection buttons = new PropertyListCollection();
        PropertyList btn = new PropertyList();
        btn.setProperty("id", "btSave");
        btn.setProperty("buttontype", "User");
        PropertyList common = new PropertyList();
        if (newlyaddedstr != null && newlyaddedstr != "") {
            common.setProperty("text", this.getTranslationProcessor().translate("Save*"));
        } else {
            common.setProperty("text", this.getTranslationProcessor().translate("Save"));
        }
        common.setProperty("image", "WEB-CORE/images/png/Save.png");
        common.setProperty("imagesmall", "WEB-CORE/images/png32/Save.png");
        common.setProperty("group", "File");
        common.setProperty("ribbonstyle", "small");
        toolbar.setProperty("buttons", "Ribbon");
        btn.setProperty("commonprops", common);
        PropertyList user = new PropertyList();
        user.setProperty("action", "multiSDIAttributeMaint.save('" + this.sdcid + "', " + this.esigEnabled + ")");
        user.setProperty("releaselock", "N");
        btn.setProperty("userbuttonprops", user);
        if (!this.viewonly) {
            buttons.add(btn);
        }
        boolean popupLayout = "popup".equalsIgnoreCase((String)this.pageContext.getAttribute("layout"));
        if (this.pagedata.getProperty("popup", "N").equalsIgnoreCase("Y") || popupLayout) {
            btn = new PropertyList();
            btn.setProperty("id", "btClose");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", this.getTranslationProcessor().translate("Close"));
            common.setProperty("image", "WEB-CORE/images/png/Close.png");
            common.setProperty("imagelarge", "WEB-CORE/images/png32/Close.png");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "multiSDIAttributeMaint.close( );");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        } else {
            btn = new PropertyList();
            btn.setProperty("id", "btReturn");
            btn.setProperty("buttontype", "User");
            common = new PropertyList();
            common.setProperty("text", this.getTranslationProcessor().translate("Return"));
            common.setProperty("show", "$G{pagedata.layout =='navigator' ? 'N' : 'Y'}");
            common.setProperty("image", "WEB-CORE/images/png/ReturntoList.png");
            common.setProperty("imagelarge", "WEB-CORE/images/png32/ReturntoList.png");
            common.setProperty("ribbonstyle", "Large");
            toolbar.setProperty("buttons", "Ribbon");
            btn.setProperty("commonprops", common);
            user = new PropertyList();
            user.setProperty("action", "multiSDIAttributeMaint.returnTo();");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
        }
        toolbar.setProperty("buttons", buttons);
        if (this.allowUserChangeView && !this.browser.isPhone()) {
            toolbar.setProperty("customgrouptext", this.getTranslationProcessor().translate("Choose a View"));
            StringBuffer customContent = new StringBuffer();
            customContent.append("<div><select onchange=\"multiSDIAttributeMaint.changeGridLayout( this.value );sapphire.page.navigate( 'rc?command=page&page=" + this.pagedata.getProperty("page") + "&sdcid=" + this.sdcid + "&keyid1=" + this.keyid1 + "&keyid2=" + this.keyid2 + "&keyid3=" + this.keyid3 + "&layout=" + this.pageContext.getRequest().getParameter("layout") + "&gridlayout=' + this.value )\">");
            customContent.append("<option value=\"SDI Across\" " + (this.isAttributeAcross ? "" : "selected") + ">" + this.getTranslationProcessor().translate("SDI Across") + "</option>");
            customContent.append("<option value=\"Attribute Across\" " + (this.isAttributeAcross ? "selected" : "") + ">" + this.getTranslationProcessor().translate("Attribute Across") + "</option>");
            customContent.append("</select></div>");
            toolbar.setProperty("customgroupcontent", customContent.toString());
        }
        AdvancedToolbar advancedToolbar = new AdvancedToolbar();
        advancedToolbar.setPageContext(this.pageContext);
        advancedToolbar.setElementid("advancedtoolbar");
        advancedToolbar.setElementProperties(toolbar);
        html.append("<script>");
        html.append(JS_CLASS).append(".").append("viewonly = ").append(this.viewonly).append(";");
        html.append("</script>");
        html.append(advancedToolbar.getHtml());
        return html;
    }

    private String getGridScript() {
        if (this.cellsarray != null && this.cellsarray.size() > 0) {
            return SDITagUtil.getGrid(this.cellsarray, JS_CLASS, this.browser, this.getTranslationProcessor());
        }
        return "";
    }

    private String getPrimaryColumnsGridScript() {
        if (this.primarycolumncellsarray != null && this.primarycolumncellsarray.size() > 0) {
            return SDITagUtil.getGrid(this.primarycolumncellsarray, "multiSDIAttributeMaintColumn", this.browser, this.getTranslationProcessor());
        }
        return "";
    }

    private String getGridScript1() {
        if (this.childSDICellArray != null && this.childSDICellArray.size() > 0) {
            return SDITagUtil.getGrid(this.childSDICellArray, "multiSDIAttributeMaintChild1", this.browser, this.getTranslationProcessor());
        }
        return "";
    }

    private String getGridScript2() {
        if (this.subchildSDICellArray != null && this.subchildSDICellArray.size() > 0) {
            return SDITagUtil.getGrid(this.subchildSDICellArray, "multiSDIAttributeMaintChild2", this.browser, this.getTranslationProcessor());
        }
        return "";
    }

    private void setFKey(DataSet ds) {
        for (int r = 0; r < ds.getRowCount(); ++r) {
            String k1 = ds.getValue(r, PROPERTY_KEYID1);
            String k2 = ds.getValue(r, PROPERTY_KEYID2);
            String k3 = ds.getValue(r, PROPERTY_KEYID3);
            String __key = k1 + ";" + k2 + ";" + k3;
            ds.setString(r, "__fkey", __key);
        }
    }

    private SDIData getData() {
        String newlyaddedStr = this.pagedata.getProperty("newlyaddedArr");
        String changeafterAdd = this.pagedata.getProperty("changesafterAdd");
        StringBuffer sql = new StringBuffer();
        if (this.keyid1.length() > 0 && this.sdcid.length() > 0) {
            PropertyListCollection primarycolumns;
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(this.sdcid);
            sdiRequest.setKeyid1List(this.keyid1);
            if (this.keyid2 != null && this.keyid2.length() > 0) {
                sdiRequest.setKeyid2List(this.keyid2);
                if (this.keyid3 != null && this.keyid3.length() > 0) {
                    sdiRequest.setKeyid3List(this.keyid3);
                }
            }
            StringBuilder sb = new StringBuilder("[");
            if (this.pagedata != null && (primarycolumns = this.pagedata.getCollection("columns")) != null) {
                if (primarycolumns.size() == 0) {
                    String keycolid1 = this.sdcprops.getProperty("keycolid1");
                    String keycolid2 = this.sdcprops.getProperty("keycolid2");
                    String keycolid3 = this.sdcprops.getProperty("keycolid3");
                    String desccol = this.sdcprops.getProperty("desccol");
                    String singular = this.sdcprops.getProperty("singular");
                    PropertyList properties = new PropertyList();
                    properties.setProperty("columnid", keycolid1);
                    String title = this.getSDCProcessor().getSDCColumnProperty(this.sdcid, keycolid1, "columnlabel");
                    if (title.length() == 0) {
                        title = singular.length() > 0 ? singular : keycolid1;
                    }
                    properties.setProperty("title", title);
                    primarycolumns.add(properties);
                    if (keycolid2.length() > 0) {
                        properties = new PropertyList();
                        properties.setProperty("columnid", keycolid2);
                        title = this.getSDCProcessor().getSDCColumnProperty(this.sdcid, keycolid2, "columnlabel");
                        if (title.length() == 0) {
                            title = keycolid2;
                        }
                        properties.setProperty("title", title);
                        primarycolumns.add(properties);
                    }
                    if (keycolid3.length() > 0) {
                        properties = new PropertyList();
                        properties.setProperty("columnid", keycolid3);
                        title = this.getSDCProcessor().getSDCColumnProperty(this.sdcid, keycolid3, "columnlabel");
                        if (title.length() == 0) {
                            title = keycolid3;
                        }
                        properties.setProperty("title", title);
                        primarycolumns.add(properties);
                    }
                    if (desccol.length() > 0) {
                        properties = new PropertyList();
                        properties.setProperty("columnid", desccol);
                        title = this.getSDCProcessor().getSDCColumnProperty(this.sdcid, desccol, "columnlabel");
                        properties.setProperty("title", title);
                        properties.setProperty("width", "100");
                        primarycolumns.add(properties);
                    }
                }
                for (int i = 0; i < primarycolumns.size(); ++i) {
                    sb.append((i > 0 ? "," : "") + primarycolumns.getPropertyList(i).getProperty("columnid"));
                }
                sb.append("]");
            }
            StringBuilder sb1 = new StringBuilder("[*,");
            if (this.pagedata != null) {
                PropertyListCollection primarycolumns2 = this.pagedata.getCollection("columns");
                PropertyListCollection columns = this.pagedata.getCollection("primarycolumns");
                if (OpalUtil.isNotEmpty(primarycolumns2)) {
                    for (int i = 0; i < primarycolumns2.size(); ++i) {
                        if (primarycolumns2.getPropertyList(i).getProperty("columnid").indexOf(" ") <= 0) continue;
                        sb1.append((sb1.length() > 3 ? "," : "") + primarycolumns2.getPropertyList(i).getProperty("columnid"));
                    }
                }
                if (OpalUtil.isNotEmpty(columns)) {
                    for (int i = 0; i < columns.size(); ++i) {
                        if (columns.getPropertyList(i).getProperty("columnid").indexOf(" ") <= 0) continue;
                        sb1.append((sb1.length() > 3 ? "," : "") + columns.getPropertyList(i).getProperty("columnid"));
                    }
                }
                sb1.append("]");
            }
            sdiRequest.setRequestItem("primary" + (sb1.length() > 4 ? sb1.toString() : ""));
            sdiRequest.setRequestItem("attribute");
            if (!this.sdcid.equalsIgnoreCase("DataSet") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                sdiRequest.setRequestItem("sdiworkitem");
                sdiRequest.setRequestItem("sdiworkitemitem");
                sdiRequest.setRequestItem("sdiworkitemattribute");
                sdiRequest.setRequestItem("dataset");
                sdiRequest.setRequestItem("datasetattribute");
            }
            sdiRequest.setRetainRsetid(true);
            sdiRequest.setDataLockOption("LA");
            sdiRequest.setLockOption("LA");
            sdiRequest.setPrimaryLockOption("LA");
            sdiRequest.setValidateCheckout(true);
            sdiRequest.setAutoLockTimeout(true);
            sdiRequest.setReturnMaskedData(true);
            SDIProcessor sdi = new SDIProcessor(this.pageContext);
            SDIData sdidata = sdi.getSDIData(sdiRequest);
            if (sdidata != null) {
                DataSet attributes;
                String __key;
                String k3;
                String k1;
                String[] keyID1;
                String[] inData;
                String[] newlyAddedArr;
                boolean forceprimary;
                DataSet primary = sdidata.getDataset("primary");
                boolean bl = forceprimary = this.sdcid.equalsIgnoreCase("SDIWorkItem") && this.forceprimary && primary != null && primary.getRowCount() > 0;
                if (!forceprimary && (this.sdcid.equalsIgnoreCase("SDIWorkItem") || this.sdcid.equalsIgnoreCase("DataSet")) && primary != null && primary.getRowCount() > 0) {
                    forceprimary = true;
                    this.forceprimary = true;
                }
                if (this.sdcid.equals("SDIWorkItem")) {
                    this.dsSdiWorkItem = primary;
                    this.dsSdiWorkItemAttributes = sdidata.getDataset("attribute");
                    this.dsSdiWorkItemAttributesOld.copyRow(this.dsSdiWorkItemAttributes, -1, 1);
                    if (newlyaddedStr != null && !newlyaddedStr.equals("")) {
                        if (this.dsSdiWorkItemAttributes.getRowCount() > 0) {
                            int length = this.dsSdiWorkItemAttributes.getRowCount();
                            for (int k = 0; k < length; ++k) {
                                this.dsSdiWorkItemAttributes.deleteRow(0);
                            }
                        }
                        if (!(newlyAddedArr = newlyaddedStr.split(RECORD_SEPARATOR))[0].equals("No Elements")) {
                            for (int i = 0; i < newlyAddedArr.length; ++i) {
                                inData = newlyAddedArr[i].split(VALUE_SEPARATOR);
                                keyID1 = inData[2].split(";");
                                if (!inData[1].equals(this.sdcid)) continue;
                                int size = this.dsSdiWorkItemAttributes.addRow();
                                this.dsSdiWorkItemAttributes = MultiSDIAttributeMaint.addTempDSRows(this.dsSdiWorkItemAttributes, inData, keyID1, size, this.dsSdiWorkItemAttributesOld);
                            }
                        }
                    }
                    if (changeafterAdd.length() > 0 && this.dsSdiWorkItemAttributes.getRowCount() > 0) {
                        this.dsSdiWorkItemAttributes = MultiSDIAttributeMaint.checkAndAddChanges(changeafterAdd, this.dsSdiWorkItemAttributes);
                    }
                    sdidata.setDataset("attribute", this.dsSdiWorkItemAttributes);
                } else if (this.sdcid.equals("DataSet")) {
                    this.dsSdiData = primary;
                    this.dsSdiDataAttributes = sdidata.getDataset("attribute");
                    this.dsSdiDataAttributesOld.copyRow(this.dsSdiDataAttributes, -1, 1);
                    if (newlyaddedStr != null && !newlyaddedStr.equals("")) {
                        if (this.dsSdiDataAttributes.getRowCount() > 0) {
                            int length = this.dsSdiDataAttributes.getRowCount();
                            for (int k = 0; k < length; ++k) {
                                this.dsSdiDataAttributes.deleteRow(0);
                            }
                        }
                        if (!(newlyAddedArr = newlyaddedStr.split(RECORD_SEPARATOR))[0].equals("No Elements")) {
                            for (int i = 0; i < newlyAddedArr.length; ++i) {
                                inData = newlyAddedArr[i].split(VALUE_SEPARATOR);
                                keyID1 = inData[2].split(";");
                                if (!inData[1].equals(this.sdcid)) continue;
                                int size = this.dsSdiDataAttributes.addRow();
                                this.dsSdiDataAttributes = MultiSDIAttributeMaint.addTempDSRows(this.dsSdiDataAttributes, inData, keyID1, size, this.dsSdiDataAttributesOld);
                            }
                        }
                    }
                    if (changeafterAdd.length() > 0 && this.dsSdiDataAttributes.getRowCount() > 0) {
                        this.dsSdiDataAttributes = MultiSDIAttributeMaint.checkAndAddChanges(changeafterAdd, this.dsSdiDataAttributes);
                    }
                    sdidata.setDataset("attribute", this.dsSdiDataAttributes);
                } else {
                    this.dsSdiData = sdidata.getDataset("dataset");
                    this.dsSdiDataAttributes = sdidata.getDataset("datasetattribute");
                    this.dsSdiDataAttributesOld.copyRow(this.dsSdiDataAttributes, -1, 1);
                    this.dsSdiWorkItem = sdidata.getDataset("sdiworkitem");
                    this.dsSdiWorkItemAttributes = sdidata.getDataset("sdiworkitemattribute");
                    this.dsSdiWorkItemAttributesOld.copyRow(this.dsSdiWorkItemAttributes, -1, 1);
                    DataSet dsSDCAttribute = sdidata.getDataset("attribute");
                    dsSDCAttribute.addColumn("oldtextvalue", 0);
                    this.dsSDCAttributeOld.copyRow(dsSDCAttribute, -1, 1);
                    if (newlyaddedStr != null && !newlyaddedStr.equals("")) {
                        String[] newlyAddedArr2;
                        if (this.dsSdiWorkItemAttributes.getRowCount() > 0) {
                            int length = this.dsSdiWorkItemAttributes.getRowCount();
                            for (int k = 0; k < length; ++k) {
                                this.dsSdiWorkItemAttributes.deleteRow(0);
                            }
                        }
                        if (this.dsSdiDataAttributes.getRowCount() > 0) {
                            int length = this.dsSdiDataAttributes.getRowCount();
                            for (int k = 0; k < length; ++k) {
                                this.dsSdiDataAttributes.deleteRow(0);
                            }
                        }
                        if (dsSDCAttribute.getRowCount() > 0) {
                            int length = dsSDCAttribute.getRowCount();
                            for (int k = 0; k < length; ++k) {
                                dsSDCAttribute.deleteRow(0);
                            }
                        }
                        if (!(newlyAddedArr2 = newlyaddedStr.split(RECORD_SEPARATOR))[0].equals("No Elements")) {
                            for (int i = 0; i < newlyAddedArr2.length; ++i) {
                                int size;
                                String[] inData2 = newlyAddedArr2[i].split(VALUE_SEPARATOR);
                                String[] keyID12 = inData2[2].split(";");
                                if (inData2[1].equals("SDIWorkItem")) {
                                    size = this.dsSdiWorkItemAttributes.addRow();
                                    this.dsSdiWorkItemAttributes = MultiSDIAttributeMaint.addTempDSRows(this.dsSdiWorkItemAttributes, inData2, keyID12, size, this.dsSdiWorkItemAttributesOld);
                                    continue;
                                }
                                if (inData2[1].equals("DataSet")) {
                                    size = this.dsSdiDataAttributes.addRow();
                                    this.dsSdiDataAttributes = MultiSDIAttributeMaint.addTempDSRows(this.dsSdiDataAttributes, inData2, keyID12, size, this.dsSdiDataAttributesOld);
                                    continue;
                                }
                                size = dsSDCAttribute.addRow();
                                dsSDCAttribute = MultiSDIAttributeMaint.addTempDSRows(dsSDCAttribute, inData2, keyID12, size, this.dsSDCAttributeOld);
                            }
                        }
                    }
                    if (changeafterAdd.length() > 0 && dsSDCAttribute.getRowCount() > 0) {
                        dsSDCAttribute = MultiSDIAttributeMaint.checkAndAddChanges(changeafterAdd, dsSDCAttribute);
                    }
                    if (changeafterAdd.length() > 0 && this.dsSdiDataAttributes.getRowCount() > 0) {
                        this.dsSdiDataAttributes = MultiSDIAttributeMaint.checkAndAddChanges(changeafterAdd, this.dsSdiDataAttributes);
                    }
                    if (changeafterAdd.length() > 0 && this.dsSdiWorkItemAttributes.getRowCount() > 0) {
                        this.dsSdiWorkItemAttributes = MultiSDIAttributeMaint.checkAndAddChanges(changeafterAdd, this.dsSdiWorkItemAttributes);
                    }
                    sdidata.setDataset("attribute", dsSDCAttribute);
                    sdidata.setDataset("sdiworkitemattribute", this.dsSdiWorkItemAttributes);
                    sdidata.setDataset("datasetattribute", this.dsSdiDataAttributes);
                    if (this.isAttributeAcross) {
                        for (int p = 0; p < primary.getRowCount(); ++p) {
                            HashMap<String, String> filterSDIWI;
                            String keycolid1 = this.sdcprops.getProperty("keycolid1");
                            String keycolid2 = this.sdcprops.getProperty("keycolid2");
                            String keycolid3 = this.sdcprops.getProperty("keycolid3");
                            String currentPrimaryKey = primary.getValue(p, keycolid1) + ";" + primary.getValue(p, keycolid2, "(null)") + ";" + primary.getValue(p, keycolid3, "(null)");
                            String[] pkeys = StringUtil.split(currentPrimaryKey, ";");
                            if (this.dsSdiWorkItem.getRowCount() > 0) {
                                if (!this.dsSdiWorkItem.isValidColumn("__parentkey")) {
                                    this.dsSdiWorkItem.addColumn("__parentkey", 0);
                                }
                                filterSDIWI = new HashMap<String, String>();
                                filterSDIWI.put(PROPERTY_KEYID1, pkeys[0]);
                                filterSDIWI.put(PROPERTY_KEYID2, pkeys[1]);
                                filterSDIWI.put(PROPERTY_KEYID3, pkeys[2]);
                                DataSet thisSDIWI = this.dsSdiWorkItem.getFilteredDataSet(filterSDIWI);
                                thisSDIWI.setValue(-1, "__parentkey", currentPrimaryKey);
                            }
                            if (this.dsSdiData.getRowCount() <= 0) continue;
                            if (!this.dsSdiData.isValidColumn("__parentkey")) {
                                this.dsSdiData.addColumn("__parentkey", 0);
                            }
                            filterSDIWI = new HashMap();
                            filterSDIWI.put(PROPERTY_KEYID1, pkeys[0]);
                            filterSDIWI.put(PROPERTY_KEYID2, pkeys[1]);
                            filterSDIWI.put(PROPERTY_KEYID3, pkeys[2]);
                            DataSet thisSDIData = this.dsSdiData.getFilteredDataSet(filterSDIWI);
                            thisSDIData.setValue(-1, "__parentkey", currentPrimaryKey);
                        }
                        this.dsChildSdiWorkItem = this.dsSdiWorkItem;
                        this.dsChildSdiData = this.dsSdiData;
                    }
                }
                String c_sdcid = "";
                if (forceprimary) {
                    c_sdcid = primary.getValue(0, PROPERTY_SDCID);
                }
                if (primary != null) {
                    if (primary.getColumnValues("__lockedby", "").length() > 0) {
                        this.logger.info("Rset locked.");
                        this.viewonly = true;
                    }
                    primary.addColumn("__key", 0);
                    if (forceprimary) {
                        primary.addColumn("__fkey", 0);
                    }
                    for (int r = 0; r < primary.getRowCount(); ++r) {
                        String lockedby = primary.getValue(r, "__lockedby");
                        String checkedoutby = primary.getValue(r, "__checkedoutbyuser");
                        String checkedoutbydepartment = primary.getValue(r, "__checkedoutbydepartment");
                        LockedImage lockedImageObj = LockedImage.getLockedImage(lockedby, checkedoutby, checkedoutbydepartment, this.connectionInfo, this.getTranslationProcessor());
                        if (this.sdcid.equals("SDIAttachment")) {
                            primary.setValue(r, "__key", primary.getValue(r, "sdiattachmentid", "") + ";(null);(null)");
                        } else {
                            k1 = primary.getValue(r, this.sdcprops.getProperty("keycolid1"), "");
                            String k2 = this.sdcprops.getProperty("keycolid2").length() > 0 ? primary.getValue(r, this.sdcprops.getProperty("keycolid2"), "") : "";
                            k3 = this.sdcprops.getProperty("keycolid3").length() > 0 ? primary.getValue(r, this.sdcprops.getProperty("keycolid3"), "") : "";
                            __key = k1 + (k2.length() > 0 ? ";" + k2 : ";(null)") + (k3.length() > 0 ? ";" + k3 : ";(null)");
                            primary.setValue(r, "__key", __key);
                            this.lockedImageMap.put(__key, lockedImageObj);
                            if ("Y".equals(primary.getValue(r, "templateflag"))) {
                                this.templateSet.add(__key);
                            }
                        }
                        if (!forceprimary) continue;
                        String c_k1 = primary.getValue(r, PROPERTY_KEYID1, "");
                        String c_k2 = this.getSDCProcessor().getProperty(c_sdcid, "keycolid2").length() > 0 ? primary.getValue(r, PROPERTY_KEYID2, "") : "";
                        String c_k3 = this.getSDCProcessor().getProperty(c_sdcid, "keycolid3").length() > 0 ? primary.getValue(r, PROPERTY_KEYID3, "") : "";
                        String c___key = c_k1 + (c_k2.length() > 0 ? ";" + c_k2 : ";(null)") + (c_k3.length() > 0 ? ";" + c_k3 : ";(null)");
                        primary.setValue(r, "__fkey", c___key);
                    }
                }
                if ((attributes = sdidata.getDataset("attribute")) != null) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("hiddenflag", "Y");
                    DataSet filtered = attributes.getFilteredDataSet(filter, true);
                    sdidata.setDataset("attribute", filtered);
                }
                this.rsetid = sdidata.getRsetid();
                if (forceprimary) {
                    SDIRequest c_sdireq = new SDIRequest();
                    c_sdireq.setSDCid(c_sdcid);
                    c_sdireq.setKeyid1List(primary.getColumnValues(PROPERTY_KEYID1, ";"));
                    c_sdireq.setKeyid2List(primary.getColumnValues(PROPERTY_KEYID2, ";"));
                    c_sdireq.setKeyid3List(primary.getColumnValues(PROPERTY_KEYID3, ";"));
                    c_sdireq.setRequestItem("primary" + (sb1.length() > 4 ? sb1.toString() : ""));
                    c_sdireq.setRequestItem("attribute");
                    if (this.sdcid.equals("DataSet")) {
                        c_sdireq.setRequestItem("sdiworkitem");
                        c_sdireq.setRequestItem("sdiworkitemitem");
                        c_sdireq.setRequestItem("sdiworkitemattribute");
                    } else if (this.sdcid.equals("SDIWorkItem")) {
                        c_sdireq.setRequestItem("dataset");
                        c_sdireq.setRequestItem("datasetattribute");
                    } else {
                        c_sdireq.setRequestItem("sdiworkitem");
                        c_sdireq.setRequestItem("sdiworkitemitem");
                        c_sdireq.setRequestItem("sdiworkitemattribute");
                        c_sdireq.setRequestItem("dataset");
                        c_sdireq.setRequestItem("datasetattribute");
                    }
                    SDIData c_sdidata = sdi.getSDIData(c_sdireq);
                    DataSet c_fp = c_sdidata.getDataset("primary");
                    if (this.sdcid.equals("DataSet")) {
                        this.dsSdiWorkItem = c_sdidata.getDataset("sdiworkitem");
                        this.dsSdiWorkItemItem = c_sdidata.getDataset("sdiworkitemitem");
                        this.setFKey(this.dsSdiWorkItem);
                    } else if (this.sdcid.equals("SDIWorkItem")) {
                        DataSet ds = c_sdidata.getDataset("dataset");
                        this.setFKey(ds);
                        DataSet dsAttributes = c_sdidata.getDataset("datasetattribute");
                        for (int wi = 0; wi < this.dsSdiWorkItem.getRowCount(); ++wi) {
                            String k12 = this.dsSdiWorkItem.getValue(wi, "sdiworkitemid") + ";(null);(null)";
                            HashMap<String, Object> findSDIData = new HashMap<String, Object>();
                            findSDIData.put("__fkey", this.dsSdiWorkItem.getValue(wi, "__fkey"));
                            findSDIData.put("sourceworkitemid", this.dsSdiWorkItem.getValue(wi, "workitemid"));
                            findSDIData.put("sourceworkiteminstance", this.dsSdiWorkItem.getBigDecimal(wi, "workiteminstance"));
                            DataSet dsThisWI = ds.getFilteredDataSet(findSDIData);
                            if (dsThisWI.getRowCount() <= 0) continue;
                            dsThisWI.setString(-1, "__parentkey", k12);
                            this.dsSdiData.copyRow(dsThisWI, -1, 1);
                            for (int d = 0; d < dsThisWI.getRowCount(); ++d) {
                                String sdidataid = dsThisWI.getValue(d, "sdidataid");
                                findSDIData.clear();
                                findSDIData.put(PROPERTY_KEYID1, sdidataid);
                                findSDIData.put(PROPERTY_SDCID, "DataSet");
                                DataSet thisDSAttributes = dsAttributes.getFilteredDataSet(findSDIData);
                                this.dsSdiDataAttributes.copyRow(thisDSAttributes, -1, 1);
                            }
                        }
                        this.setFKey(this.dsSdiData);
                        this.dsChildSdiData = this.dsSdiData;
                    }
                    c_fp.addColumn("__key", 0);
                    for (int r = 0; r < c_fp.getRowCount(); ++r) {
                        k1 = c_fp.getValue(r, this.getSDCProcessor().getProperty(c_sdcid, "keycolid1"), "");
                        String k2 = this.getSDCProcessor().getProperty(c_sdcid, "keycolid2").length() > 0 ? c_fp.getValue(r, this.getSDCProcessor().getProperty(c_sdcid, "keycolid2"), "") : "";
                        k3 = this.getSDCProcessor().getProperty(c_sdcid, "keycolid3").length() > 0 ? c_fp.getValue(r, this.getSDCProcessor().getProperty(c_sdcid, "keycolid3"), "") : "";
                        __key = k1 + (k2.length() > 0 ? ";" + k2 : ";(null)") + (k3.length() > 0 ? ";" + k3 : ";(null)");
                        c_fp.setValue(r, "__key", __key);
                    }
                    sdidata.setDataset(PROPERTY_FORCEPRIMARY, c_fp);
                }
            }
            return sdidata;
        }
        this.logger.warn("No keyid1 or sdcid provided.");
        return null;
    }

    private String getReplacedHeader(String xKey) {
        boolean forceprimary = this.forceprimary && this.sdiData.getDataset(PROPERTY_FORCEPRIMARY) != null && this.sdiData.getDataset("primary").getRowCount() > 0;
        String sdcid = forceprimary ? this.sdiData.getDataset("primary").getValue(0, PROPERTY_SDCID, "") : this.sdcid;
        PropertyList sdcprops = forceprimary ? this.getSDCProcessor().getPropertyList(sdcid) : this.sdcprops;
        String title = "";
        int prow = -1;
        String out = this.columnHeadText;
        DataSet primary = forceprimary ? this.sdiData.getDataset(PROPERTY_FORCEPRIMARY) : this.sdiData.getDataset("primary");
        int row = -1;
        if (forceprimary) {
            prow = this.sdiData.getDataset("primary").findRow("__key", xKey);
            String fkey = this.sdiData.getDataset("primary").getValue(prow, "__fkey", "");
            row = primary.findRow("__key", fkey);
        } else {
            row = primary.findRow("__key", xKey);
        }
        if (row > -1) {
            for (String token : this.columnHeadTextTokens) {
                String value = token.equalsIgnoreCase(PROPERTY_KEYID1) || token.equalsIgnoreCase("keycolid1") ? primary.getValue(row, sdcprops.getProperty("keycolid1"), "") : (token.equalsIgnoreCase(PROPERTY_KEYID2) || token.equalsIgnoreCase("keycolid2") ? (sdcprops.getProperty("keycolid2").length() > 0 && primary.isValidColumn(sdcprops.getProperty("keycolid2")) ? primary.getValue(row, sdcprops.getProperty("keycolid2"), "") : "") : (token.equalsIgnoreCase(PROPERTY_KEYID3) || token.equalsIgnoreCase("keycolid3") ? (sdcprops.getProperty("keycolid2").length() > 0 && primary.isValidColumn(sdcprops.getProperty("keycolid3")) ? primary.getValue(row, sdcprops.getProperty("keycolid3"), "") : "") : (token.equalsIgnoreCase("desccol") ? (sdcprops.getProperty("desccol").length() > 0 && primary.isValidColumn(sdcprops.getProperty("desccol")) ? primary.getValue(row, sdcprops.getProperty("desccol"), "") : "") : (token.equalsIgnoreCase(PROPERTY_SDCID) ? sdcid : (primary.isValidColumn(token) ? primary.getValue(row, token, "") : "")))));
                out = StringUtil.replaceAll(out, "[" + token + "]", value, true);
            }
            if (forceprimary && prow > -1) {
                title = this.getTranslationProcessor().translate(this.getSDCProcessor().getProperty(this.sdcid, "singular")) + " " + this.sdiData.getDataset("primary").getValue(prow, this.getSDCProcessor().getProperty(this.sdcid, "keycolid1"));
            } else {
                title = this.getTranslationProcessor().translate(this.getSDCProcessor().getProperty(sdcid, "singular")) + " " + primary.getValue(row, this.getSDCProcessor().getProperty(sdcid, "keycolid1"));
                if (primary.isValidColumn(sdcprops.getProperty("keycolid2"))) {
                    title = title + " (";
                    title = title + primary.getValue(row, sdcprops.getProperty("keycolid2"));
                    if (primary.isValidColumn(sdcprops.getProperty("keycolid3"))) {
                        title = title + ", ";
                        title = title + primary.getValue(row, sdcprops.getProperty("keycolid3"));
                    }
                    title = title + ")";
                }
            }
        }
        return "<div title=\"" + title + "\">" + out + "</div>";
    }

    @Override
    protected String getSDIAcrossModeParentColumnValue(String xKey, String xValue, String columnId, DataSet ds) {
        DataSet dsPrimary = this.sdiData.getDataset("primary");
        int prow = dsPrimary.findRow("__key", xKey);
        String fkey = dsPrimary.getValue(prow, "__fkey", "");
        int row = ds.findRow("__key", fkey);
        return ds.getValue(row, columnId);
    }

    @Override
    protected String getSDIAcrossModeColumnValue(String xKey, String xValue, String columnId, DataSet ds) {
        DataSet dsPrimary = this.sdiData.getDataset("primary");
        int prow = dsPrimary.findRow("__key", xKey);
        return ds.getValue(prow, columnId);
    }

    @Override
    protected String getSDIAcrossModeParentWorkItemColumnValue(String xKey, String xValue, String columnId, DataSet ds) {
        DataSet dsPrimary = this.sdiData.getDataset("primary");
        int prow = dsPrimary.findRow("__key", xKey);
        String fkey = dsPrimary.getValue(prow, "__fkey", "");
        String sourceWid = dsPrimary.getValue(prow, "sourceworkitemid");
        if (sourceWid.length() == 0) {
            return "&nbsp;";
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("__fkey", fkey);
        map.put("workitemid", sourceWid);
        map.put("workiteminstance", dsPrimary.getBigDecimal(prow, "sourceworkiteminstance"));
        int row = ds.findRow(map);
        return ds.getValue(row, columnId);
    }

    @Override
    public String getXHeader(String xKey, String xTitle) {
        return this.getXHeader(xKey, xTitle, true);
    }

    @Override
    public String getXHeader(String xKey, String xTitle, boolean useTableHeader) {
        if (!this.isAttributeAcross) {
            String header = this.getReplacedHeader(xKey);
            LockedImage lockedImageObj = this.lockedImageMap.get(xKey);
            String lockedImage = lockedImageObj != null ? lockedImageObj.getLockedImage() : "";
            return header + "<div id=\"lockedimageholder_" + SafeHTML.encodeForHTMLAttribute(xKey) + "\" style=\"vertical-align:bottom:0px;\">" + lockedImage + "</div>";
        }
        if (!this.readOnlySet.contains(xKey) && !this.informationOnlySet.contains(xKey)) {
            ++this.colheaderIndex;
        }
        String td = useTableHeader ? "th" : "td";
        return "<table id=\"attHeadTable\" style=\"width:100%\" cellspacing=0 cellpadding=0><tr><" + td + ">" + this.getTranslationProcessor().translate(xTitle) + "</" + td + ">" + (this.isAttributeAcross && !this.readOnlySet.contains(xKey) && !this.informationOnlySet.contains(xKey) && !this.viewonly ? "<" + td + " style=\"text-align:right\"><img style=\"vertical-align:bottom\" src=\"WEB-OPAL/images/movedown.gif\" onclick=\"" + JS_CLASS + ".filldown( " + this.colheaderIndex + ", false )\"/></" + td + ">" : "") + "</tr></table>";
    }

    @Override
    public String getColumnXHeader(String xKey, String xTitle, boolean useTableHeader) {
        if (!this.readOnlySet.contains(xKey)) {
            ++this.colheaderIndex1;
        }
        String td = useTableHeader ? "th" : "td";
        return "<table style=\"width:100%\"><tr><" + td + ">" + this.getTranslationProcessor().translate(xTitle) + "</" + td + ">" + (this.isAttributeAcross && !this.readOnlySet.contains(xKey) && !this.viewonly ? "<" + td + " style=\"text-align:right\"><img style=\"vertical-align:bottom\" src=\"WEB-OPAL/images/movedown.gif\" onclick=\"" + JS_CLASS + ".filldown( " + this.colheaderIndex1 + ", true )\"/></" + td + ">" : "") + "</tr></table>";
    }

    @Override
    public String getChildSDIXHeader(String xKey, String xTitle, String sdcId, int colIndex) {
        int childType;
        int n = childType = sdcId.equals("SDIWorkItem") ? 1 : 2;
        if (!this.isAttributeAcross) {
            String header = this.getReplacedHeader(xKey);
            LockedImage lockedImageObj = this.lockedImageMap.get(xKey);
            String lockedImage = lockedImageObj != null ? lockedImageObj.getLockedImage() : "";
            return header + "<div id=\"lockedimageholder_" + SafeHTML.encodeForHTMLAttribute(xKey) + "\" style=\"vertical-align:bottom:0px;\">" + lockedImage + "</div>";
        }
        return "<table style=\"width:100%\"><tr><th>" + this.getTranslationProcessor().translate(xTitle) + "</th>" + (this.isAttributeAcross && !this.readOnlySet.contains(xKey) && !this.informationOnlySet.contains(xKey) && !this.viewonly ? "<td style=\"text-align:right\"><img style=\"vertical-align:bottom\" src=\"WEB-OPAL/images/movedown.gif\" onclick=\"multiSDIAttributeMaint.filldown(" + colIndex + ", false, '" + childType + "' )\"/></td>" : "") + "</tr></table>";
    }

    @Override
    public boolean isReadOnlyInformationOnly(String xKey) {
        return this.readOnlySet.contains(xKey) || this.informationOnlySet.contains(xKey);
    }

    @Override
    public String getYHeader(String yKey, String yTitle) {
        if (this.isAttributeAcross) {
            String header = this.getReplacedHeader(yKey);
            LockedImage lockedImageObj = this.lockedImageMap.get(yKey);
            String lockedImage = lockedImageObj != null ? lockedImageObj.getLockedImage() : "";
            return "<table style=\"width:100%\"><tr><td>" + header + "</td><td id=\"lockedimageholder_" + SafeHTML.encodeForHTMLAttribute(yKey) + "\" style=\"text-align:right\">" + lockedImage + "</td></tr></table>";
        }
        ++this.yheaderIndex;
        return "<table style=\"width:100%\"><tr><td>" + yTitle + "</td>" + (!this.isAttributeAcross && !this.readOnlySet.contains(yKey) && !this.viewonly ? "<td style=\"text-align:right\"><img style=\"transform:rotate(270deg)\" src=\"WEB-OPAL/images/movedown.gif\" onclick=\"multiSDIAttributeMaint.fillacross( " + this.yheaderIndex + " )\"/></td>" : "") + "</tr></table>";
    }

    @Override
    public String getHeaderHTML() {
        StringBuffer html = new StringBuffer();
        html.append(super.getHeaderHTML());
        html.append("<script src=\"WEB-CORE/elements/attributes/scripts/multisdiattributemaint.js\"></script>");
        html.append("<style>");
        html.append("textarea{resize:none;max-width:500px;}");
        html.append("</style>");
        if (!this.hidetoolbar) {
            html.append(this.getToolBar());
        }
        html.append("<div id=\"messageBoxDiv\" style=\"display:none;padding:10px;margin:5px;border:solid 1px green;height:10px\"></div>");
        return html.toString();
    }

    @Override
    public String getFooterHTML() {
        ArrayList<String> groups;
        String newlyaddedstr = this.pagedata.getProperty("newlyaddedArr");
        String changeafterAdd = this.pagedata.getProperty("changesafterAdd");
        String delteAttBh = this.pagedata.getProperty("deleteattributebehavior");
        String attForDel = this.pagedata.getProperty("attForDel");
        String cells = this.pagedata.getProperty("cells");
        StringBuffer html = new StringBuffer();
        html.append("<textarea id=\"gridtextarea\" onblur=\"hideGridTextArea()\" onmouseout=\"this.blur();this.onblur()\" onchange=\"currentTextAreaChanged( this )\" onkeyup=\"currentTextAreaChanged( this )\" rows=\"10\" cols=\"40\" style=\"border:1px solid;display: none; z-index: 100; position: absolute; top: 100px; left: 100px\"></textarea>\n");
        html.append("<div style=\"position:absolute; display:none\" id=\"dd_div\" class=\"dropdowndiv\" onkeydown=\"dd_divKeyPress()\" onmouseover=\"this.onblur = null;\" onmouseout=\"this.onblur = dd_divBlur;\"></div>\n");
        html.append("<script>\n");
        html.append(this.pageContext.getAttribute("dd_dropdownvalues") != null ? this.pageContext.getAttribute("dd_dropdownvalues") : "");
        html.append(this.pageContext.getAttribute("dd_dropdownfields") != null ? this.pageContext.getAttribute("dd_dropdownfields") : "");
        html.append("dd_offsetAjust.offset = document.getElementById('multiMaintGridDiv');");
        html.append("dd_offsetAjust.top = ").append(this.hidetoolbar ? "0" : "46").append(";");
        html.append("dd_offsetAjust.left = 0;");
        if (this.collapseGroups && this.browser.isIE() && (groups = this.getGroups()).size() > 0) {
            html.append("sapphire.events.attachEvent( window, 'onload',function(){");
            String prefix = this.elementid + "_groupcell_";
            for (int i = 0; i < groups.size(); ++i) {
                String gid = prefix + StringUtil.replaceAll(groups.get(i).toString(), " ", "_").trim();
                String cid = "multiattributemaint_" + StringUtil.replaceAll(groups.get(i).toString(), " ", "_").trim();
                html.append("").append(this.groupClick).append("(document.getElementById('" + gid + "'),'").append(cid).append("', true);");
            }
            html.append("});");
        }
        if (this.dropdownComboValues.length() > 0) {
            html.append(this.dropdownComboValues + "\n");
        }
        html.append("</script>");
        html.append("<form name=\"frmSubmit\" id=\"frmSubmit\" action=\"rc").append("\" method=\"POST\" style=\"display:none;\">");
        html.append("<input type=\"hidden\" name=\"command\" value=\"").append("page").append("\">");
        html.append("<input type=\"hidden\" id=\"transactionid\" name=\"transactionid\" value=\"" + "multimaintgrid_" + System.currentTimeMillis() + "\">");
        html.append("<input type=\"hidden\" name=\"page\" value=\"").append(this.requestContext.getProperty("page")).append("\">");
        html.append("<input type=\"hidden\" name=\"hidetoolbar\" value=\"").append(this.hidetoolbar ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"hidelayout\" value=\"").append(this.requestContext.getProperty("hidelayout")).append("\">");
        html.append("<input type=\"hidden\" name=\"popup\" value=\"").append(this.requestContext.getProperty("popup")).append("\">");
        html.append("<input type=\"hidden\" name=\"sdcid\" value=\"").append(this.sdcid).append("\">");
        html.append("<input type=\"hidden\" name=\"keyid1\" value=\"").append(this.keyid1).append("\">");
        html.append("<input type=\"hidden\" name=\"newlyaddedArr\" id=\"newlyaddedArr\" value=\"").append(newlyaddedstr).append("\">");
        html.append("<input type=\"hidden\" name=\"changesafterAdd\" id=\"changesafterAdd\" value=\"").append(changeafterAdd).append("\">");
        html.append("<input type=\"hidden\" name=\"delteAttBh\" id=\"delteAttBh\" value=\"").append(delteAttBh).append("\">");
        html.append("<input type=\"hidden\" name=\"attForDel\" id=\"attForDel\" value=\"").append(attForDel).append("\">");
        html.append("<input type=\"hidden\" name=\"cells\" id=\"cells\" value=\"").append(cells).append("\">");
        if (this.keyid2 != null && this.keyid2.length() > 0) {
            html.append("<input type\"hidden\" name=\"keyid2\" value=\"").append(this.keyid2).append("\">");
            if (this.keyid3 != null && this.keyid3.length() > 0) {
                html.append("<input type\"hidden\" name=\"keyid3\" value=\"").append(this.keyid3).append("\">");
            }
        }
        String layout = this.pageContext.getRequest().getParameter("layout");
        String indexofsetdisplay = this.pageContext.getRequest().getParameter("indexofsetdisplay");
        html.append("<input type=\"hidden\" name=\"layout\" value=\"").append(layout != null ? layout : "").append("\">");
        html.append("<input type=\"hidden\" name=\"gridlayout\" value=\"").append(this.isAttributeAcross ? "Attribute Across" : "SDIAcross").append("\">");
        html.append("<input type=\"hidden\" name=\"indexofsetdisplay\" value=\"").append(indexofsetdisplay != null ? indexofsetdisplay : "").append("\">");
        html.append("<input type=\"hidden\" id=\"expandedgroups\" name=\"expandedgroups\" value=\"\">");
        html.append("</form>");
        html.append("<script>");
        html.append(JS_CLASS).append(".cells = ").append(this.clientCells.toString()).append(";");
        JSONObject jay = new JSONObject(this.validations);
        html.append(JS_CLASS).append(".validations = ").append(jay.toString()).append(";");
        JSONArray jarr = new JSONArray(this.nonduplicates);
        html.append(JS_CLASS).append(".nonduplicates = ").append(jarr.toString()).append(";");
        html.append(JS_CLASS).append(".expandedGroups  = '").append(this.expandedgroups).append("';");
        if (this.rsetid != null && this.rsetid.length() > 0) {
            html.append("__rsetlist = '" + this.rsetid + "';");
        }
        html.append("if (typeof(sapdateformat) == 'undefined'){\n");
        html.append("var sapdateformat = ").append(RegexConverter.getSapDateFormat(this.pageContext)).append(";\n");
        html.append("var decimalSeparator = sapphire.connection.decimalSeparator;\n");
        html.append("var groupingSeparator = sapphire.connection.groupingSeparator;\n");
        html.append("var isAttributeAcross = '" + this.isAttributeAcross + "';\n");
        html.append("var multiSDIAttributeMaint_pagedata = " + (this.pagedata != null ? this.pagedata.toJSONString() : "{'isAttributeAcross':'" + this.isAttributeAcross + "'}") + ";\n");
        html.append("}\n");
        html.append("var sapdateformat4DigitYear = ").append(RegexConverter.getSapDateFormat4or2DigitYear(this.pageContext, "4")).append(";\n");
        html.append("var sapdateformat2DigitYear = ").append(RegexConverter.getSapDateFormat4or2DigitYear(this.pageContext, "2")).append(";\n");
        html.append(JS_CLASS).append(".popup = ").append(this.requestContext.getProperty("popup").equalsIgnoreCase("Y") || "popup".equalsIgnoreCase((String)this.pageContext.getAttribute("layout"))).append(";");
        html.append("</script>");
        html.append(this.getGridScript());
        html.append(this.getPrimaryColumnsGridScript());
        html.append(this.getGridScript1());
        html.append(this.getGridScript2());
        return html.toString();
    }

    private boolean isSDILocked(String primaryKey) {
        LockedImage lockedImageObj = this.lockedImageMap.get(primaryKey);
        boolean locked = false;
        if (lockedImageObj != null) {
            if (lockedImageObj.isLocked()) {
                locked = true;
            } else if (("Y".equals(this.changecontrolledflag) || "T".equals(this.changecontrolledflag) && this.templateSet.contains(primaryKey)) && !lockedImageObj.isCheckedOut()) {
                locked = true;
            }
        }
        return locked;
    }

    @Override
    public String getCell(String xKey, String yKey, Map cell) {
        return this.getCell(xKey, yKey, cell, "", -1);
    }

    @Override
    public String getCell(String xKey, String yKey, Map cell, String childSDCId, int childLevel) {
        String newlyaddedStr = this.pagedata.getProperty("newlyaddedArr");
        boolean isPrimary = cell.get("__primaryRow") != null;
        String primaryKey = this.isAttributeAcross ? yKey : xKey;
        ArrayList<Object> current = new ArrayList();
        if (isPrimary && this.isAttributeAcross) {
            if (this.prevYKeyPrimaryColumn.length() == 0 || !this.prevYKeyPrimaryColumn.equalsIgnoreCase(yKey)) {
                this.primarycolumncellsarray.add(new ArrayList());
            }
            current = this.primarycolumncellsarray.get(this.primarycolumncellsarray.size() - 1);
            this.prevYKeyPrimaryColumn = yKey;
        } else if (childLevel == 1) {
            if (this.prevYKey1.length() == 0 || !this.prevYKey1.equalsIgnoreCase(yKey)) {
                this.childSDICellArray.add(new ArrayList());
            }
            current = this.childSDICellArray.get(this.childSDICellArray.size() - 1);
            this.prevYKey1 = yKey;
        } else if (childLevel == 2) {
            if (this.prevYKey2.length() == 0 || !this.prevYKey2.equalsIgnoreCase(yKey)) {
                this.subchildSDICellArray.add(new ArrayList());
            }
            current = this.subchildSDICellArray.get(this.subchildSDICellArray.size() - 1);
            this.prevYKey2 = yKey;
        } else {
            if (this.prevYKey.length() == 0 || !this.prevYKey.equalsIgnoreCase(yKey)) {
                this.cellsarray.add(new ArrayList());
            }
            current = this.cellsarray.get(this.cellsarray.size() - 1);
            this.prevYKey = yKey;
        }
        try {
            if (isPrimary) {
                String valuesJS;
                String columnid = this.isAttributeAcross ? xKey.substring(xKey.indexOf("_") + 1) : yKey.substring(yKey.indexOf("_") + 1);
                int row = (Integer)cell.get("__primaryRow");
                DataSet primaryData = this.sdiData.getDataset("primary");
                row = this.isAttributeAcross ? primaryData.findRow("__key", yKey) : primaryData.findRow("__key", xKey);
                String id = this.isAttributeAcross ? yKey + ";" + xKey : xKey + ";" + yKey;
                String fieldName = StringUtil.replaceAll(id, ";", "-");
                SDITagUtil sdiTagUtil = SDITagUtil.getInstance(this.pageContext);
                MaintColumn maintColumn = new MaintColumn(this.pageContext, null, this.requestContext.getConnectionId());
                PropertyList column = (PropertyList)cell.get(columnid + "_columnDef");
                if (this.isSDILocked(primaryKey)) {
                    column.setProperty("mode", "readonly");
                }
                maintColumn.setElementProperties(this.primarymaintelement);
                PropertyList newColProps = column.copy();
                String[] groovyprops = new String[]{"title", "mode", "validation"};
                for (int i = 0; i < groovyprops.length; ++i) {
                    String propvalue = newColProps.getProperty(groovyprops[i]);
                    if (propvalue.indexOf("$G{") != 0) continue;
                    propvalue = GroovyUtil.evaluate(propvalue, this.connectionInfo, this.sdiInfo, this.element, this.requestContext.getPropertyList().getPropertyList("pagedata"), (PropertyList)this.pageContext.getAttribute(this.sdcid + "_props"));
                    newColProps.setProperty(groovyprops[i], propvalue);
                }
                if (primaryData.isMasked(row, columnid) || this.viewonly) {
                    newColProps.setProperty("mode", "readonly");
                }
                maintColumn.setColumn(newColProps);
                maintColumn.setSdcPropertyList(this.getSDCProcessor().getPropertyList(this.sdcid));
                PropertyList attributes = maintColumn.getInputAttributes();
                attributes.setProperty("onchange", "multiSDIAttributeMaint.change(event,this,'" + this.sdcid + "','" + xKey + "','" + yKey + "')");
                attributes.setProperty("oninput", "multiSDIAttributeMaint.change(event,this,'" + this.sdcid + "','" + xKey + "','" + yKey + "')");
                attributes.setProperty("name", fieldName);
                attributes.setProperty("__valuemasked", primaryData.isMasked(row, columnid) ? "Y" : "N");
                attributes.setProperty("value", primaryData.getValue(row, columnid));
                current.add(fieldName);
                SDITagInfo sdiTagInfo = new SDITagInfo(new HashMap());
                StringBuffer html = new StringBuffer();
                html.append("<div class=\"multisdiattributemaint_cell\" style=\"display:inline").append(this.browser.isIE() ? "-flex" : "-block").append(";").append(this.browser.isIE() ? "white-space: nowrap;" : "").append("\">");
                html.append(sdiTagUtil.getInputHtml(attributes, sdiTagInfo));
                html.append("</div>");
                String mode = attributes.getProperty("mode");
                if ("dropdowncombo".equals(mode) && this.dropdownComboValues.indexOf(valuesJS = sdiTagUtil.getValueListJSArray(attributes, sdiTagInfo)) < 0) {
                    this.dropdownComboValues.append("\n" + valuesJS + ";");
                }
                JSONObject clientCell = new JSONObject();
                clientCell.put("x", xKey);
                clientCell.put("y", yKey);
                clientCell.put("elementId", fieldName);
                clientCell.put("value", primaryData.getValue(row, this.isAttributeAcross ? xKey : yKey));
                this.clientCells.put(clientCell);
                if (attributes.getProperty("validation").length() > 0 && !this.validations.containsKey(id)) {
                    this.validations.put(id, attributes.getProperty("validation"));
                }
                return html.toString();
            }
            M18NUtil m18nServer = new M18NUtil(this.connectionInfo);
            DataSet attributeData = childSDCId.equals("SDIWorkItem") ? this.dsSdiWorkItemAttributes : (childSDCId.equals("DataSet") ? this.dsSdiDataAttributes : this.sdiData.getDataset("attribute"));
            String attributeValue = "";
            boolean allowRequired = true;
            int rownumber = ((BigDecimal)cell.get("__rownum")).intValue();
            boolean hidden = attributeData.getValue(rownumber, "hiddenflag", "N").equalsIgnoreCase("Y");
            boolean mandatory = allowRequired && attributeData.getValue(rownumber, "mandatoryflag", "N").equalsIgnoreCase("Y");
            String attributeid = attributeData.getValue(rownumber, "attributeid", "");
            String attributeSDCID = attributeData.getValue(rownumber, "attributesdcid", "");
            String attributeInstance = attributeData.getValue(rownumber, "attributeinstance", "");
            String keyID1 = attributeData.getValue(rownumber, PROPERTY_KEYID1, "");
            String keyID2 = attributeData.getValue(rownumber, PROPERTY_KEYID2, "");
            String keyID3 = attributeData.getValue(rownumber, PROPERTY_KEYID3, "");
            PropertyList attributeDef = this.getAttributeDef(attributeid, attributeSDCID);
            if (attributeDef != null && attributeDef.getProperty("datatype") != "") {
                attributeData.setValue(rownumber, "datatype", attributeDef.getProperty("datatype"));
                if (attributeDef.getProperty("datatype").equals("D") || attributeDef.getProperty("datatype").equals("O")) {
                    attributeData.addColumn("datevalue", 0);
                    if (!attributeData.getValue(rownumber, "textvalue").equals("")) {
                        attributeData.setValue(rownumber, "datevalue", attributeData.getValue(rownumber, "textvalue"));
                    }
                } else if (attributeDef.getProperty("datatype").equals("C")) {
                    attributeData.addColumn("clobvalue", 0);
                    if (!attributeData.getValue(rownumber, "textvalue").equals("")) {
                        attributeData.setValue(rownumber, "clobvalue", attributeData.getValue(rownumber, "textvalue"));
                    }
                } else if (attributeDef.getProperty("datatype").equals("N")) {
                    attributeData.addColumn("numericvalue", 0);
                    if (!attributeData.getValue(rownumber, "textvalue").equals("")) {
                        attributeData.setValue(rownumber, "numericvalue", attributeData.getValue(rownumber, "textvalue"));
                    }
                }
            }
            String attributeTitle = attributeDef != null ? attributeDef.getProperty("attributetitle", attributeid) : "";
            String defaultEditorStyle = attributeDef != null ? attributeDef.getProperty("editorstyleid") : "";
            String def_instructionflag = attributeDef != null ? attributeDef.getProperty("instructionflag", attributeid) : "";
            String instructiontext = "";
            String cur_instructionflag = attributeData.getValue(rownumber, "instructionflag", "");
            if ("O".equalsIgnoreCase(def_instructionflag)) {
                this.informationOnlySet.add(xKey);
            }
            StringBuffer toadd = new StringBuffer();
            if (cur_instructionflag.length() > 0 && !cur_instructionflag.equalsIgnoreCase("N")) {
                String k3;
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT instructiontext FROM sdiattribute ");
                sql.append("WHERE ");
                sql.append("attributeid=").append(safeSQL.addVar(attributeid)).append(" ");
                sql.append("AND ");
                sql.append("sdcid=").append(safeSQL.addVar(attributeData.getValue(rownumber, "sourcesdcid", ""))).append(" ");
                sql.append("AND ");
                sql.append("keyid1=").append(safeSQL.addVar(attributeData.getValue(rownumber, "sourcekeyid1", ""))).append(" ");
                String k2 = attributeData.getValue(rownumber, "sourcekeyid2", "");
                if (k2.length() > 0) {
                    sql.append("AND ");
                    sql.append("keyid2=").append(safeSQL.addVar(k2)).append(" ");
                }
                if ((k3 = attributeData.getValue(rownumber, "sourcekeyid3", "")).length() > 0) {
                    sql.append("AND ");
                    sql.append("keyid3=").append(safeSQL.addVar(k3)).append(" ");
                }
                sql.append("AND ");
                sql.append("attributeinstance=").append(safeSQL.addVar(attributeData.getValue(rownumber, "sourceattributeinstance", ""))).append("");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
                if (ds != null && ds.size() > 0) {
                    instructiontext = ds.getValue(0, "instructiontext", "");
                } else {
                    this.logger.warn("Could not obtain source of adhoc attribute.");
                }
            } else if (def_instructionflag.length() > 0 && !def_instructionflag.equalsIgnoreCase("N")) {
                String string = instructiontext = attributeDef != null ? attributeDef.getProperty("instructiontext") : "";
            }
            if (instructiontext.length() > 0) {
                instructiontext = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(instructiontext, "\r", ""), "\n", ""), "\"", "&quot;"), "'", "&#39;");
            }
            EditorStyleField editorStyleField = null;
            if (cur_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                toadd.append("<div id=\"Info_").append(attributeid + "|" + attributeInstance + "|" + attributeSDCID + "|" + keyID1 + ";" + keyID2 + ";" + keyID3).append("\" style=\"display:inline-block;\" title=\"").append(this.getTranslationProcessor().translate("Information. Click to show.")).append("\" onclick=\"").append("").append("sapphire.alert('").append(instructiontext).append("');\">").append(this.getTranslationProcessor().translate("Information")).append("</div>");
            } else {
                String editorstyle = cur_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A") ? "Yes No Checkbox" : attributeData.getValue(rownumber, "editorstyleid", defaultEditorStyle);
                BaseSDIAttributeAction.AttributeType attributetype = BaseSDIAttributeAction.getAttributeTypeFromString(attributeData.getValue(rownumber, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(BaseSDIAttributeAction.AttributeType.adhoc)));
                boolean updateable = attributeData.getValue(rownumber, "updateableflag", "Y").equalsIgnoreCase("Y");
                String editorstyleid = attributeData.getValue(rownumber, "editorstyleid", defaultEditorStyle);
                String onchange = "multiSDIAttributeMaint.change(event,this,'" + this.sdcid + "','" + xKey + "','" + yKey + "')";
                editorStyleField = MaintAttribute.getAttributeField(this.pageContext, this.sdiInfo, new M18NUtil(), this.connectionInfo, this.elementid, editorstyleid, attributeid, attributeTitle, rownumber, rownumber, attributeData, false, hidden, updateable, this.viewonly, onchange);
                if (!this.viewonly) {
                    String compositeKey;
                    if (this.isSDILocked(primaryKey)) {
                        editorStyleField.setReadonly(true);
                    }
                    editorStyleField.setColumnProperty("oninput", onchange);
                    String validation = editorStyleField.getColumnProperty("validation");
                    if (mandatory && this.element != null && this.mandatory) {
                        validation = validation.length() > 0 ? "Mandatory;" + validation : "Mandatory";
                        editorStyleField.setColumnProperty("validation", validation);
                    }
                    if (!this.validations.containsKey(compositeKey = xKey + ";" + yKey)) {
                        this.validations.put(compositeKey, editorStyleField.getColumnProperty("validation"));
                    }
                    current.add(editorStyleField.getFieldName());
                    toadd.append(editorStyleField.getHtml());
                } else {
                    PropertyList editorstyleprops;
                    String sdcid = attributeData.getValue(rownumber, PROPERTY_SDCID, "");
                    String reftypeid = attributeData.getValue(rownumber, "editreftypeid", "");
                    try {
                        editorstyleprops = editorstyle.length() > 0 ? EditorStyleField.getEditorStyleProperties(editorstyle, sdcid, reftypeid, this.getConnectionProcessor().getSapphireConnection(), this.getQueryProcessor()) : null;
                    }
                    catch (Exception e) {
                        editorstyleprops = null;
                    }
                    attributeValue = MaintAttribute.getAttributeValue(m18nServer, this.connectionInfo, attributeData, rownumber, false, false);
                    if (editorstyleprops != null && editorstyleprops.getProperty("mode").equalsIgnoreCase("checkbox")) {
                        toadd.append("<input type=\"checkbox\" ").append(attributeValue.equalsIgnoreCase("Y") ? " checked" : "").append(" onclick=\"return false;\">");
                    } else {
                        toadd.append(attributeValue.length() > 0 ? SafeHTML.encodeForHTMLAttribute(attributeValue) : "&nbsp;");
                    }
                }
            }
            String delteAttBh = this.pagedata.getProperty("deleteattributebehavior");
            boolean instrFlag = mandatory || instructiontext.length() > 0 && !def_instructionflag.equalsIgnoreCase("O") && !cur_instructionflag.equalsIgnoreCase("O");
            StringBuffer html = new StringBuffer();
            html.append("<div class=\"multisdiattributemaint_cell\" style=\"display:inline").append(this.browser.isIE() ? "-flex" : "-block").append(";").append(this.browser.isIE() ? "white-space: nowrap;" : "").append("\">");
            if (instrFlag) {
                if (toadd.indexOf("</div>") != -1) {
                    html.append(toadd.substring(0, toadd.indexOf("</div>")));
                } else {
                    html.append(toadd);
                }
            } else {
                html.append(toadd);
            }
            html.append("&nbsp;");
            String columnID = "";
            columnID = attributeData.getValue(rownumber, "datatype").equals("D") || attributeData.getValue(rownumber, "datatype").equals("O") ? "datevalue" : (attributeData.getValue(rownumber, "datatype").equals("C") ? "clobvalue" : (attributeData.getValue(rownumber, "datatype").equals("N") ? "numericvalue" : "textvalue"));
            String flag = attributeData.getValue(rownumber, "createby") != null ? attributeData.getValue(rownumber, "createby") : "";
            String flag2 = "N";
            if ("deleteanyattributes".equals(delteAttBh)) {
                if (attributeData.getValue(rownumber, columnID) == "" && !"AddAttribute".equals(flag) && attributeData.getValue(rownumber, "oldtextvalue").equals("")) {
                    flag2 = "Y";
                } else if (attributeData.getValue(rownumber, columnID) != "" && !"AddAttribute".equals(flag) && attributeData.getValue(rownumber, "oldtextvalue").equals("")) {
                    if (!newlyaddedStr.equals("")) {
                        flag2 = "Y";
                    }
                } else if ("AddAttribute".equals(flag)) {
                    flag2 = "Y";
                }
            }
            if (mandatory) {
                html.append("<div class=\"multisdiattributemaint_flag\" title=\"").append(this.getTranslationProcessor().translate("Required")).append("\" style=\"display:inline").append(this.browser.isIE() ? "-flex" : "-block").append(";\">");
                html.append("(R)");
                html.append("</div>");
            }
            if (instructiontext.length() > 0 && !def_instructionflag.equalsIgnoreCase("O") && !cur_instructionflag.equalsIgnoreCase("O")) {
                html.append("<div class=\"multisdiattributemaint_flag\" onclick=\"").append("").append("sapphire.alert('").append(instructiontext).append("');\" title=\"").append(this.getTranslationProcessor().translate("Instructions. Click to show.")).append("\" style=\"display:inline").append(this.browser.isIE() ? "-flex" : "-block").append(";\">");
                html.append("(I)");
                html.append("</div>");
            }
            if (instrFlag) {
                html.append("&nbsp;");
            }
            if (!"donotdelete".equals(delteAttBh) && ("deleteanyattributes".equals(delteAttBh) && "Y".equals(flag2) || "deletenewattributes".equals(delteAttBh) && flag.equals("AddAttribute"))) {
                if (flag.equals("AddAttribute")) {
                    html.append("<a id=\"newAtt-" + xKey + "|" + yKey + "\" style=\"width:100%;height:100%;cursor:pointer;\" align=\"center\" onclick=\"").append(JS_CLASS).append(".remove('").append(this.sdcid).append("','").append(xKey).append("','").append(yKey).append("'," + this.esigEnabled + ");").append("\">");
                } else {
                    html.append("<a id=\"divRem_" + xKey + "|" + yKey + "\" style=\"width:100%;height:100%;cursor:pointer;\" align=\"center\" onclick=\"").append(JS_CLASS).append(".remove('").append(this.sdcid).append("','").append(xKey).append("','").append(yKey).append("'," + this.esigEnabled + ");").append("\">");
                }
                Image image = new Image(this.pageContext);
                image.setImageId("Delete");
                image.setStyle("cursor:pointer");
                image.setDimensions(12, 12);
                image.setTitle(this.getTranslationProcessor().translate("Click to remove attribute."));
                html.append(image.getHtml());
                html.append("</div>");
                if (instrFlag && toadd.indexOf("</div>") != -1) {
                    html.append("</div>");
                }
            }
            JSONObject clientCell = new JSONObject();
            clientCell.put("x", xKey);
            clientCell.put("y", yKey);
            clientCell.put("elementId", editorStyleField != null ? editorStyleField.getFieldName() : "");
            if (this.viewonly) {
                clientCell.put("value", attributeValue);
            } else {
                clientCell.put("value", editorStyleField != null ? editorStyleField.getAttributes().getProperty("value") : "");
            }
            this.clientCells.put(clientCell);
            return html.toString();
        }
        catch (Exception e) {
            current.add("");
            this.logger.error("Error rendering attribute cell for " + xKey + ":" + yKey, e);
            return "<div style=\"color:red;\">Attribute Error</div>";
        }
    }

    @Override
    public String getEmptyCell(String xKey, String yKey) {
        return this.getEmptyCell(xKey, yKey, "", -1);
    }

    @Override
    public String getEmptyCell(String xKey, String yKey, String sdcId, int childLevel) {
        ArrayList<String> current;
        String primaryKey;
        String string = primaryKey = this.isAttributeAcross ? yKey : xKey;
        if (sdcId.length() == 0) {
            sdcId = this.sdcid;
        }
        boolean isLocked = this.isSDILocked(primaryKey);
        if (childLevel == 1) {
            if (this.prevYKey1.length() == 0 || !this.prevYKey1.equalsIgnoreCase(yKey)) {
                this.childSDICellArray.add(new ArrayList());
            }
            current = this.childSDICellArray.get(this.childSDICellArray.size() - 1);
            if (!this.informationOnlySet.contains(xKey)) {
                current.add("");
            }
            this.prevYKey1 = yKey;
        } else if (childLevel == 2) {
            if (this.prevYKey2.length() == 0 || !this.prevYKey2.equalsIgnoreCase(yKey)) {
                this.subchildSDICellArray.add(new ArrayList());
            }
            current = this.subchildSDICellArray.get(this.subchildSDICellArray.size() - 1);
            if (!this.informationOnlySet.contains(xKey)) {
                current.add("");
            }
            this.prevYKey2 = yKey;
        } else {
            if (this.prevYKey.length() == 0 || !this.prevYKey.equalsIgnoreCase(yKey)) {
                this.cellsarray.add(new ArrayList());
            }
            current = this.cellsarray.get(this.cellsarray.size() - 1);
            if (!this.informationOnlySet.contains(xKey)) {
                current.add("");
            }
            this.prevYKey = yKey;
        }
        PropertyList def = yKey.length() > 0 ? this.getAttributeDef(StringUtil.split(yKey, ";")[0], sdcId) : null;
        StringBuffer html = new StringBuffer();
        if (!this.viewonly && !isLocked && def != null) {
            Image image = new Image(this.pageContext);
            image.setImageId("Add2");
            image.setDimensions(16, 16);
            image.setDisabled(true);
            image.setOpacity(60.0f);
            image.setTitle(this.getTranslationProcessor().translate("Click to add attribute."));
            html.append("<div style=\"width:").append("100%").append(";height:100%;cursor:pointer;\" name=\"buttonDiv\" align=\"center\" onclick=\"").append(JS_CLASS).append(".add('").append(sdcId).append("','").append(xKey).append("','").append(yKey).append("'," + this.esigEnabled + ");").append("\">");
            html.append(image.getHtml());
            html.append("</div>");
        } else {
            html.append("<div style=\"width:").append("100%").append(";height:100%;\">");
            html.append("&nbsp;");
            html.append("</div>");
        }
        return html.toString();
    }

    @Override
    public String getYHeaderEndColumn() {
        return "&nbsp;";
    }

    @Override
    public String getEndColumn(String xKey) {
        StringBuffer html = new StringBuffer();
        if (!this.viewonly && !this.isSDILocked(xKey)) {
            Image image = new Image(this.pageContext);
            image.setImageId("Add");
            image.setDimensions(16, 16);
            image.setTitle(this.getTranslationProcessor().translate("Click to add new " + this.sdcid + " attribute."));
            html.append("<div id=\"div_" + xKey + "\" name=\"buttonDiv\" style=\"width:100%;height:100%;cursor:pointer;padding-top: 5px;\" align=\"center\" onclick=\"").append(JS_CLASS).append(".add('").append(this.sdcid).append("','").append(xKey).append("','").append("").append("'," + this.esigEnabled + ");").append("\">");
            html.append(image.getHtml());
            html.append("</div>");
        } else {
            html.append("<div id=\"div_" + xKey + "\" name=\"buttonDiv\" style=\"width:100%;height:100%;padding-top: 5px;\">");
            html.append("&nbsp;");
            html.append("</div>");
        }
        return html.toString();
    }

    @Override
    public String getEndColumn(String xKey, String sdcid, String workitemId, String workItemversionId) {
        StringBuffer html = new StringBuffer();
        if (!this.viewonly && !this.isSDILocked(xKey)) {
            Image image = new Image(this.pageContext);
            image.setImageId("Add");
            image.setDimensions(16, 16);
            image.setTitle(this.getTranslationProcessor().translate("Click to add new " + sdcid + " attribute."));
            html.append("<div id=\"div_" + xKey + "\" name=\"buttonDiv\" style=\"width:100%;height:100%;cursor:pointer;padding-top: 5px;\" align=\"center\" onclick=\"").append(JS_CLASS).append(".add('").append(sdcid).append("','").append(xKey).append("','").append("").append("'," + this.esigEnabled + ",'" + workitemId + "','" + workItemversionId + "' );").append("\">");
            html.append(image.getHtml());
            html.append("</div>");
        } else {
            html.append("<div id=\"div_" + xKey + "\" name=\"buttonDiv\" style=\"width:100%;height:100%;padding-top: 5px;\">");
            html.append("&nbsp;");
            html.append("</div>");
        }
        return html.toString();
    }

    @Override
    public String getEndColumn(String xKey, String sdcid) {
        return this.getEndColumn(xKey, sdcid, "", "");
    }

    @Override
    protected DataSet getChildSDIWIDataSet() {
        return this.dsChildSdiWorkItem;
    }

    @Override
    protected boolean isAttributeAcross() {
        return this.isAttributeAcross;
    }

    @Override
    protected String getSDIDataSortBy() {
        PropertyListCollection sortBy = this.pagedata.getCollectionNotNull("sdidatasortby");
        StringBuffer sortByStr = new StringBuffer();
        if (sortBy.size() > 0) {
            for (int g = 0; g < sortBy.size(); ++g) {
                PropertyList pl = sortBy.getPropertyList(g);
                String colId = pl.getProperty("columnid");
                if (colId.length() == 0) continue;
                String direction = pl.getProperty("direction");
                sortByStr.append(",").append(colId).append("D".equalsIgnoreCase(direction) ? " D" : " ");
            }
        }
        if (sortByStr.length() > 0) {
            sortByStr.append(",usersequence");
            return sortByStr.substring(1);
        }
        sortByStr.append("usersequence");
        return sortByStr.toString();
    }

    @Override
    protected String getSDIWISortBy() {
        PropertyListCollection sortBy = this.pagedata.getCollectionNotNull("sdiworkitemsortby");
        StringBuffer sortByStr = new StringBuffer();
        if (sortBy.size() > 0) {
            for (int g = 0; g < sortBy.size(); ++g) {
                PropertyList pl = sortBy.getPropertyList(g);
                String colId = pl.getProperty("columnid");
                if (colId.length() == 0) continue;
                String direction = pl.getProperty("direction");
                sortByStr.append(",").append(colId).append("D".equalsIgnoreCase(direction) ? " D" : " ");
            }
        }
        if (sortByStr.length() > 0) {
            sortByStr.append(",usersequence,workitemid,workiteminstance");
            return sortByStr.substring(1);
        }
        sortByStr.append("usersequence,workitemid,workiteminstance");
        return sortByStr.toString();
    }

    @Override
    protected boolean hideSDIWorkItemAttributes() {
        if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
            return false;
        }
        return !"Y".equalsIgnoreCase(this.pagedata.getProperty("showworkitemattributes", "Y"));
    }

    @Override
    protected boolean hideSDIDataAttributes() {
        if (this.sdcid.equalsIgnoreCase("DataSet")) {
            return false;
        }
        return !"Y".equalsIgnoreCase(this.pagedata.getProperty("showdatasetattributes", "Y"));
    }

    @Override
    protected String getHeaderText(String propertyid) {
        return this.pagedata.getProperty(propertyid);
    }

    @Override
    protected DataSet getThisSDIChildSDIWIRows(String key) {
        BigDecimal sourcewinstance;
        String sourcewid;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("__parentkey", key);
        DataSet dsCurrentPrimaryWI = this.dsChildSdiWorkItem.getFilteredDataSet(map);
        DataSet dsCurrentPrimarySDIData = this.dsChildSdiData.getFilteredDataSet(map);
        if (!this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
            for (int d = 0; d < dsCurrentPrimarySDIData.getRowCount(); ++d) {
                sourcewid = dsCurrentPrimarySDIData.getString(d, "sourceworkitemid");
                sourcewinstance = dsCurrentPrimarySDIData.getBigDecimal(d, "sourceworkiteminstance");
                HashMap<String, Object> findsdiworkitem = new HashMap<String, Object>();
                findsdiworkitem.put("workitemid", sourcewid);
                findsdiworkitem.put("workiteminstance", sourcewinstance);
                DataSet dsFilter = dsCurrentPrimaryWI.getFilteredDataSet(findsdiworkitem);
                if (dsFilter.getRowCount() != 0 || this.hideSDIDataAttributes()) continue;
                int rowAdded = dsCurrentPrimaryWI.addRow();
                dsCurrentPrimaryWI.setString(rowAdded, PROPERTY_KEYID1, dsCurrentPrimarySDIData.getValue(d, PROPERTY_KEYID1));
                dsCurrentPrimaryWI.setString(rowAdded, PROPERTY_KEYID2, dsCurrentPrimarySDIData.getValue(d, PROPERTY_KEYID2));
                dsCurrentPrimaryWI.setString(rowAdded, PROPERTY_KEYID3, dsCurrentPrimarySDIData.getValue(d, PROPERTY_KEYID3));
                dsCurrentPrimaryWI.setString(rowAdded, PROPERTY_SDCID, this.sdcid);
                dsCurrentPrimaryWI.setString(rowAdded, "workitemid", sourcewid);
                dsCurrentPrimaryWI.setNumber(rowAdded, "workiteminstance", sourcewinstance);
                dsCurrentPrimaryWI.setString(rowAdded, "__parentkey", key);
            }
        }
        if (this.hideSDIWorkItemAttributes()) {
            int i = 0;
            while (i < dsCurrentPrimaryWI.getRowCount()) {
                sourcewid = dsCurrentPrimaryWI.getString(i, "workitemid");
                sourcewinstance = dsCurrentPrimaryWI.getBigDecimal(i, "workiteminstance");
                HashMap<String, Object> findsdidata = new HashMap<String, Object>();
                findsdidata.put("sourceworkitemid", sourcewid);
                findsdidata.put("sourceworkiteminstance", sourcewinstance);
                DataSet sdidata = dsCurrentPrimarySDIData.getFilteredDataSet(findsdidata);
                if (sdidata.getRowCount() == 0) {
                    dsCurrentPrimaryWI.remove(i);
                    continue;
                }
                ++i;
            }
        }
        dsCurrentPrimaryWI.sort(this.getSDIWISortBy());
        return dsCurrentPrimaryWI;
    }

    @Override
    protected DataSet getThisSDIChildSDIDataRows(String key, DataSet dsCurrentPrimaryWI) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("__parentkey", key);
        if (this.sdcid.equalsIgnoreCase("SDIWorkItem") && this.hideSDIDataAttributes()) {
            return new DataSet();
        }
        DataSet dsCurrentPrimarySDIData = this.dsChildSdiData.getFilteredDataSet(map);
        boolean hideWIAttr = this.hideSDIWorkItemAttributes();
        if (dsCurrentPrimaryWI.getRowCount() > 0) {
            for (int d = 0; d < dsCurrentPrimaryWI.getRowCount(); ++d) {
                String wid = dsCurrentPrimaryWI.getValue(d, "workitemid");
                BigDecimal winstance = dsCurrentPrimaryWI.getBigDecimal(d, "workiteminstance");
                HashMap<String, Object> findsdidata = new HashMap<String, Object>();
                findsdidata.put("sourceworkitemid", wid);
                findsdidata.put("sourceworkiteminstance", winstance);
                DataSet dsFilter = dsCurrentPrimarySDIData.getFilteredDataSet(findsdidata);
                if (wid.length() <= 0 || dsFilter.getRowCount() != 0 || hideWIAttr) continue;
                int rowAdded = dsCurrentPrimarySDIData.addRow();
                dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID1, dsCurrentPrimaryWI.getValue(d, PROPERTY_KEYID1));
                dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID2, dsCurrentPrimaryWI.getValue(d, PROPERTY_KEYID2));
                dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID3, dsCurrentPrimaryWI.getValue(d, PROPERTY_KEYID3));
                dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_SDCID, this.sdcid);
                dsCurrentPrimarySDIData.setString(rowAdded, "sourceworkitemid", wid);
                dsCurrentPrimarySDIData.setNumber(rowAdded, "sourceworkiteminstance", winstance);
                dsCurrentPrimarySDIData.setString(rowAdded, "__parentkey", key);
            }
        }
        if (this.hideSDIDataAttributes()) {
            int i = 0;
            while (i < dsCurrentPrimarySDIData.getRowCount()) {
                String sourcewid = dsCurrentPrimarySDIData.getString(i, "sourceworkitemid");
                BigDecimal sourcewinstance = dsCurrentPrimarySDIData.getBigDecimal(i, "sourceworkiteminstance");
                HashMap<String, Object> findsdiwi = new HashMap<String, Object>();
                findsdiwi.put("workitemid", sourcewid);
                findsdiwi.put("workiteminstance", sourcewinstance);
                DataSet sdiwi = dsCurrentPrimaryWI.getFilteredDataSet(findsdiwi);
                if (sdiwi.getRowCount() == 0) {
                    dsCurrentPrimarySDIData.remove(i);
                    continue;
                }
                ++i;
            }
        }
        return dsCurrentPrimarySDIData;
    }

    @Override
    protected DataSet getChildSDIDataDataSet() {
        return this.dsChildSdiData;
    }

    @Override
    protected String getSDCId() {
        return this.sdcid;
    }

    @Override
    protected String getParentSDCId() {
        return this.parentSDCId;
    }

    @Override
    public void setChildSDIKeys() {
        if (this.isAttributeAcross) {
            boolean hideChildSDIWorkItemAttributes = this.hideSDIWorkItemAttributes();
            boolean hideChildSDIDataAttributes = this.hideSDIDataAttributes();
            if (this.dsChildSdiWorkItem.getRowCount() > 0 && !hideChildSDIWorkItemAttributes) {
                this.setChildSDIKeys(this.dsChildSdiWorkItem, "sdiworkitemid", this.dsSdiWorkItemAttributes, 1);
            }
            if (this.dsChildSdiData.getRowCount() > 0 && !hideChildSDIDataAttributes) {
                this.setChildSDIKeys(this.dsChildSdiData, "sdidataid", this.dsSdiDataAttributes, 2);
            }
        }
    }

    public void setChildSDIKeys(DataSet dsChild, String keycolumn, DataSet attributeData, int childLevel) {
        String[] keys1 = StringUtil.split(dsChild.getColumnValues(keycolumn, ";"), ";");
        String[] keys2 = null;
        String[] keys3 = null;
        if (attributeData.getRowCount() > 0) {
            int i;
            ArrayList<String> foundKey = new ArrayList<String>();
            ArrayList<String[]> usedYs = new ArrayList<String[]>();
            ArrayList<String[]> usedAttrs = new ArrayList<String[]>();
            attributeData.addColumn("__rownum", 1);
            for (i = 0; i < attributeData.getRowCount(); ++i) {
                String y_titlenew;
                boolean allowduplicates;
                String def_instructionflag;
                attributeData.setNumber(i, "__rownum", i);
                MaintAttribute.MaintenanceMode attributetype = MaintAttribute.getMode(attributeData.getValue(i, "attributesourcetype"));
                String attributesdcid = attributeData.getValue(i, "attributesdcid", "");
                if (attributetype != MaintAttribute.MaintenanceMode.adhoc && (attributetype != MaintAttribute.MaintenanceMode.linkdef || !attributesdcid.equalsIgnoreCase(childLevel == 1 ? "SDIWorkItem" : (childLevel == 2 ? "DataSet" : this.sdcid)))) continue;
                String attributeid = attributeData.getValue(i, "attributeid", "");
                String cur_instructionflag = attributeData.getValue(i, "instructionflag", "");
                int attributeinstance = attributeData.getBigDecimal(i, "attributeinstance").intValue();
                String currkey1 = attributeData.getValue(i, PROPERTY_KEYID1, "");
                String currkey2 = this.keyid2.length() > 0 ? attributeData.getValue(i, PROPERTY_KEYID2, "") : "";
                String currkey3 = this.keyid3.length() > 0 ? attributeData.getValue(i, PROPERTY_KEYID3, "") : "";
                String x_title = currkey1 + (currkey2.length() > 0 ? (currkey3.length() > 0 ? " (" + currkey2 + ") " + currkey3 : " (" + currkey2 + ")") : "");
                PropertyList attributedef = this.getAttributeDef(attributeid, childLevel == 1 ? "SDIWorkItem" : "DataSet");
                String string = def_instructionflag = attributedef != null ? attributedef.getProperty("instructionflag", attributeid) : "";
                String groupid = this.isAttributeAcross ? "NA" : (attributedef != null ? attributedef.getProperty("attributegroup", "NA") : "NA");
                String y_title = attributedef != null ? attributedef.getProperty("attributetitle", "") : attributeid;
                y_title = y_title.length() > 0 ? this.getTranslationProcessor().translate(y_title) : attributeid;
                boolean bl = allowduplicates = attributedef != null ? attributedef.getProperty("allowduplicatesflag", "Y").equalsIgnoreCase("Y") : true;
                if (attributeinstance > 1) {
                    y_title = y_title + " (" + attributeinstance + ")";
                }
                x_title = this.getTranslationProcessor().translate(x_title);
                String y_key = attributeid + ";" + attributeinstance + ";" + attributesdcid;
                String keyid1 = attributeData.getValue(i, PROPERTY_KEYID1, "");
                String keyid2 = attributeData.getValue(i, PROPERTY_KEYID2, "");
                String keyid3 = attributeData.getValue(i, PROPERTY_KEYID3, "");
                String x_key = keyid1 + ";" + keyid2 + ";" + keyid3;
                if (!allowduplicates && !this.nonduplicates.contains(attributeid + "|" + x_key)) {
                    this.nonduplicates.add(attributeid + "|" + x_key);
                }
                String x_keynew = this.isAttributeAcross ? y_key : x_key;
                String x_titlenew = this.isAttributeAcross ? y_title : x_title;
                String y_keynew = this.isAttributeAcross ? x_key : y_key;
                String string2 = y_titlenew = this.isAttributeAcross ? x_title : y_title;
                if ("O".equalsIgnoreCase(def_instructionflag)) {
                    this.informationOnlySet.add(x_keynew);
                }
                this.setChildKey(x_keynew, x_titlenew, y_keynew, y_titlenew, groupid, (HashMap)attributeData.get(i), childLevel);
                foundKey.add(this.isAttributeAcross ? y_keynew : x_keynew);
                usedYs.add(new String[]{y_keynew, y_titlenew, groupid});
                usedAttrs.add(new String[]{y_key, y_title, groupid});
            }
            if (foundKey.size() != keys1.length) {
                if (usedYs.size() > 0) {
                    for (i = 0; i < keys1.length; ++i) {
                        String x_key = keys1[i];
                        if (keys2 != null) {
                            x_key = x_key + ";" + (String)keys2[i];
                            x_key = keys3 != null ? x_key + ";" + (String)keys3[i] : x_key + ";(null)";
                        } else {
                            x_key = x_key + ";(null);(null)";
                        }
                        if (foundKey.contains(x_key)) continue;
                        String x_title = keys1[i] + (keys2 != null ? (keys3 != null ? " (" + (String)keys2[i] + ") " + (String)keys3[i] : " (" + (String)keys2[i] + ")") : "");
                        for (int a = 0; a < usedAttrs.size(); ++a) {
                            String xk = x_key;
                            String xt = x_title;
                            String yk = ((String[])usedAttrs.get(a))[0];
                            String yt = ((String[])usedAttrs.get(a))[1];
                            String g = ((String[])usedAttrs.get(a))[2];
                            this.setChildKey(yk, yt, xk, xt, g, null, childLevel);
                        }
                    }
                } else {
                    this.buildEmptyGrid(keys1, keys2, keys3, true, childLevel);
                }
            }
        } else {
            this.buildEmptyGrid(keys1, keys2, keys3, true, childLevel);
        }
    }

    @Override
    public int renderAdvancedScrollGridHeaderCols(StringBuffer html, String id) {
        boolean rightBorder;
        String title;
        String colId;
        PropertyList pl;
        int g;
        boolean show;
        PropertyList pl2;
        int g2;
        int disColCount = 0;
        boolean hideWIAttr = this.hideSDIWorkItemAttributes();
        boolean hideDSAttr = this.hideSDIDataAttributes();
        String minwidth = "min-width:100px";
        if (this.parentSDCColumns.size() > 0) {
            for (g2 = 0; g2 < this.parentSDCColumns.size(); ++g2) {
                pl2 = this.parentSDCColumns.getPropertyList(g2);
                show = "Y".equalsIgnoreCase(pl2.getProperty("show", "Y"));
                if (!this.sdcid.equalsIgnoreCase("SDIWorkitem") && !this.sdcid.equalsIgnoreCase("DataSet")) {
                    boolean bl = show = !"hidden".equalsIgnoreCase(pl2.getProperty("mode", ""));
                }
                if (!show) continue;
                ++disColCount;
            }
        }
        if (!(this.sdiwiSDCColumns.size() <= 0 || hideWIAttr && hideDSAttr)) {
            for (g2 = 0; g2 < this.sdiwiSDCColumns.size(); ++g2) {
                pl2 = this.sdiwiSDCColumns.getPropertyList(g2);
                show = "Y".equalsIgnoreCase(pl2.getProperty("show", "Y"));
                if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                    boolean bl = show = !"hidden".equalsIgnoreCase(pl2.getProperty("mode", ""));
                }
                if (!show) continue;
                ++disColCount;
            }
        }
        if (this.datasetSDCColumns.size() > 0 && !hideDSAttr) {
            for (g2 = 0; g2 < this.datasetSDCColumns.size(); ++g2) {
                pl2 = this.datasetSDCColumns.getPropertyList(g2);
                show = "Y".equalsIgnoreCase(pl2.getProperty("show", "Y"));
                if (this.sdcid.equalsIgnoreCase("DataSet")) {
                    boolean bl = show = !"hidden".equalsIgnoreCase(pl2.getProperty("mode", ""));
                }
                if (!show) continue;
                ++disColCount;
            }
        }
        int disColCount1 = 0;
        String rtBorder = this.rightBorderStyle;
        if (this.parentSDCColumns.size() > 0) {
            for (g = 0; g < this.parentSDCColumns.size(); ++g) {
                pl = this.parentSDCColumns.getPropertyList(g);
                colId = pl.getProperty("columnid");
                if (colId.indexOf(" ") > 0) {
                    colId = RequestParser.parseAlias(colId);
                }
                title = pl.getProperty("title", colId);
                String width = pl.getProperty("width", "100");
                boolean show2 = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                if (!this.sdcid.equalsIgnoreCase("SDIWorkitem") && !this.sdcid.equalsIgnoreCase("DataSet")) {
                    boolean bl = show2 = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                }
                if (!show2) continue;
                boolean bl = rightBorder = disColCount == ++disColCount1;
                if (!this.sdcid.equalsIgnoreCase("DataSet") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                    this.getColumnHeader(html, colId, width, rightBorder ? rtBorder : "");
                    continue;
                }
                this.getChildSDCColumnHeader(html, title, width, rightBorder ? rtBorder : "");
            }
        }
        if (!(this.sdiwiSDCColumns.size() <= 0 || hideWIAttr && hideDSAttr)) {
            for (g = 0; g < this.sdiwiSDCColumns.size(); ++g) {
                pl = this.sdiwiSDCColumns.getPropertyList(g);
                colId = pl.getProperty("columnid");
                if (colId.indexOf(" ") > 0) {
                    colId = RequestParser.parseAlias(colId);
                }
                title = pl.getProperty("title", colId);
                boolean show3 = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                    show3 = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                }
                String width = pl.getProperty("width", "100");
                if (!show3) continue;
                boolean bl = rightBorder = disColCount == ++disColCount1;
                if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                    this.getColumnHeader(html, colId, width, rightBorder ? rtBorder : "");
                    continue;
                }
                this.getChildSDCColumnHeader(html, title, width, rightBorder ? rtBorder : "");
            }
        }
        if (this.datasetSDCColumns.size() > 0 && !hideDSAttr) {
            for (g = 0; g < this.datasetSDCColumns.size(); ++g) {
                pl = this.datasetSDCColumns.getPropertyList(g);
                colId = pl.getProperty("columnid");
                if (colId.indexOf(" ") > 0) {
                    colId = RequestParser.parseAlias(colId);
                }
                title = pl.getProperty("title", colId);
                boolean show4 = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                String width = pl.getProperty("width", "100");
                if (this.sdcid.equalsIgnoreCase("DataSet")) {
                    boolean bl = show4 = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                }
                if (!show4) continue;
                boolean bl = rightBorder = disColCount == ++disColCount1;
                if (this.sdcid.equalsIgnoreCase("DataSet")) {
                    this.getColumnHeader(html, colId, width, rightBorder ? rtBorder : "");
                    continue;
                }
                this.getChildSDCColumnHeader(html, title, width, rightBorder ? rtBorder : "");
            }
        }
        return disColCount;
    }

    @Override
    public void renderAdvancedScrollGridYHeader(StringBuffer html, String styleClass, int totalDispCols) {
        String heightN = "height";
        if (this.browser.isIE()) {
            heightN = "min-height";
        }
        String minwidth = "min-width:100px";
        boolean hideDSAttr = this.hideSDIDataAttributes();
        boolean hideWIAttr = this.hideSDIWorkItemAttributes();
        boolean primarySDCAttributes = !this.sdcid.equals("DataSet") && !this.sdcid.equals("SDIWorkItem");
        DataSet currentDataSet = this.sdiData.getDataset("primary");
        String parentKeyCol1 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid1") : this.parentsdcprops.getProperty("keycolid1");
        String parentKeyCol2 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid2") : this.parentsdcprops.getProperty("keycolid2");
        String parentKeyCol3 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid3") : this.parentsdcprops.getProperty("keycolid3");
        DataSet primary = this.sortedPrimary = this.sortPrimary();
        for (int p = 0; p < primary.getRowCount(); ++p) {
            int dispcolCnt = 0;
            String keyId1 = primary.getValue(p, parentKeyCol1);
            String keyId2 = "";
            String keyId3 = "";
            HashMap<String, String> findPrimary = new HashMap<String, String>();
            if (this.parentSDCId.length() > 0) {
                findPrimary.put(PROPERTY_SDCID, this.parentSDCId);
            } else {
                findPrimary.put(PROPERTY_SDCID, this.sdcid);
            }
            findPrimary.put(PROPERTY_KEYID1, keyId1);
            if (parentKeyCol2.length() > 0) {
                findPrimary.put(PROPERTY_KEYID2, primary.getValue(p, parentKeyCol2));
            } else {
                keyId2 = "(null)";
            }
            if (parentKeyCol3.length() > 0) {
                findPrimary.put(PROPERTY_KEYID3, primary.getValue(p, parentKeyCol3));
            } else {
                keyId3 = "(null)";
            }
            DataSet dsCurrentPrimarySDIData = new DataSet();
            DataSet dsCurrentPrimaryWI = new DataSet();
            if (this.sdcid.equalsIgnoreCase("DataSet")) {
                dsCurrentPrimarySDIData = currentDataSet.getFilteredDataSet(findPrimary);
                dsCurrentPrimaryWI = this.dsSdiWorkItem.getFilteredDataSet(findPrimary);
            } else if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                dsCurrentPrimaryWI = currentDataSet.getFilteredDataSet(findPrimary);
            } else {
                dsCurrentPrimaryWI = this.dsSdiWorkItem.getFilteredDataSet(findPrimary);
                dsCurrentPrimarySDIData = this.dsSdiData.getFilteredDataSet(findPrimary);
            }
            int dsRowCount = dsCurrentPrimarySDIData.getRowCount();
            DataSet currentSDIWorkitems = new DataSet();
            int sdidataCnt = 1;
            if (!this.sdcid.equals("SDIWorkItem")) {
                int rowAdded;
                DataSet dsFilter;
                int d;
                if (!this.sdcid.equalsIgnoreCase("DataSet")) {
                    for (d = 0; d < dsCurrentPrimaryWI.getRowCount(); ++d) {
                        String wid = dsCurrentPrimaryWI.getValue(d, "workitemid");
                        BigDecimal winstance = dsCurrentPrimaryWI.getBigDecimal(d, "workiteminstance");
                        HashMap<String, Object> findsdidata = new HashMap<String, Object>();
                        findsdidata.put("sourceworkitemid", wid);
                        findsdidata.put("sourceworkiteminstance", winstance);
                        dsFilter = dsCurrentPrimarySDIData.getFilteredDataSet(findsdidata);
                        if (dsFilter.getRowCount() != 0 || hideWIAttr) continue;
                        rowAdded = dsCurrentPrimarySDIData.addRow();
                        dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID1, keyId1);
                        dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID2, keyId2);
                        dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_KEYID3, keyId3);
                        dsCurrentPrimarySDIData.setString(rowAdded, PROPERTY_SDCID, this.sdcid);
                        dsCurrentPrimarySDIData.setString(rowAdded, "sourceworkitemid", wid);
                        dsCurrentPrimarySDIData.setNumber(rowAdded, "sourceworkiteminstance", winstance);
                    }
                }
                for (d = 0; d < dsCurrentPrimarySDIData.getRowCount(); ++d) {
                    String sourcewid = dsCurrentPrimarySDIData.getValue(d, "sourceworkitemid");
                    BigDecimal sourcewinstance = dsCurrentPrimarySDIData.getBigDecimal(d, "sourceworkiteminstance");
                    if (sourcewid.length() > 0) {
                        HashMap<String, Object> findsdiworkitem = new HashMap<String, Object>();
                        findsdiworkitem.put("workitemid", sourcewid);
                        findsdiworkitem.put("workiteminstance", sourcewinstance);
                        dsFilter = dsCurrentPrimaryWI.getFilteredDataSet(findsdiworkitem);
                        if (dsFilter.getRowCount() > 0) {
                            currentSDIWorkitems.copyRow(dsFilter, -1, 1);
                            continue;
                        }
                        rowAdded = currentSDIWorkitems.addRow();
                        currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID1, keyId1);
                        currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID2, keyId2);
                        currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID3, keyId3);
                        currentSDIWorkitems.setString(rowAdded, PROPERTY_SDCID, this.sdcid.equalsIgnoreCase("DataSet") ? this.parentSDCId : this.sdcid);
                        currentSDIWorkitems.setString(rowAdded, "workitemid", sourcewid);
                        currentSDIWorkitems.setNumber(rowAdded, "workiteminstance", sourcewinstance);
                        continue;
                    }
                    if (hideDSAttr) continue;
                    int rowAdded2 = currentSDIWorkitems.addRow();
                    currentSDIWorkitems.setString(rowAdded2, PROPERTY_KEYID1, keyId1);
                    currentSDIWorkitems.setString(rowAdded2, PROPERTY_KEYID2, keyId2);
                    currentSDIWorkitems.setString(rowAdded2, PROPERTY_KEYID3, keyId3);
                    currentSDIWorkitems.setString(rowAdded2, PROPERTY_SDCID, this.sdcid.equalsIgnoreCase("DataSet") ? this.parentSDCId : this.sdcid);
                    currentSDIWorkitems.setString(rowAdded2, "workitemid", "");
                    currentSDIWorkitems.setNumber(rowAdded2, "workteminstance", new BigDecimal(-1));
                }
            } else if (this.sdcid.equals("SDIWorkItem")) {
                currentSDIWorkitems.copyRow(dsCurrentPrimaryWI, -1, 1);
                dsCurrentPrimarySDIData = this.dsSdiData.getFilteredDataSet(findPrimary);
            } else if (dsCurrentPrimaryWI.getRowCount() > 0) {
                currentSDIWorkitems.copyRow(dsCurrentPrimaryWI, -1, 1);
            } else {
                currentSDIWorkitems.addRow();
            }
            if (styleClass.length() > 0) {
                html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\" class=\"").append(styleClass).append("\">");
            } else {
                html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\">");
            }
            boolean trEnd = false;
            if (this.parentSDCColumns.size() > 0) {
                for (int g = 0; g < this.parentSDCColumns.size(); ++g) {
                    String rtBorder;
                    String colValue;
                    PropertyList pl = this.parentSDCColumns.getPropertyList(g);
                    String colId = pl.getProperty("columnid");
                    if (colId.indexOf(" ") > 0) {
                        colId = RequestParser.parseAlias(colId);
                    }
                    String encodedColValue = (colValue = primary.getValue(p, colId, "&nbsp;")).equals("&nbsp;") ? colValue : SafeHTML.encodeForHTMLAttribute(colValue);
                    boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                    if (!this.sdcid.equalsIgnoreCase("Dataset") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                        show = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                    }
                    String width = pl.getProperty("width", "100");
                    String mode = pl.getProperty("mode");
                    String key = primary.getValue(p, "__key");
                    boolean readOnly = mode.equalsIgnoreCase("readonly");
                    key.length();
                    if (!show) continue;
                    boolean rightBorder = ++dispcolCnt == totalDispCols;
                    String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                    if (!this.sdcid.equalsIgnoreCase("SDIWorkItem") && !this.sdcid.equalsIgnoreCase("DataSet")) {
                        boolean keyColumn;
                        boolean bl = keyColumn = colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid1")) || colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid2")) || colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid3"));
                        if (readOnly) {
                            html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;border-bottom:none;" + rtBorder + "\" >");
                            html.append(encodedColValue);
                            html.append("</td>");
                            continue;
                        }
                        String colKey = (g < 10 ? "0" : "") + g + "_" + colId;
                        this.getColumnCell(colKey, key, html, colId, colValue, width, rtBorder);
                        continue;
                    }
                    html.append("<td class=\"maintform_field\" valign=\"top\" style=\"min-width:" + width + "px;text-align:left;padding-left:0px;background-color:white;word-wrap:break-word;border-bottom:none;" + rtBorder + "\" >").append(encodedColValue).append("</td>");
                }
            }
            if (currentSDIWorkitems.getRowCount() == 0) {
                currentSDIWorkitems.addRow();
            } else {
                currentSDIWorkitems.sort(this.getSDIWISortBy());
            }
            ArrayList<DataSet> wiGroups = currentSDIWorkitems.getGroupedDataSets(this.getSDIWISortBy());
            int sdiwiGrpCount = wiGroups.size();
            for (int w = 0; w < wiGroups.size(); ++w) {
                String colId;
                boolean keyColumn;
                String colKey;
                boolean readOnly;
                String key;
                String mode;
                DataSet dswi = wiGroups.get(w);
                String wid = dswi.getValue(0, "workitemid");
                String wiinstance = dswi.getValue(0, "workiteminstance");
                HashMap<String, Object> findsdiworkitem = new HashMap<String, Object>();
                if (wid.length() == 0 && hideDSAttr && hideWIAttr) continue;
                if (wid.length() > 0 && !wiinstance.equals("-1")) {
                    findsdiworkitem.put("sourceworkitemid", dswi.getValue(0, "workitemid"));
                    findsdiworkitem.put("sourceworkiteminstance", dswi.getBigDecimal(0, "workiteminstance"));
                } else {
                    findsdiworkitem.put("sourceworkitemid", null);
                    findsdiworkitem.put("sourceworkiteminstance", null);
                }
                DataSet thisWISDIData = dsCurrentPrimarySDIData.getFilteredDataSet(findsdiworkitem);
                if (thisWISDIData.getRowCount() == 0) {
                    if (hideWIAttr && hideDSAttr) continue;
                    thisWISDIData.addRow();
                }
                sdidataCnt = thisWISDIData.getRowCount();
                if (hideDSAttr) {
                    sdidataCnt = 1;
                }
                if (!hideDSAttr || !hideWIAttr) {
                    for (int g = 0; g < this.sdiwiSDCColumns.size(); ++g) {
                        boolean keyColumn2;
                        String colValue;
                        PropertyList pl = this.sdiwiSDCColumns.getPropertyList(g);
                        String colId2 = pl.getProperty("columnid");
                        if (colId2.indexOf(" ") > 0) {
                            colId2 = RequestParser.parseAlias(colId2);
                        }
                        String encodedColValue = (colValue = dswi.getValue(0, colId2, "&nbsp;")).equals("&nbsp;") ? colValue : SafeHTML.encodeForHTMLAttribute(colValue);
                        boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                        if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                            show = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                        }
                        String width = pl.getProperty("width", "100");
                        mode = pl.getProperty("mode");
                        key = dswi.getValue(0, "__key");
                        readOnly = mode.equalsIgnoreCase("readonly");
                        if (!show) continue;
                        boolean rightBorder = ++dispcolCnt == totalDispCols;
                        String rtBorder = rightBorder ? this.rightBorderStyle : "";
                        boolean bl = keyColumn2 = colId2.equalsIgnoreCase("workitemid") || colId2.equalsIgnoreCase("workiteminstance") || colId2.equalsIgnoreCase("sdiworkitemid");
                        if (this.sdcid.equals("SDIWorkItem")) {
                            if (readOnly) {
                                html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;border-bottom:none;" + rtBorder + "\" >");
                                html.append(encodedColValue);
                                html.append("</td>");
                                continue;
                            }
                            colKey = (g < 10 ? "0" : "") + g + "_" + colId2;
                            this.getColumnCell(colKey, key, html, colId2, colValue, width, rtBorder);
                            continue;
                        }
                        html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;border-bottom:none;" + rtBorder + "\" >");
                        html.append("<div  style=\"width:100%\">");
                        html.append(encodedColValue);
                        html.append("</div>");
                        html.append("</td>");
                    }
                }
                thisWISDIData.sort(this.getSDIDataSortBy());
                String width = "";
                if (!hideDSAttr) {
                    for (int g = 0; g < this.datasetSDCColumns.size(); ++g) {
                        String rtBorder;
                        PropertyList pl = this.datasetSDCColumns.getPropertyList(g);
                        String colId3 = pl.getProperty("columnid");
                        if (colId3.indexOf(" ") > 0) {
                            colId3 = RequestParser.parseAlias(colId3);
                        }
                        width = pl.getProperty("width", "100");
                        String colValue = thisWISDIData.getValue(0, colId3, "&nbsp;");
                        String encodedColValue = colValue.equals("&nbsp;") ? colValue : SafeHTML.encodeForHTMLAttribute(colValue);
                        boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                        if (this.sdcid.equalsIgnoreCase("DataSet")) {
                            show = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                        }
                        width = pl.getProperty("width", "100");
                        mode = pl.getProperty("mode");
                        key = thisWISDIData.getValue(0, "__key");
                        readOnly = mode.equalsIgnoreCase("readonly");
                        boolean bl = keyColumn = colId3.equalsIgnoreCase("paramlistid") || colId3.equalsIgnoreCase("paramlistversionid") || colId3.equalsIgnoreCase("variantid") || colId3.equalsIgnoreCase("dataset") || colId3.equalsIgnoreCase("sdidataid");
                        if (!show) continue;
                        boolean rightBorder = ++dispcolCnt == totalDispCols;
                        String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                        if (this.sdcid.equalsIgnoreCase("DataSet")) {
                            if (readOnly) {
                                html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;" + rtBorder + "\" >");
                                html.append(encodedColValue);
                                html.append("</td>");
                                continue;
                            }
                            colKey = (g < 10 ? "0" : "") + g + "_" + colId3;
                            this.getColumnCell(colKey, key, html, colId3, colValue, width, rtBorder);
                            continue;
                        }
                        html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;" + rtBorder + "\" >");
                        html.append("<div  style=\"width:100%\">");
                        html.append(encodedColValue);
                        html.append("</div>");
                        html.append("</td>");
                    }
                }
                if (!trEnd) {
                    html.append("</tr>");
                    trEnd = true;
                    dispcolCnt = 0;
                }
                for (int r = 1; r < sdidataCnt; ++r) {
                    PropertyList pl;
                    int g;
                    html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\" class=\"").append(styleClass).append("\">");
                    trEnd = false;
                    for (g = 0; g < this.parentSDCColumns.size(); ++g) {
                        String rtBorder;
                        pl = this.parentSDCColumns.getPropertyList(g);
                        width = this.parentSDCColumns.getPropertyList(g).getProperty("width", "100");
                        boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                        String mode2 = pl.getProperty("mode");
                        if (!this.sdcid.equalsIgnoreCase("Dataset") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                            boolean bl = show = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                        }
                        if (!show) continue;
                        boolean rightBorder = ++dispcolCnt == totalDispCols;
                        String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                        if (!this.sdcid.equalsIgnoreCase("SDIWorkItem") && !this.sdcid.equalsIgnoreCase("DataSet")) {
                            String colId4 = pl.getProperty("columnid");
                            if (colId4.indexOf(" ") > 0) {
                                colId4 = RequestParser.parseAlias(colId4);
                            }
                            boolean readOnly2 = mode2.equalsIgnoreCase("readonly");
                            boolean bl = keyColumn = colId4.equalsIgnoreCase(this.sdcprops.getProperty("keycolid1")) || colId4.equalsIgnoreCase(this.sdcprops.getProperty("keycolid2")) || colId4.equalsIgnoreCase(this.sdcprops.getProperty("keycolid3"));
                            if (!readOnly2 && !keyColumn) {
                                width = "100";
                            }
                        }
                        html.append("<th class=\"maintform_fieldtitle\" valign=\"top\" style=\"min-width:" + width + ";border-top:none;border-bottom:none;text-align:left;padding-left:0px;word-wrap:break-word;background-color:white;" + rtBorder + "\" >&nbsp;</th>");
                    }
                    if (!hideWIAttr || !hideDSAttr) {
                        for (g = 0; g < this.sdiwiSDCColumns.size(); ++g) {
                            String rtBorder;
                            pl = this.sdiwiSDCColumns.getPropertyList(g);
                            boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                            if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                                show = !"hidden".equalsIgnoreCase(pl.getProperty("mode", ""));
                            }
                            width = pl.getProperty("width", "100");
                            if (!show) continue;
                            boolean rightBorder = ++dispcolCnt == totalDispCols;
                            String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                            if (this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                                boolean keyColumn3;
                                colId = pl.getProperty("columnid");
                                if (colId.indexOf(" ") > 0) {
                                    colId = RequestParser.parseAlias(colId);
                                }
                                boolean readOnly3 = pl.getProperty("mode").equalsIgnoreCase("readonly");
                                boolean bl = keyColumn3 = colId.equalsIgnoreCase("workitemid") || colId.equalsIgnoreCase("workiteminstance") || colId.equalsIgnoreCase("sdiworkitemid");
                                if (!readOnly3 && !keyColumn3) {
                                    width = "100";
                                }
                            }
                            html.append("<th class=\"maintform_fieldtitle\" valign=\"top\" style=\"min-width:" + width + ";border-top:none;border-bottom:none;text-align:left;padding-left:0px;word-wrap:break-word;background-color:white;" + rtBorder + "\" >&nbsp;</th>");
                        }
                    }
                    if (!hideDSAttr) {
                        for (g = 0; g < this.datasetSDCColumns.size(); ++g) {
                            String rtBorder;
                            boolean keyColumn4;
                            String colValue;
                            pl = this.datasetSDCColumns.getPropertyList(g);
                            String colId5 = pl.getProperty("columnid");
                            if (colId5.indexOf(" ") > 0) {
                                colId5 = RequestParser.parseAlias(colId5);
                            }
                            String encodedColValue = (colValue = thisWISDIData.getValue(r, colId5, "&nbsp;")).equals("&nbsp;") ? colValue : SafeHTML.encodeForHTMLAttribute(colValue);
                            mode = pl.getProperty("mode");
                            boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                            if (this.sdcid.equalsIgnoreCase("DataSet")) {
                                show = !"hidden".equalsIgnoreCase(mode);
                            }
                            width = pl.getProperty("width", "100");
                            String key2 = thisWISDIData.getValue(r, "__key");
                            boolean readOnly4 = mode.equalsIgnoreCase("readonly");
                            boolean bl = keyColumn4 = colId5.equalsIgnoreCase("paramlistid") || colId5.equalsIgnoreCase("paramlistversionid") || colId5.equalsIgnoreCase("variantid") || colId5.equalsIgnoreCase("dataset") || colId5.equalsIgnoreCase("sdidataid");
                            if (!show) continue;
                            boolean rightBorder = ++dispcolCnt == totalDispCols;
                            String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                            if (this.sdcid.equalsIgnoreCase("DataSet")) {
                                if (readOnly4 || keyColumn4) {
                                    html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;white-space:wrap;" + rtBorder + "\" >");
                                    html.append(encodedColValue);
                                    html.append("</td>");
                                    continue;
                                }
                                String colKey2 = (g < 10 ? "0" : "") + g + "_" + colId5;
                                this.getColumnCell(colKey2, key2, html, colId5, colValue, width, rtBorder);
                                continue;
                            }
                            html.append("<td class=\"maintform_field\" style=\"min-width:" + width + "px;background-color:white;vertical-align:top;padding-top: 5px;;background-color:white;;word-wrap:break-word;" + rtBorder + "\">");
                            html.append("<div  style=\"width:100%\">");
                            html.append(encodedColValue);
                            html.append("</div>");
                            html.append("</td>");
                        }
                    }
                    html.append("</tr>");
                    trEnd = true;
                    dispcolCnt = 0;
                }
                if (w >= sdiwiGrpCount - 1 || hideWIAttr && hideDSAttr) continue;
                html.append("<tr style=\"").append(heightN).append(":" + this.rowHeight + "px;\" class=\"").append(styleClass).append("\">");
                trEnd = false;
                for (int g = 0; g < this.parentSDCColumns.size(); ++g) {
                    String rtBorder;
                    PropertyList pl = this.parentSDCColumns.getPropertyList(g);
                    width = pl.getProperty("width", "100");
                    String mode3 = pl.getProperty("mode");
                    boolean show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                    if (!this.sdcid.equalsIgnoreCase("DataSet") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
                        boolean bl = show = !mode3.equalsIgnoreCase("hidden");
                    }
                    if (!show) continue;
                    boolean rightBorder = ++dispcolCnt == totalDispCols;
                    String string = rtBorder = rightBorder ? this.rightBorderStyle : "";
                    if (!this.sdcid.equalsIgnoreCase("SDIWorkItem") && !this.sdcid.equalsIgnoreCase("DataSet")) {
                        boolean keyColumn5;
                        colId = pl.getProperty("columnid");
                        if (colId.indexOf(" ") > 0) {
                            colId = RequestParser.parseAlias(colId);
                        }
                        boolean readOnly5 = mode3.equalsIgnoreCase("readonly");
                        boolean bl = keyColumn5 = colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid1")) || colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid2")) || colId.equalsIgnoreCase(this.sdcprops.getProperty("keycolid3"));
                        if (!readOnly5 && !keyColumn5) {
                            width = "100";
                        }
                    }
                    html.append("<th class=\"maintform_fieldtitle\" valign=\"top\" style=\"min-width:" + width + ";border-top:none;border-bottom:none;text-align:left;padding-left:0px;word-wrap:break-word;background-color:white;" + rtBorder + "\" >&nbsp;</th>");
                }
            }
            if (trEnd) continue;
            html.append("</tr>");
        }
    }

    public DataSet sortPrimary() {
        String parentKeyCol3;
        boolean primarySDCAttributes = !this.sdcid.equals("DataSet") && !this.sdcid.equals("SDIWorkItem");
        DataSet primary = this.sdiData.getDataset(primarySDCAttributes ? "primary" : PROPERTY_FORCEPRIMARY);
        String parentKeyCol1 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid1") : this.parentsdcprops.getProperty("keycolid1");
        String parentKeyCol2 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid2") : this.parentsdcprops.getProperty("keycolid2");
        String string = parentKeyCol3 = primarySDCAttributes ? this.sdcprops.getProperty("keycolid3") : this.parentsdcprops.getProperty("keycolid3");
        if (this.isSortByKeyOrder && primarySDCAttributes) {
            return primary;
        }
        PropertyListCollection primarySortBy = this.pagedata.getCollectionNotNull("primarysortby");
        StringBuffer sortByStr = new StringBuffer();
        if (primarySortBy.size() > 0) {
            String colAlias = "";
            for (int g = 0; g < primarySortBy.size(); ++g) {
                PropertyList pl = primarySortBy.getPropertyList(g);
                String colId = pl.getProperty("columnid");
                if (colId.length() == 0) continue;
                String direction = pl.getProperty("direction");
                sortByStr.append(",").append(colId).append("D".equalsIgnoreCase(direction) ? " D" : " ");
            }
        }
        if (sortByStr.indexOf("," + parentKeyCol1 + " ") < 0) {
            sortByStr.append(",").append(parentKeyCol1);
        }
        if (parentKeyCol2.length() > 0 && sortByStr.indexOf("," + parentKeyCol2 + " ") < 0) {
            sortByStr.append(",").append(parentKeyCol2);
        }
        if (parentKeyCol3.length() > 0 && sortByStr.indexOf("," + parentKeyCol3 + " ") < 0) {
            sortByStr.append(",").append(parentKeyCol3);
        }
        if (sortByStr.length() > 0) {
            primary.sort(sortByStr.substring(1));
        }
        return primary;
    }

    @Override
    public DataSet sortAdvancedScrollGridRows() {
        DataSet primary;
        DataSet dataSet = primary = this.sortedPrimary.getRowCount() > 0 ? this.sortedPrimary : this.sortPrimary();
        if (primary == null) {
            primary = new DataSet();
        }
        if (!this.sdcid.equalsIgnoreCase("DataSet") && !this.sdcid.equalsIgnoreCase("SDIWorkItem")) {
            return primary;
        }
        DataSet currentDataSet = this.sdiData.getDataset("primary");
        String parentSDC = currentDataSet.getValue(0, PROPERTY_SDCID);
        String sdiwiSortBy = this.getSDIWISortBy();
        String sdidataSortBy = this.getSDIDataSortBy();
        DataSet dsSortedData = new DataSet();
        for (int r = 0; r < primary.getRowCount(); ++r) {
            String k1 = primary.getValue(r, this.getSDCProcessor().getProperty(parentSDC, "keycolid1"), "");
            String k2 = this.getSDCProcessor().getProperty(parentSDC, "keycolid2").length() > 0 ? primary.getValue(r, this.getSDCProcessor().getProperty(parentSDC, "keycolid2"), "") : "";
            String k3 = this.getSDCProcessor().getProperty(parentSDC, "keycolid3").length() > 0 ? primary.getValue(r, this.getSDCProcessor().getProperty(parentSDC, "keycolid3"), "") : "";
            String __key = k1 + (k2.length() > 0 ? ";" + k2 : ";(null)") + (k3.length() > 0 ? ";" + k3 : ";(null)");
            HashMap<String, Object> findDS = new HashMap<String, Object>();
            findDS.put("__fkey", __key);
            if (this.sdcid.equals("DataSet")) {
                DataSet currentSDIWorkitems = this.dsSdiWorkItem.getFilteredDataSet(findDS);
                DataSet thisSDISDIData = currentDataSet.getFilteredDataSet(findDS);
                for (int k = 0; k < thisSDISDIData.getRowCount(); ++k) {
                    String sourcewid = thisSDISDIData.getString(k, "sourceworkitemid");
                    BigDecimal sourcewiinstance = thisSDISDIData.getBigDecimal(k, "sourceworkiteminstance");
                    findDS.clear();
                    findDS.put("workitemid", sourcewid);
                    findDS.put("workiteminstance", sourcewiinstance);
                    DataSet dsSDIWIFiltered = currentSDIWorkitems.getFilteredDataSet(findDS);
                    if (dsSDIWIFiltered.getRowCount() != 0) continue;
                    int rowAdded = currentSDIWorkitems.addRow();
                    currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID1, k1);
                    currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID2, k2);
                    currentSDIWorkitems.setString(rowAdded, PROPERTY_KEYID3, k3);
                    currentSDIWorkitems.setString(rowAdded, PROPERTY_SDCID, this.parentSDCId);
                    currentSDIWorkitems.setString(rowAdded, "workitemid", sourcewid);
                    currentSDIWorkitems.setNumber(rowAdded, "workiteminstance", sourcewiinstance);
                }
                if (currentSDIWorkitems.getRowCount() <= 0) continue;
                currentSDIWorkitems.sort(sdiwiSortBy);
                ArrayList<DataSet> wiGroups = currentSDIWorkitems.getGroupedDataSets(sdiwiSortBy);
                for (int k = 0; k < wiGroups.size(); ++k) {
                    DataSet dsWIs = wiGroups.get(k);
                    findDS.clear();
                    findDS.put("sourceworkitemid", dsWIs.getString(0, "workitemid"));
                    findDS.put("sourceworkiteminstance", dsWIs.getBigDecimal(0, "workiteminstance"));
                    DataSet dsFiltered = thisSDISDIData.getFilteredDataSet(findDS);
                    if (sdidataSortBy.length() > 0) {
                        dsFiltered.sort(sdidataSortBy);
                    } else {
                        dsFiltered.sort("usersequence");
                    }
                    dsSortedData.copyRow(dsFiltered, -1, 1);
                }
                continue;
            }
            if (!this.sdcid.equals("SDIWorkItem")) continue;
            DataSet dssdiwi = currentDataSet.getFilteredDataSet(findDS);
            dssdiwi.sort(sdiwiSortBy);
            dsSortedData.copyRow(dssdiwi, -1, 1);
        }
        return dsSortedData;
    }

    protected void renderAdvancedScrollGridSDIAcrossCornerHead(StringBuffer html, String id) {
        html.append("<th ").append(id.length() > 0 ? "id=\"" + id + "\" " : "").append("class=\"gridmaint_fieldtitle\" style=\"").append(this.scrollGrid ? "width:100px;border-bottom:0px;" : "").append("text-align:left;\">").append(this.getTranslationProcessor().translate(this.getCornerHeader())).append("</th>");
    }

    @Override
    protected void renderAdvancedScrollGridSDIAcrossHeader(StringBuffer html, String id) {
        boolean show;
        String width;
        String title;
        String colId;
        PropertyList pl;
        int g;
        boolean forcePrimary = this.sdcid.equals("DataSet") || this.sdcid.equals("SDIWorkItem");
        DataSet primary = this.sdiData.getDataset(forcePrimary ? PROPERTY_FORCEPRIMARY : "primary");
        StringBuffer htmlColumValues = new StringBuffer();
        html.append("<tr style=\"height:" + this.rowHeight + "px;\">");
        html.append("<td id=\"" + this.elementid + "_firstTd\"").append(" class=\"maintform_fieldtitle\" style=\"").append("min-width:100px;border-bottom:0px;").append("text-align:left;word-wrap:break-word;\">");
        html.append("<table style=\"width:100%\"  cellspacing=\"0\">");
        for (g = 0; g < this.parentSDCColumns.size(); ++g) {
            boolean keyColumn;
            pl = this.parentSDCColumns.getPropertyList(g);
            colId = pl.getProperty("columnid");
            if (colId.indexOf(" ") > 0) {
                colId = RequestParser.parseAlias(colId);
            }
            title = pl.getProperty("title", colId);
            width = pl.getProperty("width", "100");
            show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
            String mode = pl.getProperty("mode");
            boolean bl = keyColumn = colId.equals(this.sdcprops.getProperty("keycolid1")) || colId.equals(this.sdcprops.getProperty("keycolid2")) || colId.equals(this.sdcprops.getProperty("keycolid3"));
            if (this.parentSDCId.length() == 0) {
                if (keyColumn) {
                    mode = "readonly";
                }
                if (!mode.equalsIgnoreCase("readonly")) {
                    show = false;
                }
            }
            if (!show) continue;
            html.append("<tr style=\"height:" + this.rowHeight + "px;\"><th class=\"gridmaint_fieldtitle\" style=\"width:100px;border-bottom:0px;text-align:left;;word-wrap:break-word\">").append(title).append("</th></tr>");
            htmlColumValues.append("<tr").append(" style=\"height:" + this.rowHeight + "px\"").append(">");
            this.renderAdvancedScrollGridSDIAcrossHeaderColumns(htmlColumValues, primary, colId, this.parentSDCId);
            htmlColumValues.append("</tr>");
        }
        if (this.sdcid.equalsIgnoreCase("DataSet")) {
            for (g = 0; g < this.sdiwiSDCColumns.size(); ++g) {
                pl = this.sdiwiSDCColumns.getPropertyList(g);
                colId = pl.getProperty("columnid");
                if (colId.indexOf(" ") > 0) {
                    colId = RequestParser.parseAlias(colId);
                }
                title = pl.getProperty("title", colId);
                width = pl.getProperty("width", "100");
                show = "Y".equalsIgnoreCase(pl.getProperty("show", "Y"));
                if (!show) continue;
                html.append("<tr style=\"height:" + this.rowHeight + "px;\"><th class=\"gridmaint_fieldtitle\" style=\"width:100px;border-bottom:0px;text-align:left;;word-wrap:break-word\">").append(title).append("</th></tr>");
                htmlColumValues.append("<tr").append(" style=\"height:" + this.rowHeight + "px\"").append(">");
                this.renderAdvancedScrollGridSDIAcrossHeaderColumns(htmlColumValues, this.dsSdiWorkItem, colId, "SDIWorkItem");
                htmlColumValues.append("</tr>");
            }
        }
        html.append("</table></td>");
        html.append("<td >");
        html.append("<div id=\"").append(this.elementid).append("_divHeader\" style=\"overflow:hidden;width:284px;\">");
        html.append("<table class=\"maintform_table\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"").append(" style=\"width:100%;\"").append(">");
        html.append("<thead>");
        html.append(htmlColumValues);
        html.append("</thead>");
        html.append("</table>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");
    }

    @Override
    public String getCornerHeader() {
        String sdctitle;
        String c_sdcid = this.sdcid;
        String c_plural = this.sdcid;
        String c_single = this.sdcid;
        if (this.sdcid.equalsIgnoreCase("SDIWorkItem") && this.forceprimary) {
            DataSet ds = this.sdiData.getDataset("primary");
            if (ds.getRowCount() > 0) {
                c_sdcid = ds.getValue(0, PROPERTY_SDCID);
                c_single = this.getTranslationProcessor().translate(this.getSDCProcessor().getProperty(c_sdcid, "singular"));
                c_plural = this.getTranslationProcessor().translate(this.getSDCProcessor().getProperty(c_sdcid, "plural"));
            } else if (this.sdcprops != null) {
                c_single = this.getTranslationProcessor().translate(this.sdcprops.getProperty("singular", ""));
                c_plural = this.getTranslationProcessor().translate(this.sdcprops.getProperty("plural", ""));
            }
        } else if (this.sdcprops != null) {
            c_single = this.getTranslationProcessor().translate(this.sdcprops.getProperty("singular", ""));
            c_plural = this.getTranslationProcessor().translate(this.sdcprops.getProperty("plural", ""));
        }
        if (this.cornerText.length() > 0) {
            sdctitle = StringUtil.replaceAll(StringUtil.replaceAll(StringUtil.replaceAll(this.cornerText, "[sdcid]", this.sdcid, false), "[singular]", c_single, false), "[plural]", c_plural, false);
            return sdctitle;
        }
        sdctitle = c_plural.length() > 0 ? c_plural : (c_single.length() > 0 ? c_single : c_sdcid);
        return sdctitle.length() > 0 ? this.getTranslationProcessor().translate(sdctitle.substring(0, 1).toUpperCase() + sdctitle.substring(1)) : this.sdcid;
    }

    private static DataSet addTempDSRows(DataSet attributeData, String[] inData, String[] keyID1, int size, DataSet old) {
        attributeData.setValue(size, "attributeid", inData[0]);
        attributeData.setValue(size, PROPERTY_SDCID, inData[1]);
        attributeData.setValue(size, "attributesdcid", inData[1]);
        attributeData.setValue(size, PROPERTY_KEYID1, keyID1[0]);
        attributeData.setValue(size, PROPERTY_KEYID2, keyID1[1]);
        attributeData.setValue(size, PROPERTY_KEYID3, keyID1[2]);
        attributeData.setValue(size, "sdiattributeid", keyID1[0]);
        attributeData.setValue(size, "attributeinstance", inData[3]);
        if (inData[4].equals("new")) {
            attributeData.setValue(size, "createby", "AddAttribute");
        } else {
            attributeData.addColumn("oldtextvalue", 0);
            for (int i = 0; i < old.getRowCount(); ++i) {
                String attributeID = old.getValue(i, "attributeid");
                String keyID11 = old.getValue(i, PROPERTY_KEYID1);
                String keyID2 = old.getValue(i, PROPERTY_KEYID2);
                String keyID3 = old.getValue(i, PROPERTY_KEYID3);
                String attributeinstance = old.getValue(i, "attributeinstance");
                String textvalueOld = "";
                textvalueOld = old.getValue(i, "datatype").equals("D") || old.getValue(i, "datatype").equals("O") ? old.getValue(i, "datevalue") : (old.getValue(i, "datatype").equals("N") ? old.getValue(i, "numericvalue") : (old.getValue(i, "datatype").equals("C") ? old.getClob(i, "clobvalue") : old.getValue(i, "textvalue")));
                if (!attributeID.equals(inData[0]) || !keyID11.equals(keyID1[0]) || !keyID2.equals(keyID1[1]) || !keyID3.equals(keyID1[2]) || !attributeinstance.equals(inData[3])) continue;
                attributeData.setValue(size, "oldtextvalue", textvalueOld);
            }
        }
        return attributeData;
    }

    private static DataSet checkAndAddChanges(String changes, DataSet attributeData) {
        for (int i = 0; i < attributeData.getRowCount(); ++i) {
            String[] jayChanges;
            String attributeID = attributeData.getValue(i, "attributeid");
            String keyID1 = attributeData.getValue(i, PROPERTY_KEYID1);
            String keyID2 = attributeData.getValue(i, PROPERTY_KEYID2);
            String keyID3 = attributeData.getValue(i, PROPERTY_KEYID3);
            String attributeinstnce = attributeData.getValue(i, "attributeinstance");
            String keyID = keyID1 + ";" + keyID2 + ";" + keyID3;
            if (changes.length() <= 0 || (jayChanges = changes.split(RECORD_SEPARATOR)) == null) continue;
            for (int j = 0; j < jayChanges.length; ++j) {
                try {
                    String[] attDtls = jayChanges[j].split(VALUE_SEPARATOR);
                    if (!attDtls[3].equals(keyID) || !attDtls[0].equalsIgnoreCase(attributeID) || !attDtls[1].equals(attributeinstnce) || attDtls[4] == null) continue;
                    if (attributeData.getValue(i, "datatype").equals("D") || attributeData.getValue(i, "datatype").equals("O")) {
                        attributeData.setValue(i, "datevalue", attDtls[4]);
                        continue;
                    }
                    if (attributeData.getValue(i, "datatype").equals("N")) {
                        attributeData.setValue(i, "numericvalue", attDtls[4]);
                        continue;
                    }
                    if (attributeData.getValue(i, "datatype").equals("C")) {
                        attributeData.setValue(i, "clobvalue", attDtls[4]);
                        continue;
                    }
                    attributeData.setValue(i, "textvalue", attDtls[4]);
                    continue;
                }
                catch (Exception e) {
                    attributeData.setValue(i, "textvalue", "");
                }
            }
        }
        return attributeData;
    }
}

