/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFieldsField;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class WorksheetItemFields
implements ELNConstants {
    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String DATE = "date";
    public static final String DATEONLY = "dateonly";
    public Map<String, WorksheetItemFieldsField> fieldMap = new HashMap<String, WorksheetItemFieldsField>();
    private M18NUtil m18n;
    private FormatUtil formatUtil;
    private QueryProcessor queryProcessor;
    private ActionProcessor actionProcessor;
    private String worksheetId;
    private String worksheetVersionId;
    private String worksheetItemId;
    private String worksheetItemVersionId;

    public WorksheetItemFields(M18NUtil m18n, FormatUtil formatUtil, QueryProcessor queryProcessor, ActionProcessor actionProcessor, String worksheetId, String worksheetVersionId, String worksheetItemId, String worksheetItemVersionId) {
        this.m18n = m18n;
        this.formatUtil = formatUtil;
        this.queryProcessor = queryProcessor;
        this.actionProcessor = actionProcessor;
        this.worksheetId = worksheetId;
        this.worksheetVersionId = worksheetVersionId;
        this.worksheetItemId = worksheetItemId;
        this.worksheetItemVersionId = worksheetItemVersionId;
    }

    public WorksheetItemFieldsField createField(String fieldid) {
        return new WorksheetItemFieldsField(fieldid, this.m18n, this.formatUtil);
    }

    public String getWorksheetId() {
        return this.worksheetId;
    }

    public String getWorksheetVersionId() {
        return this.worksheetVersionId;
    }

    public String getWorksheetItemId() {
        return this.worksheetItemId;
    }

    public String getWorksheetItemVersionId() {
        return this.worksheetItemVersionId;
    }

    public ActionProcessor getActionProcessor() {
        return this.actionProcessor;
    }

    public QueryProcessor getQueryProcessor() {
        return this.queryProcessor;
    }

    public WorksheetItemFieldsField getField(String fieldid) {
        return this.fieldMap.get(fieldid);
    }

    public WorksheetItemFieldsField addField(String fieldid, String fieldtitle, String datatype, int sequence, PropertyList fielddef) {
        if (!this.contains(fieldid)) {
            WorksheetItemFieldsField field = this.createField(fieldid);
            field.fieldtitle = fieldtitle;
            field.datatype = datatype;
            field.sequence = sequence;
            field.fielddef = fielddef;
            field.added = true;
            field.setInstance(0, null, null, null, "");
            this.fieldMap.put(fieldid, field);
        }
        return this.getField(fieldid);
    }

    public void addField(WorksheetItemFieldsField field) {
        if (!this.contains(field.fieldid)) {
            this.fieldMap.put(field.fieldid, field);
        }
    }

    public boolean contains(String fieldid) {
        return this.fieldMap.containsKey(fieldid);
    }

    public void updateFieldTitle(String fieldid, String fieldtitle) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (!(field == null || field.fieldtitle != null && field.fieldtitle.equals(fieldtitle))) {
            field.fieldtitle = fieldtitle;
            field.edited = true;
        }
    }

    public void updateFieldDatatype(String fieldid, String datatype) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (!(field == null || field.datatype != null && field.datatype.equals(datatype))) {
            field.datatype = datatype;
            field.edited = true;
        }
    }

    public String getFieldDatatype(String fieldid) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        return field.datatype;
    }

    public void updateFieldSequence(String fieldid, int sequence) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null && field.sequence != sequence) {
            field.sequence = sequence;
            field.edited = true;
        }
    }

    public void updateFieldDef(String fieldid, PropertyList fielddef) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null && fielddef != null) {
            field.fielddef = fielddef;
            field.edited = true;
        }
    }

    public void enterFieldValue(String fieldid, int instance, String enteredtext) throws SapphireException {
        boolean changed;
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null && (changed = field.updateInstance(instance, enteredtext))) {
            field.edited = true;
        }
    }

    public void enterFieldValue(String fieldid, int instance, BigDecimal bigDecimal) throws SapphireException {
        boolean changed;
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null && (changed = field.updateInstance(instance, bigDecimal))) {
            field.edited = true;
        }
    }

    public void enterFieldValue(String fieldid, int instance, Calendar datetime) throws SapphireException {
        boolean changed;
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null && (changed = field.updateInstance(instance, datetime))) {
            field.edited = true;
        }
    }

    public void deleteField(String fieldid) {
        WorksheetItemFieldsField field = this.fieldMap.get(fieldid);
        if (field != null) {
            field.delete = true;
            field.added = false;
        }
    }

    public void deleteAll() {
        Iterator<String> iterator = this.fieldMap.keySet().iterator();
        while (iterator.hasNext()) {
            WorksheetItemFieldsField field = this.fieldMap.get(iterator.next());
            field.delete = true;
            field.added = false;
        }
    }

    public int size() {
        return this.fieldMap.size();
    }

    public Iterator<String> iterator() {
        return this.fieldMap.keySet().iterator();
    }

    public void save() throws SapphireException {
        WorksheetItemFieldsField field;
        StringBuffer activityLog = new StringBuffer();
        StringBuffer add = new StringBuffer();
        StringBuffer edit = new StringBuffer();
        StringBuffer delete = new StringBuffer();
        for (String fieldid : this.fieldMap.keySet()) {
            if (this.fieldMap.get((Object)fieldid).added) {
                add.append(";").append(fieldid);
            }
            if (this.fieldMap.get((Object)fieldid).edited) {
                edit.append(";").append(fieldid);
            }
            if (!this.fieldMap.get((Object)fieldid).delete) continue;
            delete.append(";").append(fieldid);
        }
        SafeSQL safeSQL = new SafeSQL();
        if (delete.length() > 0) {
            this.getQueryProcessor().execPreparedUpdate("DELETE FROM worksheetitemfield WHERE worksheetitemid = " + safeSQL.addVar(this.getWorksheetItemId()) + " AND worksheetitemversionid = " + safeSQL.addVar(this.getWorksheetItemVersionId()) + " AND fieldname IN ( " + safeSQL.addIn(delete.substring(1), ";") + " )", safeSQL.getValues());
            activityLog.append("Deleted all instances of field ").append(delete.substring(1));
        }
        if (add.length() > 0) {
            try {
                for (String fieldid : this.fieldMap.keySet()) {
                    field = this.fieldMap.get(fieldid);
                    if (!field.added) continue;
                    Object[] o = new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId(), fieldid, 0, field.fieldtitle, field.datatype, field.sequence, field.fielddef == null ? "" : field.fielddef.toXMLString(), field.fieldContext};
                    this.getQueryProcessor().execPreparedUpdate("INSERT INTO worksheetitemfield ( worksheetitemid, worksheetitemversionid, fieldname, fieldinstance, fieldtitle, datatype, usersequence, fielddef, fieldcontext ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )", o);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to add worksheetitem fields", e);
            }
            activityLog.append(activityLog.length() > 0 ? " and added " : "Added ").append("fields ").append(add.substring(1));
        }
        if (edit.length() > 0) {
            try {
                for (String fieldid : this.fieldMap.keySet()) {
                    if (!this.fieldMap.get((Object)fieldid).edited) continue;
                    field = this.fieldMap.get(fieldid);
                    String fieldContext = field.fieldContext;
                    String enteredtext = field.instances.get((Object)Integer.valueOf((int)0)).enteredtext;
                    BigDecimal numericvalue = field.instances.get((Object)Integer.valueOf((int)0)).numericvalue;
                    Timestamp datevalue = field.instances.get((Object)Integer.valueOf((int)0)).datevalue != null ? new Timestamp(field.instances.get((Object)Integer.valueOf((int)0)).datevalue.getTime().getTime()) : null;
                    String displayvalue = field.instances.get((Object)Integer.valueOf((int)0)).displayvalue;
                    safeSQL.reset();
                    String sql = "UPDATE worksheetitemfield SET fieldtitle = " + safeSQL.addVar(field.fieldtitle) + ", datatype = " + safeSQL.addVar(field.datatype) + ", usersequence = " + safeSQL.addVar(field.sequence) + ", fielddef = " + safeSQL.addVar(field.fielddef == null ? "" : field.fielddef.toXMLString()) + ", enteredtext = " + safeSQL.addVar(enteredtext) + ", numericvalue = " + safeSQL.addVar(numericvalue) + ", datevalue = " + safeSQL.addVar(datevalue) + ", displayvalue =  " + safeSQL.addVar(displayvalue) + ", fieldcontext =  " + safeSQL.addVar(fieldContext) + " WHERE worksheetitemid = " + safeSQL.addVar(this.getWorksheetItemId()) + " AND worksheetitemversionid = " + safeSQL.addVar(this.getWorksheetItemVersionId()) + " AND fieldname = " + safeSQL.addVar(fieldid);
                    this.getQueryProcessor().execPreparedUpdate(sql, safeSQL.getValues());
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to edit worksheetitem fields", e);
            }
            activityLog.append(activityLog.length() > 0 ? " and edited " : "Edited ").append("fields ").append(edit.substring(1));
        }
        if (activityLog.length() > 0) {
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", this.getWorksheetId());
            activityProps.setProperty("worksheetversionid", this.getWorksheetVersionId());
            activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
            activityProps.setProperty("targetkeyid1", this.getWorksheetItemId());
            activityProps.setProperty("targetkeyid2", this.getWorksheetItemVersionId());
            activityProps.setProperty("activitytype", "SetConfig");
            activityProps.setProperty("activitylog", activityLog.toString());
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        }
    }
}

