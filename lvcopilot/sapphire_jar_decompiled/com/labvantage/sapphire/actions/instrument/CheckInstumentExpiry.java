/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.instrument;

import java.time.LocalDateTime;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CheckInstumentExpiry
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringBuilder sql = new StringBuilder("select i.instrumentid, c.expirationdt, c.graceperiod, c.graceperiodunits, i.instrumentstatus");
        sql.append(" from s_sdicertification c, instrument i");
        sql.append(" where i.instrumentid = c.resourcekeyid1");
        sql.append(" and c.resourcesdcid = 'Instrument'");
        sql.append(" and c.certificationtype='Instrument'");
        sql.append(" and i.instrumentstatus='Available'");
        sql.append(" and i.certificationreqflag='Y'");
        DataSet instrumentCertificationDataSet = this.getQueryProcessor().getSqlDataSet(sql.toString());
        if (instrumentCertificationDataSet.getRowCount() > 0) {
            for (int i = 0; i < instrumentCertificationDataSet.getRowCount(); ++i) {
                String instrumentId = instrumentCertificationDataSet.getString(i, "instrumentid");
                Calendar expirationDt = instrumentCertificationDataSet.getCalendar(i, "expirationdt");
                if (expirationDt == null) continue;
                int gracePeriod = instrumentCertificationDataSet.getInt(i, "graceperiod");
                String graceperiodUnits = instrumentCertificationDataSet.getString(i, "graceperiodunits");
                LocalDateTime expirationLocalDate = LocalDateTime.ofInstant(expirationDt.toInstant(), expirationDt.getTimeZone().toZoneId());
                LocalDateTime expiryDate = null;
                if (gracePeriod > 0 && graceperiodUnits != null) {
                    switch (graceperiodUnits) {
                        case "Years": {
                            expiryDate = expirationLocalDate.plusYears(gracePeriod);
                            break;
                        }
                        case "Months": {
                            expiryDate = expirationLocalDate.plusMonths(gracePeriod);
                            break;
                        }
                        case "Weeks": {
                            expiryDate = expirationLocalDate.plusWeeks(gracePeriod);
                            break;
                        }
                        case "Days": {
                            expiryDate = expirationLocalDate.plusDays(gracePeriod);
                        }
                    }
                } else {
                    expiryDate = expirationLocalDate;
                }
                if (!expiryDate.isBefore(LocalDateTime.now(expirationDt.getTimeZone().toZoneId()))) continue;
                String updateInstrumentStatusSql = "UPDATE instrument set instrumentstatus = ? where instrumentid = ?";
                this.database.executePreparedUpdate(updateInstrumentStatusSql, new Object[]{"Unavailable", instrumentId});
            }
        }
    }
}

