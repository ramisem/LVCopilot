/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.QueryProcessor;
import sapphire.action.ActionConstants;
import sapphire.util.DataSet;

public class SpecLimitHandler
implements ActionConstants {
    private QueryProcessor qp;

    public SpecLimitHandler(QueryProcessor qp) {
        this.qp = qp;
    }

    public void injectSpecLimits(DataSet sdidataitem, String specId, String specVersionId) {
        if (sdidataitem == null) {
            return;
        }
        DataSet speclimits = this.getSpecLimits(specId, specVersionId, this.qp);
        if (speclimits == null) {
            return;
        }
        List<String> limittypes = this.getLimitTypes(speclimits);
        for (String limittype : limittypes) {
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_low", 0);
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_low_op", 0);
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_target", 0);
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_high", 0);
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_high_op", 0);
            sdidataitem.addColumn("spec_" + limittype.replaceAll(" ", "") + "_condition", 0);
        }
        for (int i = 0; i < sdidataitem.getRowCount(); ++i) {
            String paramlistid = sdidataitem.getString(i, "paramlistid");
            String paramlistversionid = sdidataitem.getString(i, "paramlistversionid");
            String variantid = sdidataitem.getString(i, "variantid");
            String paramid = sdidataitem.getString(i, "paramid");
            String paramtype = sdidataitem.getString(i, "paramtype");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("paramlistid", paramlistid);
            filter.put("paramlistversionid", paramlistversionid);
            filter.put("variantid", variantid);
            filter.put("paramid", paramid);
            filter.put("paramtype", paramtype);
            DataSet filteredSpecLimits = speclimits.getFilteredDataSet(filter);
            for (int j = 0; j < filteredSpecLimits.getRowCount(); ++j) {
                String limittypeid = filteredSpecLimits.getString(j, "limittypeid", "").replaceAll(" ", "");
                String operator1 = filteredSpecLimits.getValue(j, "operator1", "");
                String value1 = filteredSpecLimits.getValue(j, "value1", "");
                String operator2 = filteredSpecLimits.getValue(j, "operator2", "");
                String value2 = filteredSpecLimits.getValue(j, "value2", "");
                String condition = filteredSpecLimits.getValue(j, "condition", "");
                sdidataitem.setString(i, "spec_" + limittypeid + "_condition", condition);
                if (operator1.equals("<") || operator1.equals("<=")) {
                    sdidataitem.setString(i, "spec_" + limittypeid + "_high", value1);
                    sdidataitem.setString(i, "spec_" + limittypeid + "_high_op", operator1);
                }
                if (operator2.equals("<") || operator2.equals("<=")) {
                    sdidataitem.setString(i, "spec_" + limittypeid + "_high", value2);
                    sdidataitem.setString(i, "spec_" + limittypeid + "_high_op", operator2);
                }
                if (operator1.equals(">") || operator1.equals(">=")) {
                    sdidataitem.setString(i, "spec_" + limittypeid + "_low", value1);
                    sdidataitem.setString(i, "spec_" + limittypeid + "_low_op", operator1);
                }
                if (operator2.equals(">") || operator2.equals(">=")) {
                    sdidataitem.setString(i, "spec_" + limittypeid + "_low", value2);
                    sdidataitem.setString(i, "spec_" + limittypeid + "_low_op", operator2);
                }
                if (!operator1.equals("=") && !operator1.equals("In")) continue;
                sdidataitem.setString(i, "spec_" + limittypeid + "_target", value1);
            }
        }
    }

    private List<String> getLimitTypes(DataSet ds) {
        ArrayList<String> retval = new ArrayList<String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String limittypeid = ds.getString(i, "limittypeid", "");
            if (retval.contains(limittypeid)) continue;
            retval.add(limittypeid);
        }
        return retval;
    }

    private DataSet getSpecLimits(String specId, String specVersionId, QueryProcessor qp) {
        String specLimitSql = "SELECT s.specid, s.specversionid, spi.paramlistid, spi.paramlistversionid, spi.variantid, spi.paramid, spi.paramtype, spl.operator1, spl.operator2, spl.value1, spl.value2, spl.value1num, spl.value2num, slt.limittypeid, slt.condition FROM spec s, specparamitems spi, specparamlimits spl, speclimittype slt WHERE s.specid=? AND s.specversionid=? AND s.specid=spi.specid AND s.specversionid=spi.specversionid AND spi.specid=spl.specid AND spi.specversionid=spl.specversionid AND spi.paramlistid=spl.paramlistid AND spi.paramlistversionid=spl.paramlistversionid AND spi.variantid=spl.variantid AND spi.paramid=spl.paramid AND spi.paramtype=spl.paramtype AND slt.specid=s.specid AND slt.specversionid=s.specversionid AND slt.limittypesequence=spl.limittypesequence";
        Object[] specLimitParams = new String[]{specId, specVersionId};
        return qp.getPreparedSqlDataSet(specLimitSql, specLimitParams);
    }
}

