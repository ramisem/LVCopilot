/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.maint.LockedImage;
import com.labvantage.sapphire.pageelements.maint.SmartScrollGridRenderer;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class UserSDCSecurityMaint
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 86974 $";
    private SDCProcessor sdcProcessor = null;
    private boolean isUser = true;
    private boolean isSingleUser = false;
    private int cellWidth = 80;

    @Override
    public String getHtml() {
        LockedImage lockedImageObj;
        String checkedoutbydept;
        String checkedoutby;
        String lockedby;
        int i;
        SDIData sdiData;
        boolean showdeptinput;
        StringBuffer html = new StringBuffer();
        this.isUser = !"LV_JobType".equals(this.pageContext.getRequest().getParameter("sdcid"));
        SmartScrollGridRenderer grid = new SmartScrollGridRenderer("securityusers");
        QueryProcessor qp = new QueryProcessor(this.pageContext);
        this.sdcProcessor = new SDCProcessor(this.pageContext);
        String keyid1 = this.pageContext.getRequest().getParameter("keyid1");
        this.isSingleUser = keyid1.indexOf(";") < 0;
        String optionsdclist = this.pageContext.getRequest().getParameter("sdclist");
        if (optionsdclist == null || optionsdclist.length() == 0) {
            optionsdclist = (String)this.pageContext.getSession().getAttribute("sdcsecuritydisplayoptions_sdclist");
        }
        if (showdeptinput = "department".equals(this.pageContext.getRequest().getParameter("mode"))) {
            this.cellWidth = 160;
        }
        DataSet sdcoperations = null;
        SafeSQL safeSQL = new SafeSQL();
        sdcoperations = optionsdclist != null && optionsdclist.length() > 0 ? qp.getPreparedSqlDataSet("SELECT * FROM sdcoperation WHERE sdcid in (" + safeSQL.addIn(optionsdclist, ";") + ") ORDER BY sdcid, usersequence, operationid", safeSQL.getValues()) : qp.getSqlDataSet("SELECT * FROM sdcoperation WHERE sdcid not in (SELECT sdcid FROM sdc where accesscontrolledflag = 'S' or sdcid = 'LV_SecuritySet') ORDER BY sdcid, usersequence, operationid");
        DataSet primaryDs = null;
        DataSet sdcsecurityDs = null;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setKeyid1List(keyid1);
        sdiRequest.setLockOption("LA");
        String changecontrolledflag = CMTPolicy.getPolicy(this.getConnectionid(), this.isUser ? "User" : "LV_JobType").getChangeControlledFlag();
        sdiRequest.setValidateCheckout(true);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRetainRsetid(true);
        SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
        HashMap<String, LockedImage> lockedImageHashMap = new HashMap<String, LockedImage>();
        String rsetid = "";
        if (this.isUser) {
            sdiRequest.setSDCid("User");
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            rsetid = sdiData.getRsetid();
            primaryDs = sdiData.getDataset("primary");
            for (i = 0; i < primaryDs.getRowCount(); ++i) {
                lockedby = primaryDs.getValue(i, "__lockedby");
                checkedoutby = primaryDs.getValue(i, "__checkedoutbyuser");
                checkedoutbydept = primaryDs.getValue(i, "__checkedoutbydepartment");
                lockedImageObj = LockedImage.getLockedImage(lockedby, checkedoutby, checkedoutbydept, this.connectionInfo, this.getTranslationProcessor());
                lockedImageHashMap.put(primaryDs.getValue(i, "sysuserid"), lockedImageObj);
            }
            safeSQL.reset();
            sdcsecurityDs = qp.getPreparedSqlDataSet("SELECT * FROM sdcsecurity where sysuserid  in (" + safeSQL.addIn(keyid1, ";") + ") AND accesstype " + (showdeptinput ? " not in ('member','owner','world')" : " in ('member','owner','world')") + " ORDER BY sysuserid, sdcid, operationid, accesstype", safeSQL.getValues());
        } else {
            sdiRequest.setSDCid("LV_JobType");
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            rsetid = sdiData.getRsetid();
            primaryDs = sdiData.getDataset("primary");
            for (i = 0; i < primaryDs.getRowCount(); ++i) {
                lockedby = primaryDs.getValue(i, "__lockedby");
                checkedoutby = primaryDs.getValue(i, "__checkedoutbyuser");
                checkedoutbydept = primaryDs.getValue(i, "__checkedoutbydepartment");
                lockedImageObj = LockedImage.getLockedImage(lockedby, checkedoutby, checkedoutbydept, this.connectionInfo, this.getTranslationProcessor());
                lockedImageHashMap.put(primaryDs.getValue(i, "jobtypeid"), lockedImageObj);
            }
            safeSQL.reset();
            sdcsecurityDs = qp.getPreparedSqlDataSet("SELECT * FROM sdcjobtypesecurity where jobtypeid  in (" + safeSQL.addIn(keyid1, ";") + ") AND accesstype " + (showdeptinput ? " not in ('member','owner','world')" : " in ('member','owner','world')") + " ORDER BY jobtypeid, sdcid, operationid, accesstype", safeSQL.getValues());
        }
        DataSet deptDs = qp.getSqlDataSet("SELECT departmentid from department order by departmentid");
        StringBuffer deptlist = new StringBuffer();
        for (int i2 = 0; i2 < deptDs.getRowCount(); ++i2) {
            if (i2 != 0) {
                deptlist.append(";");
            }
            deptlist.append(deptDs.getString(i2, "departmentid"));
        }
        String sdcid = "";
        ArrayList<String> sdclist = new ArrayList<String>();
        TreeSet<String> operationSet = new TreeSet<String>();
        if (this.isSingleUser) {
            for (int i3 = 0; i3 < sdcoperations.getRowCount(); ++i3) {
                operationSet.add(sdcoperations.getString(i3, "operationid"));
            }
            for (String operationid : operationSet) {
                grid.addTopRightCell("<table class=\"gridmaint_table\" width=\"100%\"><tr>" + this.getOperationTD(operationid, showdeptinput) + "</tr></table>", "sdcheader_field", "", false);
            }
        } else {
            for (int i4 = 0; i4 < sdcoperations.getRowCount(); ++i4) {
                String tempsdcid = sdcoperations.getString(i4, "sdcid");
                if (tempsdcid.equals(sdcid)) continue;
                sdclist.add(tempsdcid);
                sdcid = tempsdcid;
                HashMap<String, String> filterMap = new HashMap<String, String>();
                filterMap.put("sdcid", sdcid);
                DataSet ds = sdcoperations.getFilteredDataSet(filterMap);
                grid.addTopRightCell(this.getSDCHeader(ds, showdeptinput), "sdcheader_field", "", false);
            }
        }
        if (this.isSingleUser) {
            LockedImage lockedImage = (LockedImage)lockedImageHashMap.get(keyid1);
            boolean isUseJobType = this.isUser && "J".equals(primaryDs.getValue(0, "securitytypeflag"));
            boolean readonly = false;
            if (isUseJobType) {
                readonly = true;
            } else if (lockedImage.isLocked()) {
                readonly = true;
            } else if (!lockedImage.isCheckedOut() && ("Y".equals(changecontrolledflag) || "T".equals(changecontrolledflag) && "Y".equals(primaryDs.getValue(0, "templateflag")))) {
                readonly = true;
            }
            TreeSet<String> sdcSet = new TreeSet<String>();
            for (int i5 = 0; i5 < sdcoperations.getRowCount(); ++i5) {
                sdcSet.add(sdcoperations.getString(i5, "sdcid"));
            }
            for (String primarysdcid : sdcSet) {
                grid.addBottomLeftCell("<b>" + primarysdcid + "</b>", "gridmaint_fieldtitle", true);
                Iterator operationitr = operationSet.iterator();
                boolean newrow = true;
                while (operationitr.hasNext()) {
                    String operationid = (String)operationitr.next();
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("sdcid", primarysdcid);
                    findMap.put("operationid", operationid);
                    if (sdcoperations.findRow(findMap) >= 0) {
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put("sdcid", primarysdcid);
                        filter.put("operationid", operationid);
                        DataSet availableSdcOps = sdcoperations.getFilteredDataSet(filter);
                        DataSet userSdcDs = sdcsecurityDs.getFilteredDataSet(filter);
                        grid.addBottomRightCell(this.getPrimarySdcOpCell(keyid1, availableSdcOps, userSdcDs, showdeptinput, readonly), "cell_field", newrow);
                    } else {
                        ArrayList<String> userGridRow;
                        grid.addBottomRightCell("&nbsp;", "dataentry_grid_blankcell", newrow);
                        ArrayList<ArrayList<String>> idGrid = (ArrayList<ArrayList<String>>)this.pageContext.getAttribute("idGrid");
                        if (idGrid == null) {
                            idGrid = new ArrayList<ArrayList<String>>();
                            this.pageContext.setAttribute("idGrid", idGrid);
                        }
                        if ((userGridRow = (ArrayList<String>)this.pageContext.getAttribute(primarysdcid + "_gridrow")) == null) {
                            userGridRow = new ArrayList<String>();
                            this.pageContext.setAttribute(primarysdcid + "_gridrow", userGridRow);
                            idGrid.add(userGridRow);
                        }
                        String key = "empty";
                        userGridRow.add(key);
                    }
                    newrow = false;
                }
            }
        } else {
            for (int i6 = 0; i6 < primaryDs.getRowCount(); ++i6) {
                String keycolid1 = this.isUser ? "sysuserid" : "jobtypeid";
                String primaryid = primaryDs.getString(i6, keycolid1);
                String primarydesc = this.isUser ? primaryDs.getString(i6, "sysuserdesc") : primaryDs.getString(i6, "jobtypedesc");
                LockedImage lockedImage = (LockedImage)lockedImageHashMap.get(primaryid);
                boolean isUseJobType = this.isUser && "J".equals(primaryDs.getValue(i6, "securitytypeflag"));
                boolean readonly = false;
                if (isUseJobType) {
                    readonly = true;
                } else if (lockedImage.isLocked()) {
                    readonly = true;
                } else if (!lockedImage.isCheckedOut() && ("Y".equals(changecontrolledflag) || "T".equals(changecontrolledflag) && "Y".equals(primaryDs.getValue(i6, "templateflag")))) {
                    readonly = true;
                }
                String bottomLeftHeader = "<b>" + primaryid + "</b>&nbsp;" + (isUseJobType ? "<span style=\"color:red\">(Use Job Type)</span>" : "");
                if (lockedImage != null) {
                    bottomLeftHeader = "<table style=\"width:100%\"><tr><td>" + bottomLeftHeader + "</td><td style=\"text-align:right\">" + lockedImage.getLockedImage() + "</td></tr></table>";
                }
                grid.addBottomLeftCell(bottomLeftHeader, "gridmaint_fieldtitle", true);
                for (int s = 0; s < sdclist.size(); ++s) {
                    String primarysdcid = (String)sdclist.get(s);
                    boolean newrow = false;
                    if (s == 0) {
                        newrow = true;
                    }
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("sdcid", primarysdcid);
                    DataSet availableSdcOps = sdcoperations.getFilteredDataSet(filter);
                    filter.put(keycolid1, primaryid);
                    DataSet userSdcDs = sdcsecurityDs.getFilteredDataSet(filter);
                    grid.addBottomRightCell(this.getPrimarySdcOpCell(primaryid, availableSdcOps, userSdcDs, showdeptinput, readonly), "cell_field", newrow);
                }
            }
        }
        if (this.isSingleUser) {
            LockedImage lockedImage = (LockedImage)lockedImageHashMap.get(keyid1);
            grid.addTopLeftCell("SDC" + (this.isUser && "J".equals(primaryDs.getValue(0, "securitytypeflag")) ? "&nbsp;<span style=\"color:red\">(Use Job Type)</span>" : (lockedImage != null ? lockedImage.getLockedImage() : "")), "gridmaint_fieldtitle", true);
        } else {
            grid.addTopLeftCell(this.isUser ? "User" : "Job Type", "gridmaint_fieldtitle", true);
        }
        ArrayList idGrid = (ArrayList)this.pageContext.getAttribute("idGrid");
        grid.renderGrid(html);
        html.append(SDITagUtil.getGrid(idGrid, this.getTranslationProcessor()));
        html.append("<script>var rsetid='" + rsetid + "';</script>");
        return html.toString();
    }

    public String getAvailableSDCs(ArrayList sdclist) {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < sdclist.size(); ++i) {
            html.append("<div>" + sdclist.get(i) + "</div>");
        }
        return html.toString();
    }

    private String getOperationTD(String operationid, boolean showdeptinput) {
        return "<td nowwrap align=\"center\" style=\"width:" + this.cellWidth + "px" + ";font-size:10;font-weight:normal\" class=\"gridmaint_fieldtitle\">" + (showdeptinput ? operationid : "<input readonly size=\"8\" style=\"border:none;background-color:#CCCCCC\" title=\"" + operationid + " Operation\" value=\"" + operationid + "\"/>") + "</td>";
    }

    public String getSDCHeader(DataSet sdcoperation, boolean showdeptinput) {
        StringBuffer html = new StringBuffer();
        String sdcid = sdcoperation.getString(0, "sdcid");
        String singular = this.sdcProcessor.getProperty(sdcid, "plural");
        String accesscontrolledflag = this.sdcProcessor.getProperty(sdcid, "accesscontrolledflag");
        boolean isDeptAccess = "D".equals(accesscontrolledflag) || "DataSet".equals(sdcid) && "B".equals(accesscontrolledflag);
        singular = singular != null && singular.length() > 0 ? singular : sdcid;
        html.append("<table style=\"border-collapse:collapse\">");
        html.append("<tr><td nowrap align=\"center\" style=\"background-color:" + (isDeptAccess ? "B0C4DE" : "orange") + "\" " + (isDeptAccess ? "" : " title=\"" + this.getTranslationProcessor().translate("Departmental Access is off.") + "\" ") + " class=\"gridmaint_fieldtitle\" colspan=\"" + sdcoperation.size() + "\"><table style=\"width:" + this.cellWidth * sdcoperation.size() + "px;font-weight:bold\"><tr><td>" + sdcid + "</td></tr></table></td></tr>");
        html.append("<tr>");
        for (int i = 0; i < sdcoperation.getRowCount(); ++i) {
            String operationid = sdcoperation.getString(i, "operationid");
            html.append(this.getOperationTD(operationid, showdeptinput));
        }
        html.append("</tr></table>");
        return html.toString();
    }

    public String getPrimarySdcOpCell(String primaryid, DataSet availableSdcOps, DataSet userSdcDs, boolean showdeptinput, boolean readonly) {
        StringBuffer html = new StringBuffer();
        String sdcid = availableSdcOps.getString(0, "sdcid");
        ArrayList idGrid = (ArrayList)this.pageContext.getAttribute("idGrid");
        if (idGrid == null) {
            idGrid = new ArrayList();
            this.pageContext.setAttribute("idGrid", idGrid);
        }
        html.append("<table style=\"border-collapse:collapse\"><tr>");
        for (int i = 0; i < availableSdcOps.getRowCount(); ++i) {
            ArrayList<String> userGridRow;
            String operationid = availableSdcOps.getString(i, "operationid");
            String key = sdcid + ";" + operationid + ";" + primaryid;
            HashMap<String, String> userfilterMap = new HashMap<String, String>();
            userfilterMap.put(this.isUser ? "sysuserid" : "jobtypeid", primaryid);
            userfilterMap.put("sdcid", sdcid);
            userfilterMap.put("operationid", operationid);
            DataSet usersdcoperationDs = userSdcDs.getFilteredDataSet(userfilterMap);
            String userAccessType = "";
            if (usersdcoperationDs.getRowCount() >= 1) {
                for (int a = 0; a < usersdcoperationDs.getRowCount(); ++a) {
                    String type = usersdcoperationDs.getString(a, "accesstype");
                    if (showdeptinput && !"owner".equals(type) && !"world".equals(type) && !"member".equals(type)) {
                        if (a == 0) {
                            userAccessType = type;
                            continue;
                        }
                        userAccessType = userAccessType + ";" + type;
                        continue;
                    }
                    userAccessType = type.substring(0, 1).toUpperCase();
                }
            }
            if ((userGridRow = (ArrayList<String>)this.pageContext.getAttribute((this.isSingleUser ? sdcid : primaryid) + "_gridrow")) == null) {
                userGridRow = new ArrayList<String>();
                this.pageContext.setAttribute((this.isSingleUser ? sdcid : primaryid) + "_gridrow", userGridRow);
                idGrid.add(userGridRow);
            }
            html.append("<td align=\"center\" nowrap style=\"width:" + (this.cellWidth + 8) + "px" + "\">");
            if (showdeptinput) {
                userGridRow.add(key);
                html.append("<textarea type=\"text\" edit=\"lookup\" ov=\"" + userAccessType + "\" cv=\"" + userAccessType + "\" readonly rows=\"3\" cols=\"20\" id=\"" + key + "\" name=\"" + key + "\" id=\"" + key + "\" onchange=\"validateDept( this )\">");
                html.append(StringUtil.replaceAll(userAccessType, ";", "\n"));
                html.append("</textarea>");
                if (!readonly) {
                    html.append("<image src=\"WEB-CORE/images/gif/SOP.gif\" title=\"Click to edit list\" onclick=\"showSmartListEditor( this, '" + key + "', deptlist )\">");
                }
            } else {
                if (readonly) {
                    html.append("<input readonly style=\"border:0px\" name=\"" + key + "\" id=\"" + key + "\" ov=\"" + userAccessType + "\" value=\"" + userAccessType + "\" size=\"1\" maxlength=\"1\"/>");
                } else {
                    html.append("<input name=\"" + key + "\" id=\"" + key + "\" ov=\"" + userAccessType + "\" value=\"" + userAccessType + "\" size=\"1\" maxlength=\"1\" onkeyup=\"validateMOW( this )\" onchange=\"validateMOW( this )\"/>");
                }
                userGridRow.add(key);
            }
            html.append("</td>");
        }
        html.append("</tr></table>");
        return html.toString();
    }
}

