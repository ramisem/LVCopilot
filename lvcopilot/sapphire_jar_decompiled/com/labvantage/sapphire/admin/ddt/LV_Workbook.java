/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import java.util.Arrays;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Workbook
extends BaseSDCRules
implements ELNConstants {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "workbookdesc").length() == 0) {
                throw new SapphireException("Workbooks must have a workbook name");
            }
            primary.setValue(i, "workbookstatus", "InProgress");
            if (primary.getValue(i, "worksheettemplatesflag").length() != 0) continue;
            primary.setValue(i, "worksheettemplatesflag", "A");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            DataSet worksheets;
            if (Arrays.asList(primary.getColumns()).contains("workbookdesc") && primary.getValue(i, "workbookdesc").length() == 0) {
                throw new SapphireException("Workbooks must have a workbook name");
            }
            if (!Arrays.asList(primary.getColumns()).contains("workbookstatus")) continue;
            String status = primary.getValue(i, "workbookstatus");
            if (status.length() == 0) {
                throw new SapphireException("Workbooks must have a status set");
            }
            if (!status.equals("Complete") || (worksheets = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetname, authorid FROM worksheet WHERE workbookid = ? AND workbookversionid = ? AND worksheetstatus IN ( 'Pending', 'InProgress', 'PendingApproval' )", new Object[]{primary.getValue(i, "workbookid"), primary.getValue(i, "workbookversionid")})).size() <= 0) continue;
            StringBuffer message = new StringBuffer();
            for (int j = 0; j < worksheets.size() && j < 10; ++j) {
                message.append(worksheets.getValue(j, "worksheetname")).append(" - author: ").append(worksheets.getValue(j, "authorid")).append("\n");
            }
            throw new SapphireException("Workbook cannot be set to Complete because the following worksheets are incomplete or uncancelled:\n\n" + message + (worksheets.size() > 10 ? "\n and " + (worksheets.size() - 10) + " more" : ""));
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet workbooktemplate = sdiData.getDataset("workbooktemplate");
        if (workbooktemplate != null) {
            int seqs = 0;
            for (int i = 0; i < workbooktemplate.size(); ++i) {
                if (workbooktemplate.getValue(i, "workbooktemplateid").equals("seq")) {
                    this.database.createPreparedResultSet("getseq", "SELECT " + (this.database.isOracle() ? "nvl( max( to_number( workbooktemplateid ) ) + 1 + " + seqs + ", " + seqs + " )" : "isnull( max( cast( workbooktemplateid AS Integer ) ) + 1 + " + seqs + ", " + seqs + " )") + " as workbooktemplateid FROM workbooktemplate where workbookid = ?", new Object[]{workbooktemplate.getValue(i, "workbookid")});
                    this.database.getNext("getseq");
                    workbooktemplate.setValue(i, "workbooktemplateid", this.database.getValue("getseq", "workbooktemplateid"));
                    ++seqs;
                }
                if (workbooktemplate.getValue(i, "worksheetversionid").toUpperCase().startsWith("C")) {
                    workbooktemplate.setValue(i, "worksheetversionid", "");
                }
                if (workbooktemplate.getValue(i, "typeflag").length() != 0) continue;
                workbooktemplate.setValue(i, "typeflag", "W");
            }
        }
    }
}

