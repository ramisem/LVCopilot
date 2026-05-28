/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.wap.activity;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.wap.activity.Activity;
import com.labvantage.sapphire.modules.wap.activity.WAPConstants;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActivityClassHandler
extends BaseCustom
implements WAPConstants {
    String id = "";
    String label = "";
    String sdc;
    String querywhere = "";
    String querycategory = "";
    String activitylabel = "";
    private boolean isAutoComplete;
    private boolean basicsearch;
    private boolean querysearch;
    private boolean isAutoCancel;
    private String testinglab = "";
    private String testinglabtype = "";
    PropertyListCollection columns = new PropertyListCollection();
    PropertyListCollection sortby = new PropertyListCollection();
    PropertyListCollection groupby = new PropertyListCollection();
    PropertyListCollection operations = new PropertyListCollection();
    private String contextsdcid;
    private String contextkeyid1;
    private String contextkeyid2;
    private String contextkeyid3;
    private PropertyList props;

    public String getTestingLab() {
        return this.testinglab;
    }

    public String getTestingLabType() {
        return this.testinglabtype;
    }

    public static List<String> getAllClasses(PropertyList wapPolicy) {
        ArrayList<String> allClasses = new ArrayList<String>();
        PropertyListCollection definedClasses = wapPolicy.getCollectionNotNull("activityclasses");
        for (int i = 0; i < definedClasses.size(); ++i) {
            PropertyList def = definedClasses.getPropertyList(i);
            String activityClass = def.getProperty("id");
            if (activityClass.length() <= 0 || !def.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
            allClasses.add(activityClass);
        }
        return allClasses;
    }

    public PropertyListCollection getOperations() {
        return this.operations;
    }

    public static ActivityClassHandler getInstance(String connectionid, PropertyList wapPolicy, String activityClass) throws SapphireException {
        return ActivityClassHandler.getInstance(connectionid, null, wapPolicy, activityClass, "", "", "");
    }

    public static ActivityClassHandler getInstance(String connectionid, PropertyList wapPolicy, String activityClass, String sdcid) throws SapphireException {
        return ActivityClassHandler.getInstance(connectionid, null, wapPolicy, activityClass, sdcid, "", "");
    }

    public static ActivityClassHandler getInstance(String connectionid, PropertyList wapPolicy, String activityClass, String sdcid, String testinglab, String testinglabtype) throws SapphireException {
        return ActivityClassHandler.getInstance(connectionid, null, wapPolicy, activityClass, sdcid, testinglab, testinglabtype);
    }

    public static ActivityClassHandler getInstance(String connectionid, File rakFile, PropertyList wapPolicy, String activityClass, String sdcid) throws SapphireException {
        return ActivityClassHandler.getInstance(connectionid, rakFile, wapPolicy, activityClass, sdcid, "", "");
    }

    public static ActivityClassHandler getInstance(String connectionid, File rakFile, PropertyList wapPolicy, String activityClass, String sdcid, String testinglab, String testinglabtype) throws SapphireException {
        PropertyListCollection p = wapPolicy.getCollectionNotNull("activityclasses");
        ActivityClassHandler activityClassHandler = null;
        if (p != null && p.size() > 0) {
            PropertyList activityClassProps;
            PropertyList acp = null;
            if (activityClass.length() > 0 && (activityClassProps = p.find("id", activityClass, false)) != null && (sdcid.length() == 0 || activityClassProps.getProperty("sdcid", "").equalsIgnoreCase(sdcid))) {
                if (activityClassProps.getProperty("testingdepartmentid").length() > 0) {
                    if (testinglab.length() == 0 || activityClassProps.getProperty("testingdepartmentid").equalsIgnoreCase(testinglab)) {
                        acp = activityClassProps;
                    }
                } else if (activityClassProps.getProperty("testingdepartmenttype").length() > 0) {
                    if (testinglabtype.length() == 0 || activityClassProps.getProperty("testingdepartmenttype").equalsIgnoreCase(testinglabtype)) {
                        acp = activityClassProps;
                    }
                } else {
                    acp = activityClassProps;
                }
            }
            if (acp == null && sdcid.length() > 0) {
                for (int i = 0; i < p.size(); ++i) {
                    PropertyList current = p.getPropertyList(i);
                    if (!current.getProperty("sdcid").equalsIgnoreCase(sdcid)) continue;
                    if (current.getProperty("testingdepartmentid").length() > 0) {
                        if (testinglab.length() != 0 && !current.getProperty("testingdepartmentid").equalsIgnoreCase(testinglab)) continue;
                        acp = current;
                        break;
                    }
                    if (current.getProperty("testingdepartmenttype").length() > 0) {
                        if (testinglabtype.length() != 0 && !current.getProperty("testingdepartmenttype").equalsIgnoreCase(testinglabtype)) continue;
                        acp = current;
                        break;
                    }
                    acp = current;
                    break;
                }
            }
            if (acp == null && (testinglab.length() > 0 || testinglabtype.length() > 0)) {
                PropertyList found;
                if (testinglab.length() > 0 && (found = p.find("testingdepartmentid", testinglab, false)) != null) {
                    acp = found;
                }
                if (acp == null && testinglabtype.length() > 0 && (found = p.find("testingdepartmenttype", testinglabtype, false)) != null) {
                    acp = found;
                }
            }
            if (acp != null) {
                activityClassHandler = new ActivityClassHandler(connectionid, rakFile, acp);
            }
            if (activityClassHandler == null) {
                throw new SapphireException("Unrecognized ActivityClass (" + activityClass + ") or SDC (" + sdcid + ")");
            }
        }
        return activityClassHandler;
    }

    private ActivityClassHandler(String connectionid, File rakFile, PropertyList activityContext) {
        this.setConnectionId(connectionid);
        this.setRakFile(rakFile);
        this.init(activityContext);
    }

    ActivityClassHandler(String connectionid, String id, String label, String sdcid) {
        this.setConnectionId(connectionid);
        this.id = id;
        this.label = label;
        this.sdc = sdcid;
        this.columns = new PropertyListCollection();
        this.sortby = new PropertyListCollection();
        this.groupby = new PropertyListCollection();
        this.operations = new PropertyListCollection();
    }

    ActivityClassHandler(String connectionid, String id, String label, String sdcid, String testinglabdepartmentid, String testinglabtype) {
        this.setConnectionId(connectionid);
        this.id = id;
        this.label = label;
        this.sdc = sdcid;
        this.columns = new PropertyListCollection();
        this.sortby = new PropertyListCollection();
        this.groupby = new PropertyListCollection();
        this.operations = new PropertyListCollection();
        this.testinglab = testinglabdepartmentid;
        if (testinglabdepartmentid.length() == 0) {
            this.testinglabtype = testinglabtype;
        }
    }

    private void init(PropertyList props) {
        if (props != null) {
            this.props = props;
            this.id = props.getProperty("id");
            this.label = props.getProperty("label");
            this.activitylabel = props.getProperty("activitylabel");
            this.sdc = props.getProperty("sdcid");
            this.querywhere = props.getProperty("querywhere");
            this.querycategory = props.getProperty("querycategory");
            this.activitylabel = props.getProperty("activitylabel");
            PropertyListCollection propertyListCollection = props.getPropertyList("worksdilist") != null ? (props.getPropertyList("worksdilist").getCollection("columns") != null ? props.getPropertyList("worksdilist").getCollection("columns") : new PropertyListCollection()) : (this.columns = new PropertyListCollection());
            PropertyListCollection propertyListCollection2 = props.getPropertyList("worksdilist") != null ? (props.getPropertyList("worksdilist").getCollection("sortby") != null ? props.getPropertyList("worksdilist").getCollection("sortby") : new PropertyListCollection()) : (this.sortby = new PropertyListCollection());
            this.groupby = props.getPropertyList("worksdilist") != null ? (props.getPropertyList("worksdilist").getCollection("groupby") != null ? props.getPropertyList("worksdilist").getCollection("groupby") : new PropertyListCollection()) : new PropertyListCollection();
            this.operations = props.getCollection("limspages") != null ? props.getCollection("limspages") : new PropertyListCollection();
            this.contextsdcid = props.getProperty("contextsdcid");
            this.contextkeyid1 = props.getProperty("contextkeyid1");
            this.contextkeyid2 = props.getProperty("contextkeyid2");
            this.contextkeyid3 = props.getProperty("contextkeyid3");
            this.testinglab = props.getProperty("testingdepartmentid");
            this.testinglabtype = props.getProperty("testingdepartmenttype");
            this.isAutoComplete = props.getProperty("honorautocomplete").equals("Y");
            this.isAutoCancel = props.getProperty("honorautocancel").equals("Y");
            this.basicsearch = props.getPropertyList("worksdilist") != null ? props.getPropertyList("worksdilist").getProperty("basicsearch", "Y").equals("Y") : true;
            this.querysearch = props.getPropertyList("worksdilist") != null ? props.getPropertyList("worksdilist").getProperty("querysearch", "Y").equals("Y") : true;
        }
    }

    public boolean isAutoComplete() {
        return this.isAutoComplete;
    }

    public boolean isAutoCancel() {
        return this.isAutoCancel;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public String getActivityLabel() {
        return this.activitylabel;
    }

    public String getQueryCategory() {
        return this.querycategory;
    }

    public String getQueryWhere() {
        return this.querywhere;
    }

    private String getQueryFrom() {
        return this.getSDCProcessor().getProperty(this.getSDC(), "tableid", "");
    }

    public String getSDC() {
        return this.sdc;
    }

    public DataSet getWork(String selectedsdis, String testingdepartmentid) {
        SDIRequest sdiRequest = new SDIRequest();
        StringBuilder requestitem = new StringBuilder();
        if (this.columns != null && this.columns.size() > 0) {
            for (int c = 0; c < this.columns.size(); ++c) {
                String columnid = this.columns.getPropertyList(c).getProperty("columnid", "");
                if (columnid.length() <= 0) continue;
                if (requestitem.length() > 0) {
                    requestitem.append(",");
                }
                requestitem.append(columnid);
            }
            if (requestitem.length() > 0) {
                sdiRequest.setRequestItem("primary[" + requestitem.toString() + "]");
            } else {
                sdiRequest.setRequestItem("primary");
            }
        } else {
            sdiRequest.setRequestItem("primary");
        }
        sdiRequest.setSDCid(this.getSDC());
        if (selectedsdis != null && selectedsdis.length() > 0) {
            sdiRequest.setKeyid1List(selectedsdis);
        } else {
            sdiRequest.setQueryWhere(this.getWhere(testingdepartmentid, "", true, this.getSDCProcessor()));
        }
        SDIData out = this.getSDIProcessor().getSDIData(sdiRequest);
        if (out != null) {
            return out.getDataset("primary");
        }
        return null;
    }

    public int getCount(String departmentid, String keyid1) {
        StringBuilder sql = new StringBuilder();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String table = sdcProcessor.getProperty(this.getSDC(), "tableid");
        sql.append("SELECT count(*) FROM ").append(this.getQueryFrom().length() > 0 ? this.getQueryFrom() : table);
        sql.append(" WHERE ");
        sql.append(" ").append(this.getWhere(departmentid, keyid1, true, sdcProcessor));
        try {
            return this.getQueryProcessor().getCount(sql.toString());
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return 0;
        }
    }

    public PropertyList getListPageDirectives(String testingdepartmentid, String keyid1, String restrictivewhere, SDCProcessor sdcProcessor, TranslationProcessor tp, PageContext pageContext) {
        return this.getListPageDirectives(testingdepartmentid, keyid1, restrictivewhere, true, true, sdcProcessor, tp, pageContext);
    }

    public PropertyList getListPageDirectives(String testingdepartmentid, String keyid1, String restrictivewhere, boolean hidetoolbar, boolean hideselector, SDCProcessor sdcProcessor, TranslationProcessor tp, PageContext pageContext) {
        PropertyList out = this.getListPageDirectives(restrictivewhere, hidetoolbar, hideselector, sdcProcessor, tp);
        if (testingdepartmentid.equals("(none)")) {
            testingdepartmentid = "";
        }
        out.setProperty("restrictivewhere", this.getWhere(testingdepartmentid, keyid1, true, sdcProcessor));
        return out;
    }

    public PropertyList getListPageDirectives(String filterKeyid1, SDCProcessor sdcProcessor, TranslationProcessor tp, PageContext pageContext) {
        PropertyList out = this.getListPageDirectives("", true, true, sdcProcessor, tp);
        StringBuilder where = new StringBuilder();
        String keycol = sdcProcessor.getProperty(this.getSDC(), "keycolid1");
        String table = sdcProcessor.getProperty(this.getSDC(), "tableid");
        where.append(table).append(".").append(keycol).append(" IN ('").append(StringUtil.replaceAll(filterKeyid1, ";", "','")).append("')");
        out.setProperty("restrictivewhere", where.toString());
        return out;
    }

    public PropertyList getListPageDirectives(String restrictivewhere, boolean hidetoolbar, boolean hideselector, SDCProcessor sdcProcessor, TranslationProcessor tp) {
        String sdcid = this.getSDC();
        PropertyList out = SDITagUtil.getListPageDirectives(sdcid, sdcProcessor.getProperty(sdcid, "keycolid1"), sdcProcessor.getProperty(sdcid, "keycolid2"), sdcProcessor.getProperty(sdcid, "versionedflag").equalsIgnoreCase("Y"), "checkbox", false, "", "", sdcProcessor.getProperty(sdcid, "tableid"), "", new PropertyListCollection(), tp, sdcProcessor);
        if (hidetoolbar) {
            out.setProperty("hidetoolbar", "Y");
        } else {
            PropertyListCollection buttons = new PropertyListCollection();
            PropertyList button = new PropertyList();
            button.setProperty("id", "selectandreturn");
            PropertyList commonprops = new PropertyList();
            commonprops.setProperty("text", "Select");
            commonprops.setProperty("tip", "Select and Close");
            commonprops.setProperty("image", "WEB-CORE/images/gif/SelectAndReturn.gif");
            button.setProperty("commonprops", commonprops);
            PropertyList standardbuttonprops = new PropertyList();
            standardbuttonprops.setProperty("action", "Accept");
            button.setProperty("standardbuttonprops", standardbuttonprops);
            buttons.add(button);
            button = new PropertyList();
            button.setProperty("id", "selectandreturn");
            commonprops = new PropertyList();
            commonprops.setProperty("text", "Cancel");
            commonprops.setProperty("tip", "Cancel and Close");
            commonprops.setProperty("image", "WEB-CORE/images/gif/Cancel.gif");
            button.setProperty("commonprops", commonprops);
            standardbuttonprops = new PropertyList();
            standardbuttonprops.setProperty("action", "Cancel");
            button.setProperty("standardbuttonprops", standardbuttonprops);
            buttons.add(button);
            out.setProperty("buttons", buttons);
        }
        if (hideselector) {
            out.setProperty("selectiontype", "Hidden");
        } else {
            out.setProperty("selectiontype", "Checkbox");
        }
        String attributeid = "AssignmentPage_" + sdcid + "";
        out.setProperty("listid", attributeid);
        if (this.querywhere.length() > 0) {
            restrictivewhere = restrictivewhere != null && restrictivewhere.length() > 0 ? restrictivewhere + "," + this.querywhere : this.querywhere;
        }
        if (restrictivewhere != null && restrictivewhere.length() > 0) {
            out.setProperty("restrictivewhere", restrictivewhere);
        }
        out.setProperty("sortby", this.sortby);
        out.setProperty("groupby", this.groupby);
        out.setProperty("columns", this.columns);
        PropertyList as = new PropertyList();
        as.setProperty("show", this.querysearch || this.basicsearch ? "Y" : "N");
        as.setProperty("showcollapseexpand", "N");
        as.setProperty("showinitially", "N");
        as.setProperty("showtopsearch", this.querysearch || this.basicsearch ? "Y" : "N");
        as.setProperty("forcetopsearchonly", this.querysearch || this.basicsearch ? "Y" : "N");
        if (this.querycategory.length() > 0) {
            PropertyList querysearchpl = new PropertyList();
            querysearchpl.setProperty("category", this.querycategory);
            as.setProperty("querysearch", querysearchpl);
        }
        PropertyListCollection sequence = new PropertyListCollection();
        as.setProperty("sequence", sequence);
        PropertyList querysearch = new PropertyList();
        querysearch.setProperty("contentname", "Queries");
        querysearch.setProperty("show", this.querysearch ? "Y" : "N");
        sequence.add(querysearch);
        PropertyList basicsearch = new PropertyList();
        basicsearch.setProperty("contentname", "Basic Search");
        basicsearch.setProperty("show", this.basicsearch ? "Y" : "N");
        sequence.add(basicsearch);
        out.setProperty("advancedsearch", as);
        return out;
    }

    public String getWhere(String testingdepartmentid, String keyid1, boolean mergeQueryWhere, SDCProcessor sdcProcessor) {
        StringBuilder sql = new StringBuilder();
        String sdcid = this.getSDC();
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        boolean isOracle = this.getConnectionProcessor().isOra();
        if (this.getSDC().equals("LV_Activity")) {
            sql.append("activitystatus = '").append("Draft").append("' ");
            if (testingdepartmentid.length() > 0) {
                sql.append(" AND testingdepartmentid='" + SafeSQL.encodeForSQL(testingdepartmentid, isOracle) + "' ");
            }
            sql.append(" AND activityid IN (select activityresource.activityid from activityresource where activityresource.instrumentid is null AND activityresource.analystid is null AND activityresource.workareadepartmentid is null )");
            if (keyid1 != null && keyid1.length() > 0) {
                sql.append(" AND ").append("activityid").append(" IN ('").append(SafeSQL.convertToSQLInClause(keyid1, ";", isOracle)).append("')");
            }
            if (mergeQueryWhere && this.querywhere.length() > 0) {
                sql.append(" AND (").append(this.querywhere).append(")");
            }
        } else {
            sql.append(tableid).append(".wapstatus = '").append("Pending").append("'");
            sql.append(" AND ").append(tableid).append(".testingdepartmentid ='").append(SafeSQL.encodeForSQL(testingdepartmentid, isOracle)).append("'");
            if (tableid.equalsIgnoreCase("sdiworkitem") || tableid.equalsIgnoreCase("sdidata")) {
                sql.append(" AND ").append(tableid).append(".sdcid ='Sample'");
            }
            if (keyid1.length() > 0) {
                sql.append(" AND ").append(tableid).append(".").append(sdcProcessor.getProperty(this.getSDC(), "keycolid1")).append(" IN ('").append(SafeSQL.convertToSQLInClause(keyid1, ";", isOracle)).append("')");
            }
            if (mergeQueryWhere && this.querywhere.length() > 0) {
                sql.append(" AND (").append(this.querywhere).append(")");
            }
        }
        return sql.toString();
    }

    public String[] getContextSDI(String workkeyid1) {
        if (this.contextsdcid == null || this.contextkeyid1 == null) {
            return null;
        }
        String[] contextsdi = new String[]{this.contextsdcid, this.contextkeyid1, this.contextkeyid2, this.contextkeyid3};
        this.replaceTokensFromWorkSDI(contextsdi, workkeyid1);
        return contextsdi;
    }

    public String replaceDateTokens(String inputText) {
        inputText = StringUtil.replaceAll(inputText, "[sdcid]", this.getSDC(), false);
        inputText = StringUtil.replaceAll(inputText, "[shortdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate]"), false);
        inputText = StringUtil.replaceAll(inputText, "[mediumdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate]"), false);
        inputText = StringUtil.replaceAll(inputText, "[longdate]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[longdate]"), false);
        inputText = StringUtil.replaceAll(inputText, "[datetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[mediumdate] [shorttime]"), false);
        inputText = StringUtil.replaceAll(inputText, "[shortdatetime]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate] [shorttime]"), false);
        inputText = StringUtil.replaceAll(inputText, "[time]", DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shorttime]"), false);
        return inputText;
    }

    public String replaceTokensFromWorkSDI(String inputText, String workkeyid1) {
        return this.replaceTokensFromWorkSDI(new String[]{inputText}, workkeyid1)[0];
    }

    private String doTokenReplace(DataSet sdis, String[] tok, String in) {
        String out = in;
        if (sdis != null && sdis.getRowCount() > 0) {
            for (int t = 0; t < tok.length; ++t) {
                if (!sdis.isValidColumn(tok[t])) continue;
                out = StringUtil.replaceAll(out, "[" + tok[t] + "]", sdis.getValue(0, tok[t], ""), false);
            }
        }
        return out;
    }

    public String[] replaceTokensFromWorkSDI(String[] inList, String workkeyid1) {
        DataSet sdis = null;
        for (int i = 0; i < inList.length; ++i) {
            String[] tok = StringUtil.getExpressionTokens(inList[i]);
            if (tok.length <= 0 || workkeyid1.length() <= 0) continue;
            if (sdis == null) {
                sdis = this.getWork(workkeyid1, "");
            }
            inList[i] = this.doTokenReplace(sdis, tok, inList[i]);
        }
        return inList;
    }

    public String replaceTokensFromWorkSDI(String inputText, DataSet sdis) {
        return this.replaceTokensFromWorkSDI(new String[]{inputText}, sdis)[0];
    }

    public String[] replaceTokensFromWorkSDI(String[] inList, DataSet sdis) {
        for (int i = 0; i < inList.length; ++i) {
            String[] tok = StringUtil.getExpressionTokens(inList[i]);
            if (tok.length <= 0) continue;
            this.doTokenReplace(sdis, tok, inList[i]);
        }
        return inList;
    }

    public String replaceTokensFromActivityWorkSDI(String inputText, DataSet activitywork, boolean worksdiPrefix) {
        String outstring = inputText;
        String[] tok = StringUtil.getExpressionTokens(outstring);
        if (tok.length > 0 && activitywork != null && activitywork.getRowCount() > 0) {
            for (int t = 0; t < tok.length; ++t) {
                String column;
                String string = worksdiPrefix ? (tok[t].toLowerCase().startsWith("worksdi") ? tok[t].substring(8) : "") : (column = tok[t]);
                if (column.length() <= 0) continue;
                String sep = ";";
                int in = column.indexOf("(");
                if (column.endsWith(")") && in > 0 && in < column.length() - 1) {
                    sep = column.substring(in + 1, column.length() - 1);
                    column = column.substring(0, in);
                }
                if (activitywork.isValidColumn(column)) {
                    outstring = StringUtil.replaceAll(outstring, "[" + tok[t] + "]", activitywork.getColumnValues(column, sep), false);
                    continue;
                }
                if (!column.equalsIgnoreCase("worksdcidsingle")) continue;
                outstring = StringUtil.replaceAll(outstring, "[" + tok[t] + "]", activitywork.getString(t, "worksdcid"), false);
            }
        }
        return outstring;
    }

    public String replaceTokensFromActivity(String inputText, Activity activity) {
        return this.replaceTokensFromActivity(inputText, activity, false);
    }

    public String replaceTokensFromActivity(String inputText, Activity activity, boolean activityPrefix) {
        inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "activityid]", activity.getActivityid(), false);
        if (activity.getWorksdcid().length() > 0) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "worksdcid]", activity.getWorksdcid(), false);
        }
        if (activity.getTestingDepartmentid().length() > 0) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "testingdepartmentid]", activity.getTestingDepartmentid(), false);
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "departmentid]", activity.getTestingDepartmentid(), false);
        }
        if (activity.getActivityContextSdcid().length() > 0) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "activitycontextsdcid]", activity.getActivityContextSdcid(), false);
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "contextsdcid]", activity.getActivityContextSdcid(), false);
        }
        if (activity.getActivityContextKeyid1().length() > 0 && !activity.getActivityContextKeyid1().contains("[")) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "activitycontextkeyid1]", activity.getActivityContextKeyid1(), false);
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "contextkeyid1]", activity.getActivityContextKeyid1(), false);
        }
        if (activity.getActivityContextKeyid2().length() > 0 && !activity.getActivityContextKeyid2().contains("[")) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "activitycontextkeyid2]", activity.getActivityContextKeyid2(), false);
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "contextkeyid2]", activity.getActivityContextKeyid2(), false);
        }
        if (activity.getActivityContextKeyid3().length() > 0 && !activity.getActivityContextKeyid3().contains("[")) {
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "activitycontextkeyid3]", activity.getActivityContextKeyid3(), false);
            inputText = StringUtil.replaceAll(inputText, "[" + (activityPrefix ? "activity." : "") + "contextkeyid3]", activity.getActivityContextKeyid3(), false);
        }
        return inputText;
    }

    public void addResourceRequirements(DataSet resourceRequirements) {
        PropertyListCollection resourceCollection = this.props.getCollectionNotNull("resourcerequirements");
        if (resourceCollection.size() == 0) {
            int row = resourceRequirements.addRow();
            resourceRequirements.setNumber(row, "resourcenum", 0);
            resourceRequirements.setString(row, "resourcetypeflag", "A");
            resourceRequirements.setString(row, "durationrule", "1 hour per 1");
        } else {
            for (int i = 0; i < resourceCollection.size(); ++i) {
                int row;
                PropertyList resource = resourceCollection.getPropertyList(i);
                String resourcetype = resource.getProperty("resourcetypeflag");
                String instrumenttypeid = resource.getProperty("instrumenttypeid");
                String durationrule = resource.getProperty("durationrule", "1 hour per 1");
                if (resourcetype.equals("A")) {
                    if (resourceRequirements.findRow("resourcetypeflag", "A") != -1) continue;
                    row = resourceRequirements.addRow();
                    resourceRequirements.setNumber(row, "resourcenum", row);
                    resourceRequirements.setString(row, "resourcetypeflag", resourcetype);
                    resourceRequirements.setString(row, "analysttype", "Analyst");
                    resourceRequirements.setString(row, "durationrule", durationrule);
                    continue;
                }
                if (!resourcetype.equals("I") || instrumenttypeid.length() <= 0 || resourceRequirements.findRow("instrumenttypeid", instrumenttypeid) != -1) continue;
                row = resourceRequirements.addRow();
                resourceRequirements.setNumber(row, "resourcenum", row);
                resourceRequirements.setString(row, "resourcetypeflag", resourcetype);
                resourceRequirements.setString(row, "instrumenttypeid", instrumenttypeid);
                resourceRequirements.setString(row, "durationrule", durationrule);
            }
        }
    }

    public int getMaxActivitySize() {
        return Integer.parseInt(this.props.getProperty("maxactivitysize", "1000"));
    }
}

