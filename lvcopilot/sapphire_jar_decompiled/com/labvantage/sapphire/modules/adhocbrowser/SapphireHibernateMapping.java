/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTable;
import com.labvantage.sapphire.modules.adhocbrowser.HibernateMappingXMLUtil;
import com.labvantage.sapphire.modules.adhocbrowser.SDCTable;
import com.labvantage.sapphire.modules.adhocbrowser.SearchableColumn;
import com.labvantage.sapphire.modules.adhocbrowser.TableProperty;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.WebAdminService;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SapphireHibernateMapping {
    private AdhocMetaData adhocmetadata = new AdhocMetaData();
    private SapphireConnection sapphireConnection = null;

    public SapphireHibernateMapping(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    protected AdhocMetaData getAdhocMetaData() {
        return this.adhocmetadata;
    }

    private String generateXMLMappingCommon(DataSet systableDs, DataSet syscolumnDs, DataSet sysextendedcolumnDs, DataSet mmDs, DDTService ddtService, QueryService queryService) throws Exception {
        StringBuffer mappingXml = new StringBuffer();
        Trace.log("**********Start build xml Map\n" + mappingXml);
        HashSet<String> searchableSDCSet = new HashSet<String>();
        for (int i = 0; i < systableDs.getRowCount(); ++i) {
            int s;
            DataSet tableColumns;
            String tableid = systableDs.getString(i, "tableid");
            String sdcid = this.adhocmetadata.getSdcId(tableid);
            PropertyList sdcPropertyList = null;
            PropertyListCollection links = null;
            DataSet reverselinks = new DataSet();
            if (sdcid != null && sdcid.length() > 0) {
                sdcPropertyList = ddtService.getSDCProperties(sdcid);
                links = sdcPropertyList.getCollection("links");
                reverselinks = ddtService.getReverseLinksData(sdcid);
            }
            Trace.log("Generating hbm mapping for:" + tableid + " sdcid:" + sdcid);
            searchableSDCSet.add(sdcid);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", tableid);
            DataSet searchableColumns = tableColumns = syscolumnDs.getFilteredDataSet(filter);
            DataSet searchableExtColumns = sysextendedcolumnDs.getFilteredDataSet(filter);
            HashMap<String, SearchableColumn> searchableColMap = new HashMap<String, SearchableColumn>();
            String columnid = null;
            for (s = 0; s < searchableColumns.getRowCount(); ++s) {
                columnid = searchableColumns.getString(s, "columnid");
                if (!"Y".equals(searchableColumns.getString(s, "searchableflag"))) continue;
                searchableColMap.put(columnid, new SearchableColumn(columnid, searchableColumns.getString(s, "datatype"), searchableColumns.getString(s, "columnlabel"), searchableColumns.getValue(s, "columnlength")));
            }
            for (s = 0; s < searchableExtColumns.getRowCount(); ++s) {
                columnid = searchableExtColumns.getString(s, "columnid");
                if (!"Y".equals(searchableExtColumns.getString(s, "searchableflag"))) continue;
                searchableColMap.put(columnid, new SearchableColumn(columnid, searchableExtColumns.getString(s, "datatype"), searchableExtColumns.getString(s, "columndesc"), searchableExtColumns.getValue(s, "columnlength"), searchableExtColumns.getString(s, "columndefinition")));
            }
            this.adhocmetadata.addSearchableColumnMap(tableid, searchableColMap);
            if ("role".equals(tableid)) continue;
            String xml = new SDCTable(sdcPropertyList, tableColumns, links, reverselinks).getSDCMappingXml(this.adhocmetadata);
            mappingXml.append(xml);
            Trace.log(xml);
        }
        HashSet<String> processMMTableSet = new HashSet<String>();
        String[] supportedDetails = new String[]{"sdirole", "role", "sysuserrole", "sdcsecurity", "securitysetsdc", "securitysetitem", "sdisecuritydepartment", "sdisecurityset", "departmentsysuser", "rsetitems", "rsetitemsds", "rsetitemsnl", "documentfield", "sdiattribute", "sdiattachment", "sdialias", "sdiapproval", "sdiapprovalstep", "sdidataapproval", "sdidatarelation", "sdidataitemlimits", "sdidataitemspec", "sdidatacapture", "sdiaddress", "sdiworkitemitem", "categoryitem", "v_worksheetmetadata", "worksheetsdi_Sample"};
        for (int d = 0; d < supportedDetails.length; ++d) {
            if ("sdiattachment".equals(supportedDetails[d]) && searchableSDCSet.contains("SDIAttachment")) continue;
            String xml = HibernateMappingXMLUtil.getTableMapping(DetailTable.getInstance(supportedDetails[d], queryService));
            mappingXml.append(xml);
            processMMTableSet.add(supportedDetails[d]);
        }
        for (int m = 0; m < mmDs.getRowCount(); ++m) {
            String sdcid = mmDs.getString(m, "sdcid");
            String linktableid = mmDs.getString(m, "linktableid");
            if (!searchableSDCSet.contains(sdcid) || processMMTableSet.contains(linktableid)) continue;
            Trace.log("Generating hbm mapping for ManytoMany table:" + linktableid + "for sdcid:" + sdcid);
            HashMap<String, String> filtermap = new HashMap<String, String>();
            filtermap.put("tableid", linktableid);
            DataSet columnDs = syscolumnDs.getFilteredDataSet(filtermap);
            DetailTable mmTable = new DetailTable(linktableid, columnDs.getString(0, "columnid"));
            for (int c = 0; c < columnDs.getRowCount(); ++c) {
                mmTable.addTableProperty(new TableProperty(columnDs.getString(c, "columnid"), SDCTable.getType(columnDs.getString(c, "datatype"))));
            }
            String xml = HibernateMappingXMLUtil.getTableMapping(mmTable);
            mappingXml.append(xml);
            Trace.log(xml);
            processMMTableSet.add(linktableid);
        }
        return mappingXml.toString();
    }

    public String generateXMLMapping(boolean validate) throws SapphireException {
        StringBuffer mappingXml = new StringBuffer();
        try {
            QueryService queryService = new QueryService(this.sapphireConnection);
            DDTService ddtService = new DDTService(this.sapphireConnection);
            DataSet ds = queryService.getSqlDataSet("select sdcid, tableid from sdc order by sdcid");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                this.adhocmetadata.addTableSdcEntry(ds.getString(i, "tableid"), ds.getString(i, "sdcid"));
            }
            DataSet syscolumnDs = queryService.getSqlDataSet("select * from syscolumn where datatype in ( 'C', 'D', 'N', 'R' ) order by tableid, columnsequence");
            DataSet sysextendedcolumnDs = queryService.getSqlDataSet("select * from sysextendedcolumn");
            String sdclist = "'DataSet','DataItem','TrackItemSDC','SDIWorkItem','WorkItem','LV_Document','SDIDocument','LV_Form','LV_Field','Department'";
            PropertyTree navigatorPolicy = new PropertyTree("NavigatorPolicy");
            navigatorPolicy.setValueXML(new WebAdminService(this.sapphireConnection).getPropertyTreeValue("NavigatorPolicy"));
            if (navigatorPolicy != null) {
                ArrayList nodelist = navigatorPolicy.getAllNodes();
                for (int i = 0; i < nodelist.size(); ++i) {
                    PropertyList tempPL = ((Node)nodelist.get(i)).getPropertyList();
                    PropertyListCollection treenodes = tempPL.getCollection("treenodes");
                    if (treenodes == null) continue;
                    for (int n = 0; n < treenodes.size(); ++n) {
                        String sdcid = treenodes.getPropertyList(n).getProperty("sdcid");
                        if (sdclist.indexOf("'" + sdcid + "'") >= 0) continue;
                        sdclist = sdclist + ",'" + sdcid + "'";
                    }
                }
            }
            SafeSQL safeSQL = new SafeSQL();
            DataSet systableDs = queryService.getPreparedSqlDataSet("select systable.* from systable inner join sdc on systable.tableid=sdc.tableid where ( ( sdc.searchableflag='Y' ) or sdc.sdcid in (" + safeSQL.addIn(sdclist) + ") ) order by 1", safeSQL.getValues());
            DataSet mmDs = queryService.getSqlDataSet("select * from sdclink where linktype in ('M','D') order by sdcid");
            for (int i = 0; i < systableDs.getRowCount(); ++i) {
                String tableid = systableDs.getString(i, "tableid");
                this.adhocmetadata.addSearchableTable(tableid);
            }
            mappingXml.append("<?xml version=\"1.0\"?>\n");
            if (validate) {
                mappingXml.append("<!DOCTYPE hibernate-mapping PUBLIC\n    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">");
            }
            mappingXml.append("\n<hibernate-mapping>\n");
            mappingXml.append(this.generateXMLMappingCommon(systableDs, syscolumnDs, sysextendedcolumnDs, mmDs, ddtService, queryService));
            mappingXml.append("</hibernate-mapping>");
            Trace.log("</hibernate-mapping>\n*************Done Mapping");
        }
        catch (Throwable t) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(t, false), t);
        }
        return mappingXml.toString();
    }
}

