/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class LV_ArrayLayoutViewer
extends SDISnapshotViewer {
    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent zoneRendering;
        int i;
        ConfigReportContent arrayzones = new ConfigReportContent("Array Zones", this.translationProcessor);
        String tablelabel = "Array Zones";
        String itemdisplay = "[Zone]";
        String[] keycols = new String[]{"Zone"};
        DataSet srcArrayZones = this.getArrayZones(sourceItem.getSDIData());
        DataSet refArrayZones = this.getArrayZones(refItem == null ? new SDIData() : refItem.getSDIData());
        arrayzones.startSubSection(tablelabel, "");
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "arraylayoutzone", srcArrayZones.getColumns());
        arrayzones.renderDetailTablesDiff(columnTitleMap, "arraylayoutzone", tablelabel, itemdisplay, srcArrayZones, refArrayZones, keycols, this.getTranslationProcessor(), hideEmptyColumns);
        configReportContent.appendNodeContent(arrayzones, "arraylayoutzone", tablelabel);
        for (i = 0; i < srcArrayZones.getRowCount(); ++i) {
            if (refArrayZones.findRow("Zone", srcArrayZones.getString(i, "Zone")) == -1) {
                zoneRendering = this.renderZone(i, srcArrayZones, sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), "New");
                configReportContent.append(zoneRendering.toString());
                continue;
            }
            zoneRendering = this.renderZone(i, srcArrayZones, sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), "");
            configReportContent.append(zoneRendering.toString());
        }
        for (i = 0; i < refArrayZones.getRowCount(); ++i) {
            if (srcArrayZones.findRow("Zone", refArrayZones.getString(i, "Zone")) != -1) continue;
            zoneRendering = this.renderZone(i, refArrayZones, sourceItem.getSDIData(), refItem == null ? new SDIData() : refItem.getSDIData(), "Deleted");
            configReportContent.append(zoneRendering.toString());
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    private ConfigReportContent renderZone(int i, DataSet zones, SDIData srcSdiData, SDIData refSdiData, String status) {
        String currZone = zones.getString(i, "Zone", "");
        String color = zones.getString(i, "__Color", "");
        ConfigReportContent configReportContent = new ConfigReportContent("zone content", this.translationProcessor);
        HashMap<String, String> filter = new HashMap<String, String>();
        if (!currZone.equals("(FullArray)")) {
            if (status.equals("New")) {
                configReportContent.startSubHeading(ConfigReportContent.getNewString("Zone: " + currZone), "");
            } else if (status.equals("Deleted")) {
                configReportContent.startSubHeading(ConfigReportContent.getDeletedString("Zone: " + currZone), "");
            } else {
                configReportContent.startSubHeading("Zone: " + currZone, "");
            }
            DataSet allsrczoneitems = srcSdiData.getDataset("arraylayoutzoneitem");
            filter.put("arraylayoutzone", currZone);
            DataSet srczoneitems = allsrczoneitems.getFilteredDataSet(filter);
            DataSet allrefzoneitems = refSdiData.getDataset("arraylayoutzoneitem");
            if (allrefzoneitems == null) {
                allrefzoneitems = new DataSet();
            }
            filter = new HashMap();
            filter.put("arraylayoutzone", zones.getString(i, "Zone", ""));
            DataSet refzoneitems = allrefzoneitems.getFilteredDataSet(filter);
            DataSet srclayoutitems = srcSdiData.getDataset("arraylayoutitem");
            DataSet reflayoutitems = refSdiData.getDataset("arraylayoutitem");
            if (reflayoutitems != null) {
                reflayoutitems = new DataSet();
            }
            configReportContent.append(this.renderLayoutGrid(currZone, color, srczoneitems, srclayoutitems, refzoneitems, reflayoutitems));
        }
        return configReportContent;
    }

    private String renderLayoutGrid(String zone, String color, DataSet srczoneitems, DataSet srclayoutitems, DataSet refzoneitems, DataSet reflayoutitems) {
        ConfigReportContent grid = new ConfigReportContent("grid", this.translationProcessor);
        grid.startTable();
        int lastpos = srclayoutitems.getInt(srclayoutitems.getRowCount() - 1, "xpos");
        for (int xpos = 0; xpos <= lastpos; ++xpos) {
            HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
            filter.put("xpos", new BigDecimal(xpos));
            DataSet srcRowLayoutItems = srclayoutitems.getFilteredDataSet(filter);
            srcRowLayoutItems.sort("ypos");
            if (xpos == 0) {
                grid.startRow();
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>&nbsp;&nbsp;</TD>");
                for (int i = 0; i < srcRowLayoutItems.getRowCount(); ++i) {
                    String label = srcRowLayoutItems.getValue(i, "horizontallabel");
                    grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + label + "</TD>");
                }
                grid.endRow();
            }
            grid.startRow();
            String rowlabel = srcRowLayoutItems.getValue(0, "verticallabel");
            grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10px>" + rowlabel + "</TD>");
            for (int i = 0; i < srcRowLayoutItems.getRowCount(); ++i) {
                String ypos = srcRowLayoutItems.getValue(i, "ypos");
                HashMap<String, BigDecimal> findzone = new HashMap<String, BigDecimal>();
                findzone.put("xpos", new BigDecimal(xpos));
                findzone.put("ypos", new BigDecimal(ypos));
                DataSet match = srczoneitems.getFilteredDataSet(findzone);
                DataSet refmatch = refzoneitems.getFilteredDataSet(findzone);
                if (match != null && match.getRowCount() > 0) {
                    String srccontentString = this.parseContentString(zone, match.getValue(0, "contentstring", ""));
                    String refcontentString = "";
                    if (refmatch != null && refmatch.getRowCount() > 0) {
                        refcontentString = this.parseContentString(zone, refmatch.getValue(0, "contentstring", ""));
                    }
                    String compare = ConfigReportContent.getDiffString(srccontentString, refcontentString);
                    if (refcontentString.length() == 0) {
                        String image = "WEB-CORE/images/png/Add.png";
                        grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:" + color + "\"><img src=\"" + image + "\"/>" + compare + "</TD>");
                        continue;
                    }
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:" + color + "\">" + compare + "</TD>");
                    continue;
                }
                if (refmatch != null && refmatch.getRowCount() > 0) {
                    String refcontentString = this.parseContentString(zone, refmatch.getValue(0, "contentstring", ""));
                    String srccontentString = "";
                    String image = "WEB-CORE/images/png/Delete.png";
                    String compare = ConfigReportContent.getDiffString(srccontentString, refcontentString);
                    grid.append("<TD align=\"center\" width=10px style=\"font: Arial;font-size: 8pt;border: 1px solid black;padding: 5px;background-color:" + color + "\"><img src=\"" + image + "\"/>" + compare + "</TD>");
                    continue;
                }
                grid.append("<TD class=\"viewlistcol\" align=\"center\" width=10pt>&nbsp;&nbsp;</TD>");
            }
            grid.endRow();
        }
        grid.endTable();
        return grid.toString();
    }

    private String parseContentString(String zonename, String contentString) {
        String[] contents = StringUtil.split(contentString, ";");
        String sample = contents[0];
        String repeat = contents[1];
        String treatment = contents[2];
        String dilutionfactor = contents[3];
        String dilution = contents[4];
        String ret = ("" + zonename.charAt(0)).toUpperCase() + sample;
        if (repeat == null || repeat.length() <= 0 || !repeat.equals("0")) {
            // empty if block
        }
        if (dilution != null && dilution.length() > 0 && !dilution.equals("0") && !dilutionfactor.equals("1")) {
            ret = ret + "( D1:" + dilutionfactor + ")";
        }
        return ret;
    }

    private DataSet getArrayZones(SDIData sdiData) {
        DataSet raw = sdiData.getDataset("arraylayoutzone");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Zone", 0);
        ret.addColumn("Color", 0);
        ret.addColumn("Adhoc", 0);
        ret.addColumn("Loading Horizontal Priority", 0);
        ret.addColumn("Loading Vertical Priority", 0);
        if (raw != null) {
            for (int rawitem = 0; rawitem < raw.getRowCount(); ++rawitem) {
                if (raw.getString(rawitem, "arraylayoutzone", "").equals("(FullArray)")) continue;
                int row = ret.addRow();
                ret.setString(row, "Zone", raw.getString(rawitem, "arraylayoutzone", ""));
                String colorstr = raw.getString(rawitem, "color", "");
                ret.setString(row, "__Color", colorstr);
                if (colorstr.length() > 0) {
                    String box;
                    colorstr = box = "<table><tr><td style=\"background-color:" + colorstr + "\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td><td>" + colorstr + "</td></tr></table>";
                }
                ret.setString(row, "Color", colorstr);
                ret.setString(row, "Adhoc", raw.getString(rawitem, "adhocmodeflag", "N"));
                String loadinghorizontalpriority = raw.getValue(rawitem, "loadingpriorityhorizontal", "");
                String[] options = StringUtil.split(loadinghorizontalpriority, ";");
                String loadhorizontalstr = "";
                for (int o = 0; o < options.length; ++o) {
                    if (options[o].equals("C")) {
                        loadhorizontalstr = loadhorizontalstr + this.getContentOptioms(rawitem, raw);
                        continue;
                    }
                    if (options[o].equals("D")) {
                        loadhorizontalstr = loadhorizontalstr + this.getDilutionOptions(rawitem, raw);
                        continue;
                    }
                    if (!options[o].equals("R")) continue;
                    loadhorizontalstr = loadhorizontalstr + this.getRepeatOptions(rawitem, raw);
                }
                ret.setString(row, "Loading Horizontal Priority", loadhorizontalstr);
                String loadingverticalpriority = raw.getValue(rawitem, "loadingpriorityvertical", "");
                options = StringUtil.split(loadingverticalpriority, ";");
                String loadingverticalstr = "";
                for (int o = 0; o < options.length; ++o) {
                    if (options[o].equals("C")) {
                        loadingverticalstr = loadingverticalstr + this.getContentOptioms(rawitem, raw);
                        continue;
                    }
                    if (options[o].equals("D")) {
                        loadingverticalstr = loadingverticalstr + this.getDilutionOptions(rawitem, raw);
                        continue;
                    }
                    if (!options[o].equals("R")) continue;
                    loadingverticalstr = loadingverticalstr + this.getRepeatOptions(rawitem, raw);
                }
                ret.setString(row, "Loading Vertical Priority", loadingverticalstr);
            }
        }
        return ret;
    }

    private String getContentOptioms(int i, DataSet raw) {
        String ret = "";
        if (raw.getValue(i, "contentdirection", "").length() > 0) {
            ret = ret + " Direction:" + raw.getValue(i, "contentdirection", "");
        }
        if (raw.getValue(i, "contentbound", "").length() > 0) {
            ret = ret + " Bound:" + raw.getValue(i, "contentbound", "");
        }
        return ret;
    }

    private String getDilutionOptions(int i, DataSet raw) {
        String ret = "";
        if (raw.getValue(i, "dilutiondirection", "").length() > 0) {
            ret = ret + " Direction:" + raw.getValue(i, "dilutiondirection", "");
        }
        if (raw.getValue(i, "dilutionstep", "").length() > 0) {
            ret = ret + " Step:" + raw.getValue(i, "dilutionstep", "");
        }
        if (raw.getValue(i, "dilutionfactor", "").length() > 0) {
            ret = ret + " Factor:" + raw.getValue(i, "dilutionfactor", "");
        }
        if (raw.getValue(i, "dilutionfirstflag", "").length() > 0) {
            ret = ret + " Dilution First:" + raw.getValue(i, "dilutionfirstflag", "");
        }
        return ret;
    }

    private String getRepeatOptions(int i, DataSet raw) {
        String ret = "";
        if (raw.getValue(i, "repeatdirection", "").length() > 0) {
            ret = ret + " Direction:" + raw.getValue(i, "repeatdirection", "");
        }
        if (raw.getValue(i, "repeatcount", "").length() > 0) {
            ret = ret + " Count:" + raw.getValue(i, "repeatcount", "");
        }
        return ret;
    }

    @Override
    public String[] getIgnoreDataSets() {
        return new String[]{"arraylayoutitem", "arraylayoutzoneitem"};
    }
}

