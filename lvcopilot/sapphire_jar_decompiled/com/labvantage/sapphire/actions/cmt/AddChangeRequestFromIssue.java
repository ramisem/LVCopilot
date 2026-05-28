/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import com.labvantage.sapphire.tagext.SDITagUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.ext.BaseIssueHandler;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddChangeRequestFromIssue
extends BaseAction {
    public static final String ID = "AddChangeRequestFromIssue";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_ISSUEID = "issueid";
    public static final String PROPERTY_ISSUEREPOSITORYID = "issuerepositoryid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String issueId = properties.getProperty(PROPERTY_ISSUEID, "");
        String issueRepositoryId = properties.getProperty(PROPERTY_ISSUEREPOSITORYID, "CMTCommunication Custom");
        if (issueId.isEmpty()) {
            throw new SapphireException("Issue id is mandatory!");
        }
        String sql = "select changerequestid from changerequest where externalreference like ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{"%" + issueId + "%"});
        if (ds.getRowCount() > 0) {
            throw new SapphireException("There is already change request(s):" + ds.getColumnValues("changerequestid", ";") + " for this issue id");
        }
        this.logger.debug("Getting Policy Info.");
        PropertyList repositoryProps = IssueManagementUtil.getIssueRepositoryProperties(issueRepositoryId, this.getTranslationProcessor(), this.getConfigurationProcessor());
        String handlerClassName = repositoryProps.getProperty("handlerclass");
        this.logger.debug("Instantiating Issue Handler class: " + handlerClassName);
        BaseIssueHandler baseIssueHandler = BaseIssueHandler.getInstance(handlerClassName, this.getConnectionProcessor().getSapphireConnection());
        baseIssueHandler.setDatabase(this.database);
        this.logger.debug("Invoking Base Issue Handler method for Get Issue.");
        baseIssueHandler.executeMethod("getIssue", properties);
        PropertyList issueProps = properties.getPropertyList("issueproperties");
        JSONObject issueObj = issueProps.toJSONObject();
        JSONObject fieldsObj = issueObj.optJSONObject("fields");
        PropertyList fields = issueProps.getPropertyListNotNull("fields");
        fields.setProperty("key", issueProps.getProperty("key"));
        String link = issueProps.getProperty("link");
        fields.setProperty("link", link);
        PropertyListCollection crColumns = repositoryProps.getPropertyListNotNull("createprops").getCollectionNotNull("columns");
        PropertyList addCRprops = new PropertyList();
        addCRprops.setProperty("sdcid", "LV_ChangeRequest");
        for (int i = 0; i < crColumns.size(); ++i) {
            String columnid;
            PropertyList column = crColumns.getPropertyList(i);
            boolean enabled = column.getProperty("enabled", "Y").startsWith("Y");
            if (!enabled || (columnid = column.getProperty("columnid", "")).isEmpty()) continue;
            try {
                String columnValue;
                String mode = column.getProperty("mode");
                if (mode.equals("field")) {
                    String field = column.getProperty("field");
                    String subfield = column.getProperty("subfield");
                    columnValue = !subfield.isEmpty() ? (fieldsObj.get(field) instanceof JSONObject ? fieldsObj.optJSONObject(field).optString(subfield) : (fieldsObj.get(field) instanceof JSONArray ? fieldsObj.optJSONArray(field).optJSONObject(0).optString(subfield) : fieldsObj.optString(field))) : fieldsObj.optString(field);
                } else {
                    String pseudo = column.getProperty("pseudo");
                    columnValue = IssueManagementUtil.replaceTokens(fields, pseudo);
                }
                String displayvalue = column.getProperty("displayvalue");
                if (!displayvalue.isEmpty()) {
                    columnValue = SDITagUtil.getDisplayValue(columnValue, displayvalue);
                }
                String sqlColLength = "select columnlength from syscolumn where tableid = 'changerequest' and columnid = ? and columnlength is not null";
                DataSet dsColLength = this.getQueryProcessor().getPreparedSqlDataSet(sqlColLength, (Object[])new String[]{columnid});
                if (columnValue != null && !dsColLength.isEmpty()) {
                    int colLength = Integer.parseInt(dsColLength.getValue(0, "columnlength"));
                    if (columnValue.length() > colLength) {
                        columnValue = columnValue.substring(0, colLength);
                    }
                }
                addCRprops.setProperty(columnid, columnValue);
                continue;
            }
            catch (JSONException e) {
                this.logger.error("Failed to get " + columnid + " from the received JSON: " + e.getMessage());
            }
        }
        this.getActionProcessor().processAction("AddSDI", VERSIONID, addCRprops);
        properties.setProperty("newkeyid1", addCRprops.getProperty("newkeyid1"));
        properties.setProperty("longdescription", addCRprops.getProperty("longdescription", ""));
    }
}

