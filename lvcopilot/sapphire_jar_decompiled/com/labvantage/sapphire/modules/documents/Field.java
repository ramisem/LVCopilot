/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.servlet.command.AttachmentRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Field
implements Serializable,
DocumentConstants {
    public static final String VALUE = "value";
    private PropertyList field = new PropertyList();
    private boolean repeatable = false;
    private String datatype;
    private M18NUtil m18n;
    private String connectionid;
    private String documentid;
    private String documentversionid;
    private boolean fileCreated;
    private File tempFile;

    public Field(PropertyList field, String enteredText, PropertyListCollection instances, M18NUtil m18n) {
        PropertyList instance = new PropertyList();
        instance.setProperty("fieldid", field.getProperty("fieldid"));
        instance.setProperty("fieldinstance", "0");
        instance.setProperty("enteredtext", enteredText);
        instance.setProperty("visible", "Y");
        instance.setProperty("readonly", "N");
        instances.add(instance);
        this.init(field, instances, "enteredtext", m18n, false);
    }

    public Field(PropertyList field, PropertyListCollection instances, M18NUtil m18n) {
        this.init(field, instances, "enteredtext", m18n, false);
    }

    public Field(PropertyList field, PropertyListCollection instances, String dataProperty, M18NUtil m18n) {
        this.init(field, instances, dataProperty, m18n, false);
    }

    public Field(PropertyList field, PropertyListCollection instances, String dataProperty, M18NUtil m18n, boolean resetFieldInstance) {
        this.init(field, instances, dataProperty, m18n, resetFieldInstance);
    }

    private void init(PropertyList field, PropertyListCollection instances, String dataProperty, M18NUtil m18n, boolean resetFieldInstance) {
        String[] cbxvalues;
        this.m18n = m18n;
        this.field.putAll(field.copy());
        this.datatype = Field.getDatatype(this.field);
        this.repeatable = this.field.getProperty("repeatable", "N").equals("Y");
        String type = field.getProperty("type");
        String[] stringArray = cbxvalues = type.equals("checkbox") ? StringUtil.split(field.getProperty("values"), ";") : null;
        if (this.repeatable) {
            ArrayList<Object> values = new ArrayList<Object>();
            ArrayList<String> fieldinstance = new ArrayList<String>();
            ArrayList<String> enteredtext = new ArrayList<String>();
            ArrayList<String> displayvalue = new ArrayList<String>();
            ArrayList<PropertyList> binding = new ArrayList<PropertyList>();
            ArrayList<String> visible = new ArrayList<String>();
            ArrayList<String> readonly = new ArrayList<String>();
            ArrayList<PropertyListCollection> reviewitems = new ArrayList<PropertyListCollection>();
            ArrayList<String> attachmentnum = new ArrayList<String>();
            ArrayList<String> checked = new ArrayList<String>();
            for (int i = 0; i < instances.size(); ++i) {
                PropertyList instance = instances.getPropertyList(i);
                Object value = Field.getValueObject(instance, dataProperty, this.datatype, m18n);
                values.add(value);
                fieldinstance.add(resetFieldInstance ? String.valueOf(i) : instance.getProperty("fieldinstance"));
                enteredtext.add(instance.getProperty(dataProperty));
                displayvalue.add(instance.getProperty("displayvalue"));
                if (type.equals("checkbox")) {
                    checked.add(cbxvalues.length >= 2 && cbxvalues[0].equals(enteredtext) ? "Y" : ("Y".equals(enteredtext) ? "Y" : "N"));
                }
                if (type.equals("file")) {
                    attachmentnum.add(instance.getProperty("attachmentnum"));
                }
                binding.add(this.getBindingMap(instance));
                visible.add(String.valueOf(instance.getProperty("state", "YN").charAt(0)));
                readonly.add(String.valueOf(instance.getProperty("state", "YN").charAt(1)));
                reviewitems.add(instance.getCollection("reviewitems"));
            }
            this.field.put(VALUE, values);
            this.field.put("fieldinstance", fieldinstance);
            this.field.put("enteredtext", enteredtext);
            this.field.put("displayvalue", displayvalue);
            if (type.equals("checkbox")) {
                this.field.put("checked", checked);
            }
            if (type.equals("file")) {
                this.field.put("attachmentnum", attachmentnum);
            }
            this.field.put("binding", binding);
            this.field.put("visible", visible);
            this.field.put("readonly", readonly);
            this.field.put("reviewitems", reviewitems);
        } else {
            PropertyList instance = instances.size() > 0 ? instances.getPropertyList(0) : new PropertyList();
            this.setNonRepeatingValue(instance, type, cbxvalues, dataProperty);
        }
    }

    public int getInstanceIndex(int instance) {
        ArrayList fieldinstances;
        if (this.repeatable && (fieldinstances = (ArrayList)this.field.get("fieldinstance")) != null && fieldinstances.size() > 0) {
            for (int i = 0; i < fieldinstances.size(); ++i) {
                String fieldinstance = (String)fieldinstances.get(i);
                if (fieldinstance == null || !fieldinstance.equals(String.valueOf(instance))) continue;
                return i;
            }
        }
        return -1;
    }

    void setValue(PropertyList instance) {
        String type = this.field.getProperty("type");
        if (this.repeatable) {
            String fieldinstance;
            int i;
            ArrayList values = (ArrayList)this.field.get(VALUE);
            ArrayList fieldinstances = (ArrayList)this.field.get("fieldinstance");
            ArrayList enteredtext = (ArrayList)this.field.get("enteredtext");
            ArrayList binding = (ArrayList)this.field.get("binding");
            ArrayList reviewitems = (ArrayList)this.field.get("reviewitems");
            ArrayList checked = (ArrayList)this.field.get("checked");
            for (i = 0; !(i >= fieldinstances.size() || (fieldinstance = (String)fieldinstances.get(i)) != null && fieldinstance.equals(String.valueOf(instance.getProperty("fieldinstance")))); ++i) {
            }
            if (i < fieldinstances.size()) {
                values.set(i, Field.getValueObject(instance, "enteredtext", this.datatype, this.m18n));
                enteredtext.set(i, instance.getProperty("enteredtext"));
                binding.set(i, instance.getPropertyList("binding"));
            } else {
                fieldinstances.add(String.valueOf(i));
                values.add(Field.getValueObject(instance, "enteredtext", this.datatype, this.m18n));
                enteredtext.add(instance.getProperty("enteredtext"));
                binding.add(instance.getPropertyList("binding"));
            }
        } else {
            this.setNonRepeatingValue(instance, type, type.equals("checkbox") ? StringUtil.split(this.field.getProperty("values"), ";") : null, "enteredtext");
        }
    }

    private void setNonRepeatingValue(PropertyList instance, String type, String[] cbxvalues, String dataProperty) {
        this.field.put(VALUE, Field.getValueObject(instance, dataProperty, this.datatype, this.m18n));
        this.field.put("fieldinstance", "0");
        String enteredtext = instance.getProperty(dataProperty);
        this.field.put("enteredtext", enteredtext);
        this.field.put("displayvalue", instance.getProperty("displayvalue"));
        if (type.equals("checkbox")) {
            this.field.put("checked", cbxvalues.length >= 2 && cbxvalues[0].equals(enteredtext) ? "Y" : ("Y".equals(enteredtext) ? "Y" : "N"));
        }
        if (type.equals("file")) {
            this.field.put("attachmentnum", instance.getProperty("attachmentnum"));
        }
        this.field.put("binding", this.getBindingMap(instance));
        this.field.put("visible", String.valueOf(instance.getProperty("state", "YN").charAt(0)));
        this.field.put("readonly", String.valueOf(instance.getProperty("state", "YN").charAt(1)));
        this.field.put("reviewitems", instance.getCollection("reviewitems"));
    }

    public Object getValue() {
        return this.field.get(VALUE);
    }

    public ArrayList getValueList() {
        return (ArrayList)this.field.get(VALUE);
    }

    public ArrayList getList(String propertyid) {
        return this.isRepeatable() ? (ArrayList)this.get(propertyid) : null;
    }

    public File getFile() {
        if (this.isFile() && !this.isRepeatable()) {
            try {
                AttachmentProcessor arp = new AttachmentProcessor(this.connectionid);
                Attachment attachment = arp.getSDIAttachment("LV_Document", this.documentid, this.documentversionid, "(null)", Integer.parseInt(this.field.getProperty("attachmentnum")));
                this.tempFile = File.createTempFile("documentfield_", "");
                InputStream in = attachment.getInputStream();
                AttachmentRequest.streamAttachment(in, new FileOutputStream(this.tempFile));
                this.fileCreated = true;
                return this.tempFile;
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public boolean isFileCreated() {
        return this.fileCreated;
    }

    void deleteTempFile() {
        if (this.tempFile != null && this.tempFile.exists()) {
            this.tempFile.delete();
        }
    }

    public String toString() {
        Object valueObject = this.field.get(VALUE);
        if (valueObject != null) {
            if (this.repeatable) {
                StringBuffer stringValue = new StringBuffer();
                ArrayList values = (ArrayList)valueObject;
                ArrayList fieldinstance = (ArrayList)this.field.get("fieldinstance");
                if (values != null) {
                    for (int i = 0; i < values.size(); ++i) {
                        Object value = values.get(i);
                        if (value != null) {
                            if (this.datatype.equals("date")) {
                                stringValue.append(this.getProperty("separator", ";")).append(this.m18n.format((Calendar)value));
                                continue;
                            }
                            if (this.datatype.equals("dateonly")) {
                                stringValue.append(this.getProperty("separator", ";")).append(this.m18n.formatDateOnly((Calendar)value, false));
                                continue;
                            }
                            if (this.datatype.equals("number")) {
                                stringValue.append(this.getProperty("separator", ";")).append(this.m18n.format((BigDecimal)value));
                                continue;
                            }
                            if (this.datatype.equals("file")) {
                                stringValue.append(this.getProperty("separator", ";")).append(this.documentid + "|" + this.documentversionid + "|" + this.field.getProperty("fieldid") + "|" + fieldinstance.get(i));
                                continue;
                            }
                            stringValue.append(this.getProperty("separator", ";")).append((String)value);
                            continue;
                        }
                        stringValue.append(this.getProperty("separator", ";"));
                    }
                }
                return stringValue.length() > 0 ? stringValue.substring(1) : "";
            }
            if (this.datatype.equals("date")) {
                return this.m18n.format((Calendar)valueObject);
            }
            if (this.datatype.equals("dateonly")) {
                return this.m18n.formatDateOnly((Calendar)valueObject, false);
            }
            if (this.datatype.equals("number")) {
                return this.m18n.format((BigDecimal)valueObject);
            }
            if (this.datatype.equals("file")) {
                return this.documentid + "|" + this.documentversionid + "|" + this.field.getProperty("fieldid") + "|" + this.field.getProperty("fieldinstance");
            }
            return (String)valueObject;
        }
        return "null";
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public int getRepeats() {
        return this.isRepeatable() ? this.getValueList().size() : 1;
    }

    public boolean exists() {
        Object value = this.field.get(VALUE);
        return value != null && (!(value instanceof String) || ((String)value).length() > 0);
    }

    public boolean equals(Object compare) {
        Object value = this.field.get(VALUE);
        return value != null && value instanceof String ? ((String)value).equals(compare) : this.equals(compare);
    }

    public int length() {
        Object value = this.field.get(VALUE);
        return value != null && value instanceof String ? ((String)value).length() : -1;
    }

    public boolean isNumber() {
        return this.field.getProperty("datatype").equals("number");
    }

    public boolean isFile() {
        return this.field.getProperty("type").equals("file");
    }

    public boolean isDate() {
        return this.field.getProperty("datatype", this.field.getProperty("type").equals("date") ? "date" : "string").equals("date");
    }

    public static String getDatatype(PropertyList field) {
        return field.getProperty("datatype", field.getProperty("type").equals("date") ? "date" : (field.getProperty("type").equals("file") ? "file" : "string"));
    }

    public static Object getValueObject(String textValue, String datatype, M18NUtil m18n) {
        PropertyList fieldValueInstance = new PropertyList();
        fieldValueInstance.setProperty("enteredtext", textValue);
        return Field.getValueObject(fieldValueInstance, "enteredtext", datatype, m18n);
    }

    public static Object getValueObject(PropertyList fieldValueInstance, String textProperty, String datatype, M18NUtil m18n) {
        String textValue = fieldValueInstance.getProperty(textProperty);
        if (datatype.equals("date")) {
            if (textValue.length() > 0) {
                Calendar cal = m18n.parseCalendar(textValue);
                if (cal != null) {
                    fieldValueInstance.setProperty(textProperty, m18n.format(cal));
                }
                return cal;
            }
        } else if (datatype.equals("dateonly")) {
            if (textValue.length() > 0) {
                Calendar cal = m18n.parseCalendar(textValue, false);
                if (cal != null) {
                    if (cal.get(11) == 0 && cal.get(12) == 0 && cal.get(13) == 0 && cal.get(14) == 0) {
                        fieldValueInstance.setProperty(textProperty, m18n.formatDateOnly(cal, false));
                    } else {
                        cal = null;
                    }
                }
                return cal;
            }
        } else if (datatype.equals("number")) {
            if (textValue.length() > 0) {
                try {
                    BigDecimal num = m18n.parseBigDecimal(textValue);
                    fieldValueInstance.setProperty(textProperty, m18n.format(num));
                    return num;
                }
                catch (NumberFormatException nfe) {
                    return null;
                }
            }
        } else if (datatype.equals("file")) {
            if (textValue.length() > 0) {
                return "New File";
            }
        } else {
            return textValue;
        }
        return null;
    }

    public String getProperty(String propertyid) {
        return this.field.getProperty(propertyid);
    }

    public String getProperty(String propertyid, String defaultValue) {
        return this.field.getProperty(propertyid, defaultValue);
    }

    public Object get(String propertyid) {
        return this.field.get(propertyid);
    }

    public PropertyList getPropertyList(String propertyid) {
        return this.field.getPropertyList(propertyid);
    }

    public PropertyListCollection getAttributes() {
        return this.field.getCollection("attributes");
    }

    void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    void setDocumentid(String documentid, String documentversionid) {
        this.documentid = documentid;
        this.documentversionid = documentversionid;
    }

    public PropertyList getBindingMap(PropertyList instance) {
        if (instance.containsKey("binding")) {
            return instance.getPropertyList("binding");
        }
        if (instance.containsKey("c_binding")) {
            String[] parts = StringUtil.split(instance.getProperty("c_binding"), "|");
            PropertyList binding = new PropertyList();
            binding.setProperty("sdcid", parts[0]);
            binding.setProperty("keyid1", parts[1]);
            binding.setProperty("keyid2", parts[2]);
            binding.setProperty("keyid3", parts[3]);
            if (parts[4].length() > 0) {
                binding.setProperty("paramlistid", parts[4]);
                binding.setProperty("paramlistversionid", parts[5]);
                binding.setProperty("variantid", parts[6]);
                binding.setProperty("dataset", parts[7]);
            }
            if (parts[8].length() > 0) {
                binding.setProperty("paramid", parts[8]);
                binding.setProperty("paramtype", parts[9]);
                binding.setProperty("replicateid", parts[10]);
                binding.setProperty("dataentrytype", parts[14]);
                binding.setProperty("mandatory", parts[17]);
                binding.setProperty("calcexclude", parts[15]);
                binding.setProperty("maxlength", parts[18]);
                binding.setProperty("datareleased", parts[16]);
                binding.setProperty("entrysdcid", parts[20]);
                binding.setProperty("entryreftypeid", parts[19]);
            }
            if (parts[11].length() > 0) {
                binding.setProperty("sdidataitemid", parts[11]);
            }
            if (parts[24].length() > 0) {
                binding.setProperty("sdidataid", parts[24]);
            }
            if (parts[12].length() > 0) {
                binding.setProperty("instrumentid", parts[12]);
            }
            if (parts[13].length() > 0) {
                binding.setProperty("instrumentfieldid", parts[13]);
            }
            if (parts[25].length() > 0) {
                binding.setProperty("columnid", parts[25]);
            }
            if (parts[21].length() > 0) {
                binding.setProperty("relationid", parts[21]);
                binding.setProperty("reagenttypeid", parts[22]);
                binding.setProperty("reagentcomponent", parts[23]);
            }
            if (parts[26].length() > 0) {
                binding.setProperty("binds", parts[26]);
            }
            if (parts[27].length() > 0) {
                binding.setProperty("speccondition", parts[27]);
            }
            return binding;
        }
        return null;
    }
}

