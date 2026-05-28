/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.mss;

public class DataDetailView {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String getDataitemSpecsAndLimits(String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicateid, String specid, String specversionid, String keyidwhere, String paramlistidwhere, String paramlistversionidwhere, String variantidwhere, String datasetwhere, String paramidwhere, String paramtypewhere, String replicateidwhere, String specidwhere, String specversionidwhere) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT sdis.sdcid, sdis.keyid1, sdis.keyid2, sdis.keyid3, sdis.usersequence , ");
        sqlStmt.append("  sdis.paramlistid, sdis.paramlistversionid, sdis.variantid, sdis.dataset, sdis.paramid, sdis.paramtype, sdis.replicateid,  ");
        sqlStmt.append("  sdis.specid, sdis.specversionid, sdis.condition, spl.limittypesequence,  ");
        sqlStmt.append("  IsNull (spl.operator1, '') + IsNull (spl.value1,'') val1, ");
        sqlStmt.append("  IsNull (spl.operator2, '') + IsNull (spl.value2, '') val2, spl.unitsid,");
        sqlStmt.append("  IsNull (slt.limittypeid,'') + '(' + IsNull ( slt.condition,'') + ')' typecondition ");
        sqlStmt.append("FROM sdidataitemspec sdis, specparamitems spi, specparamlimits spl, speclimittype slt ");
        sqlStmt.append("WHERE sdis.sdcid = 'Sample' ");
        sqlStmt.append(keyidwhere.replaceAll("keyid1", "sdis.keyid1"));
        sqlStmt.append("AND sdis.keyid2 = '(null)' ");
        sqlStmt.append("AND sdis.keyid3 = '(null)' ");
        if (!paramlistid.equalsIgnoreCase("")) {
            sqlStmt.append(paramlistidwhere.replaceAll("paramlistid", "sdis.paramlistid"));
        }
        if (!paramlistversionid.equalsIgnoreCase("")) {
            sqlStmt.append(paramlistversionidwhere.replaceAll("paramlistversionid", "sdis.paramlistversionid"));
        }
        if (!variantid.equalsIgnoreCase("")) {
            sqlStmt.append(variantidwhere.replaceAll("variantid", "sdis.variantid"));
        }
        if (!dataset.equalsIgnoreCase("")) {
            sqlStmt.append(datasetwhere.replaceAll("dataset", "sdis.dataset"));
        }
        if (!paramid.equalsIgnoreCase("")) {
            sqlStmt.append(paramidwhere.replaceAll("paramid", "sdis.paramid"));
        }
        if (!paramtype.equalsIgnoreCase("")) {
            sqlStmt.append(paramtypewhere.replaceAll("paramtype", "sdis.paramtype"));
        }
        if (!replicateid.equalsIgnoreCase("")) {
            sqlStmt.append(replicateidwhere.replaceAll("replicateid", "sdis.replicateid"));
        }
        if (!specid.equalsIgnoreCase("")) {
            sqlStmt.append(specidwhere.replaceAll("specid", "sdis.specid"));
        }
        if (!specversionid.equalsIgnoreCase("")) {
            sqlStmt.append(specversionidwhere.replaceAll("specversionid", "sdis.specversionid"));
        }
        sqlStmt.append("AND sdis.specid = spi.specid ");
        sqlStmt.append("AND sdis.specversionid = spi.specversionid ");
        sqlStmt.append("AND ");
        sqlStmt.append(" ( ");
        sqlStmt.append("  ( ");
        sqlStmt.append("       spi.allowanyparamlistflag = 'Y' ");
        sqlStmt.append("  ) ");
        sqlStmt.append("  OR ");
        sqlStmt.append("  ( ");
        sqlStmt.append("       sdis.paramlistid = spi.paramlistid ");
        sqlStmt.append("   AND sdis.paramlistversionid = spi.paramlistversionid ");
        sqlStmt.append("   AND sdis.variantid = spi.variantid ");
        sqlStmt.append("   AND (spi.allowanyparamlistflag = 'N' OR spi.allowanyparamlistflag IS NULL OR spi.allowanyparamlistflag = '') ");
        sqlStmt.append("  ) ");
        sqlStmt.append("  OR ");
        sqlStmt.append("  ( ");
        sqlStmt.append("       sdis.paramlistid = spi.paramlistid ");
        sqlStmt.append("   AND sdis.variantid = spi.variantid ");
        sqlStmt.append("   AND spi.allowanyparamlistflag = 'V' ");
        sqlStmt.append("  ) ");
        sqlStmt.append("  OR ");
        sqlStmt.append("  ( ");
        sqlStmt.append("       sdis.paramlistid = spi.paramlistid ");
        sqlStmt.append("   AND spi.allowanyparamlistflag = 'A' ");
        sqlStmt.append("  ) ");
        sqlStmt.append(" ) ");
        sqlStmt.append("AND sdis.paramid = spi.paramid ");
        sqlStmt.append("AND sdis.paramtype = spi.paramtype ");
        sqlStmt.append("AND spl.specid = spi.specid ");
        sqlStmt.append("AND spl.specversionid = spi.specversionid ");
        sqlStmt.append("AND spl.paramlistid = spi.paramlistid ");
        sqlStmt.append("AND spl.paramlistversionid = spi.paramlistversionid ");
        sqlStmt.append("AND spl.variantid = spi.variantid ");
        sqlStmt.append("AND spl.paramid = spi.paramid ");
        sqlStmt.append("AND spl.paramtype = spi.paramtype ");
        sqlStmt.append("AND slt.specid = spl.specid ");
        sqlStmt.append("AND slt.specversionid = spl.specversionid ");
        sqlStmt.append("AND slt.limittypesequence = spl.limittypesequence ");
        sqlStmt.append("ORDER BY sdis.keyid1, sdis.paramlistid, sdis.paramlistversionid, sdis.variantid,  ");
        sqlStmt.append("  sdis.dataset, sdis.paramid, sdis.paramtype, sdis.replicateid,  ");
        sqlStmt.append("  sdis.specid, sdis.specversionid, sdis.usersequence");
        return sqlStmt.toString();
    }
}

