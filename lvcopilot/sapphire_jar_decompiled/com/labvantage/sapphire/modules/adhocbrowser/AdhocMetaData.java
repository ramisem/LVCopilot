/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocQueryPropertyHandler;
import com.labvantage.sapphire.modules.adhocbrowser.SearchableColumn;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.servlet.RequestContext;

public class AdhocMetaData
implements Serializable {
    private HashMap tableSdcMap = new HashMap();
    private HashMap sdcTableMap = new HashMap();
    private Set searchableTableSet = new HashSet();
    private HashMap searchableTableColumn = new HashMap();
    private HashMap reverseFKTableMap = new HashMap();
    private HashMap tableReverseFKChildMap = new HashMap();

    public boolean isSearchableTable(String tableid) {
        return this.searchableTableSet.contains(tableid);
    }

    public boolean isSearchableColumn(String tableid, String columnid) {
        return this.searchableTableColumn.get(tableid) != null && ((HashMap)this.searchableTableColumn.get(tableid)).get(columnid) != null;
    }

    public void addReverseFKTableEntry(String linkid, String tableid) {
        this.reverseFKTableMap.put(linkid, tableid);
    }

    public void addReverseFKTableToList(String tableid, String linkid) {
        if (this.tableReverseFKChildMap.get(tableid) == null) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(linkid);
            this.tableReverseFKChildMap.put(tableid, list);
        } else {
            ((ArrayList)this.tableReverseFKChildMap.get(tableid)).add(linkid);
        }
    }

    public String getReverseFKTableId(String linkid) {
        return (String)this.reverseFKTableMap.get(linkid);
    }

    public ArrayList getReverseFKChildList(String tableid) {
        return (ArrayList)this.tableReverseFKChildMap.get(tableid);
    }

    public SearchableColumn getSearchableColumn(String tableid, String columnid) {
        if (this.searchableTableColumn.get(tableid) != null && ((HashMap)this.searchableTableColumn.get(tableid)).get(columnid) != null) {
            return (SearchableColumn)((HashMap)this.searchableTableColumn.get(tableid)).get(columnid);
        }
        return null;
    }

    public String getSdcId(String tableid) {
        return (String)this.tableSdcMap.get(tableid);
    }

    public String getTableid(String sdcid) {
        return (String)this.sdcTableMap.get(sdcid);
    }

    public void addTableSdcEntry(String tableid, String sdcid) {
        this.tableSdcMap.put(tableid, sdcid);
        this.sdcTableMap.put(sdcid, tableid);
    }

    public void addSearchableTable(String tableid) {
        this.searchableTableSet.add(tableid);
    }

    public void addSearchableColumnMap(String tableid, HashMap searchableColMap) {
        this.searchableTableColumn.put(tableid, searchableColMap);
    }

    public Set getSearchableTableSet() {
        return this.searchableTableSet;
    }

    public static String getReferenceEntityName(String connectionid, String tableid, String columnid) throws SapphireException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("mode", "getreferenceentityname");
        props.put("tableid", tableid);
        props.put("columnid", columnid);
        HashMap requestProps = new RequestProcessor(connectionid).processRequest(AdhocQueryPropertyHandler.class.getName(), props);
        tableid = (String)requestProps.get("referenceentityname");
        return tableid;
    }

    public static AdhocMetaData getInstance(PageContext pageContext) throws SapphireException {
        return AdhocMetaData.getInstance((HttpServletRequest)pageContext.getRequest());
    }

    public static AdhocMetaData getInstance(HttpServletRequest request) throws SapphireException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("mode", "getadhocmetadata");
        HashMap requestProps = new RequestProcessor(requestContext.getConnectionId()).processRequest(AdhocQueryPropertyHandler.class.getName(), props);
        AdhocMetaData adhocmetadata = (AdhocMetaData)requestProps.get("adhocmetadata");
        return adhocmetadata;
    }
}

