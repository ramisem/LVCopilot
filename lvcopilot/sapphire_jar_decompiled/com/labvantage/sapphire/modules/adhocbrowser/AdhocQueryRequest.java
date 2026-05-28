/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocArgument;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteria;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArg;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocCriteriaArgGroup;
import com.labvantage.sapphire.modules.adhocbrowser.OrderByArg;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.DataSet;

public class AdhocQueryRequest {
    private String sdcid;
    private int maxResults = 100;
    private String betweenGroupBoolean = "or";
    private AdhocCriteria adhocCriteria = new AdhocCriteria();
    private ArrayList orderby = new ArrayList();
    private ArrayList view = new ArrayList();
    private boolean isRequestCount = false;
    private String restrictiveWhere;
    private int queryTimeout = -1;
    private String searchWithinKeyid1;
    private String searchWithinKeyid2;
    private String searchWithinKeyid3;
    private String searchWithinRset;

    public void setArgumentFromXML(String xml) {
        this.setArgumentFromDataSet(new DataSet(xml));
    }

    public void setArgumentFromDataSet(DataSet searchrequestDs) {
        for (int i = 0; i < searchrequestDs.size(); ++i) {
            String viewcolid;
            if ("V".equals(searchrequestDs.getString(i, "group"))) {
                viewcolid = searchrequestDs.getString(i, "columnid");
                if (viewcolid == null || viewcolid.length() <= 0) continue;
                AdhocArgument viewArg = new AdhocArgument();
                viewArg.setColumnid(viewcolid);
                this.view.add(viewArg);
                continue;
            }
            if (!"S".equals(searchrequestDs.getString(i, "group"))) continue;
            viewcolid = searchrequestDs.getString(i, "columnid");
            String operator = searchrequestDs.getString(i, "operator");
            if (viewcolid == null || viewcolid.length() <= 0) continue;
            OrderByArg orderByArg = new OrderByArg();
            orderByArg.setColumnid(viewcolid);
            orderByArg.setDirection(operator);
            this.orderby.add(orderByArg);
        }
        for (int cg = 0; cg < 20; ++cg) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("group", "" + cg);
            DataSet group = searchrequestDs.getFilteredDataSet(filter);
            if (group == null || group.size() <= 0) continue;
            AdhocCriteriaArgGroup argGroup = new AdhocCriteriaArgGroup();
            for (int j = 0; j < group.getRowCount(); ++j) {
                String columnid;
                if (j == 0) {
                    String argrelationflag = group.getString(0, "argrelationflag", "a");
                    String groupName = group.getString(0, "groupname", "");
                    argGroup.setCriteriaRelation(argrelationflag);
                    argGroup.setGroupName(groupName);
                }
                if ((columnid = group.getString(j, "columnid", "")).length() <= 0) continue;
                AdhocCriteriaArg criteriaArg = new AdhocCriteriaArg();
                criteriaArg.setColumnid(columnid);
                criteriaArg.setOperator(group.getString(j, "operator", ""));
                criteriaArg.setValueObject(group.getString(j, "value", ""));
                criteriaArg.setColumntype(group.getString(j, "columntype"));
                argGroup.addCriteriaArg(criteriaArg);
            }
            this.adhocCriteria.addCriteriaArgCroup(argGroup);
        }
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getBetweenGroupBoolean() {
        return this.betweenGroupBoolean;
    }

    public void setBetweenGroupBoolean(String betweenGroupBoolean) {
        this.betweenGroupBoolean = betweenGroupBoolean;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public AdhocCriteria getCriteria() {
        return this.adhocCriteria;
    }

    public void setCriteria(AdhocCriteria adhocCriteria) {
        this.adhocCriteria = adhocCriteria;
    }

    public ArrayList getOrderby() {
        return this.orderby;
    }

    public void addOrderbyArg(OrderByArg orderbyarg) {
        this.orderby.add(orderbyarg);
    }

    public void addOrderbyArg(String columnid) {
        OrderByArg orderbyArg = new OrderByArg();
        orderbyArg.setColumnid(columnid);
        orderbyArg.setDirection("asc");
        this.orderby.add(orderbyArg);
    }

    public ArrayList getView() {
        return this.view;
    }

    public void addViewArg(AdhocArgument viewarg) {
        this.view.add(viewarg);
    }

    public void addViewArg(String columnid) {
        AdhocArgument viewArg = new AdhocArgument();
        viewArg.setColumnid(columnid);
        this.view.add(viewArg);
    }

    public boolean containViewArg(String columnid) {
        for (int i = 0; i < this.view.size(); ++i) {
            if (!columnid.equals(((AdhocArgument)this.view.get(i)).getColumnid())) continue;
            return true;
        }
        return false;
    }

    public boolean isRequestCount() {
        return this.isRequestCount;
    }

    public void setRequestCount(boolean requestCount) {
        this.isRequestCount = requestCount;
    }

    public String getRestrictiveWhere() {
        return this.restrictiveWhere;
    }

    public void setRestrictiveWhere(String restrictiveWhere) {
        this.restrictiveWhere = restrictiveWhere;
    }

    public int getQueryTimeout() {
        return this.queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public String getSearchWithinKeyid1() {
        return this.searchWithinKeyid1;
    }

    public void setSearchWithinKeyid1(String searchWithinKeyid1) {
        this.searchWithinKeyid1 = searchWithinKeyid1;
    }

    public String getSearchWithinKeyid2() {
        return this.searchWithinKeyid2;
    }

    public void setSearchWithinKeyid2(String searchWithinKeyid2) {
        this.searchWithinKeyid2 = searchWithinKeyid2;
    }

    public String getSearchWithinKeyid3() {
        return this.searchWithinKeyid3;
    }

    public void setSearchWithinKeyid3(String searchWithinKeyid3) {
        this.searchWithinKeyid3 = searchWithinKeyid3;
    }

    public String getSearchWithinRset() {
        return this.searchWithinRset;
    }

    public void setSearchWithinRset(String searchWithinRset) {
        this.searchWithinRset = searchWithinRset;
    }
}

