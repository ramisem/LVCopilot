/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class WorksheetItemTokenResolver
implements ELNConstants {
    private String worksheetid;
    private String worksheetversionid;
    private QueryProcessor qp = null;
    private SapphireConnection sapphireConnection = null;

    public WorksheetItemTokenResolver(String worksheetid, String worksheetversionid, QueryProcessor qp, SapphireConnection sapphireConnection) {
        this.worksheetid = worksheetid;
        this.worksheetversionid = worksheetversionid;
        this.qp = qp;
        this.sapphireConnection = sapphireConnection;
    }

    public WorksheetItemTokenResolver(String worksheetid, String worksheetversionid) {
        this.worksheetid = worksheetid;
        this.worksheetversionid = worksheetversionid;
    }

    public String resolveTokens(String worksheetitemid, String worksheetitemversionid, String value, DataSet dependencyList) {
        if (value != null && value.length() > 0 && this.qp != null) {
            if (value.contains("$P{") && value.contains("}")) {
                String[] tokens = StringUtil.getTokens(value, "$P{", "}", false);
                DataSet worksheetitemparams = this.qp.getPreparedSqlDataSet("SELECT * FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
                if (tokens.length > 0) {
                    for (String token : tokens) {
                        String replaceWith = "";
                        if (token.startsWith("$G")) continue;
                        int row = worksheetitemparams.findRow("paramname", token);
                        if (row != -1) {
                            int i;
                            StringBuilder values;
                            String valuesdcid = worksheetitemparams.getValue(row, "valuesdcid");
                            String valuekeyid1 = worksheetitemparams.getValue(row, "valuekeyid1");
                            String valuekeyid2 = worksheetitemparams.getValue(row, "valuekeyid2");
                            String valuetype = worksheetitemparams.getValue(row, "valuetype");
                            String valuelabel = worksheetitemparams.getValue(row, "valuelabel");
                            if (valuetype.equalsIgnoreCase("field")) {
                                DataSet worksheetitemfields = this.qp.getPreparedSqlDataSet("SELECT enteredtext, displayvalue, numericvalue, datevalue FROM worksheetitemfield WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND fieldname = ?", new Object[]{valuekeyid1, valuekeyid2, valuelabel});
                                values = new StringBuilder();
                                for (i = 0; i < worksheetitemfields.size(); ++i) {
                                    values.append(";").append(worksheetitemfields.getValue(i, "displayvalue", worksheetitemfields.getValue(i, "enteredtext")));
                                }
                                replaceWith = values.length() > 0 ? values.substring(1) : "";
                            } else if (valuetype.equalsIgnoreCase("sdilist")) {
                                DataSet worksheetitemsdis = this.qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3 FROM worksheetitemsdi WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{valuekeyid1, valuekeyid2});
                                values = new StringBuilder();
                                for (i = 0; i < worksheetitemsdis.size(); ++i) {
                                    values.append(";").append(worksheetitemsdis.getValue(i, valuelabel.toLowerCase()));
                                }
                                replaceWith = values.length() > 0 ? values.substring(1) : "";
                            } else if (valuetype.equalsIgnoreCase("metadata")) {
                                DataSet sdiattributes = this.qp.getPreparedSqlDataSet("SELECT textvalue, numericvalue, datevalue FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attributeid = ?", new Object[]{valuesdcid, valuekeyid1, valuekeyid2, valuelabel});
                                values = new StringBuilder();
                                for (i = 0; i < sdiattributes.size(); ++i) {
                                    values.append(";").append(sdiattributes.getValue(i, "textvalue"));
                                }
                                String string = replaceWith = values.length() > 0 ? values.substring(1) : "";
                            }
                            if (replaceWith.length() == 0) {
                                replaceWith = worksheetitemparams.getValue(row, "paramvalue");
                            }
                        }
                        replaceWith = StringUtil.replaceAll(replaceWith, ";", "','");
                        replaceWith = StringUtil.replaceAll(replaceWith, "%3B", "','");
                        value = StringUtil.replaceAll(value, "$P{" + token + "}", replaceWith);
                    }
                }
            }
            value = this.doSub(worksheetitemid, worksheetitemversionid, value, "$S{", "}", dependencyList);
            value = this.doSub(worksheetitemid, worksheetitemversionid, value, "[", "]", dependencyList);
        }
        return value;
    }

    private String doSub(String worksheetitemid, String worksheetitemversionid, String value, String start, String end, DataSet dependencyList) {
        String[] tokens;
        if (value.contains(start) && value.contains(end) && (tokens = StringUtil.getTokens(value, start, end, false)).length > 0) {
            for (String token : tokens) {
                String replaceWith = "";
                if (token.startsWith("$G")) continue;
                if ((token.startsWith("user.") || token.startsWith("currentuser.")) && this.sapphireConnection != null) {
                    String property = token.substring(token.indexOf(".") + 1);
                    if (property.equalsIgnoreCase("sysuserid") || property.equalsIgnoreCase("sysuser")) {
                        replaceWith = this.sapphireConnection.getSysuserId();
                    } else if (property.equalsIgnoreCase("sysusername") || property.equalsIgnoreCase("username") || property.equalsIgnoreCase("name")) {
                        replaceWith = this.sapphireConnection.getSysuserName();
                    } else if (property.equalsIgnoreCase("defaultdepartment") || property.equalsIgnoreCase("defaultdepartmentid")) {
                        replaceWith = this.sapphireConnection.getDefaultDepartment();
                    } else if (property.equalsIgnoreCase("departmentlist")) {
                        replaceWith = this.sapphireConnection.getDepartmentList();
                    } else if (property.equalsIgnoreCase("connectionid")) {
                        replaceWith = this.sapphireConnection.getConnectionId();
                    } else if (property.equalsIgnoreCase("databaseid")) {
                        replaceWith = this.sapphireConnection.getDatabaseId();
                    }
                } else if (token.startsWith("author.")) {
                    DataSet authorDetails = this.qp.getPreparedSqlDataSet("SELECT sysuser.sysuserid, sysuser.sysuserdesc, sysuser.defaultdepartment FROM worksheet, sysuser WHERE sysuser.sysuserid=worksheet.authorid AND worksheetid=? AND worksheetversionid=?", (Object[])new String[]{this.worksheetid, this.worksheetversionid});
                    if (authorDetails.size() > 0) {
                        String property = token.substring(token.indexOf(".") + 1);
                        if (property.equalsIgnoreCase("sysuserid") || property.equalsIgnoreCase("sysuser")) {
                            replaceWith = authorDetails.getValue(0, "sysuserid");
                        } else if (property.equalsIgnoreCase("sysusername") || property.equalsIgnoreCase("username") || property.equalsIgnoreCase("name")) {
                            replaceWith = authorDetails.getValue(0, "sysuserdesc");
                        } else if (property.equalsIgnoreCase("defaultdepartment") || property.equalsIgnoreCase("defaultdepartmentid")) {
                            replaceWith = authorDetails.getValue(0, "defaultdepartment");
                        }
                    }
                } else if (token.equalsIgnoreCase("currentuser") && this.sapphireConnection != null) {
                    replaceWith = this.sapphireConnection.getSysuserId();
                } else if (token.equalsIgnoreCase("worksheetid")) {
                    replaceWith = this.worksheetid;
                } else if (token.equalsIgnoreCase("worksheetversionid")) {
                    replaceWith = this.worksheetversionid;
                } else if (token.equalsIgnoreCase("worksheetitemid")) {
                    replaceWith = worksheetitemid;
                } else if (token.equalsIgnoreCase("worksheetitemversionid")) {
                    replaceWith = worksheetitemversionid;
                } else if (token.startsWith("metadata.") && this.qp != null) {
                    String attributeid = token.substring("metadata.".length());
                    DataSet attribute = this.qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, attributeid, datatype, datevalue, numericvalue, textvalue, defaultdatevalue, defaultnumericvalue, defaulttextvalue FROM sdiattribute WHERE sdcid=? AND keyid1=? AND keyid2=? AND attributeid=?", (Object[])new String[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid, attributeid});
                    if (attribute.size() == 0) {
                        attribute = this.qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, attributeid, datatype, datevalue, numericvalue, textvalue, defaultdatevalue, defaultnumericvalue, defaulttextvalue FROM sdiattribute WHERE sdcid=? AND keyid1=? AND keyid2=? AND attributeid=?", (Object[])new String[]{"LV_Worksheet", this.worksheetid, this.worksheetversionid, attributeid});
                    }
                    if (attribute.size() > 0) {
                        String datatype = attribute.getValue(0, "datatype");
                        String columnid = datatype.equals("D") || datatype.equals("O") ? "datevalue" : (datatype.equals("N") ? "numericvalue" : "textvalue");
                        replaceWith = attribute.getValue(0, columnid, attribute.getValue(0, "default" + columnid));
                        WorksheetItemTokenResolver.addDepenency(dependencyList, worksheetitemid, worksheetitemversionid, attribute.getValue(0, "sdcid"), attribute.getValue(0, "keyid1"), attribute.getValue(0, "keyid2"));
                    }
                } else if ((token.startsWith("field.") || token.startsWith("fields.")) && this.qp != null) {
                    String fieldName = token.substring((token.startsWith("field.") ? "field." : "fields.").length());
                    DataSet field = this.qp.getPreparedSqlDataSet("SELECT wsi.worksheetitemid, wsi.worksheetitemversionid, wsif.enteredtext, wsif.displayvalue, wsif.numericvalue, wsif.datevalue FROM worksheetitemfield wsif, worksheetitem wsi WHERE wsi.worksheetitemid = wsif.worksheetitemid AND wsi.worksheetitemversionid = wsif.worksheetitemversionid AND wsi.worksheetid = ? AND wsi.worksheetversionid = ? AND fieldname = ? ORDER BY wsi.worksheetitemid", new Object[]{this.worksheetid, this.worksheetversionid, fieldName});
                    if (field.size() == 1) {
                        replaceWith = field.getValue(0, "displayvalue", field.getValue(0, "enteredtext"));
                        WorksheetItemTokenResolver.addDepenency(dependencyList, worksheetitemid, worksheetitemversionid, "LV_WorksheetItem", field.getValue(0, "worksheetitemid"), field.getValue(0, "worksheetitemversionid"));
                    } else if (field.size() > 0) {
                        int row = field.findRow("worksheetitemid", worksheetitemid);
                        row = Math.max(row, 0);
                        replaceWith = field.getValue(row, "displayvalue", field.getValue(row, "enteredtext"));
                        WorksheetItemTokenResolver.addDepenency(dependencyList, worksheetitemid, worksheetitemversionid, "LV_WorksheetItem", field.getValue(row, "worksheetitemid"), field.getValue(row, "worksheetitemversionid"));
                    }
                }
                value = StringUtil.replaceAll(value, start + token + end, replaceWith);
            }
        }
        return value;
    }

    public void populateFormBindingMap(PropertyList worksheetVars, String worksheetitemid, String worksheetitemversionid) {
        DataSet fieldDataSet = this.qp.getPreparedSqlDataSet("SELECT wsif.enteredtext, wsif.displayvalue, wsif.numericvalue, wsif.datevalue, wsif.fieldname FROM worksheetitemfield wsif, worksheetitem wsi WHERE wsi.worksheetitemid = wsif.worksheetitemid AND wsi.worksheetitemversionid = wsif.worksheetitemversionid AND wsi.worksheetid = ? AND wsi.worksheetversionid = ? ORDER BY wsi.worksheetitemid", new Object[]{this.worksheetid, this.worksheetversionid});
        PropertyList fields = new PropertyList();
        for (int i = 0; i < fieldDataSet.size(); ++i) {
            String fieldname = fieldDataSet.getValue(i, "fieldname");
            String value = fieldDataSet.getValue(i, "displayvalue", fieldDataSet.getValue(i, "enteredtext"));
            fields.setProperty(fieldname, value);
        }
        worksheetVars.setProperty("fields", fields);
        DataSet attributes = this.qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, attributeid, datatype, datevalue, numericvalue, textvalue, defaultdatevalue, defaultnumericvalue, defaulttextvalue FROM sdiattribute WHERE sdcid=? AND keyid1=? AND keyid2=? UNION SELECT sdcid, keyid1, keyid2, attributeid, datatype, datevalue, numericvalue, textvalue, defaultdatevalue, defaultnumericvalue, defaulttextvalue FROM sdiattribute WHERE sdcid=? AND keyid1=? AND keyid2=?", (Object[])new String[]{"LV_Worksheet", this.worksheetid, this.worksheetversionid, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid});
        PropertyList metadata = new PropertyList();
        for (int i = 0; i < attributes.size(); ++i) {
            String attributeid = attributes.getValue(i, "attributeid");
            String datatype = attributes.getValue(i, "datatype");
            String columnid = datatype.equals("D") || datatype.equals("O") ? "datevalue" : (datatype.equals("N") ? "numericvalue" : "textvalue");
            String value = attributes.getValue(i, columnid, attributes.getValue(i, "default" + columnid));
            metadata.setProperty(attributeid, value);
        }
        worksheetVars.setProperty("metadata", metadata);
    }

    public static void addDepenency(DataSet dependencyList, String worksheetitemid, String worksheetitemversionid, String dependssdcid, String dependskeyid1, String dependskeyid2) {
        if (dependencyList == null) {
            return;
        }
        int row = dependencyList.addRow();
        dependencyList.setString(row, "worksheetitemid", worksheetitemid);
        dependencyList.setString(row, "worksheetitemversionid", worksheetitemversionid);
        dependencyList.setString(row, "dependssdcid", dependssdcid);
        dependencyList.setString(row, "dependskeyid1", dependskeyid1);
        dependencyList.setString(row, "dependskeyid2", dependskeyid2);
    }
}

