/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.AbstractDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlDateParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlNumberParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class PreparedSqlDataSetProvider
extends AbstractDataSetProvider {
    private final String sdcId;
    private final String select;
    private final String from;
    private final String where;
    private final String groupBy;
    private final String orderBy;
    private final List<Object> preparedSqlParamList;
    private final boolean extendedDataTypes;
    private final Integer maxRowCount;

    public PreparedSqlDataSetProvider(PreparedSqlDataSetConfiguration preparedSqlDataSetConf, String connectionId, DataBindingMap dataBindingMap) throws SapphireException {
        super(connectionId);
        this.sdcId = preparedSqlDataSetConf.getSdcId().evaluate(dataBindingMap);
        this.select = preparedSqlDataSetConf.getSelect().evaluate(dataBindingMap);
        this.from = preparedSqlDataSetConf.getFrom().evaluate(dataBindingMap);
        this.where = preparedSqlDataSetConf.getWhere().evaluate(dataBindingMap);
        this.groupBy = preparedSqlDataSetConf.getGroupBy().evaluate(dataBindingMap);
        this.orderBy = preparedSqlDataSetConf.getOrderBy().evaluate(dataBindingMap);
        this.extendedDataTypes = preparedSqlDataSetConf.extendedDataTypes();
        this.maxRowCount = preparedSqlDataSetConf.getMaxRowCount().evaluateNoException(dataBindingMap);
        if (this.select.isEmpty()) {
            throw new IllegalArgumentException("Select clause is empty");
        }
        if (this.sdcId.isEmpty() && this.from.isEmpty()) {
            throw new IllegalArgumentException("From clause is empty and SDC ID is empty");
        }
        M18NUtil m18NUtilUser = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        M18NUtil m18NUtilSystem = new M18NUtil();
        this.preparedSqlParamList = new ArrayList<Object>();
        List<PreparedSqlParamConfiguration> preparedSqlParamConfList = preparedSqlDataSetConf.getPreparedSqlParamConfList();
        for (PreparedSqlParamConfiguration preparedSqlParamConf : preparedSqlParamConfList) {
            String paramStringValue = preparedSqlParamConf.getParamStringValue().evaluate(dataBindingMap);
            ArrayList<String> paramStringValueList = new ArrayList<String>();
            PreparedSqlParamConfiguration.PreparedSqlParamType paramType = preparedSqlParamConf.getPreparedSqlParamType();
            paramStringValueList.addAll(Arrays.asList(paramStringValue.split(preparedSqlParamConf.getParamStringValueSeparator(), -1)));
            for (String value : paramStringValueList) {
                Object param;
                if (!preparedSqlParamConf.isEnabled().evaluate(dataBindingMap).booleanValue()) continue;
                if (paramType == PreparedSqlParamConfiguration.PreparedSqlParamType.STRING) {
                    param = value;
                } else if (paramType == PreparedSqlParamConfiguration.PreparedSqlParamType.DATE) {
                    PreparedSqlDateParamConfiguration preparedSqlDateParamConf = preparedSqlParamConf.getPreparedSqlDateParamConfiguration();
                    param = this.getDateParam(preparedSqlDateParamConf, value, m18NUtilUser, m18NUtilSystem, dataBindingMap);
                } else if (paramType == PreparedSqlParamConfiguration.PreparedSqlParamType.NUMBER) {
                    PreparedSqlNumberParamConfiguration preparedSqlNumberParamConf = preparedSqlParamConf.getPreparedSqlNumberParamConfiguration();
                    param = this.getNumberParam(preparedSqlNumberParamConf, value, m18NUtilUser, m18NUtilSystem);
                } else {
                    throw new IllegalArgumentException("Unknown prepared SQL param type: " + (Object)((Object)paramType));
                }
                this.preparedSqlParamList.add(param);
            }
        }
    }

    private Object getNumberParam(PreparedSqlNumberParamConfiguration preparedSqlNumberParamConf, String value, M18NUtil m18NUtilUser, M18NUtil m18NUtilSystem) {
        BigDecimal param;
        PreparedSqlNumberParamConfiguration.PreparedSqlNumberParamType numberParamType = preparedSqlNumberParamConf.getPreparedSqlNumberParamType();
        if (numberParamType == PreparedSqlNumberParamConfiguration.PreparedSqlNumberParamType.SYSTEM) {
            param = m18NUtilSystem.parseBigDecimal(value);
        } else if (numberParamType == PreparedSqlNumberParamConfiguration.PreparedSqlNumberParamType.USER) {
            param = m18NUtilUser.parseBigDecimal(value);
        } else {
            throw new IllegalArgumentException("Unknown prepared SQL number param type: " + (Object)((Object)numberParamType));
        }
        return param;
    }

    private Object getDateParam(PreparedSqlDateParamConfiguration preparedSqlDateParamConf, String value, M18NUtil m18NUtilUser, M18NUtil m18NUtilSystem, DataBindingMap chartBindingMap) throws SapphireException {
        Timestamp param;
        PreparedSqlDateParamConfiguration.PreparedSqlDateParamType dateParamType = preparedSqlDateParamConf.getPreparedSqlDateParamType();
        if (dateParamType == PreparedSqlDateParamConfiguration.PreparedSqlDateParamType.SYSTEM) {
            param = new Timestamp(m18NUtilSystem.parseCalendar(value).getTimeInMillis());
        } else if (dateParamType == PreparedSqlDateParamConfiguration.PreparedSqlDateParamType.USER) {
            param = new Timestamp(m18NUtilUser.parseCalendar(value).getTimeInMillis());
        } else if (dateParamType == PreparedSqlDateParamConfiguration.PreparedSqlDateParamType.CUSTOM) {
            String customFormat = preparedSqlDateParamConf.getCustomFormat().evaluate(chartBindingMap);
            try {
                param = new Timestamp(new SimpleDateFormat(customFormat).parse(value).getTime());
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Cannot parse date string " + value + " using custom date format " + customFormat);
            }
        } else {
            throw new IllegalArgumentException("Unknown prepared SQL date param type: " + (Object)((Object)dateParamType));
        }
        return param;
    }

    @Override
    public DataSet getDataSet() {
        String fromClause;
        StringBuilder preparedSql = new StringBuilder();
        String dbms = this.getConnectionProcessor().getSapphireConnection().getSapphireDatabase().getDbms();
        String selectClause = "";
        selectClause = this.maxRowCount != null && dbms.equals("MSS") ? selectClause + " TOP " + this.maxRowCount + " " + this.select.trim() : this.select.trim();
        String securityWhereFragment = "";
        if (!this.sdcId.isEmpty()) {
            PropertyList dashboardSecurityWhereClauseProps = new PropertyList();
            dashboardSecurityWhereClauseProps.setProperty("sdcid", this.sdcId);
            try {
                this.getActionProcessor().processActionClass("com.labvantage.sapphire.modules.dashboard.util.DashboardSecurityWhereClause", dashboardSecurityWhereClauseProps);
            }
            catch (ActionException e) {
                throw new IllegalArgumentException("Cannot get security where clause for SDC ID:" + this.sdcId);
            }
            securityWhereFragment = dashboardSecurityWhereClauseProps.getProperty("whereclause");
        }
        String whereClause = !securityWhereFragment.isEmpty() ? securityWhereFragment + " AND " + this.where.trim() : this.where.trim();
        if (!this.sdcId.isEmpty()) {
            HashMap sdcProps = this.getSDCProcessor().getSDCProperties(this.sdcId);
            String tableId = (String)sdcProps.get("tableid");
            if (tableId.isEmpty()) {
                throw new IllegalArgumentException("Invalid SDC ID: " + this.sdcId);
            }
            fromClause = this.from.trim().isEmpty() ? tableId : tableId + " " + this.from.trim();
        } else {
            fromClause = this.from.trim();
        }
        if (this.maxRowCount != null && dbms.equals("ORA")) {
            preparedSql.append("SELECT * FROM (");
        }
        preparedSql.append("SELECT ").append(selectClause).append(" ");
        preparedSql.append("FROM ").append(fromClause).append(" ");
        if (!whereClause.isEmpty()) {
            preparedSql.append("WHERE ").append(whereClause).append(" ");
        }
        if (!this.groupBy.trim().isEmpty()) {
            preparedSql.append("GROUP BY ").append(this.groupBy.trim()).append(" ");
        }
        if (!this.orderBy.trim().isEmpty()) {
            preparedSql.append("ORDER BY ").append(this.orderBy.trim()).append(" ");
        }
        if (this.maxRowCount != null && dbms.equals("ORA")) {
            preparedSql.append(") WHERE ROWNUM <= ").append(this.maxRowCount).append("");
        }
        return this.getQueryProcessor().getPreparedSqlDataSet(preparedSql.toString(), this.preparedSqlParamList.toArray(), this.extendedDataTypes);
    }
}

