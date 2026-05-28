/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.AbstractDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.QueryArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.QueryDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.util.Calendar;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class QueryDataSetProvider
extends AbstractDataSetProvider {
    private final String queryId;
    private final String basedOnId;
    private final List<QueryArgumentConfiguration> queryArguments;
    private final BindingMap bindingMap;
    private final String plotIdColumn;
    private final String plotIdValue;
    private final String fromClause;
    private final String whereClause;
    private final String orderByClause;

    public QueryDataSetProvider(String connectionId, QueryDataSetConfiguration queryDataSetConf, DataBindingMap bindingMap) throws SapphireException {
        super(connectionId);
        this.queryId = queryDataSetConf.getQueryId();
        this.basedOnId = queryDataSetConf.getBasedOnId();
        this.queryArguments = queryDataSetConf.getQueryArguments();
        this.bindingMap = bindingMap;
        this.plotIdColumn = queryDataSetConf.getPlotIdColumn();
        this.plotIdValue = queryDataSetConf.getPlotIdColumnValue();
        this.fromClause = queryDataSetConf.getFromClause().evaluate(bindingMap);
        this.whereClause = queryDataSetConf.getWhereClause().evaluate(bindingMap);
        this.orderByClause = queryDataSetConf.getOrderByClause().evaluate(bindingMap);
    }

    @Override
    public DataSet getDataSet() {
        String argSql = "select argid, argtype from queryarg where queryid=? and basedonid=? order by usersequence";
        Object[] argParams = new String[]{this.queryId, this.basedOnId};
        DataSet argDs = this.getQueryProcessor().getPreparedSqlDataSet(argSql, argParams);
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        String[] args = new String[argDs.getRowCount()];
        for (int i = 0; i < argDs.getRowCount(); ++i) {
            String argId = argDs.getValue(i, "argid", "");
            String argType = argDs.getValue(i, "argtype", "");
            boolean isDateTime = argType.equalsIgnoreCase("absreldt") || argType.equalsIgnoreCase("dateonly");
            boolean dateOnly = argType.equalsIgnoreCase("dateonly");
            args[i] = "";
            for (QueryArgumentConfiguration queryArgumentConf : this.queryArguments) {
                if (!queryArgumentConf.getArgumentId().equals(argId)) continue;
                args[i] = queryArgumentConf.getArgumentValue().evaluateNoException(this.bindingMap);
                if (!isDateTime || !queryArgumentConf.convertToSystemDate() || args[i].isEmpty()) continue;
                Calendar cal = m18n.parseCalendar(args[i], true);
                if (dateOnly) {
                    cal.set(11, 0);
                    cal.set(12, 0);
                    cal.set(13, 0);
                    cal.set(14, 0);
                }
                args[i] = m18n.getSysQueryDateFormat().format(cal.getTime());
            }
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(this.basedOnId);
        sdiRequest.setRequestItem("primary");
        if (!this.queryId.isEmpty()) {
            sdiRequest.setQueryid(this.queryId);
        }
        if (args.length > 0) {
            sdiRequest.setQueryParams(args);
        }
        if (!this.fromClause.isEmpty()) {
            sdiRequest.setQueryFrom(this.fromClause);
        }
        if (!this.whereClause.isEmpty()) {
            sdiRequest.setQueryWhere(this.whereClause);
        }
        if (!this.orderByClause.isEmpty()) {
            sdiRequest.setQueryOrderBy(this.orderByClause);
        }
        sdiRequest.setRetainRsetid(true);
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        this.setRSetId(sdiData.getRsetid());
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) {
            primary = new DataSet();
        }
        primary.addColumn(this.plotIdColumn, 0);
        primary.setValue(-1, this.plotIdColumn, this.plotIdValue);
        return primary;
    }
}

