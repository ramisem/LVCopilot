/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ContainerType
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53842 $";
    private static final ArrayList detailTableInfo = new ArrayList();

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty("templatekeyid1").length() > 0) {
            String templatekeyids = actionProps.getProperty("templatekeyid1");
            templatekeyids = templatekeyids + ";" + actionProps.getProperty("templatekeyid2");
            templatekeyids = templatekeyids + ";" + actionProps.getProperty("templatekeyid3");
            DataSet primary = sdiData.getDataset("primary");
            String[] createdkeyid1Arrays = primary.getColumnValues("containertypeid", ";").split(";", -1);
            String[] createdkeyid2Arrays = new String[]{""};
            String[] createdkeyid3Arrays = new String[]{""};
            for (int i = 0; i < createdkeyid1Arrays.length; ++i) {
                String createdkeyids = createdkeyid1Arrays[i];
                createdkeyids = createdkeyid2Arrays.length > i ? createdkeyids + ";" + createdkeyid2Arrays[i] : createdkeyids + ";";
                createdkeyids = createdkeyid3Arrays.length > i ? createdkeyids + ";" + createdkeyid3Arrays[i] : createdkeyids + ";";
                this.addDetailData(detailTableInfo, templatekeyids, createdkeyids);
            }
        }
    }

    private void addDetailData(ArrayList detailTableInfo, String templatekeyids, String createdkeyids) throws SapphireException {
        Iterator detailTableInfoIter = detailTableInfo.iterator();
        while (detailTableInfoIter.hasNext()) {
            String[] detailTableInfoIterArray = StringUtil.split((String)detailTableInfoIter.next(), ";");
            String[] detailTableColumns = detailTableInfoIterArray[1].split(",", -1);
            String[] templatekeyidArrays = templatekeyids.split(";", -1);
            String[] createdkeyidArrays = createdkeyids.split(";", -1);
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer fetchTemplateDetailsSQL = new StringBuffer();
            fetchTemplateDetailsSQL.append("SELECT ");
            fetchTemplateDetailsSQL.append(detailTableInfoIterArray[1]);
            fetchTemplateDetailsSQL.append(" FROM ");
            fetchTemplateDetailsSQL.append(detailTableInfoIterArray[0]);
            fetchTemplateDetailsSQL.append(" WHERE ");
            fetchTemplateDetailsSQL.append(detailTableColumns[0]).append("=");
            fetchTemplateDetailsSQL.append(safeSQL.addVar(templatekeyidArrays[0]));
            if (templatekeyidArrays[1].length() > 0) {
                fetchTemplateDetailsSQL.append(" AND ");
                fetchTemplateDetailsSQL.append(detailTableColumns[1]).append("=");
                fetchTemplateDetailsSQL.append(safeSQL.addVar(templatekeyidArrays[1]));
            }
            if (templatekeyidArrays[2].length() > 0) {
                fetchTemplateDetailsSQL.append(" AND ");
                fetchTemplateDetailsSQL.append(detailTableColumns[2]).append("=");
                fetchTemplateDetailsSQL.append(safeSQL.addVar(templatekeyidArrays[2]));
            }
            DataSet dsTemplate = this.getQueryProcessor().getPreparedSqlDataSet(fetchTemplateDetailsSQL.toString(), safeSQL.getValues());
            try {
                StringBuffer prepareStatementSQL = new StringBuffer();
                prepareStatementSQL.append("INSERT INTO ");
                prepareStatementSQL.append(detailTableInfoIterArray[0]).append("(");
                prepareStatementSQL.append(detailTableInfoIterArray[1]);
                prepareStatementSQL.append(") values(");
                for (int i = 0; i < detailTableColumns.length; ++i) {
                    prepareStatementSQL.append(" ?,");
                }
                prepareStatementSQL.deleteCharAt(prepareStatementSQL.length() - 1);
                prepareStatementSQL.append(" )");
                PreparedStatement statement = this.database.prepareStatement(prepareStatementSQL.toString());
                for (int i = 0; i < dsTemplate.getRowCount(); ++i) {
                    int j = 0;
                    statement.setString(j + 1, createdkeyidArrays[0]);
                    ++j;
                    if (templatekeyidArrays[1].length() > 0) {
                        statement.setString(j + 1, createdkeyidArrays[1]);
                        ++j;
                    }
                    if (templatekeyidArrays[2].length() > 0) {
                        statement.setString(j + 1, createdkeyidArrays[2]);
                        ++j;
                    }
                    while (j < detailTableColumns.length) {
                        statement.setString(j + 1, dsTemplate.getString(i, detailTableColumns[j]));
                        ++j;
                    }
                    statement.executeUpdate();
                }
            }
            catch (SQLException e) {
                this.setError("InsertError", "ERROR", "Failed to insert new detail values in table " + detailTableInfoIterArray[0]);
            }
        }
    }

    static {
        detailTableInfo.add("s_sampletypecontainertype;containertypeid,s_sampletypeid,activeflag,sampletypecontainertypedesc");
    }
}

