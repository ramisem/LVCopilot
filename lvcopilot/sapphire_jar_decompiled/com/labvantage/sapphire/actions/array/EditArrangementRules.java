/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.array;

import com.labvantage.sapphire.actions.array.ArrayUtil;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditArrangementRules
extends BaseAction
implements sapphire.action.EditArrangementRules {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int i;
        String arraylayoutid = properties.getProperty("arraylayoutid", "");
        String arraylayoutversionid = properties.getProperty("arraylayoutversionid", "");
        if (arraylayoutid.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Array layout id is mandatory"));
        }
        if (arraylayoutversionid.length() == 0) {
            arraylayoutversionid = ArrayUtil.getArrayLayoutCurrentVersion(this.getQueryProcessor(), arraylayoutid);
        }
        String horizontallabel = properties.getProperty("horizontallabel", "");
        String verticallabel = properties.getProperty("verticallabel", "");
        if (horizontallabel.length() == 0 || verticallabel.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Horizontal and vertical labels are mandatory for editing arrangement rules"));
        }
        String zone = properties.getProperty("zone", "");
        if (zone.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Zone is mandatory"));
        }
        String samplenumber = properties.getProperty("samplenumber", "");
        String repeat = properties.getProperty("repeat", "");
        String dilution = properties.getProperty("dilution", "");
        String dilutionfactor = properties.getProperty("dilutionfactor", "");
        if (samplenumber.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Sample Number is mandatory"));
        }
        if (repeat.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Repeat is mandatory"));
        }
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(arraylayoutid);
        safeSQL.addVar(arraylayoutversionid);
        String sql = "SELECT * FROM arraylayoutitem WHERE arraylayoutid = ? AND arraylayoutversionid = ? ";
        DataSet arraylayoutitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (arraylayoutitems == null || arraylayoutitems.getRowCount() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to get array layout details for specified arraylayoutid/arraylayoutversionid"));
        }
        sql = "SELECT * FROM arraylayoutzoneitem  WHERE arraylayoutid = ? and arraylayoutversionid = ? ";
        DataSet arraylayoutzoneitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (arraylayoutzoneitems == null || arraylayoutzoneitems.getRowCount() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Failed to get array layout details for specified arraylayoutid/arraylayoutversionid"));
        }
        String[] horizontallabels = StringUtil.split(horizontallabel, ";");
        String[] verticallabels = StringUtil.split(verticallabel, ";");
        DataSet edits = new DataSet();
        String[] zones = StringUtil.split(zone, ";");
        String[] samplenums = StringUtil.split(samplenumber, ";");
        String[] repeats = StringUtil.split(repeat, ";");
        if (horizontallabels.length != verticallabels.length || horizontallabels.length != zones.length || horizontallabels.length != samplenums.length || horizontallabels.length != repeats.length) {
            throw new SapphireException(this.getTranslationProcessor().translate("Mismatch in number of items specified in the labels, zones, samplenumbers, repeats"));
        }
        if (dilution.length() == 0) {
            dilution = PropertyUtil.repeat("0", horizontallabels.length, ";");
        }
        String[] dilutions = StringUtil.split(dilution, ";");
        if (dilutionfactor.length() == 0) {
            dilutionfactor = PropertyUtil.repeat("0", horizontallabels.length, ";");
        }
        String[] dfs = StringUtil.split(dilutionfactor, ";");
        for (i = 0; i < horizontallabels.length; ++i) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("horizontallabel", horizontallabels[i]);
            filter.put("verticallabel", verticallabels[i]);
            DataSet matchLayoutItem = arraylayoutitems.getFilteredDataSet(filter);
            if (matchLayoutItem.getRowCount() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Specified label does not exist in the array layout"));
            }
            int xpos = matchLayoutItem.getInt(0, "xpos");
            int ypos = matchLayoutItem.getInt(0, "ypos");
            filter = new HashMap();
            filter.put("xpos", new BigDecimal(xpos));
            filter.put("ypos", new BigDecimal(ypos));
            filter.put("arraylayoutzone", zones[i]);
            DataSet matchZoneItem = arraylayoutzoneitems.getFilteredDataSet(filter);
            if (matchZoneItem.getRowCount() == 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Failed to find matching array layout zone item for xpos:" + xpos + ", ypos:" + ypos + ", arraylayoutzone:" + zones[i]));
            }
            System.out.println("Found array layout zone item for  xpos:" + xpos + ", ypos:" + ypos + ", arraylayoutzone:" + zones[i]);
            edits.copyRow(matchZoneItem, 0, 1);
            String contentString = samplenums[i] + ";" + repeats[i] + ";0;" + dilutions[i] + ";" + dfs[i];
            edits.setString(i, "contentstring", contentString);
        }
        for (i = 0; i < edits.getRowCount(); ++i) {
            String updateSQL = "UPDATE arraylayoutzoneitem SET contentstring = ? WHERE arraylayoutid = ? AND arraylayoutversionid = ?  AND arraylayoutzone = ? AND xpos = ? AND ypos = ?";
            this.getQueryProcessor().execPreparedUpdate(updateSQL, new Object[]{edits.getString(i, "contentstring"), edits.getString(i, "arraylayoutid"), edits.getString(i, "arraylayoutversionid"), edits.getString(i, "arraylayoutzone"), edits.getInt(i, "xpos"), edits.getInt(i, "ypos")});
        }
    }
}

