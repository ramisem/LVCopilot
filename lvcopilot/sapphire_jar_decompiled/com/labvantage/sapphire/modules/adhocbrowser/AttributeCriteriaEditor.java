/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.CriteriaEditor;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AttributeCriteriaEditor
extends CriteriaEditor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public PropertyList getEditorProperty(String sdcid, PropertyList column, AdhocMetaData adhocmetadata, SDCProcessor sdcProcessor, SDITagUtil sdiTagUtil, TranslationProcessor tp, PropertyList pagedata) throws SapphireException {
        String columnid = column.getProperty("columnid");
        String attributeid = StringUtil.getTokens(columnid)[0];
        String attributeSdcid = sdcid;
        if (columnid.indexOf(".sdiattribute[") > 0) {
            try {
                String reftableid = AdhocMetaData.getReferenceEntityName(sdcProcessor.getConnectionid(), adhocmetadata.getTableid(sdcid), columnid.substring(0, columnid.lastIndexOf(".")));
                attributeSdcid = adhocmetadata.getSdcId(reftableid);
            }
            catch (Exception reftableid) {
                // empty catch block
            }
        }
        QueryProcessor qp = new QueryProcessor(sdcProcessor.getConnectionid());
        if ("LV_Worksheet".equals(attributeSdcid)) {
            attributeSdcid = "LV_Worksheet','LV_WorksheetSection','LV_WorksheetItem";
        }
        SafeSQL safeSQL = new SafeSQL();
        String s = "select s.datatype, s.attributetitle, s.attributedefid attributeid, s.editsdcid, s.editreftypeid, e.editordefinition from attributedef s left outer join editorstyle e on s.editorstyleid=e.editorstyleid where s.basedonid in (" + safeSQL.addIn(attributeSdcid) + ") and s.attributedefid=" + safeSQL.addVar(attributeid) + "";
        DataSet attributeDs = qp.getPreparedSqlDataSet(s, safeSQL.getValues(), true);
        String editordefintionxml = attributeDs.getValue(0, "editordefinition");
        PropertyList editorpl = new PropertyList("fieldpl");
        editorpl.setPropertyList(editordefintionxml);
        String connectionid = tp.getConnectionid();
        ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        for (String key : editorpl.keySet()) {
            String value = editorpl.getProperty(key);
            if (value.indexOf("$G{") < 0) continue;
            HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
            bindMap.put("user", connectionInfo.getUserAttributeMap());
            bindMap.put("pagedata", pagedata);
            value = StringUtil.replaceAll(value, "[currentuser]", connectionInfo.getSysuserId());
            try {
                String newvalue = GroovyUtil.getInstance(connectionInfo).evaluateSecure(value, bindMap);
                editorpl.setProperty(key, newvalue);
            }
            catch (Exception e) {
                editorpl.setProperty(key, "");
            }
        }
        String linksdcid = attributeDs.getValue(0, "editsdcid");
        String editreftypeid = attributeDs.getValue(0, "editreftypeid");
        PropertyList lookuplink = editorpl.getPropertyList("lookuplink");
        if (lookuplink != null && lookuplink.getProperty("sdcid").length() == 0 && linksdcid.length() > 0) {
            lookuplink.setProperty("sdcid", linksdcid);
        }
        if (editorpl.getProperty("reftypeid").length() == 0 && editreftypeid.length() > 0) {
            editorpl.setProperty("reftypeid", editreftypeid);
        }
        column.putAll(editorpl);
        String title = attributeDs.getValue(0, "attributetitle").length() > 0 ? attributeDs.getValue(0, "attributetitle") : attributeDs.getValue(0, "attributeid");
        String datatype = attributeDs.getValue(0, "datatype");
        column.setProperty("label", title);
        column.setProperty("columntype", datatype);
        if (lookuplink == null || lookuplink.getProperty("href").length() == 0) {
            CriteriaEditor.setDefaultLookupLink(column, lookuplink == null ? linksdcid : lookuplink.getProperty("sdcid"), qp);
        }
        if (column.getProperty("mode").length() == 0) {
            if (linksdcid.length() > 0) {
                column.setProperty("mode", "lookup");
            } else if (editreftypeid.length() > 0) {
                column.setProperty("mode", "dropdownlist");
            } else if ("D".equals(datatype) || "O".equals(datatype)) {
                column.setProperty("mode", "datelookup");
            }
        }
        return column;
    }
}

