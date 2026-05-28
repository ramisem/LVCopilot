/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.LV_SecuritySetRO;
import java.io.File;
import java.util.HashMap;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class LV_SecuritySetUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderSecuritySetInfo(BaseSDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet Info: ");
        LV_SecuritySetRO securitySetRO = (LV_SecuritySetRO)ro;
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addRowItem("Security Set", securitySetRO.getSecuritySetId(), 4);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Description", securitySetRO.getDescription(), 4);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Owner SDI", securitySetRO.getOwnerSDI());
        configReportContent.addRowItem("Owner Check", securitySetRO.getOwnerCheckFlag());
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addRowItem("Notes", securitySetRO.getPrimaryValue("notes"), 4);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderSecuritySetInfoDiff(BaseSDCRO srcRO, BaseSDCRO refRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet Info: ");
        LV_SecuritySetRO srcSecuritySetRO = (LV_SecuritySetRO)srcRO;
        LV_SecuritySetRO refSecuritySetRO = (LV_SecuritySetRO)refRO;
        configReportContent.startTable();
        configReportContent.startRow();
        if (refSecuritySetRO != null && refSecuritySetRO.currentSDI != null) {
            configReportContent.addDiffRowItem("Security Set ID", srcSecuritySetRO.getSecuritySetId(), refSecuritySetRO.getSecuritySetId(), 4, false, this.getTranslationProcessor(), false);
            configReportContent.endRow();
        } else {
            configReportContent.addDiffRowItem("Security Set ID", srcSecuritySetRO.getSecuritySetId(), "", 4, false, this.getTranslationProcessor(), false);
            configReportContent.endRow();
        }
        configReportContent.startRow();
        if (refSecuritySetRO != null && refSecuritySetRO.currentSDI != null) {
            configReportContent.addDiffRowItem("Description", srcSecuritySetRO.getDescription(), refSecuritySetRO.getDescription(), 4, false, this.getTranslationProcessor(), false);
        } else {
            configReportContent.addDiffRowItem("Description", srcSecuritySetRO.getDescription(), "", 4, false, this.getTranslationProcessor(), false);
        }
        configReportContent.endRow();
        configReportContent.startRow();
        if (refSecuritySetRO != null && refSecuritySetRO.currentSDI != null) {
            configReportContent.addDiffRowItem("Owner SDI", srcSecuritySetRO.getOwnerSDI(), refSecuritySetRO.getOwnerSDI());
            configReportContent.addDiffRowItem("Owner Check", srcSecuritySetRO.getOwnerCheckFlag(), refSecuritySetRO.getOwnerCheckFlag());
        } else {
            configReportContent.addDiffRowItem("Owner SDI", srcSecuritySetRO.getOwnerSDI(), "");
            configReportContent.addDiffRowItem("Owner Check", srcSecuritySetRO.getOwnerCheckFlag(), "");
        }
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Notes", srcSecuritySetRO.getPrimaryValue("notes"), refSecuritySetRO.getPrimaryValue("notes"), 4, false, this.getTranslationProcessor(), false);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderSecuritySetMatrix(BaseSDCRO ro, boolean configreport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet Mapping: ");
        try {
            if (configreport) {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/png/User.png"), new File(this.folder + "/images/WEB-CORE/images/png/User.png"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/png/Users.png"), new File(this.folder + "/images/WEB-CORE/images/png/Users.png"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/UserPreferences.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/UserPreferences.gif"));
            }
        }
        catch (Exception e) {
            Trace.log("Failed to copy images");
        }
        LV_SecuritySetRO securitySetRO = (LV_SecuritySetRO)ro;
        DataSet ssi = securitySetRO.getSecuritySetItems();
        DataSet unorderedsdcs = securitySetRO.getSecuritySetSDCs();
        DataSet orderedsdcs = this.getOrderedSdcs(unorderedsdcs);
        HashMap<String, String> sdcOps = new HashMap<String, String>();
        for (int i = 0; i < orderedsdcs.getRowCount(); ++i) {
            String currSdc = orderedsdcs.getString(i, "securitysetsdcid");
            sdcOps.put(currSdc, securitySetRO.getSDCOperations(currSdc));
        }
        String[] ssids = securitySetRO.getDistinctSecuritySetItemIds();
        configReportContent.append(this.renderOperationMatrix(orderedsdcs, sdcOps, ssids, ssi, configreport).toString());
        return configReportContent;
    }

    public DataSet getOrderedSdcs(DataSet sdclist) {
        for (int i = 0; i < sdclist.getRowCount(); ++i) {
            String sdcLabel = this.getSDCProcessor().getProperty(sdclist.getString(i, "securitysetsdcid"), "singular");
            sdclist.setString(i, "sdcname", sdcLabel);
        }
        DataSet copy = sdclist.copy();
        int securitysetrow = -1;
        for (int i = 0; i < sdclist.size(); ++i) {
            if (!copy.getString(i, "securitysetsdcid", "").equals("LV_SecuritySet")) continue;
            copy.deleteRow(i);
            securitysetrow = i;
            break;
        }
        copy.sort("sdcname");
        DataSet ret = new DataSet();
        ret.copyRow(sdclist, securitysetrow, 1);
        ret.copyRow(copy, -1, 1);
        return ret;
    }

    public ConfigReportContent renderSecuritySetMatrixDiff(BaseSDCRO srcRO, BaseSDCRO refRO, boolean configreport) {
        String currSdc;
        int i;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet Mapping: ");
        LV_SecuritySetRO srcSecuritySetRO = (LV_SecuritySetRO)srcRO;
        LV_SecuritySetRO refSecuritySetRO = (LV_SecuritySetRO)refRO;
        DataSet srcSsi = srcSecuritySetRO.getSecuritySetItems();
        DataSet refSsi = refSecuritySetRO == null || refSecuritySetRO.currentSDI == null ? new DataSet() : refSecuritySetRO.getSecuritySetItems();
        DataSet unorderedsrcSdcs = srcSecuritySetRO.getSecuritySetSDCs();
        DataSet unorderedrefSdcs = refSecuritySetRO == null || refSecuritySetRO.currentSDI == null ? new DataSet() : refSecuritySetRO.getSecuritySetSDCs();
        DataSet orderedsrcsdcs = this.getOrderedSdcs(unorderedsrcSdcs);
        DataSet orderedrefsdcs = refSecuritySetRO == null || refSecuritySetRO.currentSDI == null ? new DataSet() : this.getOrderedSdcs(unorderedrefSdcs);
        HashMap<String, String> srcSdcOps = new HashMap<String, String>();
        HashMap<String, String> refSdcOps = new HashMap<String, String>();
        for (i = 0; i < orderedsrcsdcs.getRowCount(); ++i) {
            currSdc = orderedsrcsdcs.getString(i, "securitysetsdcid");
            srcSdcOps.put(currSdc, srcSecuritySetRO.getSDCOperations(currSdc));
        }
        for (i = 0; i < orderedrefsdcs.getRowCount(); ++i) {
            currSdc = orderedrefsdcs.getString(i, "securitysetsdcid");
            refSdcOps.put(currSdc, refSecuritySetRO.getSDCOperations(currSdc));
        }
        String[] src_ssids = srcSecuritySetRO.getDistinctSecuritySetItemIds();
        String[] ref_ssids = refSecuritySetRO == null || refSecuritySetRO.currentSDI == null ? new String[]{} : refSecuritySetRO.getDistinctSecuritySetItemIds();
        configReportContent.append(this.renderOperationMatrixDiff(orderedsrcsdcs, orderedrefsdcs, srcSdcOps, refSdcOps, src_ssids, ref_ssids, srcSsi, refSsi, configreport).toString());
        return configReportContent;
    }

    public ConfigReportContent renderOperationMatrix(DataSet orderedsdclist, HashMap sdcOps, String[] ssids, DataSet ssi, boolean configreport) {
        return this.renderOperationMatrixDiff(orderedsdclist, orderedsdclist, sdcOps, sdcOps, ssids, ssids, ssi, ssi, configreport);
    }

    public boolean checkInRef(String sdcid, DataSet orderedsdclist) {
        for (int i = 0; i < orderedsdclist.getRowCount(); ++i) {
            if (!sdcid.equals(orderedsdclist.getString(i, "securitysetsdcid"))) continue;
            return true;
        }
        return false;
    }

    private ConfigReportContent renderOperationMatrixDiff(DataSet orderedsrcsdcs, DataSet orderedrefsdcs, HashMap srcSdcOps, HashMap refSdcOps, String[] srcSsids, String[] refSsids, DataSet srcSSI, DataSet refSSI, boolean configreport) {
        int i;
        String opsForCurrSDC;
        String currSDC;
        int s;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SecuritySet operation matrix");
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.append("<TD class=\"viewlhs\">UserID/JobTypeID</TD>");
        DataSet mergedSdcList = new DataSet();
        for (s = 0; s < orderedsrcsdcs.getRowCount(); ++s) {
            String sdcLabel;
            configReportContent.append("<TD class=\"viewrhs\">");
            currSDC = orderedsrcsdcs.getString(s, "securitysetsdcid");
            opsForCurrSDC = srcSdcOps.get(currSDC).toString();
            int r = mergedSdcList.addRow();
            mergedSdcList.setString(r, "securitysetsdcid", currSDC);
            String[] ops = StringUtil.split(opsForCurrSDC, ";");
            boolean newSdc = false;
            if (!this.checkInRef(currSDC, orderedrefsdcs)) {
                newSdc = true;
            }
            if ((sdcLabel = this.getSDCProcessor().getProperty(currSDC, "singular")) == null || sdcLabel.length() == 0) {
                sdcLabel = currSDC;
            }
            configReportContent.startTableInner();
            configReportContent.startRow();
            String sdcStr = newSdc ? ConfigReportContent.getNewString(sdcLabel) : sdcLabel;
            String headercell = "<TD class=\"viewlhs\" colspan=\"" + ops.length + "\" + width=\"" + ops.length * 120 + "pt\" align=CENTER>" + sdcStr + "</TD>";
            configReportContent.append(headercell);
            configReportContent.endRow();
            configReportContent.startRow();
            for (int i2 = 0; i2 < ops.length; ++i2) {
                configReportContent.append("<TD class=\"viewlhs\" width=\"120pt\" align=CENTER>").append(ops[i2]).append("</TD>");
            }
            configReportContent.endRow();
            configReportContent.endTable();
            configReportContent.append("</TD>");
        }
        for (s = 0; s < orderedrefsdcs.getRowCount(); ++s) {
            currSDC = orderedrefsdcs.getString(s, "securitysetsdcid");
            opsForCurrSDC = refSdcOps.get(currSDC).toString();
            String[] ops = StringUtil.split(opsForCurrSDC, ";");
            boolean deletedSdc = false;
            if (!this.checkInRef(currSDC, orderedsrcsdcs)) {
                deletedSdc = true;
            }
            if (!deletedSdc) continue;
            int r = mergedSdcList.addRow();
            mergedSdcList.setString(r, "securitysetsdcid", currSDC);
            configReportContent.append("<TD class=\"viewrhs\">");
            configReportContent.startTableInner();
            configReportContent.startRow();
            String sdcStr = deletedSdc ? ConfigReportContent.getDeletedString(currSDC) : currSDC;
            String headercell = "<TD class=\"viewlhs\" colspan=\"" + ops.length + "\" + width=\"" + ops.length * 120 + "pt\" align=CENTER>" + sdcStr + "</TD>";
            configReportContent.append(headercell);
            configReportContent.endRow();
            configReportContent.startRow();
            for (int i3 = 0; i3 < ops.length; ++i3) {
                configReportContent.append("<TD class=\"viewlhs\" width=\"120pt\" align=CENTER>").append(ops[i3]).append("</TD>");
            }
            configReportContent.endRow();
            configReportContent.endTable();
            configReportContent.append("</TD>");
        }
        configReportContent.endRow();
        for (i = 0; i < srcSsids.length; ++i) {
            configReportContent.append(this.addDiffRowItemForSSID(mergedSdcList, srcSsids[i], srcSSI, refSSI, srcSdcOps, refSdcOps, configreport).toString());
        }
        for (i = 0; i < refSsids.length; ++i) {
            boolean found = false;
            for (int s2 = 0; s2 < srcSsids.length; ++s2) {
                if (!refSsids[i].equals(srcSsids[s2])) continue;
                found = true;
                break;
            }
            if (found) continue;
            configReportContent.append(this.addDiffRowItemForSSID(mergedSdcList, refSsids[i], srcSSI, refSSI, srcSdcOps, refSdcOps, configreport).toString());
        }
        configReportContent.endTable();
        return configReportContent;
    }

    public String getSSItemLabel(String ssid, DataSet ssitems) {
        String type;
        String label = ssid;
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("securitysetitemid", ssid);
        DataSet find = ssitems.getFilteredDataSet(filter);
        if (find != null && find.getRowCount() > 0 && "U".equals(type = find.getString(0, "itemtypeflag"))) {
            String sql = "SELECT sysuserdesc FROM sysuser WHERE sysuserid='" + ssid + "'";
            DataSet d = this.getQueryProcessor().getSqlDataSet(sql);
            if (d != null && d.getRowCount() > 0) {
                label = d.getString(0, "sysuserdesc");
            }
        }
        if (label == null || label.length() == 0) {
            label = ssid;
        }
        return label;
    }

    public String getSSItemImage(String ssid, DataSet ssitems, boolean configreport) {
        String image = "";
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("securitysetitemid", ssid);
        DataSet find = ssitems.getFilteredDataSet(filter);
        if (find != null && find.getRowCount() > 0) {
            String type = find.getString(0, "itemtypeflag");
            image = "A".equals(type) ? (configreport ? "<img src=\"../images/WEB-CORE/images/png/Users.png\">" : "<img src=\"WEB-CORE/images/png/Users.png\">") : ("U".equals(type) ? (configreport ? "<img src=\"../images/WEB-CORE/images/png/User.png\">" : "<img src=\"WEB-CORE/images/png/User.png\">") : (configreport ? "<img src=\"../images/WEB-CORE/images/gif/UserPreferences.gif\">" : "<img src=\"WEB-CORE/images/gif/UserPreferences.gif\">"));
        }
        return image;
    }

    public String getSSItemImage(String ssid, DataSet ssitems, DataSet rssitems, boolean configreport) {
        String image = this.getSSItemImage(ssid, ssitems, configreport);
        if (image == null || image.length() == 0) {
            image = this.getSSItemImage(ssid, rssitems, configreport);
        }
        return image;
    }

    public ConfigReportContent addRowItemForSSID(DataSet orderedsdclist, String ssid, DataSet ssitems, HashMap sdcOps, boolean configreport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "building matrix");
        configReportContent.startRow();
        String ssiimage = this.getSSItemImage(ssid, ssitems, configreport);
        String ssicontent = ssiimage + " " + this.getSSItemLabel(ssid, ssitems);
        configReportContent.append("<TD style='border: 1px solid black;border-collapse: collapse;'>").append(ssicontent).append("</TD>");
        for (int s = 0; s < orderedsdclist.getRowCount(); ++s) {
            String currSDC = orderedsdclist.getString(s, "securitysetsdcid");
            String opsForCurrSDC = sdcOps.get(currSDC).toString();
            String[] ops = StringUtil.split(opsForCurrSDC, ";");
            String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\">";
            if (!configreport) {
                includeImg = "<img src=\"WEB-CORE/images/gif/Confirm.gif\">";
            }
            configReportContent.append("<TD>");
            configReportContent.append("<table style='border: 1px solid black;border-collapse: collapse;'>");
            configReportContent.startRow();
            for (int i = 0; i < ops.length; ++i) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("securitysetitemid", ssid);
                filter.put("securitysetsdcid", currSDC);
                filter.put("operationid", ops[i]);
                DataSet find = ssitems.getFilteredDataSet(filter);
                if (find != null && find.getRowCount() > 0) {
                    configReportContent.append("<TD>").append(includeImg).append("</TD>");
                    continue;
                }
                configReportContent.append("<TD>").append("&nbsp;&nbsp;").append("</TD>");
            }
            configReportContent.endRow();
            configReportContent.endTable();
            configReportContent.append("</TD>");
        }
        configReportContent.endRow();
        return configReportContent;
    }

    public ConfigReportContent addDiffRowItemForSSID(DataSet mergedSdcList, String itemid, DataSet srcSSI, DataSet refSSI, HashMap srcSdcOps, HashMap refSdcOps, boolean configreport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "building matrix");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("securitysetitemid", itemid);
        DataSet matchesInSrc = srcSSI.getFilteredDataSet(filter);
        DataSet matchesInRef = refSSI.getFilteredDataSet(filter);
        configReportContent.startRow();
        String itemlabel = this.getSSItemLabel(itemid, srcSSI);
        String ssiimage = this.getSSItemImage(itemid, srcSSI, refSSI, configreport);
        if (matchesInSrc.getRowCount() > 0 && matchesInRef.getRowCount() == 0) {
            itemlabel = ConfigReportContent.getNewString(itemlabel);
        } else if (matchesInRef.getRowCount() > 0 && matchesInSrc.getRowCount() == 0) {
            itemlabel = ConfigReportContent.getDeletedString(itemlabel);
        }
        String content = ssiimage + " " + itemlabel;
        configReportContent.append("<TD style='border: 1px solid black;border-collapse: collapse;'>").append(content).append("</TD>");
        for (int s = 0; s < mergedSdcList.getRowCount(); ++s) {
            DataSet findInRef;
            DataSet findInSrc;
            int i;
            String blankimg;
            String deleteImg;
            String addImg;
            String includeImg;
            String[] ops;
            String opsForCurrSDC;
            String currSDC = mergedSdcList.getString(s, "securitysetsdcid");
            if (srcSdcOps.get(currSDC) != null) {
                opsForCurrSDC = srcSdcOps.get(currSDC).toString();
                ops = StringUtil.split(opsForCurrSDC, ";");
                includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\">";
                addImg = "<img src=\"../images/WEB-CORE/images/gif/Add.gif\">";
                deleteImg = "<img src=\"../images/WEB-CORE/images/gif/Delete.gif\">";
                blankimg = "<img src=\"../images/WEB-CORE/images/blank.png\">";
                if (!configreport) {
                    includeImg = "<img src=\"WEB-CORE/images/gif/Confirm.gif\">";
                    addImg = "<img src=\"WEB-CORE/images/gif/Add.gif\">";
                    deleteImg = "<img src=\"WEB-CORE/images/gif/Delete.gif\">";
                    blankimg = "<img src=\"WEB-CORE/images/blank.png\">";
                }
                configReportContent.append("<TD style='border: 1px solid black;border-collapse: collapse;'>");
                configReportContent.startTableInner();
                configReportContent.startRow();
                for (i = 0; i < ops.length; ++i) {
                    filter = new HashMap();
                    filter.put("securitysetitemid", itemid);
                    filter.put("securitysetsdcid", currSDC);
                    filter.put("operationid", ops[i]);
                    findInSrc = srcSSI.getFilteredDataSet(filter);
                    findInRef = refSSI.getFilteredDataSet(filter);
                    if (findInSrc != null && findInSrc.getRowCount() > 0) {
                        if (findInRef != null && findInRef.getRowCount() > 0) {
                            configReportContent.append("<TD  align=\"center\">").append(includeImg).append("</TD>");
                            continue;
                        }
                        configReportContent.append("<TD  align=\"center\">").append(addImg).append("</TD>");
                        continue;
                    }
                    if (findInRef != null && findInRef.getRowCount() > 0) {
                        configReportContent.append("<TD  align=\"center\">").append(deleteImg).append("</TD>");
                        continue;
                    }
                    configReportContent.append("<TD  align=\"center\">").append(blankimg).append("</TD>");
                }
                configReportContent.endRow();
                configReportContent.endTable();
                configReportContent.append("</TD>");
                continue;
            }
            if (refSdcOps.get(currSDC) == null) continue;
            opsForCurrSDC = refSdcOps.get(currSDC).toString();
            ops = StringUtil.split(opsForCurrSDC, ";");
            includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\">";
            addImg = "<img src=\"../images/WEB-CORE/images/gif/Add.gif\">";
            deleteImg = "<img src=\"../images/WEB-CORE/images/gif/Delete.gif\">";
            blankimg = "<img src=\"../images/WEB-CORE/images/blank.png\">";
            if (!configreport) {
                includeImg = "<img src=\"WEB-CORE/images/gif/Confirm.gif\">";
                addImg = "<img src=\"WEB-CORE/images/gif/Add.gif\">";
                deleteImg = "<img src=\"WEB-CORE/images/gif/Delete.gif\">";
                blankimg = "<img src=\"WEB-CORE/images/blank.png\">";
            }
            configReportContent.append("<TD>");
            configReportContent.append("<table style='border: 0px solid black;border-collapse: collapse;'>");
            configReportContent.startRow();
            for (i = 0; i < ops.length; ++i) {
                filter = new HashMap();
                filter.put("securitysetitemid", itemid);
                filter.put("securitysetsdcid", currSDC);
                filter.put("operationid", ops[i]);
                findInSrc = srcSSI.getFilteredDataSet(filter);
                findInRef = refSSI.getFilteredDataSet(filter);
                if (findInSrc != null && findInSrc.getRowCount() > 0) {
                    if (findInRef != null && findInRef.getRowCount() > 0) {
                        configReportContent.append("<TD  align=\"center\">").append(includeImg).append("</TD>");
                        continue;
                    }
                    configReportContent.append("<TD  align=\"center\">").append(addImg).append("</TD>");
                    continue;
                }
                if (findInRef != null && findInRef.getRowCount() > 0) {
                    configReportContent.append("<TD  align=\"center\">").append(deleteImg).append("</TD>");
                    continue;
                }
                configReportContent.append("<TD  align=\"center\">").append(blankimg).append("</TD>");
            }
            configReportContent.endRow();
            configReportContent.endTable();
            configReportContent.append("</TD>");
        }
        configReportContent.endRow();
        return configReportContent;
    }
}

