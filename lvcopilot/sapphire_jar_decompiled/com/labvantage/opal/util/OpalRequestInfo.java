/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;

public class OpalRequestInfo {
    private String LABVANTAGE_CVS_ID = "$Revision: 54135 $";
    private String __KeyId1 = null;
    private String __KeyId2 = null;
    private String __KeyId3 = null;

    public void populateKeysFromRSetItems(PageContext pageContext, String rsetid) throws Exception {
        StringBuffer keyid1List = new StringBuffer();
        StringBuffer keyid2List = new StringBuffer();
        StringBuffer keyid3List = new StringBuffer();
        Object sqlStmt = null;
        SQLGenerator sqlGenerator = null;
        int maxRowCount = 0;
        sqlGenerator = SQLFactory.getSqlGenerator(pageContext);
        if (sqlGenerator == null) {
            throw new Exception("Couldnot get SQLGenerator. Unsupported database.");
        }
        QueryProcessor qp = new QueryProcessor(pageContext);
        SafeSQL safeSQL = sqlGenerator.getKeysFromRSetItemsSQLStmt(rsetid);
        DataSet dataset = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (dataset != null) {
            maxRowCount = dataset.getRowCount();
            for (int count = 0; count < maxRowCount; ++count) {
                keyid1List.append(dataset.getValue(count, "keyid1", ""));
                keyid2List.append(dataset.getValue(count, "keyid2", ""));
                keyid3List.append(dataset.getValue(count, "keyid3", ""));
                if (count + 1 >= maxRowCount) continue;
                keyid1List.append(";");
                keyid2List.append(";");
                keyid3List.append(";");
            }
        }
        this.__KeyId1 = keyid1List.toString();
        this.__KeyId2 = keyid2List.toString();
        this.__KeyId3 = keyid3List.toString();
    }

    public void populateKeysFromSDIRequest(SDIRequest sdiRequest) {
        this.__KeyId1 = sdiRequest.getKeyid1List();
        this.__KeyId2 = sdiRequest.getKeyid2List();
        this.__KeyId3 = sdiRequest.getKeyid3List();
    }

    public String getKeyid1() {
        return this.__KeyId1;
    }

    public String getKeyId2() {
        return this.__KeyId2;
    }

    public String getKeyId3() {
        return this.__KeyId3;
    }
}

