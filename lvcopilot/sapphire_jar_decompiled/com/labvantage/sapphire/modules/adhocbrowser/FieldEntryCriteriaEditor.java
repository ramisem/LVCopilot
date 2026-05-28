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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FieldEntryCriteriaEditor
extends CriteriaEditor {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public PropertyList getEditorProperty(String sdcid, PropertyList column, AdhocMetaData adhocmetadata, SDCProcessor sdcProcessor, SDITagUtil sdiTagUtil, TranslationProcessor tp, PropertyList pagedata) throws SapphireException {
        String columnid = column.getProperty("columnid");
        QueryProcessor qp = new QueryProcessor(sdcProcessor.getConnectionid());
        boolean isWorksheetitemfield = columnid.indexOf("worksheetitemfield") >= 0;
        PropertyList fieldpl = isWorksheetitemfield ? this.getWorksheetItemFieldPropertyList(columnid, sdcProcessor, qp, pagedata) : this.getDocumentFieldPropertyList(columnid, sdcProcessor, qp, pagedata);
        column.setProperty("name", FieldEntryCriteriaEditor.getUniqueId());
        column.setProperty("title", fieldpl.getProperty("title"));
        String fieldtype = fieldpl.getProperty("fieldtype");
        String defaulteditor = isWorksheetitemfield ? fieldpl.getProperty("type") : fieldpl.getProperty("defaulteditor");
        column.setProperty("datatype", fieldtype);
        if ("N".equals(fieldtype)) {
            column.setProperty("onblur", "validateValue( 'Number', this )");
            column.setProperty("mode", "input");
            column.setProperty("size", "10");
        } else if ("D".equals(fieldtype) || "O".equals(fieldtype)) {
            column.setProperty("onblur", "validateValue( 'Date', this )");
            column.setProperty("mode", "datelookup");
            if ("O".equals(fieldtype)) {
                column.setProperty("format", "O");
            }
            column.setProperty("size", "14");
            column.setProperty("img", "WEB-CORE/elements/images/lookup_date.gif");
        } else if ("C".equals(fieldtype)) {
            String entrysdcid = fieldpl.getProperty("sdcid");
            String lookuppageid = fieldpl.getProperty("lookuppageid");
            column.putAll(fieldpl);
            if ("lookup".equals(defaulteditor) && (lookuppageid.length() > 0 || entrysdcid.length() > 0)) {
                column.setProperty("mode", "lookup");
                column.setProperty("img", "WEB-CORE/elements/images/lookup.gif");
                column.setProperty("sdcid", entrysdcid);
                column.setProperty("linksdcid", entrysdcid);
                if (lookuppageid != null && lookuppageid.length() > 0) {
                    PropertyList lookuplink = new PropertyList();
                    lookuplink.setProperty("href", lookuppageid);
                    column.setProperty("lookuplink", lookuplink);
                }
                FieldEntryCriteriaEditor.setDefaultLookupLink(column, entrysdcid, qp);
            } else if ("dropdown".equals(defaulteditor) || "radiobutton".equals(defaulteditor) || "checkbox".equals(defaulteditor)) {
                column.setProperty("mode", "dropdownlist");
                column.setProperty("displayvalue", column.getProperty("values"));
            } else {
                column.setProperty("mode", "input");
                column.setProperty("size", "40");
            }
        } else {
            column.setProperty("mode", "input");
            column.setProperty("size", "40");
        }
        return column;
    }

    private String convertDataType(String datatype) {
        return "string".equals(datatype) ? "C" : ("number".equals(datatype) ? "N" : ("date".equals(datatype) ? "D" : ("dateonly".equals(datatype) ? "O" : "C")));
    }

    private PropertyList getWorksheetItemFieldPropertyList(String columnid, SDCProcessor sdcProcessor, QueryProcessor qp, PropertyList pagedata) throws SapphireException {
        String fieldid = StringUtil.getTokens(columnid)[0];
        String connectionid = sdcProcessor.getConnectionid();
        DataSet fieldDs = qp.getPreparedSqlDataSet("select * from worksheetitemfield where fieldname=?", new Object[]{fieldid}, true);
        String fielddef = fieldDs.getValue(0, "fielddef");
        String fieldtype = fieldDs.getValue(0, "datatype");
        PropertyList fieldpl = new PropertyList("fieldpl");
        if (fielddef.length() > 0) {
            fieldpl.setPropertyList(fielddef);
            fieldtype = fieldpl.getProperty("type").length() > 0 ? fieldpl.getProperty("type") : fieldtype;
        }
        fieldpl.setProperty("fieldtype", this.convertDataType(fieldtype));
        fieldpl.setProperty("title", fieldDs.getValue(0, "fieldtitle"));
        ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        for (String key : fieldpl.keySet()) {
            String value = fieldpl.getProperty(key);
            if (value.indexOf("$G{") < 0) continue;
            HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
            bindMap.put("user", connectionInfo.getUserAttributeMap());
            bindMap.put("pagedata", pagedata);
            value = StringUtil.replaceAll(value, "[currentuser]", connectionInfo.getSysuserId());
            try {
                String newvalue = GroovyUtil.getInstance(connectionInfo).evaluateSecure(value, bindMap);
                fieldpl.setProperty(key, newvalue);
            }
            catch (Exception e) {
                fieldpl.setProperty(key, "");
            }
        }
        return fieldpl;
    }

    private PropertyList getDocumentFieldPropertyList(String columnid, SDCProcessor sdcProcessor, QueryProcessor qp, PropertyList pagedata) throws SapphireException {
        String fieldid = StringUtil.getTokens(columnid)[0];
        String connectionid = sdcProcessor.getConnectionid();
        DataSet fieldDs = qp.getPreparedSqlDataSet("select * from field where fieldid=?", new Object[]{fieldid}, true);
        String fieldobject = fieldDs.getValue(0, "fieldobject");
        PropertyList fieldpl = new PropertyList("fieldpl");
        fieldpl.setPropertyList(fieldobject);
        fieldpl.setProperty("fieldtype", this.convertDataType(fieldDs.getValue(0, "fieldtype")));
        fieldpl.setProperty("defaulteditor", fieldDs.getValue(0, "defaulteditor"));
        fieldpl.setProperty("title", fieldDs.getValue(0, "fieldlabel"));
        ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        for (String key : fieldpl.keySet()) {
            String value = fieldpl.getProperty(key);
            if (value.indexOf("$G{") < 0) continue;
            HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
            bindMap.put("user", connectionInfo.getUserAttributeMap());
            bindMap.put("pagedata", pagedata);
            value = StringUtil.replaceAll(value, "[currentuser]", connectionInfo.getSysuserId());
            try {
                String newvalue = GroovyUtil.getInstance(connectionInfo).evaluateSecure(value, bindMap);
                fieldpl.setProperty(key, newvalue);
            }
            catch (Exception e) {
                fieldpl.setProperty(key, "");
            }
        }
        return fieldpl;
    }
}

