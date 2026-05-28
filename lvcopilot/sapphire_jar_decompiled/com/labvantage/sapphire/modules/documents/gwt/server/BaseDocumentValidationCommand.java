/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.FormGroup;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseDocumentValidationCommand
extends BaseDocumentCommand {
    public BaseDocumentValidationCommand(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    protected boolean inspectGroups(Form form, HashMap fieldMap, PropertyListCollection groupValues) throws SapphireException {
        boolean validationErrors = false;
        PropertyListCollection groups = form.getGroups();
        if (groups != null) {
            for (int i = 0; i < groups.size(); ++i) {
                String enabled;
                PropertyList group = groups.getPropertyList(i);
                String groupid = group.getProperty("groupid");
                FormGroup formGroup = new FormGroup(group);
                String value = "";
                PropertyListCollection reviewItems = new PropertyListCollection();
                PropertyListCollection members = formGroup.getMembers();
                HashMap groupMap = new HashMap();
                ArrayList memberList = new ArrayList();
                for (int j = 0; j < members.size(); ++j) {
                    String fieldid = members.getPropertyList(j).getProperty("fieldid");
                    groupMap.put(fieldid, fieldMap.get(fieldid));
                    memberList.add(fieldMap.get(fieldid));
                }
                formGroup.setMembers(memberList);
                boolean visible = true;
                boolean readonly = false;
                HashMap<String, Serializable> bindings = new HashMap<String, Serializable>();
                bindings.put("fields", fieldMap);
                bindings.put("groupfields", groupMap);
                bindings.put("group", formGroup);
                bindings.put("members", memberList);
                value = this.evalGroovyExpression(groupid + " valuerule", bindings, group.getProperty("valuerule"), true, false, false, true, true, false);
                PropertyListCollection validation = group.getCollection("validation");
                if (validation != null) {
                    HashMap<String, HashMap> valuebindings = new HashMap<String, HashMap>();
                    valuebindings.put("fields", fieldMap);
                    for (int j = 0; j < validation.size(); ++j) {
                        PropertyList validationitem = validation.getPropertyList(j);
                        String enabled2 = this.evalGroovyExpression(groupid, bindings, validationitem.getProperty("enabled", "Y"), false);
                        if (!enabled2.equals("Y") && !enabled2.equals("true") && !enabled2.equals("1") && (!enabled2.equals("V") || !visible) && (!enabled2.equals("E") || readonly)) continue;
                        validationitem.setProperty("groupid", groupid);
                        validationitem.setProperty("title", group.getProperty("title", groupid));
                        validationitem.setProperty("groupvalue", value);
                        String operation = validationitem.getProperty("operation");
                        String operator = validationitem.getProperty("operator");
                        String value1 = validationitem.getProperty("value1");
                        String value2 = validationitem.getProperty("value2");
                        value1 = this.evalGroovyExpression(groupid + " validation - value1", valuebindings, value1, false);
                        value2 = this.evalGroovyExpression(groupid + " validation - value2", valuebindings, value2, false);
                        int count = 0;
                        boolean gv = false;
                        if ("groupvalue".equalsIgnoreCase(operation)) {
                            gv = true;
                        } else {
                            for (String fieldid : groupMap.keySet()) {
                                Object checked;
                                int k;
                                Field formField = (Field)groupMap.get(fieldid);
                                if ("enteredcount".equalsIgnoreCase(operation)) {
                                    Object entered = formField.get("enteredtext");
                                    if (entered == null) continue;
                                    if (entered instanceof String) {
                                        if (((String)entered).length() <= 0) continue;
                                        ++count;
                                        continue;
                                    }
                                    ArrayList enteredvalues = (ArrayList)entered;
                                    for (k = 0; k < enteredvalues.size(); ++k) {
                                        if (((String)enteredvalues.get(k)).length() <= 0) continue;
                                        ++count;
                                    }
                                    continue;
                                }
                                if (!"checkedcount".equalsIgnoreCase(operation) || (checked = formField.get("checked")) == null) continue;
                                if (checked instanceof String) {
                                    if (!((String)checked).equals("Y")) continue;
                                    ++count;
                                    continue;
                                }
                                ArrayList checkvalues = (ArrayList)checked;
                                for (k = 0; k < checkvalues.size(); ++k) {
                                    if (!((String)checkvalues.get(k)).equals("Y")) continue;
                                    ++count;
                                }
                            }
                        }
                        if ("EQ".equalsIgnoreCase(operator) && value1.length() > 0 && (gv ? !value.equals(value1) : Integer.parseInt(value1) != count)) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("NE".equalsIgnoreCase(operator) && value1.length() > 0 && (gv ? value.equals(value1) : Integer.parseInt(value1) == count)) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("GT".equalsIgnoreCase(operator) && value1.length() > 0) {
                            if (gv && value.compareTo(value1) >= 0) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if (gv || Integer.parseInt(value1) < count) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("GE".equalsIgnoreCase(operator) && value1.length() > 0) {
                            if (gv && value.compareTo(value1) > 0) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if (gv || Integer.parseInt(value1) <= count) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("LT".equalsIgnoreCase(operator) && value1.length() > 0) {
                            if (gv && value.compareTo(value1) <= 0) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if (gv || Integer.parseInt(value1) > count) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("LE".equalsIgnoreCase(operator) && value1.length() > 0) {
                            if (gv && value.compareTo(value1) < 0) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if (gv || Integer.parseInt(value1) >= count) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("C".equalsIgnoreCase(operator) || "CONTAINS".equalsIgnoreCase(operator)) {
                            StringBuffer text = new StringBuffer(value1);
                            validationErrors = this.validateList(value, "IN", text, reviewItems, validationitem);
                            continue;
                        }
                        if ("IB".equalsIgnoreCase(operator) && value1.length() > 0 && value2.length() > 0 && (Integer.parseInt(value1) > count || Integer.parseInt(value2) < count)) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("EB".equalsIgnoreCase(operator) && value1.length() > 0 && value2.length() > 0 && (Integer.parseInt(value1) >= count || Integer.parseInt(value2) <= count)) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("IO".equalsIgnoreCase(operator) && value1.length() > 0 && value2.length() > 0 && Integer.parseInt(value1) < count && Integer.parseInt(value2) > count) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if (!"EO".equalsIgnoreCase(operator) || value1.length() <= 0 || value2.length() <= 0 || Integer.parseInt(value1) > count || Integer.parseInt(value2) < count) continue;
                        validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                    }
                }
                HashMap<String, Object> bindingMap = new HashMap<String, Object>();
                bindingMap.put("fields", fieldMap);
                bindingMap.put("groupfields", groupMap);
                bindingMap.put("members", memberList);
                bindingMap.put("group", formGroup);
                formGroup.setValue(value);
                PropertyList complexValidation = group.getPropertyList("complexvalidation");
                if (complexValidation != null && ((enabled = this.evalGroovyExpression(groupid, bindings, complexValidation.getProperty("enabled", "Y"), false)).equals("Y") || enabled.equals("true") || enabled.equals("1") || enabled.equals("V") && visible || enabled.equals("E") && !readonly)) {
                    complexValidation.setProperty("groupid", groupid);
                    complexValidation.setProperty("title", group.getProperty("title", groupid));
                    complexValidation.setProperty("groupvalue", value);
                    if (this.evalComplexValidation(groupid, complexValidation, bindingMap, reviewItems)) {
                        validationErrors = true;
                    }
                }
                PropertyList groupValue = new PropertyList();
                groupValue.setProperty("groupid", groupid);
                groupValue.setProperty("objecttype", "group");
                groupValue.setProperty("reviewitems", reviewItems);
                groupValue.setProperty("value", value);
                groupValues.add(groupValue);
            }
        }
        return validationErrors;
    }

    protected boolean inspectFields(Form form, HashMap fieldMap, String operation, boolean validate, boolean forceValidate, boolean completePartialEntries, boolean complexValidation, PropertyListCollection fieldValues) {
        boolean validationErrors = false;
        SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        QueryProcessor queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        PropertyListCollection fields = form.getFields();
        for (int i = 0; i < fields.size(); ++i) {
            PropertyList field = fields.getPropertyList(i);
            String fieldid = field.getProperty("fieldid");
            Field formField = (Field)fieldMap.get(fieldid);
            int repeats = formField.getRepeats();
            PropertyListCollection instances = new PropertyListCollection();
            for (int repeat = 0; repeat < repeats; ++repeat) {
                PropertyListCollection submittedReviewItems;
                PropertyListCollection reviewItems = new PropertyListCollection();
                boolean validateField = validate;
                PropertyListCollection propertyListCollection = submittedReviewItems = formField.isRepeatable() ? (PropertyListCollection)formField.getList("reviewitems").get(repeat) : (PropertyListCollection)formField.get("reviewitems");
                if (submittedReviewItems != null) {
                    int validationItems = 0;
                    int ackValidationItems = 0;
                    boolean hasMandatory = false;
                    for (int j = 0; j < submittedReviewItems.size(); ++j) {
                        PropertyList reviewItem = submittedReviewItems.getPropertyList(j);
                        String reviewitemtype = reviewItem.getProperty("reviewitemtype");
                        if (reviewitemtype.equals("Y")) {
                            hasMandatory = true;
                            continue;
                        }
                        if (reviewitemtype.equals("V")) {
                            ++validationItems;
                            if (reviewItem.getProperty("reviewitemstatus", "F").equals("A")) {
                                ++ackValidationItems;
                                reviewItems.add(reviewItem);
                                continue;
                            }
                            fieldMap.put("__unacknowledgederrors", "Y");
                            continue;
                        }
                        reviewItems.add(reviewItem);
                    }
                    if (submittedReviewItems.size() > 0 && validationItems == ackValidationItems && !forceValidate && !hasMandatory) {
                        validateField = false;
                    }
                }
                PropertyList instance = new PropertyList();
                instance.setProperty("fieldid", fieldid);
                instance.setProperty("fieldinstance", formField.isRepeatable() ? (String)formField.getList("fieldinstance").get(repeat) : (String)formField.get("fieldinstance"));
                instance.setProperty("reviewitems", reviewItems);
                boolean validationError = false;
                validationError = this.inspectField(form, field, operation, validateField, completePartialEntries, complexValidation, false, false, instance, fieldMap, sdcProcessor, queryProcessor);
                if (validationError) {
                    validationErrors = true;
                }
                instances.add(instance);
            }
            PropertyList fieldValue = new PropertyList();
            fieldValue.setProperty("fieldid", fieldid);
            fieldValue.setProperty("instances", instances);
            fieldValue.setProperty("processingfield", field.getProperty("processingfield", form.isDefaultprocessingfields() ? "Y" : "N"));
            fieldValues.add(fieldValue);
        }
        return validationErrors;
    }

    public boolean inspectField(Form form, PropertyList field, String operation, boolean validate, boolean completePartialEntries, boolean complexValidate, boolean valueChanged, boolean multiSampleCalcs, PropertyList fieldValue, HashMap fieldMap, SDCProcessor sdcProcessor, QueryProcessor queryProcessor) {
        boolean validationErrors = false;
        try {
            String displayvalue;
            String fieldid = field.getProperty("fieldid");
            String sectionid = field.getProperty("sectionid");
            String datatype = Field.getDatatype(field);
            boolean repeatable = field.getProperty("repeatable", "N").equals("Y");
            int instance = Integer.parseInt(fieldValue.getProperty("fieldinstance"));
            FormatUtil formatUtil = FormatUtil.getInstance(this.sapphireConnection);
            Field formField = (Field)fieldMap.get(fieldid);
            int index = formField.getInstanceIndex(instance);
            String enteredtext = repeatable ? (String)formField.getList("enteredtext").get(index) : (String)formField.get("enteredtext");
            String string = displayvalue = repeatable ? (String)formField.getList("displayvalue").get(index) : (String)formField.get("displayvalue");
            if (displayvalue == null || displayvalue.length() == 0) {
                displayvalue = enteredtext;
            }
            boolean visible = (repeatable ? (String)formField.getList("visible").get(index) : (String)formField.get("visible")).equals("Y");
            boolean readonly = (repeatable ? (String)formField.getList("readonly").get(index) : (String)formField.get("readonly")).equals("Y");
            Object valueObject = repeatable ? ((ArrayList)formField.getValue()).get(index) : formField.getValue();
            PropertyList binding = repeatable ? (PropertyList)formField.getList("binding").get(index) : (PropertyList)formField.get("binding");
            PropertyListCollection reviewItems = fieldValue.getCollection("reviewitems");
            if (reviewItems == null) {
                reviewItems = new PropertyListCollection();
            }
            HashMap bindings = ProcessingUtil.createBindingsMap(this.sapphireConnection, "INSPECTFIELD");
            bindings.put("fields", fieldMap);
            if (sectionid.length() > 0) {
                bindings.put("fieldinstance", (HashMap)fieldMap.get(sectionid + "_" + instance));
            }
            if (validate && !operation.equalsIgnoreCase("pending")) {
                boolean mandatory;
                if (datatype.equals("date") && enteredtext.length() > 0 && valueObject == null) {
                    validationErrors = this.addValidationError(reviewItems, "Invalid date format!", new PropertyList());
                } else if (datatype.equals("dateonly") && enteredtext.length() > 0 && valueObject == null) {
                    validationErrors = this.addValidationError(reviewItems, "Invalid date only format!", new PropertyList());
                } else if (datatype.equals("number") && enteredtext.length() > 0 && valueObject == null) {
                    validationErrors = this.addValidationError(reviewItems, "Invalid number format!", new PropertyList());
                }
                String mandatoryValue = this.evalGroovyExpression(fieldid, bindings, field.getProperty("mandatory", "N"), false);
                boolean bl = mandatory = binding != null && binding.getProperty("mandatory").equals("Y") || mandatoryValue.equalsIgnoreCase("V") && visible || mandatoryValue.equalsIgnoreCase("E") && !readonly || mandatoryValue.equalsIgnoreCase("Y") || mandatoryValue.equalsIgnoreCase("true") || mandatoryValue.equals("1");
                if (!validationErrors && mandatory && enteredtext.length() == 0) {
                    validationErrors = this.addMandatoryError(reviewItems, field.getProperty("identifyfield", "N").equals("Y") ? "Mandatory identity field not entered!" : (field.getProperty("processingfield", form.isDefaultprocessingfields() ? "Y" : "N").equals("Y") && !form.isApprovable() && !form.isDde() ? "Mandatory processing field not entered!" : "Mandatory field not entered!"));
                }
                if (enteredtext.length() > 0 && field.getProperty("autocheck", "N").equals("Y")) {
                    String sdcid = field.getProperty("sdcid");
                    if (sdcid.length() > 0) {
                        StringBuffer text = new StringBuffer(enteredtext);
                        validationErrors = this.validateSdi(sdcProcessor, queryProcessor, sdcid, "IN", text, field.getCollection("keyset"), fieldMap, reviewItems, null, completePartialEntries);
                        enteredtext = text.toString();
                    } else {
                        String reftypeid = field.getProperty("reftypeid");
                        if (reftypeid.length() > 0) {
                            StringBuffer text = new StringBuffer(enteredtext);
                            validationErrors = this.validateReftype(queryProcessor, reftypeid, "IN", text, reviewItems, null, completePartialEntries);
                            enteredtext = text.toString();
                        } else {
                            String values = field.getProperty("values");
                            if (values.length() > 0) {
                                StringBuffer text = new StringBuffer(enteredtext);
                                validationErrors = this.validateList(values, "IN", text, reviewItems, null);
                                enteredtext = text.toString();
                            }
                        }
                    }
                }
            }
            if (validate && !validationErrors) {
                String enabled;
                PropertyList complexValidation;
                PropertyListCollection validation = field.getCollection("validation");
                if (validation != null && validation.size() > 0) {
                    for (int i = 0; i < validation.size(); ++i) {
                        PropertyList validationitem = validation.getPropertyList(i);
                        String enabled2 = this.evalGroovyExpression(fieldid, bindings, validationitem.getProperty("enabled", "Y"), false);
                        if (!enabled2.equals("Y") && !enabled2.equals("true") && !enabled2.equals("1") && (!enabled2.equals("V") || !visible) && (!enabled2.equals("E") || readonly)) continue;
                        validationitem.setProperty("fieldid", fieldid);
                        validationitem.setProperty("title", field.getProperty("title", fieldid));
                        validationitem.setProperty("enteredtext", enteredtext);
                        String vioperation = validationitem.getProperty("operation");
                        String operator = validationitem.getProperty("operator");
                        String value1 = validationitem.getProperty("value1");
                        String value2 = validationitem.getProperty("value2");
                        value1 = this.evalGroovyExpression(fieldid + " validation - value1", bindings, value1, false);
                        value2 = this.evalGroovyExpression(fieldid + " validation - value2", bindings, value2, false);
                        validationitem.setProperty("value1_value", value1);
                        validationitem.setProperty("value2_value", value2);
                        if ("length".equalsIgnoreCase(vioperation)) {
                            if (value1.toLowerCase().startsWith("ddt:") || value2.toLowerCase().startsWith("ddt:")) {
                                try {
                                    PropertyList column;
                                    PropertyListCollection columns;
                                    DDTService ddtService = new DDTService(this.sapphireConnection);
                                    if (value1.toLowerCase().startsWith("ddt:")) {
                                        columns = ddtService.getTableColumns(value1.substring(4, value1.indexOf(".")).trim());
                                        column = columns.getPropertyList(value1.substring(value1.indexOf(".") + 1).trim());
                                        String string2 = value1 = column != null ? column.getProperty("columnlength") : "";
                                    }
                                    if (value2.toLowerCase().startsWith("ddt:")) {
                                        columns = ddtService.getTableColumns(value2.substring(4, value2.indexOf(".")).trim());
                                        column = columns.getPropertyList(value2.substring(value2.indexOf(".") + 1).trim());
                                        value2 = column != null ? column.getProperty("columnlength") : "";
                                    }
                                }
                                catch (ServiceException e) {
                                    this.logger.error("Invalid ddt expression in value1 or value2!", e);
                                }
                            }
                            if ("EQ".equalsIgnoreCase(operator) && enteredtext.length() != Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("NE".equalsIgnoreCase(operator) && enteredtext.length() == Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("GT".equalsIgnoreCase(operator) && enteredtext.length() <= Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("GE".equalsIgnoreCase(operator) && enteredtext.length() < Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("LT".equalsIgnoreCase(operator) && enteredtext.length() >= Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("LE".equalsIgnoreCase(operator) && enteredtext.length() > Integer.parseInt(value1)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("IB".equalsIgnoreCase(operator) && (enteredtext.length() < Integer.parseInt(value1) || enteredtext.length() > Integer.parseInt(value2))) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("EB".equalsIgnoreCase(operator) && (enteredtext.length() <= Integer.parseInt(value1) || enteredtext.length() >= Integer.parseInt(value2))) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("IO".equalsIgnoreCase(operator) && enteredtext.length() > Integer.parseInt(value1) && enteredtext.length() < Integer.parseInt(value2)) {
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if (!"EO".equalsIgnoreCase(operator) || enteredtext.length() < Integer.parseInt(value1) || enteredtext.length() > Integer.parseInt(value2)) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if ("value".equalsIgnoreCase(vioperation)) {
                            BigDecimal numValue2;
                            BigDecimal numValue;
                            if ("IN".equalsIgnoreCase(operator) || "NI".equalsIgnoreCase(operator)) {
                                StringBuffer text;
                                if (enteredtext.length() <= 0) continue;
                                if (value1.toLowerCase().startsWith("list:")) {
                                    text = new StringBuffer(enteredtext);
                                    validationErrors = this.validateList(value1.substring(5).trim(), operator, text, reviewItems, validationitem);
                                    enteredtext = text.toString();
                                    continue;
                                }
                                if (value1.toLowerCase().startsWith("reftype:")) {
                                    text = new StringBuffer(enteredtext);
                                    validationErrors = this.validateReftype(queryProcessor, value1.substring(8).trim(), operator, text, reviewItems, validationitem, completePartialEntries);
                                    enteredtext = text.toString();
                                    continue;
                                }
                                if (value1.toLowerCase().startsWith("sdc:")) {
                                    text = new StringBuffer(enteredtext);
                                    validationErrors = this.validateSdi(sdcProcessor, queryProcessor, value1.substring(4).trim(), operator, text, field.getCollection("keyset"), fieldMap, reviewItems, validationitem, completePartialEntries);
                                    enteredtext = text.toString();
                                    continue;
                                }
                                if (value1.toLowerCase().startsWith("sql:")) {
                                    String sql = value1.substring(4).trim();
                                    DataSet results = queryProcessor.getSqlDataSet("SELECT 1 from sysconfig WHERE sysconfig.propertyid = 'build' AND '" + enteredtext + "' " + ("NI".equalsIgnoreCase(operator) ? "NOT" : "") + " IN ( " + sql + ")");
                                    if (results != null && results.size() != 0) continue;
                                    validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                    continue;
                                }
                                if (!value1.toLowerCase().startsWith("regex:")) continue;
                                String pattern = value1.substring(6).trim();
                                boolean match = enteredtext.matches(pattern);
                                if ("IN".equalsIgnoreCase(operator) && !match) {
                                    validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                    continue;
                                }
                                if (!"NI".equalsIgnoreCase(operator) || !match) continue;
                                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                                continue;
                            }
                            if ("C".equalsIgnoreCase(operator) || "CONTAINS".equalsIgnoreCase(operator) || valueObject == null) continue;
                            if (valueObject instanceof BigDecimal) {
                                numValue = (BigDecimal)valueObject;
                                BigDecimal numValue1 = value1.length() > 0 ? formatUtil.parseBigDecimal(value1) : null;
                                numValue2 = value2.length() > 0 ? formatUtil.parseBigDecimal(value2) : null;
                                validationErrors = this.validateCompare(numValue, operator, numValue1, numValue2, validationErrors, reviewItems, validationitem);
                                continue;
                            }
                            if (valueObject instanceof String) {
                                try {
                                    numValue = formatUtil.parseBigDecimal((String)valueObject);
                                    BigDecimal numValue1 = value1.length() > 0 ? formatUtil.parseBigDecimal(value1) : null;
                                    numValue2 = value2.length() > 0 ? formatUtil.parseBigDecimal(value2) : null;
                                    validationErrors = this.validateCompare(numValue, operator, numValue1, numValue2, validationErrors, reviewItems, validationitem);
                                }
                                catch (Exception e) {
                                    String strValue1 = value1.length() > 0 ? value1 : null;
                                    String strValue2 = value1.length() > 0 ? value2 : null;
                                    validationErrors = this.validateCompare(valueObject, operator, strValue1, strValue2, validationErrors, reviewItems, validationitem);
                                }
                                continue;
                            }
                            if (!(valueObject instanceof Calendar)) continue;
                            M18NUtil m18n = new M18NUtil(this.sapphireConnection);
                            Calendar calValue = (Calendar)valueObject;
                            Calendar calValue1 = value1.length() > 0 ? m18n.parseCalendar(value1) : null;
                            Calendar calValue2 = value2.length() > 0 ? m18n.parseCalendar(value2) : null;
                            validationErrors = this.validateCompare(calValue, operator, calValue1, calValue2, validationErrors, reviewItems, validationitem);
                            continue;
                        }
                        if ("groovy".equalsIgnoreCase(vioperation)) {
                            if (!"VI".equalsIgnoreCase(operator) || !value1.equals("false")) continue;
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                            continue;
                        }
                        if (!"integer".equalsIgnoreCase(vioperation)) continue;
                        try {
                            Integer.parseInt(enteredtext);
                            continue;
                        }
                        catch (NumberFormatException e) {
                            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
                        }
                    }
                }
                if (complexValidate && (complexValidation = field.getPropertyList("complexvalidation")) != null && ((enabled = this.evalGroovyExpression(fieldid, bindings, complexValidation.getProperty("enabled", "Y"), false)).equals("Y") || enabled.equals("true") || enabled.equals("1") || enabled.equals("V") && visible || enabled.equals("E") && !readonly)) {
                    complexValidation.setProperty("fieldid", fieldid);
                    complexValidation.setProperty("title", field.getProperty("title", fieldid));
                    if (this.evalComplexValidation(fieldid, complexValidation, bindings, reviewItems)) {
                        validationErrors = true;
                    }
                }
            }
            if (valueChanged && binding != null && (field.getProperty("autosave").equals("dataentry") || binding.getProperty("reagentcomponent").equals("lot"))) {
                boolean hasDataEntryFields = false;
                DataSet reagentOverrides = null;
                HashMap<String, String> deProps = new HashMap<String, String>();
                deProps.put("islivelimitchecking", "Y");
                deProps.put("sdcid", binding.getProperty("sdcid"));
                if (field.getProperty("autosave").equals("dataentry")) {
                    deProps.put("keyid1", binding.getProperty("keyid1"));
                    deProps.put("keyid2", binding.getProperty("keyid2"));
                    deProps.put("keyid3", binding.getProperty("keyid3"));
                    deProps.put("paramlistid", binding.getProperty("paramlistid"));
                    deProps.put("paramlistversionid", binding.getProperty("paramlistversionid"));
                    deProps.put("variantid", binding.getProperty("variantid"));
                    deProps.put("dataset", binding.getProperty("dataset"));
                    deProps.put("paramid", binding.getProperty("paramid"));
                    deProps.put("paramtype", binding.getProperty("paramtype"));
                    deProps.put("replicateid", binding.getProperty("replicateid"));
                    deProps.put("enteredtext", enteredtext);
                    deProps.put("calcexcludeflag", binding.getProperty("calcexclude"));
                    hasDataEntryFields = true;
                } else {
                    reagentOverrides = this.addReagentOverride(reagentOverrides, binding, enteredtext);
                }
                HashMap<String, Serializable> deMap = new HashMap<String, Serializable>();
                String fieldsdidataitemid = binding.getProperty("sdidataitemid");
                deMap.put(fieldsdidataitemid, formField);
                Iterator it = fieldMap.keySet().iterator();
                while (it.hasNext()) {
                    Object fieldMapEntry = fieldMap.get(it.next());
                    if (!(fieldMapEntry instanceof Field)) continue;
                    Field otherField = (Field)fieldMapEntry;
                    if (otherField.isRepeatable()) {
                        ArrayList otherBindings = otherField.getList("binding");
                        ArrayList otherFieldInstances = otherField.getList("fieldinstance");
                        for (int i = 0; i < otherBindings.size(); ++i) {
                            PropertyList otherBinding = (PropertyList)otherBindings.get(i);
                            if (otherBinding == null || binding.getProperty("sdidataitemid").equals(otherBinding.getProperty("sdidataitemid")) && (!otherBinding.getProperty("reagentcomponent").equals("lot") || binding.getProperty("relationid").equals(otherBinding.getProperty("relationid"))) || !binding.getProperty("sdcid").equals(otherBinding.getProperty("sdcid")) || !multiSampleCalcs && !binding.getProperty("keyid1").equals(otherBinding.getProperty("keyid1"))) continue;
                            if (otherBinding.getProperty("dataentrytype").length() > 0) {
                                this.addDataEntryItem(deProps, otherBinding, String.valueOf(otherField.getValueList().get(i)));
                                hasDataEntryFields = true;
                                deMap.put(otherBinding.getProperty("sdidataitemid"), otherField);
                                deMap.put(otherBinding.getProperty("sdidataitemid") + "_instance", new Integer((String)otherFieldInstances.get(i)));
                                continue;
                            }
                            if (!otherBinding.getProperty("reagentcomponent").equals("lot") || otherField.getValueList() == null || otherField.getValueList().get(i).toString().length() <= 0) continue;
                            reagentOverrides = this.addReagentOverride(reagentOverrides, otherBinding, String.valueOf(otherField.getValueList().get(i)));
                        }
                        continue;
                    }
                    PropertyList otherBinding = (PropertyList)otherField.get("binding");
                    if (otherBinding == null || binding.getProperty("sdidataitemid").equals(otherBinding.getProperty("sdidataitemid")) && (!otherBinding.getProperty("reagentcomponent").equals("lot") || binding.getProperty("relationid").equals(otherBinding.getProperty("relationid"))) || !multiSampleCalcs && !binding.getProperty("keyid1").equals(otherBinding.getProperty("keyid1"))) continue;
                    if (otherBinding.getProperty("dataentrytype").length() > 0) {
                        this.addDataEntryItem(deProps, otherBinding, String.valueOf(otherField.getValue()));
                        hasDataEntryFields = true;
                        deMap.put(otherBinding.getProperty("sdidataitemid"), otherField);
                        continue;
                    }
                    if (!otherBinding.getProperty("reagentcomponent").equals("lot") || otherField.getValue() == null || otherField.getValue().toString().length() <= 0) continue;
                    reagentOverrides = this.addReagentOverride(reagentOverrides, otherBinding, String.valueOf(otherField.getValue()));
                }
                if (reagentOverrides != null && reagentOverrides.size() > 0) {
                    deProps.put("overridesdidatarelation", reagentOverrides.toXML());
                }
                if (hasDataEntryFields) {
                    ActionProcessor ap = new ActionProcessor(this.sapphireConnection.getConnectionId());
                    ap.processAction("EnterDataItem", "1", deProps);
                    DataSet sdidataitem = (DataSet)deProps.get("sdidataitem");
                    DataSet sdidataitemspec = (DataSet)deProps.get("sdidataitemspec");
                    HashMap specFilter = new HashMap();
                    for (int i = 0; i < sdidataitem.size(); ++i) {
                        Field diField;
                        String sdidataitemid = sdidataitem.getValue(i, "sdidataitemid");
                        String valuestatus = sdidataitem.getValue(i, "valuestatus");
                        if (fieldsdidataitemid.equals(sdidataitemid)) {
                            if (valuestatus.length() == 0) {
                                enteredtext = sdidataitem.getValue(i, "enteredtext");
                                displayvalue = sdidataitem.getValue(i, "displayvalue");
                                this.getSpecDetails(sdidataitem, sdidataitemspec, specFilter, i, binding);
                                continue;
                            }
                            validationErrors = this.addValidationError(reviewItems, "Data entry failed: " + valuestatus, new PropertyList());
                            continue;
                        }
                        if (sdidataitem.getValue(i, "calcrule").length() <= 0 || (diField = (Field)deMap.get(sdidataitemid)) == null) continue;
                        String fieldinstance = diField.isRepeatable() ? String.valueOf((Integer)deMap.get(sdidataitemid + "_instance")) : "0";
                        PropertyList dataentryValue = deMap.containsKey(diField.getProperty("fieldid")) ? (PropertyList)deMap.get(diField.getProperty("fieldid")) : new PropertyList();
                        dataentryValue.setProperty("fieldid", diField.getProperty("fieldid"));
                        PropertyListCollection dataentryInstances = dataentryValue.containsKey("instances") ? dataentryValue.getCollection("instances") : new PropertyListCollection();
                        PropertyList dataentryInstance = new PropertyList();
                        dataentryInstance.setProperty("fieldid", diField.getProperty("fieldid"));
                        dataentryInstance.setProperty("fieldinstance", fieldinstance);
                        dataentryInstance.setProperty("enteredtext", sdidataitem.getValue(i, "enteredtext"));
                        dataentryInstance.setProperty("displayvalue", sdidataitem.getValue(i, "displayvalue"));
                        dataentryInstances.add(dataentryInstance);
                        dataentryValue.setProperty("instances", dataentryInstances);
                        deMap.put(diField.getProperty("fieldid"), dataentryValue);
                        this.getSpecDetails(sdidataitem, sdidataitemspec, specFilter, i, dataentryInstance);
                    }
                    PropertyListCollection dataentryValues = new PropertyListCollection();
                    for (String fid : deMap.keySet()) {
                        Object entry = deMap.get(fid);
                        if (!(entry instanceof PropertyList)) continue;
                        dataentryValues.add(entry);
                    }
                    fieldValue.setProperty("dataentryvalues", dataentryValues);
                }
            } else if (valueChanged) {
                displayvalue = enteredtext;
            }
            fieldValue.setProperty("enteredtext", operation.equalsIgnoreCase("check") && field.getProperty("type").equals("file") ? "[[IGNORE]]" : enteredtext);
            fieldValue.setProperty("displayvalue", displayvalue);
            fieldValue.setProperty("visible", visible ? "Y" : "N");
            fieldValue.setProperty("binding", binding);
            if (reviewItems.size() > 0) {
                fieldValue.setProperty("reviewitems", reviewItems);
            }
            if (validationErrors) {
                fieldMap.put("__unacknowledgederrors", "Y");
            }
        }
        catch (Exception e) {
            PropertyListCollection reviewItems = new PropertyListCollection();
            this.addValidationError(reviewItems, "Unexpected error inspecting field. Exception: " + e.getMessage(), new PropertyList());
            fieldValue.setProperty("reviewitems", reviewItems);
            this.logger.error("Unexpected error inspecting field. Exception: " + e.getMessage(), e);
            validationErrors = true;
        }
        return validationErrors;
    }

    private void addDataEntryItem(HashMap deProps, PropertyList otherBinding, String enteredtext) {
        deProps.put("keyid1", deProps.get("keyid1") != null ? deProps.get("keyid1") + ";" + otherBinding.getProperty("keyid1") : otherBinding.getProperty("keyid1"));
        deProps.put("keyid2", deProps.get("keyid2") != null ? deProps.get("keyid2") + ";" + otherBinding.getProperty("keyid2") : otherBinding.getProperty("keyid2"));
        deProps.put("keyid3", deProps.get("keyid3") != null ? deProps.get("keyid3") + ";" + otherBinding.getProperty("keyid3") : otherBinding.getProperty("keyid3"));
        deProps.put("paramlistid", deProps.get("paramlistid") != null ? deProps.get("paramlistid") + ";" + otherBinding.getProperty("paramlistid") : otherBinding.getProperty("paramlistid"));
        deProps.put("paramlistversionid", deProps.get("paramlistversionid") != null ? deProps.get("paramlistversionid") + ";" + otherBinding.getProperty("paramlistversionid") : otherBinding.getProperty("paramlistversionid"));
        deProps.put("variantid", deProps.get("variantid") != null ? deProps.get("variantid") + ";" + otherBinding.getProperty("variantid") : otherBinding.getProperty("variantid"));
        deProps.put("dataset", deProps.get("dataset") != null ? deProps.get("dataset") + ";" + otherBinding.getProperty("dataset") : otherBinding.getProperty("dataset"));
        deProps.put("paramid", deProps.get("paramid") != null ? deProps.get("paramid") + ";" + otherBinding.getProperty("paramid") : otherBinding.getProperty("paramid"));
        deProps.put("paramtype", deProps.get("paramtype") != null ? deProps.get("paramtype") + ";" + otherBinding.getProperty("paramtype") : otherBinding.getProperty("paramtype"));
        deProps.put("replicateid", deProps.get("replicateid") != null ? deProps.get("replicateid") + ";" + otherBinding.getProperty("replicateid") : otherBinding.getProperty("replicateid"));
        deProps.put("enteredtext", deProps.get("enteredtext") != null ? deProps.get("enteredtext") + ";" + enteredtext : enteredtext);
        deProps.put("calcexcludeflag", deProps.get("calcexcludeflag") != null ? deProps.get("calcexcludeflag") + ";" + otherBinding.getProperty("calcexclude") : otherBinding.getProperty("calcexclude"));
    }

    private DataSet addReagentOverride(DataSet reagentOverrides, PropertyList otherBinding, String lot) {
        if (reagentOverrides == null) {
            reagentOverrides = new DataSet();
            reagentOverrides.addColumn("sdcid", 0);
            reagentOverrides.addColumn("keyid1", 0);
            reagentOverrides.addColumn("keyid2", 0);
            reagentOverrides.addColumn("keyid3", 0);
            reagentOverrides.addColumn("paramlistid", 0);
            reagentOverrides.addColumn("paramlistversionid", 0);
            reagentOverrides.addColumn("variantid", 0);
            reagentOverrides.addColumn("dataset", 1);
            reagentOverrides.addColumn("relationtype", 0);
            reagentOverrides.addColumn("tosdcid", 0);
            reagentOverrides.addColumn("tokeyid1", 0);
            reagentOverrides.addColumn("tokeyid2", 0);
            reagentOverrides.addColumn("tokeyid3", 0);
        }
        String[] keyid1 = StringUtil.split(otherBinding.getProperty("keyid1"), ";");
        String[] keyid2 = StringUtil.split(otherBinding.getProperty("keyid2"), ";");
        String[] keyid3 = StringUtil.split(otherBinding.getProperty("keyid3"), ";");
        String[] paramlistid = StringUtil.split(otherBinding.getProperty("paramlistid"), ";");
        String[] paramlistversionid = StringUtil.split(otherBinding.getProperty("paramlistversionid"), ";");
        String[] variantid = StringUtil.split(otherBinding.getProperty("variantid"), ";");
        String[] dataset = StringUtil.split(otherBinding.getProperty("dataset"), ";");
        String[] reagenttypeid = StringUtil.split(otherBinding.getProperty("reagenttypeid"), ";");
        for (int i = 0; i < keyid1.length; ++i) {
            int newrow = reagentOverrides.addRow();
            reagentOverrides.setString(newrow, "sdcid", otherBinding.getProperty("sdcid"));
            reagentOverrides.setString(newrow, "keyid1", keyid1[i]);
            reagentOverrides.setString(newrow, "keyid2", keyid2[i]);
            reagentOverrides.setString(newrow, "keyid3", keyid3[i]);
            reagentOverrides.setString(newrow, "paramlistid", paramlistid[i]);
            reagentOverrides.setString(newrow, "paramlistversionid", paramlistversionid[i]);
            reagentOverrides.setString(newrow, "variantid", variantid[i]);
            reagentOverrides.setNumber(newrow, "dataset", dataset[i]);
            reagentOverrides.setString(newrow, "relationtype", reagenttypeid[i]);
            reagentOverrides.setString(newrow, "tosdcid", "LV_ReagentLot");
            reagentOverrides.setString(newrow, "tokeyid1", lot);
            reagentOverrides.setString(newrow, "tokeyid2", "(null)");
            reagentOverrides.setString(newrow, "tokeyid3", "(null)");
        }
        return reagentOverrides;
    }

    private void getSpecDetails(DataSet sdidataitem, DataSet sdidataitemspec, HashMap specFilter, int dataitemRow, PropertyList dataentryBinding) {
        if (sdidataitemspec.size() > 0) {
            String condition;
            specFilter.put("paramlistid", sdidataitem.getString(dataitemRow, "paramlistid"));
            specFilter.put("paramlistversionid", sdidataitem.getString(dataitemRow, "paramlistversionid"));
            specFilter.put("variantid", sdidataitem.getString(dataitemRow, "variantid"));
            specFilter.put("dataset", sdidataitem.getBigDecimal(dataitemRow, "dataset"));
            specFilter.put("paramid", sdidataitem.getString(dataitemRow, "paramid"));
            specFilter.put("paramtype", sdidataitem.getString(dataitemRow, "paramtype"));
            specFilter.put("replicateid", sdidataitem.getBigDecimal(dataitemRow, "replicateid"));
            DataSet filteredsdidataitemspec = sdidataitemspec.getFilteredDataSet(specFilter);
            if (filteredsdidataitemspec.size() > 0 && (condition = filteredsdidataitemspec.getValue(0, "condition")).length() > 0) {
                dataentryBinding.setProperty("speccondition", condition);
            }
        }
    }

    private boolean validateCompare(Object objectValue, String operator, Object value1, Object value2, boolean validationErrors, PropertyListCollection reviewItems, PropertyList validationitem) {
        if ("EQ".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) != 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("NE".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) == 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("GT".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) <= 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("GE".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) < 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("LT".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) >= 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("LE".equalsIgnoreCase(operator)) {
            if (value1 != null && ((Comparable)objectValue).compareTo(value1) > 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("IB".equalsIgnoreCase(operator)) {
            if (value1 != null && value2 != null && (((Comparable)objectValue).compareTo(value1) < 0 || ((Comparable)objectValue).compareTo(value2) > 0)) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("EB".equalsIgnoreCase(operator)) {
            if (value1 != null && value2 != null && (((Comparable)objectValue).compareTo(value1) <= 0 || ((Comparable)objectValue).compareTo(value2) >= 0)) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("IO".equalsIgnoreCase(operator)) {
            if (value1 != null && value2 != null && ((Comparable)objectValue).compareTo(value1) > 0 && ((Comparable)objectValue).compareTo(value2) < 0) {
                validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
            }
        } else if ("EO".equalsIgnoreCase(operator) && value1 != null && value2 != null && ((Comparable)objectValue).compareTo(value1) >= 0 && ((Comparable)objectValue).compareTo(value2) <= 0) {
            validationErrors = this.addValidationError(reviewItems, validationitem.getProperty("message"), validationitem);
        }
        return validationErrors;
    }

    private boolean validateList(String list, String operator, StringBuffer text, PropertyListCollection reviewItems, PropertyList validationItem) {
        String[] values = StringUtil.split(list, ";");
        boolean found = false;
        for (int j = 0; !found && j < values.length; ++j) {
            if (!values[j].equalsIgnoreCase(text.toString())) continue;
            text = new StringBuffer(values[j]);
            found = true;
        }
        if ("IN".equalsIgnoreCase(operator) && !found) {
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "Invalid list value entered!", validationItem);
        }
        if ("NI".equalsIgnoreCase(operator) && found) {
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "", validationItem);
        }
        return false;
    }

    private boolean validateReftype(QueryProcessor queryProcessor, String reftypeid, String operator, StringBuffer text, PropertyListCollection reviewItems, PropertyList validationItem, boolean completePartialEntries) {
        Object[] param;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT refvalueid FROM refvalue WHERE ( activeflag='Y' OR activeflag is null) AND reftypeid = ? AND ");
        if (completePartialEntries) {
            sql.append("refvalueid like ?");
            param = new Object[]{reftypeid, text.toString() + "%"};
        } else {
            sql.append("refvalueid = ?");
            param = new Object[]{reftypeid, text.toString()};
        }
        DataSet results = queryProcessor.getPreparedSqlDataSet(sql.toString(), param);
        if ("IN".equalsIgnoreCase(operator)) {
            if (results != null && results.size() == 1) {
                text = new StringBuffer(results.getValue(0, "refvalueid"));
                return false;
            }
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "Invalid " + reftypeid.toLowerCase() + " entered!", validationItem);
        }
        if (results != null && results.size() == 1) {
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "", validationItem);
        }
        return false;
    }

    private boolean validateSdi(SDCProcessor sdcProcessor, QueryProcessor queryProcessor, String sdcid, String operator, StringBuffer text, PropertyListCollection keyset, HashMap fieldMap, PropertyListCollection reviewItems, PropertyList validationItem, boolean completePartialEntries) {
        String keyval2;
        int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String keyval1 = keyset == null ? text.toString() : ((Field)fieldMap.get(keyset.getPropertyList(0).getProperty("fieldid"))).toString();
        String string = keyval2 = keyset == null ? "" : ((Field)fieldMap.get(keyset.getPropertyList(1).getProperty("fieldid"))).toString();
        String keyval3 = keyset == null ? "" : (keyset.size() == 3 ? ((Field)fieldMap.get(keyset.getPropertyList(2).getProperty("fieldid"))).toString() : "");
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT " + keycolid1 + (keycols > 1 ? "," + keycolid2 : "") + (keycols > 2 ? "," + keycolid3 : "") + " FROM " + sdcProcessor.getProperty(sdcid, "tableid") + " WHERE ");
        ArrayList<String> p = new ArrayList<String>();
        if (completePartialEntries) {
            sql.append(" " + keycolid1 + " like ? ");
            p.add(keyval1 + "%");
            if (keycols > 1 && keyval2.length() > 0 && keyset != null) {
                sql.append(" AND " + keycolid2 + " like ? ");
                p.add(keyval2 + "%");
            }
            if (keycols > 2 && keyval3.length() > 0 && keyset != null) {
                sql.append(" AND " + keycolid3 + " like ? ");
                p.add(keyval3 + "%");
            }
        } else {
            sql.append(" " + keycolid1 + " = ?");
            p.add(keyval1);
            if (keycols > 1 && keyval2.length() > 0 && keyset != null) {
                sql.append(" AND " + keycolid2 + " = ? ");
                p.add(keyval2);
            }
            if (keycols > 2 && keyval3.length() > 0 && keyset != null) {
                sql.append(" AND " + keycolid3 + " = ? ");
                p.add(keyval3);
            }
        }
        DataSet results = queryProcessor.getPreparedSqlDataSet(sql.toString(), p.toArray());
        if ("IN".equalsIgnoreCase(operator)) {
            if (results != null && results.size() > 0) {
                text = new StringBuffer(results.getValue(0, keycolid1));
                return false;
            }
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "Invalid " + sdcProcessor.getProperty(sdcid, "singular").toLowerCase() + " field entered!", validationItem);
        }
        if (results != null && results.size() > 0) {
            return this.addValidationError(reviewItems, validationItem != null ? validationItem.getProperty("message") : "", validationItem);
        }
        return false;
    }

    private boolean evalComplexValidation(String expressionowner, PropertyList complexValidation, HashMap bindingMap, PropertyListCollection reviewItems) throws SapphireException {
        String script;
        if (complexValidation != null && (script = complexValidation.getProperty("groovyscript")).length() > 0) {
            DBUtil dbu = new DBUtil();
            StringBuffer log = new StringBuffer();
            try {
                ProcessingUtil.getSapphireObjectBindings(this.sapphireConnection, bindingMap, dbu, log, "DOCUMENTPROCESSING", true, true, true, true, true, true);
                script = ProcessingUtil.insertHeaderCode(script, false);
                bindingMap.put("user", new ConnectionInfo(this.sapphireConnection).getUserAttributeMap());
                String result = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(script, bindingMap, "Error evaluating complex validation expression for " + expressionowner + ".\n\n[exception] when evaluating " + script);
                if (!result.equals(complexValidation.getProperty("passcondition", "true"))) {
                    boolean bl = this.addValidationError(reviewItems, complexValidation.getProperty("message"), complexValidation);
                    return bl;
                }
            }
            catch (Exception e) {
                String message = e.getMessage();
                throw new SapphireException(message.contains("//startinsert") ? message.substring(0, message.indexOf("//startinsert")) + "\n" + message.substring(message.indexOf("//endinsert") + 11) : message, e);
            }
            finally {
                dbu.reset();
            }
        }
        return false;
    }

    private boolean addValidationError(PropertyListCollection reviewItems, String message, PropertyList validationtem) {
        PropertyList reviewItem = new PropertyList();
        reviewItem.setProperty("reviewitemtype", "V");
        reviewItem.setProperty("reviewitemtext", message.length() > 0 ? this.evalTokens(validationtem, message) : "Validation error");
        reviewItem.setProperty("reviewitemstatus", "F");
        reviewItem.setProperty("createby", this.sapphireConnection.getSysuserId());
        reviewItems.add(reviewItem);
        return true;
    }

    private boolean addMandatoryError(PropertyListCollection reviewItems, String message) {
        PropertyList reviewItem = new PropertyList();
        reviewItem.setProperty("reviewitemtype", "Y");
        reviewItem.setProperty("reviewitemtext", message);
        reviewItem.setProperty("reviewitemstatus", "F");
        reviewItem.setProperty("createby", this.sapphireConnection.getSysuserId());
        reviewItems.add(reviewItem);
        return true;
    }
}

