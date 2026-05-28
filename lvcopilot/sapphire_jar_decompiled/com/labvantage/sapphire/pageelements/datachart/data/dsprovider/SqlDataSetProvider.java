/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.AbstractDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.SqlDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public final class SqlDataSetProvider
extends AbstractDataSetProvider {
    private final String sql;

    public SqlDataSetProvider(SqlDataSetConfiguration sqlDataSetConf, String connectionId, DataBindingMap bindingMap) throws SapphireException {
        super(connectionId);
        if (sqlDataSetConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.sql = sqlDataSetConf.getSql().evaluate(bindingMap);
        if (!this.sql.isEmpty() && !this.sql.trim().toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Only select clause is allowed: " + this.sql);
        }
    }

    @Override
    public DataSet getDataSet() {
        DataSet dataSet = null;
        if (!this.sql.isEmpty()) {
            dataSet = this.getQueryProcessor().getSqlDataSet(this.sql, true);
        }
        if (dataSet == null) {
            dataSet = new DataSet();
        }
        return dataSet;
    }
}

