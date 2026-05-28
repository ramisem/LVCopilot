/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedsearchElementRenderer
extends BaseElementRenderer {
    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "Advanced Search");
        content.startSubSection("Advanced Search", "The following table displays the search types enabled and the Roles they are available to.");
        DataSet curr = this.getSearchOptionsSummary(elementProps);
        if (includeDiffReport && refElementProps != null) {
            DataSet ref = this.getSearchOptionsSummary(refElementProps);
            curr = this.getDiffSearchInfo(curr, ref);
        }
        content.renderListTable(curr, this.translationProcessor);
        content.endSubSection("", elementId);
        return content;
    }

    private DataSet getDiffSearchInfo(DataSet curr, DataSet ref) {
        int i;
        DataSet diff = new DataSet();
        String[] currcols = curr.getColumns();
        String[] refcols = ref.getColumns();
        ArrayList<String> allCols = new ArrayList<String>();
        for (i = 0; i < currcols.length; ++i) {
            diff.addColumn(currcols[i], 0);
            allCols.add(currcols[i]);
        }
        for (i = 0; i < refcols.length; ++i) {
            if (diff.isValidColumn(refcols[i])) continue;
            diff.addColumn(refcols[i], 0);
            allCols.add(refcols[i]);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("Search Type", "Basic Search");
        DataSet basic = curr.getFilteredDataSet(filter);
        DataSet refBasic = ref.getFilteredDataSet(filter);
        String basicSearchStr = "Basic Search";
        if (basic.getRowCount() > 0) {
            if (refBasic.getRowCount() == 0) {
                basicSearchStr = ConfigReportContent.getNewString(basicSearchStr);
            }
        } else if (refBasic.getRowCount() > 0) {
            basicSearchStr = ConfigReportContent.getDeletedString(basicSearchStr);
        }
        int currDiffItem = diff.addRow();
        diff.setString(currDiffItem, "Search Type", basicSearchStr);
        if (basic.getRowCount() > 0) {
            for (int i2 = 0; i2 < currcols.length; ++i2) {
                basic.getString(0, currcols[i2]);
            }
        }
        return diff;
    }

    public DataSet getSearchOptionsSummary(PropertyList elementProps) {
        PropertyListCollection coll = elementProps.getCollection("sequence");
        Object refColl = null;
        DataSet matrix = new DataSet();
        matrix.setColidCaseSensitive(true);
        matrix.addColumn("Search Type", 0);
        if (coll == null || coll.size() == 0) {
            PropertyList basicSearchOptions = elementProps.getPropertyListNotNull("basicsearch");
            PropertyList querySearchOptions = elementProps.getPropertyListNotNull("querysearch");
            PropertyList categorySearchOptions = elementProps.getPropertyListNotNull("categorysearch");
            PropertyList folderSearchOptions = elementProps.getPropertyListNotNull("foldersearch");
            PropertyList scanSearchOptions = elementProps.getPropertyListNotNull("scansearch");
            PropertyList adhocSearchOptions = elementProps.getPropertyListNotNull("adhocsearch");
            String showBasic = basicSearchOptions.getProperty("show", "");
            String showQuery = querySearchOptions.getProperty("show", "");
            String showCategory = categorySearchOptions.getProperty("show", "");
            String showFolder = folderSearchOptions.getProperty("show", "");
            String showScan = scanSearchOptions.getProperty("show", "");
            String showAdhoc = adhocSearchOptions.getProperty("show", "");
            int item = 0;
            if (!"N".equals(showBasic)) {
                matrix = this.addSearchInfo(matrix, basicSearchOptions, "Basic Search", item++);
            }
            if (!"N".equals(showCategory)) {
                matrix = this.addSearchInfo(matrix, categorySearchOptions, "Category Search", item++);
            }
            if (!"N".equals(showFolder)) {
                matrix = this.addSearchInfo(matrix, folderSearchOptions, "Folder Search", item++);
            }
            if (!"N".equals(showScan)) {
                matrix = this.addSearchInfo(matrix, scanSearchOptions, "Scan List", item++);
            }
            if (!"N".equals(showAdhoc)) {
                matrix = this.addSearchInfo(matrix, adhocSearchOptions, "Adhoc Search", item++);
            }
            if (!"N".equals(showQuery)) {
                matrix = this.addSearchInfo(matrix, querySearchOptions, "Query Search: " + this.getQuerySearchInfo(elementProps), item++);
            }
            return matrix;
        }
        int item = 0;
        for (int i = 0; i < coll.size(); ++i) {
            PropertyList pl = coll.getPropertyList(i);
            String id = pl.getProperty("id");
            String show = pl.getProperty("show", "");
            String contentName = pl.getProperty("contentname", id);
            if ("N".equals(show)) continue;
            matrix = "basic".equals(id) ? this.addSearchInfo(matrix, pl, contentName, item++) : ("categories".equals(id) ? this.addSearchInfo(matrix, pl, contentName, item++) : ("folders".equals(id) ? this.addSearchInfo(matrix, pl, contentName, item++) : ("adhoc".equals(id) ? this.addSearchInfo(matrix, pl, contentName, item++) : ("scanlist".equals(id) ? this.addSearchInfo(matrix, pl, contentName, item++) : ("queries".equals(id) ? this.addSearchInfo(matrix, pl, contentName + ":" + this.getQuerySearchInfo(elementProps), item++) : this.addSearchInfo(matrix, pl, contentName, item++))))));
        }
        return matrix;
    }

    private DataSet addSearchInfo(DataSet matrix, PropertyList pl, String searchType, int item) {
        matrix.addRow();
        matrix.setString(item, "Search Type", searchType);
        String roleList = pl.getAttribute("rolelist");
        if (roleList.length() > 0) {
            String[] roles = StringUtil.split(roleList, ";");
            for (int roleitem = 0; roleitem < roles.length; ++roleitem) {
                if (!matrix.isValidColumn(roles[roleitem])) {
                    matrix.addColumn(roles[roleitem], 0);
                }
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roles[roleitem] + "\" title=\"" + roles[roleitem] + "\">";
                matrix.setString(item, roles[roleitem], includeImg);
            }
        } else {
            String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"All\" title=\"All\">";
            matrix.setString(item, "All", includeImg);
        }
        return matrix;
    }

    private String getQuerySearchInfo(PropertyList elementProps) {
        String sdcid = elementProps.getProperty("sdcid", "");
        PropertyList querySearchOptions = elementProps.getPropertyListNotNull("querysearch");
        PropertyListCollection searchTypes = elementProps.getCollection("sequence");
        String show = "";
        if (searchTypes == null || searchTypes.size() == 0) {
            show = querySearchOptions.getProperty("show", "");
        } else {
            for (int i = 0; i < searchTypes.size(); ++i) {
                PropertyList pl = searchTypes.getPropertyList(i);
                if (!"queries".equals(pl.getProperty("id"))) continue;
                show = pl.getProperty("show", "");
                break;
            }
        }
        String val = "";
        if (!"N".equals(show)) {
            String categorylist = querySearchOptions.getProperty("category");
            String filterlist = querySearchOptions.getProperty("filter");
            String from = "query";
            SafeSQL safeSQL = new SafeSQL();
            String where = "query.basedonid=" + safeSQL.addVar(sdcid);
            if (categorylist != null && categorylist.trim().length() > 0) {
                from = from + ",categoryitem";
                where = where + " and query.queryid=categoryitem.keyid1 and categoryitem.sdcid='Query' and categoryid in (" + safeSQL.addIn(categorylist, ";") + ") ";
            }
            if (filterlist != null && filterlist.trim().length() > 0) {
                where = where + " and query.queryid in (" + safeSQL.addIn(filterlist, ";") + ") ";
            }
            DataSet ds = this.queryProcessor.getPreparedSqlDataSet("SELECT queryid, basedonid FROM " + from + " WHERE " + where, safeSQL.getValues());
            StringBuffer list = new StringBuffer();
            list.append(val);
            if (ds.size() > 0) {
                String queryList = "";
                for (int i = 0; i < ds.size(); ++i) {
                    if (i != 0) {
                        queryList = queryList + ",";
                    }
                    String queryId = ds.getString(i, "queryid");
                    String fklink = "<P>" + ConfigReportContent.createHyperLink("Query", queryId, ds.getString(i, "basedonid"), "", this.sdisIncluded, this.frames);
                    queryList = queryList + fklink;
                }
                list.append("<P>The following list of queries are displayed:");
                list.append(queryList);
            } else {
                list.append("<P>No queries are found.");
            }
            return list.toString();
        }
        val = "Not Displayed";
        return val;
    }
}

