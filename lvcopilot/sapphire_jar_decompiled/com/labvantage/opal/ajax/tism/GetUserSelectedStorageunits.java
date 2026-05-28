/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.tism;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetUserSelectedStorageunits
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54429 $";
    public static final String PROPERTY_TARGETSU = "userconfig_tism_targetsu";
    public static final String PROPERTY_TARGETSU_PINNED = "userconfig_tism_targetsu_pinned";
    public static final String PROPERTY_SOURCESU = "userconfig_tism_sourcesu";
    public static final String PROPERTY_SOURCESU_PINNED = "userconfig_tism_sourcesu_pinned";
    private String PROPERTY_NORMAL = "userconfig_tism_targetsu";
    private String PROPERTY_PINNED = "userconfig_tism_targetsu_pinned";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String function;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String pinstorageunitid = ajaxResponse.getRequestParameter("storageunitid", "");
        String frame = ajaxResponse.getRequestParameter("frame", "target").toLowerCase();
        String taskContextFrame = ajaxResponse.getRequestParameter("taskContextFrame", "");
        String frameName = taskContextFrame + frame + "frame_iframe.";
        if ("target".equals(frame)) {
            this.PROPERTY_NORMAL = PROPERTY_TARGETSU;
            this.PROPERTY_PINNED = PROPERTY_TARGETSU_PINNED;
            function = "sapphire.page.getTop()." + frameName + "setTargetStorageUnitId";
        } else {
            this.PROPERTY_NORMAL = PROPERTY_SOURCESU;
            this.PROPERTY_PINNED = PROPERTY_SOURCESU_PINNED;
            function = "sapphire.page.getTop()." + frameName + "setSourceStorageUnitId";
        }
        StringBuilder sb = new StringBuilder();
        ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
        String userid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        if (StringUtil.getLen(pinstorageunitid) > 0L) {
            String mode = ajaxResponse.getRequestParameter("mode");
            if ("pin".equals(mode)) {
                this.pinStorageUnit(configurationProcessor, pinstorageunitid, userid);
            } else if ("unpin".equals(mode)) {
                this.unpinStorageUnit(configurationProcessor, pinstorageunitid, userid);
            } else if ("delete".equals(mode)) {
                this.deleteStorageUnit(configurationProcessor, pinstorageunitid, userid);
            }
        }
        sb.append("<table style=\"width:100%\" cellspacing=0 border=0>");
        try {
            String nonpinned = configurationProcessor.getProfileProperty(userid, this.PROPERTY_NORMAL);
            String pinned = configurationProcessor.getProfileProperty(userid, this.PROPERTY_PINNED);
            if (StringUtil.getLen(nonpinned) > 0L || StringUtil.getLen(pinned) > 0L) {
                String su = StringUtil.getLen(nonpinned) > 0L ? (StringUtil.getLen(pinned) > 0L ? nonpinned + ";" + pinned : nonpinned) : pinned;
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, labelpath, storageunittype from storageunit where storageunitid in (" + safeSQL.addIn(su, ";") + ")", safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    HashMap hm;
                    String storageunitid;
                    HashMap map = new HashMap();
                    for (int i = 0; i < ds.size(); ++i) {
                        HashMap<String, String> hm2 = new HashMap<String, String>();
                        hm2.put("labelpath", ds.getValue(i, "labelpath"));
                        hm2.put("storageunittype", ds.getValue(i, "storageunittype"));
                        map.put(ds.getValue(i, "storageunitid"), hm2);
                    }
                    int index = 1;
                    List<String> list = OpalUtil.toList(nonpinned, ";");
                    for (String o : list) {
                        storageunitid = o;
                        if (StringUtil.getLen(storageunitid) <= 0L || !map.containsKey(storageunitid)) continue;
                        hm = (HashMap)map.get(storageunitid);
                        sb.append("<tr>");
                        sb.append("<td width=10 onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this)>").append(index++).append(".</td>");
                        sb.append("<td onmouseover=").append(frameName).append("tism_rowMouseOver(this,true) onmouseout=").append(frameName).append("tism_rowMouseOut(this,true)");
                        sb.append(" style='cursor:pointer;color:blue' onclick=\"").append(function).append("('").append(storageunitid).append("', true)\">");
                        sb.append("<span style='text-decoration:underline'>").append(hm.get("labelpath")).append("</span>");
                        sb.append("&nbsp;<span style='color:black'>(").append(hm.get("storageunittype")).append(")</span>");
                        sb.append("</td>");
                        sb.append("<td align=right width=12px height=12px onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this) style='padding-left:5px;cursor:pointer' onclick='").append(frameName).append("tism_loadLastSelectedUnits(\"").append(storageunitid).append("\", \"pin\")'><img src='WEB-OPAL/images/favourite_gray.png' width=12px height=12px title='").append(this.getTranslationProcessor().translate("Add to favourites")).append("'></td>");
                        sb.append("<td align=right width=12px height=12px onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this) style='cursor:pointer' onclick='").append(frameName).append("tism_loadLastSelectedUnits(\"").append(storageunitid).append("\", \"delete\")'><img src='WEB-CORE/images/gif/Delete.gif' width=12px height=12px title='").append(this.getTranslationProcessor().translate("Remove from list")).append("'></td>");
                        sb.append("</tr>");
                    }
                    if (StringUtil.getLen(pinned) > 0L) {
                        index = 1;
                        sb.append("<tr><td colspan=4><hr></td></tr>");
                        list = OpalUtil.toList(pinned, ";");
                        for (String o : list) {
                            storageunitid = o;
                            if (StringUtil.getLen(storageunitid) <= 0L || !map.containsKey(storageunitid)) continue;
                            hm = (HashMap)map.get(storageunitid);
                            sb.append("<tr>");
                            sb.append("<td width=10 onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this)>").append(index++).append(".</td>");
                            sb.append("<td onmouseover=").append(frameName).append("tism_rowMouseOver(this,true) onmouseout=").append(frameName).append("tism_rowMouseOut(this,true)");
                            sb.append(" style='cursor:pointer;color:blue' onclick=\"").append(function).append("('").append(storageunitid).append("', true)\">");
                            sb.append("<span style='text-decoration:underline'>").append(hm.get("labelpath")).append("</span>");
                            sb.append("&nbsp;<span style='color:black'>(").append(hm.get("storageunittype")).append(")</span>");
                            sb.append("</td>");
                            sb.append("<td align=right width=12px height=12px onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this) style='padding-left:5px;cursor:pointer' onclick='").append(frameName).append("tism_loadLastSelectedUnits(\"").append(storageunitid).append("\", \"unpin\")' ><img src='WEB-OPAL/images/favourite.png' width=12px height=12px title='").append(this.getTranslationProcessor().translate("Remove from favourites")).append("'></td>");
                            sb.append("<td align=right width=12px height=12px onmouseover=").append(frameName).append("tism_rowMouseOver(this) onmouseout=").append(frameName).append("tism_rowMouseOut(this) style='cursor:pointer' onclick='").append(frameName).append("tism_loadLastSelectedUnits(\"").append(storageunitid).append("\", \"delete\")'><img src='WEB-CORE/images/gif/Delete.gif' width=12px height=12px title='").append(this.getTranslationProcessor().translate("Remove from list")).append("'></td>");
                            sb.append("</tr>");
                        }
                    }
                } else {
                    sb.append("<tr>");
                    sb.append("<td>").append(this.getTranslationProcessor().translate("No previously selected storage units found")).append("</td>");
                    sb.append("</tr>");
                }
            } else {
                sb.append("<tr>");
                sb.append("<td>").append(this.getTranslationProcessor().translate("No previously selected storage units found")).append("</td>");
                sb.append("</tr>");
            }
        }
        catch (SapphireException e) {
            sb.append("<tr><td style='color:red;'>An error happened:<br>").append(e.getMessage()).append("</td></tr>");
            this.logger.error("Eror", e);
        }
        sb.append("</table>");
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }

    private void deleteStorageUnit(ConfigurationProcessor configurationProcessor, String storageunitid, String userid) {
        try {
            List<String> list;
            boolean delete = true;
            String propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_PINNED);
            if (StringUtil.getLen(propertyvalue) > 0L && (list = OpalUtil.toList(propertyvalue, ";")).contains(storageunitid)) {
                list.remove(storageunitid);
                configurationProcessor.setProfileProperty(userid, this.PROPERTY_PINNED, OpalUtil.toDelimitedString(list, ";"));
                delete = false;
            }
            if (delete && StringUtil.getLen(propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_NORMAL)) > 0L && (list = OpalUtil.toList(propertyvalue, ";")).contains(storageunitid)) {
                list.remove(storageunitid);
                configurationProcessor.setProfileProperty(userid, this.PROPERTY_NORMAL, OpalUtil.toDelimitedString(list, ";"));
            }
        }
        catch (SapphireException e) {
            this.logger.error("Eror", e);
        }
    }

    private void unpinStorageUnit(ConfigurationProcessor configurationProcessor, String storageunitid, String userid) {
        try {
            List<String> list;
            String propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_PINNED);
            if (StringUtil.getLen(propertyvalue) > 0L && (list = OpalUtil.toList(propertyvalue, ";")).contains(storageunitid)) {
                list.remove(storageunitid);
                configurationProcessor.setProfileProperty(userid, this.PROPERTY_PINNED, OpalUtil.toDelimitedString(list, ";"));
                propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_NORMAL);
                propertyvalue = StringUtil.getLen(propertyvalue) > 0L ? storageunitid + ";" + propertyvalue : storageunitid;
                configurationProcessor.setProfileProperty(userid, this.PROPERTY_NORMAL, propertyvalue);
            }
        }
        catch (SapphireException e) {
            this.logger.error("Eror", e);
        }
    }

    private void pinStorageUnit(ConfigurationProcessor configurationProcessor, String storageunitid, String userid) {
        try {
            List<String> list;
            String propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_PINNED);
            if (StringUtil.getLen(propertyvalue) > 0L) {
                list = OpalUtil.toList(propertyvalue, ";");
                propertyvalue = storageunitid;
                for (int i = 0; i < list.size(); ++i) {
                    if (i >= 10 || list.get(i).equals(storageunitid)) continue;
                    propertyvalue = propertyvalue + ";" + list.get(i);
                }
            } else {
                propertyvalue = storageunitid;
            }
            if (StringUtil.getLen(propertyvalue) > 0L) {
                configurationProcessor.setProfileProperty(userid, this.PROPERTY_PINNED, propertyvalue);
            }
            if (StringUtil.getLen(propertyvalue = configurationProcessor.getProfileProperty(userid, this.PROPERTY_NORMAL)) > 0L && (list = OpalUtil.toList(propertyvalue, ";")).contains(storageunitid)) {
                list.remove(storageunitid);
                if (list.size() > 0) {
                    configurationProcessor.setProfileProperty(userid, this.PROPERTY_NORMAL, OpalUtil.toDelimitedString(list, ";"));
                }
            }
        }
        catch (SapphireException e) {
            this.logger.error("Eror", e);
        }
    }
}

