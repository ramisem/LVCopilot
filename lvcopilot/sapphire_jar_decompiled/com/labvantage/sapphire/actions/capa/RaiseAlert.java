/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.capa;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.AlertRaisedEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RaiseAlert
extends BaseAction
implements sapphire.action.RaiseAlert {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String alertType = properties.getProperty("alerttype");
        String severity = properties.getProperty("severity", "Warning");
        String causalSDCid = properties.getProperty("causalsdcid");
        String causalKeyid1 = properties.getProperty("causalkeyid1");
        String causalKeyid2 = properties.getProperty("causalkeyid2");
        String causalKeyid3 = properties.getProperty("causalkeyid3");
        if (causalKeyid1.contains(";")) {
            throw new ActionException("You can only have one causal SDI for an alert.");
        }
        String linkSDCid = properties.getProperty("linksdcid");
        String linkKeyid1 = properties.getProperty("linkkeyid1");
        String linkKeyid2 = properties.getProperty("linkkeyid2");
        String linkKeyid3 = properties.getProperty("linkkeyid3");
        String description = properties.getProperty("description");
        String explanation = properties.getProperty("explanation");
        if (description.length() > 80) {
            description = description.substring(0, 80);
        }
        if (explanation.length() > 2000) {
            explanation = explanation.substring(0, 2000);
        }
        boolean forceNew = properties.getProperty("forcenew", "N").equals("Y");
        boolean matchSDI = properties.getProperty("matchcausalsdi", "Y").equals("Y") && causalSDCid.length() > 0 && causalKeyid1.length() > 0;
        boolean matchDescription = properties.getProperty("matchdescription", "N").equals("Y") && description.length() > 0;
        boolean matchExplanation = properties.getProperty("matchexplanation", "N").equals("Y") && explanation.length() > 0;
        boolean createIncident = true;
        if (!forceNew) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append((CharSequence)this.buildBasicSelect(alertType, severity, causalSDCid, causalKeyid1, causalKeyid2, causalKeyid3, description, explanation, matchSDI, matchDescription, matchExplanation, safeSQL));
            sql.append(" AND i.suppressuntildt is not null AND i.suppressuntildt > " + safeSQL.addVar(DateTimeUtil.getNowTimestamp()));
            sql.append(" UNION ");
            sql.append((CharSequence)this.buildBasicSelect(alertType, severity, causalSDCid, causalKeyid1, causalKeyid2, causalKeyid3, description, explanation, matchSDI, matchDescription, matchExplanation, safeSQL));
            sql.append(" AND i.incidentstatus='Initial' AND i.suppressuntildt is null");
            sql.append(" ORDER BY 2 desc");
            DataSet exists = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (exists != null && exists.size() > 0) {
                properties.setProperty("foundkeyid1", exists.getValue(0, "incidentid"));
                createIncident = false;
            }
        }
        if (createIncident) {
            PropertyList props = new PropertyList();
            props.setProperty("incidentcategory", "Alert");
            String sourceSdcid = causalSDCid;
            String sourceKeyid1 = causalKeyid1;
            String sourceKeyid2 = causalKeyid2;
            String sourceKeyid3 = causalKeyid3;
            String causalObjectFlag = causalKeyid1.length() > 0 ? "Y" : "";
            String securityUser = "";
            String securityDept = "";
            if ("Y".equalsIgnoreCase(causalObjectFlag)) {
                SDCProcessor sdcp = this.getSDCProcessor();
                boolean isIncdtDeptSecurityEnabled = "D".equalsIgnoreCase(sdcp.getProperty("LV_Incdt", "accesscontrolledflag"));
                boolean isCausalSDCDeptSecurityEnabled = "D".equalsIgnoreCase(sdcp.getProperty(causalSDCid, "accesscontrolledflag"));
                if (isIncdtDeptSecurityEnabled && isCausalSDCDeptSecurityEnabled) {
                    SafeSQL safeSQL = new SafeSQL();
                    String keycolid1 = sdcp.getProperty(causalSDCid, "keycolid1");
                    String keycolid2 = sdcp.getProperty(causalSDCid, "keycolid2");
                    String keycolid3 = sdcp.getProperty(causalSDCid, "keycolid3");
                    String tableId = sdcp.getProperty(causalSDCid, "tableid");
                    StringBuffer sql = new StringBuffer();
                    sql.append("select securityuser, securitydepartment from ").append(tableId).append(" where ").append(keycolid1).append(" = ").append(safeSQL.addVar(causalKeyid1));
                    if (keycolid2.length() > 0) {
                        sql.append(" and ").append(keycolid2).append(" = ").append(safeSQL.addVar(causalKeyid2));
                    }
                    if (keycolid3.length() > 0) {
                        sql.append(" and ").append(keycolid3).append(" = ").append(safeSQL.addVar(causalKeyid3));
                    }
                    this.database.createPreparedResultSet("getcausalsdi", sql.toString(), safeSQL.getValues());
                    if (this.database.getNext("getcausalsdi")) {
                        securityUser = this.database.getString("getcausalsdi", "securityuser");
                        securityDept = this.database.getString("getcausalsdi", "securitydepartment");
                        if (securityUser != null && securityUser.length() > 0) {
                            props.setProperty("securityuser", securityUser);
                        }
                        if (securityDept != null && securityDept.length() > 0) {
                            props.setProperty("securitydepartment", securityDept);
                        }
                    }
                    this.database.closeResultSet("getcausalsdi");
                }
            }
            if (linkKeyid1.length() > 0) {
                sourceSdcid = sourceSdcid + (sourceSdcid.length() > 0 ? ";" : "") + linkSDCid;
                sourceKeyid1 = sourceKeyid1 + (sourceKeyid1.length() > 0 ? ";" : "") + linkKeyid1;
                sourceKeyid2 = sourceKeyid2 + (sourceKeyid2.length() > 0 ? ";" : "") + linkKeyid2;
                sourceKeyid3 = sourceKeyid3 + (sourceKeyid3.length() > 0 ? ";" : "") + linkKeyid3;
                if ((causalObjectFlag = causalObjectFlag + (causalObjectFlag.length() > 0 ? ";" : "") + StringUtil.repeat("N;", StringUtil.split(linkKeyid1, ";").length)).endsWith(";")) {
                    causalObjectFlag = causalObjectFlag.substring(0, causalObjectFlag.length() - 1);
                }
            }
            props.setProperty("sourcesdcid", sourceSdcid);
            props.setProperty("sourcekeyid1", sourceKeyid1);
            props.setProperty("sourcekeyid2", sourceKeyid2);
            props.setProperty("sourcekeyid3", sourceKeyid3);
            props.setProperty("causalobjectflag", causalObjectFlag);
            props.setProperty("initialstatus", "Initial");
            props.setProperty("incidenttype", alertType);
            props.setProperty("severity", severity);
            props.setProperty("incidentdesc", description);
            props.setProperty("explanation", explanation);
            this.getActionProcessor().processAction("RecordIncident", "1", props);
            String newkeyid1 = props.getProperty("newkeyid1");
            properties.setProperty("newkeyid1", newkeyid1);
            EventManager.generateEvent(new SapphireConnection(this.database.getConnection(), this.connectionInfo), null, new AlertRaisedEventObject(newkeyid1, alertType, severity, description, explanation, causalSDCid, causalKeyid1, causalKeyid2, causalKeyid3));
        }
    }

    private StringBuilder buildBasicSelect(String alertType, String severity, String causalSDCid, String causalKeyid1, String causalKeyid2, String causalKeyid3, String description, String explanation, boolean matchSDI, boolean matchDescription, boolean matchExplanation, SafeSQL safeSQL) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.incidentid, i.incidentdt FROM incident i" + (matchSDI ? ", incidentitem ii" : "") + " WHERE i.incidenttype=" + safeSQL.addVar(alertType) + " AND i.severity=" + safeSQL.addVar(severity) + " AND i.incidentcategory='Alert' ");
        if (matchSDI) {
            sql.append(" AND i.incidentid = ii.incidentid AND ii.causalobjectflag='Y'  AND ii.sourcesdcid = " + safeSQL.addVar(causalSDCid) + " AND ii.sourcekeyid1=" + safeSQL.addVar(causalKeyid1) + (causalKeyid2.length() > 0 ? " AND ii.sourcekeyid2=" + safeSQL.addVar(causalKeyid2) : "") + (causalKeyid3.length() > 0 ? " AND ii.sourcekeyid3=" + safeSQL.addVar(causalKeyid3) : ""));
        }
        if (matchDescription) {
            sql.append(" AND i.incidentdesc=" + safeSQL.addVar(description));
        }
        if (matchExplanation) {
            sql.append(" AND i.explanation=" + safeSQL.addVar(explanation));
        }
        return sql;
    }
}

