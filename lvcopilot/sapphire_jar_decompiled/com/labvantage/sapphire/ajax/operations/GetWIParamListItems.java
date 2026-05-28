/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.simplespec.util.SimpleSpecHelper;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetWIParamListItems
extends BaseAjaxRequest {
    private final Map<List<String>, BigDecimal> workItemSequenceMap = new HashMap<List<String>, BigDecimal>();

    @Override
    public void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(httpServletRequest, httpServletResponse);
        try {
            this.doProcessRequest(ar);
        }
        catch (SapphireException e) {
            ar.setError("Error while running GetWIParamListItems", e);
        }
        ar.print();
    }

    private void doProcessRequest(AjaxResponse ar) throws SapphireException {
        String workItemIds = ar.getRequestParameter("workitemid");
        String workItemVersionIds = ar.getRequestParameter("workitemversionid");
        String filterParamType = ar.getRequestParameter("filterparamtype", "");
        String elementId = ar.getRequestParameter("elementid");
        String webPageId = ar.getRequestParameter("pageid");
        if (workItemIds == null || workItemIds.isEmpty()) {
            throw new IllegalArgumentException("Work Item ID list is null or empty: " + workItemIds);
        }
        if (workItemVersionIds == null || workItemVersionIds.isEmpty()) {
            throw new IllegalArgumentException("Work Item Version ID list is null or empty: " + workItemVersionIds);
        }
        RequestContext requestContext = this.getRequestContext();
        RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionId());
        if (webPageId.length() == 0) {
            throw new SapphireException("No Webpage id found.");
        }
        PropertyList requestData = requestProcessor.getWebPageProperties(webPageId, requestContext);
        PropertyList element = requestData.getPropertyList(elementId);
        if (element == null || element.size() == 0) {
            throw new IllegalArgumentException("Element not found!");
        }
        SimpleSpecHelper simpleSpecHelper = new SimpleSpecHelper(this.getConnectionId());
        workItemIds = HttpUtil.decodeURIComponent(workItemIds);
        workItemVersionIds = HttpUtil.decodeURIComponent(workItemVersionIds);
        ArrayList<String> workItemIdList = new ArrayList<String>(Arrays.asList(StringUtil.split(workItemIds, ";")));
        ArrayList<String> workItemVersionIdList = new ArrayList<String>(Arrays.asList(StringUtil.split(workItemVersionIds, ";")));
        List<String> actualWorkItemVersionIdList = simpleSpecHelper.getActualWorkItemVersionIdList(workItemIdList, workItemVersionIdList);
        StringBuilder getWorkItemsWhereFragment = new StringBuilder();
        ArrayList getWorkItemsParams = new ArrayList();
        for (int i = 0; i < workItemIdList.size(); ++i) {
            getWorkItemsWhereFragment.append(" OR (workitemid = ? AND workitemversionid = ?)");
            getWorkItemsParams.add(workItemIdList.get(i));
            getWorkItemsParams.add(actualWorkItemVersionIdList.get(i));
            List<String> workItemKey = Arrays.asList((String)workItemIdList.get(i), actualWorkItemVersionIdList.get(i));
            this.workItemSequenceMap.put(workItemKey, new BigDecimal((i + 1) * 1000));
        }
        HashMap<List<String>, String> workItemGroupMap = new HashMap<List<String>, String>();
        HashSet<String> workItemGroupSet = new HashSet<String>();
        String getWorkItemsSql = "SELECT workitemid groupid, workitemversionid groupversionid, keyid1 workitemid, keyid2 workitemversionid, coalesce(usersequence, 1) usersequence FROM workitemitem WHERE (" + getWorkItemsWhereFragment.substring(4) + ") AND sdcid = 'WorkItem' ORDER BY usersequence";
        DataSet getWorkItemsDs = this.getQueryProcessor().getPreparedSqlDataSet(getWorkItemsSql, getWorkItemsParams.toArray());
        for (int i = 0; i < getWorkItemsDs.getRowCount(); ++i) {
            String groupId = getWorkItemsDs.getString(i, "groupid", "");
            String workItemId = getWorkItemsDs.getString(i, "workitemid");
            String workItemVersionId = getWorkItemsDs.getString(i, "workitemversionid");
            List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
            workItemIdList.add(workItemId);
            workItemVersionIdList.add(workItemVersionId);
            workItemGroupMap.put(workItemKey, groupId);
            workItemGroupSet.add(groupId);
            BigDecimal userSequence = getWorkItemsDs.getBigDecimal(i, "usersequence");
            this.workItemSequenceMap.put(workItemKey, userSequence.add(this.workItemSequenceMap.get(Arrays.asList(groupId, "1"))));
        }
        actualWorkItemVersionIdList = simpleSpecHelper.getActualWorkItemVersionIdList(workItemIdList, workItemVersionIdList);
        List<String> currentWorkItemIdList = simpleSpecHelper.getCurrentWorkItemIdList(workItemIdList, workItemVersionIdList);
        PropertyListCollection configuredWorkItemColumnCollection = element.getCollectionNotNull("workitemcolumncollection");
        DataSet getWorkItemItemsDs = simpleSpecHelper.getWorkItemItems(workItemIdList, actualWorkItemVersionIdList, configuredWorkItemColumnCollection);
        ArrayList<String> paramListIdList = new ArrayList<String>();
        ArrayList<String> paramListVersionIdList = new ArrayList<String>();
        ArrayList<String> variantIdList = new ArrayList<String>();
        for (int i = 0; i < getWorkItemItemsDs.getRowCount(); ++i) {
            paramListIdList.add(getWorkItemItemsDs.getString(i, "paramlistid", ""));
            paramListVersionIdList.add(getWorkItemItemsDs.getString(i, "paramlistversionid", ""));
            variantIdList.add(getWorkItemItemsDs.getString(i, "variantid", ""));
        }
        List<String> actualParamListVersionIdList = simpleSpecHelper.getActualParamListVersionIdList(paramListIdList, paramListVersionIdList, variantIdList);
        List<List<String>> currentParamListKeyList = simpleSpecHelper.getCurrentParamListKeyList(paramListIdList, variantIdList, paramListVersionIdList);
        DataSet getParamItemsDs = simpleSpecHelper.getParamItems(paramListIdList, actualParamListVersionIdList, variantIdList, element.getCollectionNotNull("paramlistitemcolumncollection"), filterParamType);
        DataSet paramItemsDs = simpleSpecHelper.mergeWorkItemsWithParamItems(getWorkItemItemsDs, getParamItemsDs);
        HashMap<List<String>, Set<List<String>>> workItemParamMap = new HashMap<List<String>, Set<List<String>>>();
        PropertyListCollection rowCollection = new PropertyListCollection();
        for (int i = 0; i < paramItemsDs.getRowCount(); ++i) {
            String workItemId = paramItemsDs.getString(i, "workitemid");
            String currentWorkItemVersionId = paramItemsDs.getString(i, "workitemversionid");
            if (currentWorkItemIdList.contains(workItemId)) {
                paramItemsDs.setString(i, "workitemversionid", "");
            }
            String workItemVersionId = paramItemsDs.getString(i, "workitemversionid");
            List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
            String groupId = workItemGroupMap.containsKey(workItemKey) ? (String)workItemGroupMap.get(workItemKey) : "";
            String paramListId = paramItemsDs.getString(i, "paramlistid");
            String variantId = paramItemsDs.getString(i, "variantid");
            String currentParamListVersionId = paramItemsDs.getString(i, "paramlistversionid");
            List<String> paramListKey = Arrays.asList(paramListId, variantId);
            if (currentParamListKeyList.contains(paramListKey)) {
                paramItemsDs.setString(i, "paramlistversionid", "C");
            }
            String paramListVersionId = paramItemsDs.getString(i, "paramlistversionid");
            String paramId = paramItemsDs.getString(i, "paramid");
            String paramType = paramItemsDs.getString(i, "paramtype");
            List<String> paramKey = Arrays.asList(paramListId, paramListVersionId, variantId, paramId, paramType);
            if (workItemGroupSet.contains(workItemId)) continue;
            PropertyList rowProps = new PropertyList();
            simpleSpecHelper.addAllValues(paramItemsDs, i, rowProps);
            rowProps.setProperty("currentworkitemversionid", currentWorkItemVersionId);
            rowProps.setProperty("currentparamlistversionid", currentParamListVersionId);
            rowProps.setProperty("groupid", groupId);
            rowProps.setProperty("workitemstatus", "A");
            rowProps.setProperty("specparamitemstatus", "");
            rowProps.setProperty("selected", "N");
            rowProps.setProperty("testinglevellist", "");
            rowProps.setProperty("testinglevellist_original", "");
            HashSet<List<String>> workItemParamSet = (HashSet<List<String>>)workItemParamMap.get(workItemKey);
            if (workItemParamSet == null) {
                workItemParamSet = new HashSet<List<String>>();
                workItemParamSet.add(paramKey);
                workItemParamMap.put(workItemKey, workItemParamSet);
            } else {
                workItemParamSet.add(paramKey);
            }
            rowCollection.add(rowProps);
        }
        Collections.sort(rowCollection, new Comparator<Map<String, String>>(){

            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                BigDecimal workItemSequence2;
                List<String> workItemKey1 = Arrays.asList(o1.get("workitemid"), o1.get("currentworkitemversionid"));
                List<String> workItemKey2 = Arrays.asList(o2.get("workitemid"), o2.get("currentworkitemversionid"));
                BigDecimal workItemSequence1 = (BigDecimal)GetWIParamListItems.this.workItemSequenceMap.get(workItemKey1);
                int retValue = workItemSequence1.compareTo(workItemSequence2 = (BigDecimal)GetWIParamListItems.this.workItemSequenceMap.get(workItemKey2));
                if (retValue == 0 && (retValue = new BigDecimal(o1.get("workitemitemsequence")).compareTo(new BigDecimal(o2.get("workitemitemsequence")))) == 0) {
                    retValue = new BigDecimal(o1.get("paramlistitemsequence")).compareTo(new BigDecimal(o2.get("paramlistitemsequence")));
                }
                return retValue;
            }
        });
        simpleSpecHelper.setParamCounts(workItemParamMap, rowCollection);
        if (rowCollection.size() > 0) {
            ar.addCallbackArgument("newrows", rowCollection.toJSONString());
        } else {
            ar.addCallbackArgument("newrows", "");
        }
    }
}

