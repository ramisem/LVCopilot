/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedHashMap;
import sapphire.SapphireException;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class WorksheetItemFieldsField {
    private M18NUtil m18n;
    private FormatUtil formatUtil;
    public String fieldContext = "";
    public String fieldid;
    public String fieldtitle;
    public String datatype;
    public int sequence;
    public PropertyList fielddef;
    public boolean loaded = false;
    public boolean edited = false;
    public boolean delete = false;
    public boolean added = false;
    public LinkedHashMap<Integer, FieldInstance> instances = new LinkedHashMap();

    public WorksheetItemFieldsField(String fieldid, M18NUtil m18n, FormatUtil formatUtil) {
        this.fieldid = fieldid;
        this.m18n = m18n;
        this.formatUtil = formatUtil;
    }

    public void setInstance(int instance, String enteredtext, BigDecimal numericvalue, Calendar datevalue, String displayvalue) {
        if (!this.instances.containsKey(instance)) {
            this.instances.put(instance, new FieldInstance(enteredtext, numericvalue, datevalue, displayvalue));
        }
    }

    public void setFieldContext(String fieldContext) {
        this.fieldContext = fieldContext;
        this.edited = true;
    }

    public String getFieldContext() {
        return this.fieldContext == null ? "" : this.fieldContext;
    }

    public String getFieldValue(int instance) {
        if (this.instances.get(instance) != null) {
            String displayvalue = this.instances.get((Object)Integer.valueOf((int)instance)).displayvalue;
            String ret = displayvalue == null || displayvalue.length() > 0 ? displayvalue : this.instances.get((Object)Integer.valueOf((int)instance)).enteredtext;
            return ret == null ? "" : ret;
        }
        return "";
    }

    public boolean updateInstance(int instance, Calendar datetime) throws SapphireException {
        boolean edited = false;
        if (this.instances.containsKey(instance)) {
            if (datetime == null) {
                this.instances.put(instance, new FieldInstance(null, null, null, null));
                edited = true;
            } else {
                FieldInstance oldInstance = this.instances.get(instance);
                String datetimeText = this.m18n.format(datetime);
                FieldInstance newInstance = new FieldInstance(datetimeText, null, datetime, datetimeText);
                if (!newInstance.toString().equals(oldInstance.toString())) {
                    this.instances.put(instance, newInstance);
                    edited = true;
                }
            }
        }
        return edited;
    }

    public boolean updateInstance(int instance, String enteredtext) throws SapphireException {
        boolean edited = false;
        if (this.instances.containsKey(instance)) {
            if (enteredtext == null || enteredtext.length() == 0) {
                this.instances.put(instance, new FieldInstance(null, null, null, null));
                edited = true;
            } else {
                FieldInstance oldInstance = this.instances.get(instance);
                if (this.datatype == null || this.datatype.length() == 0) {
                    this.datatype = "string";
                    if (this.getNumericValue(enteredtext) != null) {
                        this.datatype = "number";
                    } else if (this.getDateValue(enteredtext) != null) {
                        this.datatype = "date";
                    }
                }
                if (this.datatype.equals("string")) {
                    FieldInstance newInstance = new FieldInstance(enteredtext, null, null, enteredtext);
                    if (!newInstance.toString().equals(oldInstance.toString())) {
                        this.instances.put(instance, newInstance);
                        edited = true;
                    }
                } else if (this.datatype.equals("number")) {
                    BigDecimal numericvalue = this.getNumericValue(enteredtext);
                    if (numericvalue == null) {
                        throw new SapphireException("Numeric value expected for field " + this.fieldid);
                    }
                    FieldInstance newInstance = new FieldInstance(enteredtext, numericvalue, null, this.m18n.format(numericvalue));
                    if (!newInstance.toString().equals(oldInstance.toString())) {
                        this.instances.put(instance, newInstance);
                        edited = true;
                    }
                } else if (this.datatype.equals("date") || this.datatype.equals("dateonly")) {
                    Calendar datevalue = this.getDateValue(enteredtext);
                    if (datevalue == null) {
                        throw new SapphireException("Date value expected for field " + this.fieldid);
                    }
                    FieldInstance newInstance = new FieldInstance(enteredtext, null, datevalue, this.m18n.format(datevalue));
                    if (!newInstance.toString().equals(oldInstance.toString())) {
                        this.instances.put(instance, newInstance);
                        edited = true;
                    }
                }
            }
        }
        return edited;
    }

    public boolean updateInstance(int instance, BigDecimal value) throws SapphireException {
        boolean edited = false;
        if (this.instances.containsKey(instance)) {
            if (value == null) {
                this.instances.put(instance, new FieldInstance(null, null, null, null));
                edited = true;
            } else {
                FieldInstance oldInstance = this.instances.get(instance);
                String text = this.m18n.format(value);
                FieldInstance newInstance = new FieldInstance(text, value, null, text);
                if (!newInstance.toString().equals(oldInstance.toString())) {
                    this.instances.put(instance, newInstance);
                    edited = true;
                }
            }
        }
        return edited;
    }

    private BigDecimal getNumericValue(String enteredtext) {
        try {
            BigDecimal numericvalue = this.m18n.parseBigDecimal(enteredtext);
            if (enteredtext.indexOf(this.formatUtil.getDecimalSeparator()) > 17 || enteredtext.length() > 18) {
                throw new NumberFormatException();
            }
            return numericvalue;
        }
        catch (Exception e) {
            return null;
        }
    }

    private Calendar getDateValue(String enteredtext) {
        try {
            Calendar datevalue = this.m18n.parseCalendar(enteredtext);
            if (datevalue == null || datevalue.get(1) > 3000) {
                throw new Exception();
            }
            return datevalue;
        }
        catch (Exception e) {
            return null;
        }
    }

    public class FieldInstance {
        public String enteredtext;
        public BigDecimal numericvalue;
        public final Calendar datevalue;
        public final String displayvalue;

        public FieldInstance(String enteredtext, BigDecimal numericvalue, Calendar datevalue, String displayvalue) {
            this.enteredtext = enteredtext;
            this.numericvalue = numericvalue;
            this.datevalue = datevalue;
            this.displayvalue = displayvalue;
        }

        public String toString() {
            return (this.enteredtext == null ? "" : this.enteredtext) + ";" + (this.numericvalue == null ? "" : this.numericvalue) + ";" + (this.datevalue == null ? "" : this.datevalue + ";" + this.displayvalue);
        }
    }
}

