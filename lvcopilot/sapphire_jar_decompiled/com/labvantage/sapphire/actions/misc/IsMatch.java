/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class IsMatch
extends BaseAction
implements sapphire.action.IsMatch {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String ismatch;
        block21: {
            String sdcid = properties.getProperty("sdcid");
            String columnid = properties.getProperty("columnid");
            ismatch = "No";
            if (sdcid.length() <= 0) throw new SapphireException("INVALID_PROPERTY", "The sdcid and columnid properties were missing.");
            if (columnid.length() <= 0) throw new SapphireException("INVALID_PROPERTY", "The sdcid and columnid properties were missing.");
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT " + columnid);
            sql.append(" FROM " + sdcProcessor.getProperty(sdcid, "tableid"));
            int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
            for (int i = 0; i < keycols; ++i) {
                if (i == 0) {
                    sql.append(" WHERE ");
                } else {
                    sql.append(" AND ");
                }
                sql.append(sdcProcessor.getProperty(sdcid, "keycolid" + String.valueOf(i + 1)) + " = " + safeSQL.addVar(properties.getProperty("keyid" + String.valueOf(i + 1))));
            }
            this.logger.info("Executing: " + sql.toString());
            try {
                this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
                if (!this.database.getNext()) throw new SapphireException("INVALID_PROPERTY", "No rows where found using " + sql + ". Ignoring error and continuing.");
                ResultSetMetaData rsmd = this.database.getResultSet().getMetaData();
                if (!this.database.isOracle() || rsmd.getColumnType(1) != 12) {
                    if (!this.database.isSqlServer()) throw new SapphireException("INVALID_PROPERTY", "The column '" + columnid + "' is not a string.");
                    if (rsmd.getColumnType(1) != -9) throw new SapphireException("INVALID_PROPERTY", "The column '" + columnid + "' is not a string.");
                }
                String comparevalue = properties.getProperty("value");
                if (this.database.getString(columnid) != null) {
                    int start;
                    int length;
                    String originalvalue = this.database.getString(columnid);
                    int n = properties.getProperty("length").length() > 0 ? Integer.parseInt(properties.getProperty("length")) : (length = properties.getProperty("Length").length() > 0 ? Integer.parseInt(properties.getProperty("Length")) : 0);
                    int n2 = properties.getProperty("start").length() > 0 ? Integer.parseInt(properties.getProperty("start")) : (start = properties.getProperty("Start").length() > 0 ? Integer.parseInt(properties.getProperty("Start")) : 0);
                    if (start > 0) {
                        --start;
                        if (length == 0) {
                            length = comparevalue.length();
                        }
                    }
                    if (length > 0 && length < comparevalue.length()) {
                        comparevalue = comparevalue.substring(0, length);
                    }
                    if (start != 0 || length != 0) {
                        if (start == 0 && length > 0) {
                            if (length < comparevalue.length()) {
                                comparevalue = comparevalue.substring(0, length);
                            }
                            if (length < originalvalue.length()) {
                                originalvalue = originalvalue.substring(0, length);
                            }
                        } else if (start > 0 && length == 0) {
                            originalvalue = start < originalvalue.length() ? originalvalue.substring(start) : "";
                        } else {
                            originalvalue = start < originalvalue.length() ? originalvalue.substring(start) : "";
                            if (length < comparevalue.length()) {
                                comparevalue = comparevalue.substring(0, length);
                            }
                            if (length < originalvalue.length()) {
                                originalvalue = originalvalue.substring(0, length);
                            }
                        }
                    }
                    if (Trace.on) {
                        this.logger.info("Comparing '" + comparevalue + "' with '" + originalvalue + "'");
                    }
                    ismatch = comparevalue.equalsIgnoreCase(originalvalue) ? "Yes" : "No";
                    break block21;
                }
                if (comparevalue.length() == 0) {
                    ismatch = "Yes";
                }
            }
            catch (SQLException sqle) {
                throw new SapphireException("INVALID_PROPERTY", "Could not lookup the column type: " + ErrorUtil.extractMessageFromException(sqle, ErrorUtil.isUserAdmin(this.getConnectionId())), sqle);
            }
        }
        properties.setProperty("ismatch", ismatch);
    }
}

