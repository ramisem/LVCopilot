/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.DetailTable;
import com.labvantage.sapphire.modules.adhocbrowser.HibernateMappingXMLUtil;
import com.labvantage.sapphire.modules.adhocbrowser.TableObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCTable
extends TableObject {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private final DataSet tablecolumns;
    private final PropertyListCollection links;
    private final DataSet reverselinks;
    private final ArrayList detailList = new ArrayList();
    private final HashMap detailkeyMap = new HashMap();
    ArrayList reverseFKList = new ArrayList();

    public SDCTable(PropertyList sdcPropertyList, DataSet tablecolumns, PropertyListCollection links, DataSet reverselinks) {
        super(sdcPropertyList.getProperty("tableid"), sdcPropertyList.getProperty("keycolid1"));
        this.tablecolumns = tablecolumns;
        this.links = links;
        this.reverselinks = reverselinks;
        if ("Y".equals(sdcPropertyList.getProperty("dataentryflag"))) {
            this.detailList.add("sdidata");
            this.detailList.add("sdidataapproval");
            this.detailList.add("sdidatarelation");
            this.detailList.add("sdidataitem");
            this.detailList.add("sdidataitemspec");
            this.detailList.add("sdidataitemlimits");
            this.detailList.add("sdidatacapture");
        }
        if ("Y".equals(sdcPropertyList.getProperty("aliasableflag"))) {
            this.detailList.add("sdialias");
        }
        if ("Y".equals(sdcPropertyList.getProperty("workitemflag"))) {
            this.detailList.add("sdiworkitem");
            this.detailList.add("sdiworkitemitem");
        }
        if ("Y".equals(sdcPropertyList.getProperty("accesscontrolledflag"))) {
            this.detailList.add("sdirole");
        }
        if ("Y".equals(sdcPropertyList.getProperty("allowattributesflag"))) {
            this.detailList.add("sdiattribute");
        }
        if ("Y".equals(sdcPropertyList.getProperty("categoriesflag"))) {
            this.detailList.add("categoryitem");
        }
        if ("Y".equals(sdcPropertyList.getProperty("attachmentsflag"))) {
            this.detailList.add("sdiattachment");
        }
        if ("Y".equals(sdcPropertyList.getProperty("specflag"))) {
            this.detailList.add("sdispec");
        }
        if ("LV_Worksheet".equals(sdcPropertyList.getProperty("sdcid"))) {
            this.detailList.add("v_worksheetmetadata");
            this.detailkeyMap.put("v_worksheetmetadata", "worksheetid");
            this.detailList.add("worksheetsdi_Sample");
            this.detailkeyMap.put("worksheetsdi_Sample", "worksheetid");
        }
        this.detailList.add("sdiapproval");
        this.detailList.add("sdiapprovalstep");
        this.detailList.add("sdiaddress");
        this.detailList.add("trackitem");
        this.detailkeyMap.put("trackitem", "linkkeyid1");
        String tableid = this.getName();
        if ("document".equals(tableid)) {
            this.detailList.add("sdidocument");
            this.detailkeyMap.put("sdidocument", "documentid");
            this.detailList.add("documentfield");
            this.detailkeyMap.put("documentfield", "documentid");
        } else if ("sdidocument".equals(tableid)) {
            this.detailList.add("documentfield");
            this.detailkeyMap.put("documentfield", "documentid");
        } else {
            this.detailList.add("sdidocument");
            this.detailkeyMap.put("sdidocument", "keyid1");
        }
    }

    public String getSDCMappingXml(AdhocMetaData adhocmetadata) {
        String type;
        String columnid;
        int i;
        Set searchableTableSet = adhocmetadata.getSearchableTableSet();
        StringBuffer xml = new StringBuffer();
        String tableid = this.getName();
        xml.append(HibernateMappingXMLUtil.getStandardHeader(tableid));
        HashSet<String> processedSet = new HashSet<String>();
        String keycolid1 = this.getIdProperty();
        for (i = 0; i < this.tablecolumns.getRowCount(); ++i) {
            columnid = this.tablecolumns.getString(i, "columnid");
            type = SDCTable.getType(this.tablecolumns.getString(i, "datatype"));
            if ((i != 0 || keycolid1 == null || !keycolid1.equals(columnid)) && (keycolid1 == null || !keycolid1.equals(columnid))) continue;
            if ("sdidocument".equals(this.getName())) {
                xml.append(HibernateMappingXMLUtil.getIdElement("documentid", type));
            } else {
                xml.append(HibernateMappingXMLUtil.getIdElement(columnid, type));
            }
            for (int d = 0; d < this.detailList.size(); ++d) {
                String detailtableid;
                xml.append(HibernateMappingXMLUtil.getSetElement(detailtableid, this.detailkeyMap.get(detailtableid = (String)this.detailList.get(d)) == null ? "keyid1" : (String)this.detailkeyMap.get(detailtableid)));
                processedSet.add(detailtableid);
            }
            HashSet<String> set = new HashSet<String>();
            if (this.reverselinks == null || this.reverselinks.getRowCount() <= 0) break;
            for (int rl = 0; rl < this.reverselinks.getRowCount(); ++rl) {
                String rltableid = adhocmetadata.getTableid(this.reverselinks.getString(rl, "sdcid"));
                if (!searchableTableSet.contains(rltableid)) continue;
                String linkcolumnid = this.reverselinks.getString(rl, "sdccolumnid");
                String name = rltableid + "_" + linkcolumnid;
                if (set.contains(name)) continue;
                String childentity = rltableid;
                xml.append(HibernateMappingXMLUtil.getSetElement(name, linkcolumnid, childentity));
                adhocmetadata.addReverseFKTableEntry(name, rltableid);
                adhocmetadata.addReverseFKTableToList(tableid, name);
                set.add(name);
                DetailTable.addToDetailTableSet(name);
            }
            break;
        }
        for (i = 0; i < this.tablecolumns.getRowCount(); ++i) {
            columnid = this.tablecolumns.getString(i, "columnid");
            type = SDCTable.getType(this.tablecolumns.getString(i, "datatype"));
            HashMap<String, String> filterMap = new HashMap<String, String>();
            filterMap.put("sdccolumnid", columnid);
            PropertyList columnlink = null;
            for (int k = 0; k < this.links.size() && !columnid.equals((columnlink = this.links.getPropertyList(k)).getProperty("sdccolumnid")); ++k) {
                if (k != this.links.size() - 1) continue;
                columnlink = null;
            }
            boolean haslink = false;
            String linktype = "";
            String linktableid = "";
            if (columnlink != null && columnlink.size() > 0) {
                haslink = true;
                linktype = columnlink.getProperty("linktype");
                linktableid = columnlink.getProperty("tableid");
            }
            if (!"string".equals(type) && !"timestamp".equals(type) && !"big_decimal".equals(type) && !"long".equals(type) && !"integer".equals(type) && !"double".equals(type)) continue;
            if ("F".equals(linktype) && linktableid != null && linktableid.length() > 0) {
                xml.append(HibernateMappingXMLUtil.getFKColumnPropertyElement(columnid, type));
                if (searchableTableSet.contains(linktableid)) {
                    xml.append(HibernateMappingXMLUtil.getManyToOneElement(columnid, linktableid));
                    continue;
                }
                xml.append(HibernateMappingXMLUtil.getPropertyElement(columnid, type));
                continue;
            }
            xml.append(HibernateMappingXMLUtil.getPropertyElement(columnid, type));
        }
        for (int k = 0; k < this.links.size(); ++k) {
            PropertyList columnlink = this.links.getPropertyList(k);
            String linktype = columnlink.getProperty("linktype");
            String linktableid = columnlink.getProperty("linktableid");
            if (processedSet.contains(linktableid) || !"M".equals(linktype) && !"D".equals(linktype) || linktableid == null || linktableid.length() <= 0) continue;
            xml.append(HibernateMappingXMLUtil.getSetElement(linktableid, keycolid1, linktableid));
            processedSet.add(linktableid);
        }
        xml.append(HibernateMappingXMLUtil.getStandardFooter());
        return xml.toString();
    }

    public static void getKeyColumnSelect(String tableid, String keycolid1, String keycolid2, String keycolid3, String desccol, StringBuffer selectbuffer, ArrayList selectList, boolean isRetrieveDetail) {
        if (!selectList.contains(keycolid1)) {
            if (selectList.size() == 0 || isRetrieveDetail) {
                selectbuffer.append(tableid + "." + keycolid1);
                selectList.add(keycolid1);
                if (!isRetrieveDetail) {
                    selectbuffer.append(", " + tableid + "." + desccol + " ");
                    selectList.add(desccol);
                }
            } else {
                selectbuffer.append("," + tableid + "." + keycolid1 + " ");
                selectList.add(keycolid1);
            }
        }
        if (keycolid2 != null && keycolid2.length() > 0 && !selectList.contains(keycolid2)) {
            selectList.add(keycolid2);
            selectbuffer.append("," + tableid + "." + keycolid2 + " ");
        }
        if (keycolid3 != null && keycolid3.length() > 0 && !selectList.contains(keycolid3)) {
            selectList.add(keycolid3);
            selectbuffer.append("," + tableid + "." + keycolid3 + " ");
        }
    }
}

