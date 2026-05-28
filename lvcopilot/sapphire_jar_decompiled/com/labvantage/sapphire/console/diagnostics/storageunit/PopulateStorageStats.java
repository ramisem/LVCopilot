/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.console.diagnostics.BaseDiagnostic;
import com.labvantage.sapphire.console.diagnostics.DiagnosticException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public class PopulateStorageStats
extends BaseDiagnostic {
    public PopulateStorageStats(DBAccess database, ConnectionInfo conenctionInfo) {
        super(database, conenctionInfo);
    }

    public PopulateStorageStats(String webappid, ConnectionInfo connectionInfo) {
        super(webappid, connectionInfo);
    }

    @Override
    public String getTitle() {
        return "Populate Physical Store statistics";
    }

    @Override
    public String getDescription() {
        return "Populates the counts on all Physical Store to be displayed on Storage Explorer List and View pages";
    }

    @Override
    public String runDiagnostic(PropertyList properties) throws DiagnosticException {
        throw new DiagnosticException(1, "Populate statistics on existing Physical Stores when upgrading from LV version prior to LV 8.2");
    }

    @Override
    public String runRepair(PropertyList properties) throws DiagnosticException {
        ArrayList<String> list = new ArrayList<String>();
        try {
            this.database.createResultSet("select storageunitid from storageunit where linksdcid = 'PhysicalStore'");
            ResultSet rs = this.database.getResultSet();
            while (rs.next()) {
                list.add(rs.getString("storageunitid"));
            }
            rs.close();
        }
        catch (SQLException | SapphireException e) {
            e.printStackTrace();
        }
        if (list.size() > 0) {
            try {
                if (this.database.isOracle()) {
                    this.database.executePreparedUpdate("call LV_SUS.CollectTopList( ? )", new Object[]{OpalUtil.toDelimitedString(list, ";")});
                } else if (this.database.isSqlServer()) {
                    this.database.executePreparedUpdate("call dbo.LV_SUS_CollectTopList( ? )", new Object[]{OpalUtil.toDelimitedString(list, ";")});
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
        return "Populated Physical Store statistics successfully";
    }

    @Override
    public boolean canBeRepaired() {
        return true;
    }

    @Override
    public boolean canAutoRepair() {
        return true;
    }
}

