/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.argbar.Argument;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.SQLValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class SQLArgumentValue
extends AbstractArgumentValue
implements Serializable {
    private final SQLValueConfiguration sqlValueConf;

    public SQLArgumentValue(SQLValueConfiguration sqlValueConf, String connectionId, ArgumentBarBindingMap bindingMap, Argument parent) {
        super(connectionId, sqlValueConf.getParent(), parent);
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        this.sqlValueConf = sqlValueConf;
    }

    @Override
    public List<String> getDefaultValues(ArgumentBarBindingMap bindingMap) throws SapphireException {
        if (bindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        ArrayList<String> defaultValueList = new ArrayList<String>();
        String sql = this.sqlValueConf.getSql().evaluate(bindingMap);
        if (!sql.trim().toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Argument value SQL clause is not a select clause");
        }
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        if (ds.getRowCount() > 0) {
            String valueColumnId = this.sqlValueConf.getValueColumnId();
            if (valueColumnId.isEmpty()) {
                valueColumnId = ds.getColumnId(0);
            }
            if (!ds.isValidColumn(valueColumnId)) {
                throw new IllegalArgumentException("Value column ID not found in data set: " + valueColumnId);
            }
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String value = ds.getValue(i, valueColumnId);
                defaultValueList.add(value);
            }
        }
        return defaultValueList;
    }
}

