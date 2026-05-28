/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.array;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.array.ArraysPolicy;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import com.labvantage.sapphire.util.groovy.PropertyUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ArrayUtil
implements ArrayConstants {
    private static final Boolean lock = true;
    private static final Boolean deleteLock = true;

    public static List generateLabel(String lblType, String lblStart, String lblDir, int count) {
        ArrayList<Object> labels = new ArrayList<Object>();
        if ("Numeric".equalsIgnoreCase(lblType)) {
            Integer startAt = (int)Float.parseFloat(lblStart);
            for (int i = 0; i < count; ++i) {
                labels.add(i + startAt);
            }
        } else {
            char startAt = lblStart.charAt(0);
            String start = String.valueOf(startAt);
            labels.add(start);
            for (int i = 1; i < count; ++i) {
                start = ArrayUtil.incrementAlpha(start);
                labels.add(start);
            }
        }
        if (lblDir.equalsIgnoreCase("RL") || lblDir.equalsIgnoreCase("BT")) {
            Collections.reverse(labels);
        }
        return labels;
    }

    private static String incrementAlpha(String alpha) {
        StringBuffer newAlpha = new StringBuffer();
        boolean carryOver = true;
        if (alpha != null) {
            for (int count = 1; count <= alpha.length(); ++count) {
                char character = alpha.charAt(alpha.length() - count);
                if (character == 'Z' && carryOver) {
                    newAlpha.append('A');
                    carryOver = true;
                    continue;
                }
                if (character == 'z' && carryOver) {
                    newAlpha.append('a');
                    carryOver = true;
                    continue;
                }
                if (carryOver) {
                    newAlpha.append((char)(character + '\u0001'));
                } else {
                    newAlpha.append(character);
                }
                carryOver = false;
            }
            if (carryOver) {
                if (Character.isLowerCase(alpha.charAt(0))) {
                    newAlpha.append('a');
                } else if (Character.isUpperCase(alpha.charAt(0))) {
                    newAlpha.append('A');
                }
            }
        }
        StringBuffer reversed = new StringBuffer();
        String str = newAlpha.toString();
        for (int i = str.length() - 1; i >= 0; --i) {
            reversed.append(str.charAt(i));
        }
        return reversed.toString();
    }

    public static String getContentLabels(QueryProcessor queryProcessor, DAMProcessor damProcessor, String arrayid, String contentype, String contentkeyid1list, String reagenttypeid) {
        return ArrayUtil.getContentLabels(queryProcessor, damProcessor, arrayid, contentype, contentkeyid1list, reagenttypeid, "");
    }

    public static String getContentLabels(QueryProcessor queryProcessor, DAMProcessor damProcessor, String arrayid, String contentype, String contentkeyid1list, String reagenttypeid, String parentstorageunitid) {
        if ("Sample".equals(contentype)) {
            return ArrayUtil.getSampleContentLabels(queryProcessor, damProcessor, contentkeyid1list, parentstorageunitid);
        }
        if ("LV_ReagentLot".equals(contentype)) {
            return ArrayUtil.getReagentContentLabels(queryProcessor, damProcessor, contentkeyid1list, reagenttypeid);
        }
        if ("LV_ArrayItem".equals(contentype)) {
            return ArrayUtil.getArrayItemContentLabels(queryProcessor, damProcessor, contentkeyid1list, arrayid);
        }
        if ("LV_Treatment".equals(contentype)) {
            return ArrayUtil.getTreatmentContentLabel(queryProcessor, damProcessor, contentkeyid1list);
        }
        return "Invalid";
    }

    public static String getReagentContentLabels(QueryProcessor queryProcessor, DAMProcessor damProcessor, String contentkeyid1list, String reagenttypeid) {
        String[] keys = StringUtil.split(contentkeyid1list, ";");
        StringBuilder labels = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        String inClause = "";
        String rsetid = "";
        try {
            rsetid = damProcessor.createRSet("TrackItemSDC", contentkeyid1list, null, null);
            inClause = inClause + "select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid);
        }
        catch (SapphireException e) {
            Trace.log("Failed to create rset");
            return "Invalid";
        }
        String sql = "SELECT trackitem.trackitemid, trackitem.linkkeyid1, reagentlot.REAGENTTYPEID, reagentlot.REAGENTTYPEVERSIONID FROM trackitem, reagentlot where reagentlot.reagentlotid = trackitem.linkkeyid1 and trackitem.linksdcid = 'LV_ReagentLot' and trackitem.trackitemid in (" + inClause + ")";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < keys.length; ++i) {
                String _reagenttypeid = ds.getString(i, "reagenttypeid", "");
                if (i != 0) {
                    labels.append(";");
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("trackitemid", keys[i]);
                DataSet match = ds.getFilteredDataSet(filter);
                if (match != null && match.getRowCount() > 0) {
                    String reagentlotid = match.getString(0, "linkkeyid1");
                    labels.append(_reagenttypeid).append(": ").append(reagentlotid).append("(").append(keys[i]).append(")");
                    continue;
                }
                labels.append("Invalid");
            }
        } else {
            labels.append("Invalid");
        }
        return labels.toString();
    }

    public static String getArrayItemContentLabels(QueryProcessor queryProcessor, DAMProcessor damProcessor, String contentkeyid1list, String arrayid) {
        String labels = "";
        String[] keys = StringUtil.split(contentkeyid1list, ";");
        String rsetid = null;
        DataSet ds = null;
        try {
            rsetid = damProcessor.createRSet("LV_ArrayItem", contentkeyid1list, null, null);
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT arrayid, arrayitemid, itemlabel FROM arrayitem where arrayitemid in ( select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + " )";
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        catch (SapphireException e) {
            Trace.logError("Failed to run create RSet");
            return "Invalid";
        }
        if (ds != null && ds.getRowCount() > 0) {
            String[] arrayIDS = StringUtil.split(arrayid, ";");
            for (int i = 0; i < keys.length; ++i) {
                if (i != 0) {
                    labels = labels + ";";
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("arrayitemid", keys[i]);
                DataSet match = ds.getFilteredDataSet(filter);
                if (match != null && match.getRowCount() > 0) {
                    if (arrayIDS.length == keys.length) {
                        labels = labels + match.getString(0, "arrayid") + " " + match.getString(0, "itemlabel");
                        continue;
                    }
                    labels = labels + arrayid + " " + match.getString(0, "itemlabel");
                    continue;
                }
                labels = labels + "Invalid";
            }
        } else {
            labels = labels + "Invalid";
        }
        return labels;
    }

    public static String getSampleContentLabels(QueryProcessor queryProcessor, DAMProcessor damProcessor, String contentkeyid1list, String parentstorageunitid) {
        String labels = "";
        if (parentstorageunitid == null || parentstorageunitid.length() == 0) {
            return contentkeyid1list;
        }
        String[] keys = StringUtil.split(parentstorageunitid, ";");
        String rsetid = null;
        DataSet ds = null;
        try {
            rsetid = damProcessor.createRSet("StorageUnitSDC", parentstorageunitid, null, null);
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select  c.storageunitid su, p.storageunitlabel boxid, c.storageunitlabel boxpos from storageunit c, storageunit p  where c.storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + " )\nand c.parentid = p.storageunitid ";
            ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        }
        catch (SapphireException e) {
            Trace.logError("Failed to run create RSet");
            return contentkeyid1list;
        }
        if (ds != null && ds.getRowCount() > 0) {
            String[] samplelist = StringUtil.split(contentkeyid1list, ";");
            for (int i = 0; i < keys.length; ++i) {
                if (i != 0) {
                    labels = labels + ";";
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("su", keys[i]);
                DataSet match = ds.getFilteredDataSet(filter);
                labels = match != null && match.getRowCount() > 0 ? labels + samplelist[i] + "(" + match.getString(0, "boxid") + " " + match.getString(0, "boxpos") + ")" : labels + "Invalid";
            }
        } else {
            labels = labels + "Invalid";
        }
        return labels;
    }

    public static String getTreatmentContentLabel(QueryProcessor queryProcessor, DAMProcessor damProcessor, String contentkeyid1list) {
        String[] keys = StringUtil.split(contentkeyid1list, ";");
        String inClause = "";
        String rsetid = "";
        String labels = "";
        SafeSQL safeSQL = new SafeSQL();
        try {
            rsetid = damProcessor.createRSet("LV_Treatment", contentkeyid1list, null, null);
            inClause = inClause + "select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid);
        }
        catch (SapphireException e) {
            Trace.log("Failed to create rset");
            return "Invalid";
        }
        String sql = "SELECT treatmenttypedesc, s_treatmenttypeid FROM s_treatmenttype WHERE s_treatmenttypeid IN ( " + inClause + " )";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < keys.length; ++i) {
                if (i != 0) {
                    labels = labels + ";";
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("s_treatmenttypeid", keys[i]);
                DataSet match = ds.getFilteredDataSet(filter);
                labels = match != null && match.getRowCount() > 0 ? labels + match.getString(0, "treatmenttypedesc") : labels + "Invalid";
            }
        } else {
            labels = labels + "Invalid";
        }
        return labels;
    }

    public static String getLayoutLookupHtml(PageContext pageContext, String filter) {
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String title = tp.translate("Array Layout Lookup");
        if (filter != null && filter.trim().length() > 0) {
            filter = filter + " and ";
        }
        filter = filter + " versionstatus in ('P','C')";
        String url = "rc?command=page&page=LV_ArrayLayoutLookup&fieldid=arraylayoutid;arraylayoutversionid&lookupcallback=parent.setLayoutCallback";
        if (filter.trim().length() > 0) {
            url = url + "&restrictivewhere=" + filter;
        }
        String fieldName = "arraylayoutid";
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        sb.append("<input style=\"border:1px solid green;width:250px;\" readonly=\"\" edit=\"lookup\" type=\"text\" class=\"input_field\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" onkeyup=\";showSuggestion()\" size=\"20\" value='' onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
        sb.append("<script type=\"text/javascript\">");
        sb.append("var oLUPD_").append(fieldName).append("={");
        sb.append("\"selectortype\":\"none\",");
        sb.append("\"sdcid\":\"LV_ArrayLayout\",");
        sb.append("restrictivewhere:\"").append(SafeHTML.encodeForJavaScript(filter)).append("\",");
        sb.append("columns: [");
        sb.append("\n{id:\"arraylayoutid\",mapfieldid:\"arraylayoutid\",title:\"Array Layout\",lumode:\"Display and Return\",columnid:\"arraylayoutid\",mode:\"Display Text\"},");
        sb.append("\n{id:\"arraylayoutdesc\",title:\"Description\",lumode: \"Display\",columnid:\"arraylayoutdesc\",mode:\"Display Text\"},");
        sb.append("\n{id:\"arraylayoutversionid\",mapfieldid:\"arraylayoutversionid\",title:\"Version Id\",lumode:\"Display and Return\",columnid:\"arraylayoutversionid\",mode:\"Display Text\"}]};");
        sb.append("</script>");
        sb.append("</td>");
        sb.append("<td><a style=\"display:inline;\"  href=\"javascript:void(0);\" onclick=\"sapphire.ui.dialog.open('").append(title).append("', '").append(SafeHTML.encodeForJavaScript(url)).append("',true,'700','400');\" tabindex=\"0\"><img title='").append(title).append("' border=\"0\" src=\"rc?command=image&image=FlatBlackExternalLookup1\" class=\"lookup_img\"></a></td>");
        sb.append("<td>").append(tp.translate("Version")).append("</td><td><input type=\"text\" id=\"arraylayoutversionid\" readonly=\"\" value=\"\"/></td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    public static String getMethodLookupHtml(PageContext pageContext, String filter) {
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String title = tp.translate("Array Method Lookup");
        if (filter != null && filter.trim().length() > 0) {
            filter = filter + " and ";
        }
        filter = filter + " versionstatus in ('P','C')";
        String url = "rc?command=page&page=LV_ArrayMethodLookup&fieldid=arraymethodid;arraymethodversionid&lookupcallback=parent.setMethodCallback";
        if (filter.trim().length() > 0) {
            url = url + "&restrictivewhere=" + filter;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td>");
        sb.append("<input style=\"border:1px solid green;width:250px;\" readonly=\"\" edit=\"lookup\" type=\"text\" class=\"input_field\" name=\"arraymethodid\" id=\"arraymethodid\"");
        sb.append(" onkeyup=\";showSuggestion()\" size=\"20\" value=\"\" onkeydown=\"if(event.keyCode==8){return false;};if(event.keyCode==46){sapphire.lookup.sdi.clear(this.id);}\">");
        sb.append("<script type=\"text/javascript\">");
        sb.append("var oLUPD_arraymethodid={");
        sb.append("\"selectortype\":\"none\",");
        sb.append("\"sdcid\":\"LV_ArrayMethod\",");
        sb.append("restrictivewhere:\"").append(SafeHTML.encodeForJavaScript(filter)).append("\",");
        sb.append("columns: [");
        sb.append("\n{id:\"arraymethodid\",mapfieldid:\"arraymethodid\",title:\"Array Method\",lumode:\"Display and Return\",columnid:\"arraymethodid\",mode:\"Display Text\"},");
        sb.append("\n{id:\"arraymethoddesc\",title:\"Description\",lumode:\"Display\",columnid:\"arraymethoddesc\",mode:\"Display Text\"},");
        sb.append("\n{id:\"arraymethodversionid\",mapfieldid:\"arraymethodversionid\",title:\"Version Id\",lumode:\"Display and Return\",columnid:\"arraymethodversionid\",mode:\"Display Text\"}]};");
        sb.append("</script>");
        sb.append("</td>");
        sb.append("<td><a style=\"display:inline;\" href=\"javascript:void(0);\" onclick=\"sapphire.ui.dialog.open('").append(title).append("','").append(SafeHTML.encodeForJavaScript(url)).append("',true,'700','400' );\" tabindex=\"0\"><img title='").append(title).append("' border=\"0\" src=\"rc?command=image&image=FlatBlackExternalLookup1\" class=\"lookup_img\"></a></td>");
        sb.append("<td>").append(tp.translate("Version")).append("</td><td><input type=\"text\" id=\"arraymethodversionid\" value=\"\" readonly=\"\"/></td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    public static String getLastArrayMethodItem(QueryProcessor queryProcessor, String arrayid) {
        String sql = "SELECT arrayid, arraymethodid, arraymethodinstance, usersequence FROM arrayarraymethoditem WHERE arrayid = ? ORDER BY usersequence desc";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayid});
        if (ds != null && ds.getRowCount() > 0) {
            return ds.getString(0, "arraymethodid") + "|" + ds.getValue(0, "arraymethodinstance");
        }
        return "";
    }

    public static Map<String, String> createChildSamples(Set<String> parentSampleIds, String sampleTypeId, DAMProcessor damProcessor, QueryProcessor queryProcessor, ActionProcessor actionProcessor, ConfigurationProcessor configurationProcessor) throws SapphireException {
        if (!parentSampleIds.isEmpty()) {
            String[] childs;
            HashMap<String, String> parentChildMap = new HashMap<String, String>();
            StringBuffer parentSamples = new StringBuffer();
            for (String parentSampleId : parentSampleIds) {
                parentSamples.append(";").append(parentSampleId);
            }
            String samples = parentSamples.substring(1);
            String rsetid = damProcessor.createRSet("Sample", samples, "", "");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s_sampleid,        sampletypeid FROM   s_sample,        rsetitems WHERE  s_sample.s_sampleid = rsetitems.keyid1        AND rsetitems.rsetid = " + safeSQL.addVar(rsetid);
            DataSet samplesTypeDS = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            StringBuffer aliquotSamples = new StringBuffer();
            StringBuffer derivativeSamples = new StringBuffer();
            for (int i = 0; i < samplesTypeDS.size(); ++i) {
                String s_sampleid = samplesTypeDS.getValue(i, "s_sampleid");
                String sampletypeid = samplesTypeDS.getValue(i, "sampletypeid");
                if (sampleTypeId.length() == 0 || sampleTypeId.equalsIgnoreCase(sampletypeid)) {
                    aliquotSamples.append(";").append(s_sampleid);
                    continue;
                }
                derivativeSamples.append(";").append(s_sampleid);
            }
            StringBuffer parentSampleids = new StringBuffer();
            StringBuffer childSampleids = new StringBuffer();
            PropertyList policy = configurationProcessor.getPolicy("ArraysPolicy", "Sapphire Custom");
            ArraysPolicy policyDef = new ArraysPolicy(policy);
            String childsamplestoragestatus = policyDef.getChildSampleStorageStatus();
            String childsamplestatus = policyDef.getChildSampleStatus();
            if (aliquotSamples.length() > 0) {
                String aliquotsample = aliquotSamples.substring(1);
                int listSize = PropertyUtil.getListItemCount(aliquotsample);
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("child_copies", PropertyUtil.repeat("1", listSize));
                actionProps.setProperty("parent_sampleid", aliquotsample);
                actionProps.setProperty("mode", "Aliquot");
                actionProps.setProperty("child_quantity", PropertyUtil.repeat("0", listSize));
                actionProps.setProperty("childcolumn_addtrackitem", "Y");
                if (childsamplestatus != null) {
                    if (!childsamplestatus.equals("Inherit from Parent")) {
                        actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, listSize, ";"));
                    } else {
                        actionProps.setProperty("childsamplestatus", StringUtil.repeat("inherit", listSize, ";"));
                    }
                }
                if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                    actionProps.setProperty("child_storagestatus", PropertyUtil.repeat(childsamplestoragestatus, listSize));
                    actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                    actionProps.setProperty("childcolumn_samplereceiptfequiredflag", "N");
                }
                actionProcessor.processAction("MultiSampleChild", "1", actionProps);
                childSampleids.append(actionProps.getProperty("newkeyid1"));
                parentSampleids.append(actionProps.getProperty("parentsampleid"));
            }
            if (derivativeSamples.length() > 0) {
                String derivativesample = derivativeSamples.substring(1);
                PropertyList actionProps = new PropertyList();
                int listSize = PropertyUtil.getListItemCount(derivativesample);
                actionProps.setProperty("child_copies", PropertyUtil.repeat("1", listSize));
                actionProps.setProperty("parent_sampleid", derivativesample);
                actionProps.setProperty("mode", "Derivative");
                actionProps.setProperty("child_sampletypeid", PropertyUtil.repeat(sampleTypeId, listSize));
                actionProps.setProperty("child_quantity", PropertyUtil.repeat("0", listSize));
                if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                    actionProps.setProperty("child_storagestatus", PropertyUtil.repeat(childsamplestoragestatus, listSize));
                    actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                    actionProps.setProperty("childcolumn_samplereceiptfequiredflag", "N");
                }
                if (childsamplestatus != null) {
                    if (!childsamplestatus.equals("Inherit from Parent")) {
                        actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, listSize, ";"));
                    } else {
                        actionProps.setProperty("childsamplestatus", "inherit");
                    }
                }
                actionProps.setProperty("childcolumn_addtrackitem", "Y");
                actionProcessor.processAction("MultiSampleChild", "1", actionProps);
                childSampleids.append(childSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("newkeyid1"));
                parentSampleids.append(parentSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("parentsampleid"));
            }
            String parentsampleids = parentSampleids.toString();
            String childsampleids = childSampleids.toString();
            String[] parents = StringUtil.split(parentsampleids, ";");
            if (parents.length == (childs = StringUtil.split(childsampleids, ";")).length) {
                for (int i = 0; i < parents.length; ++i) {
                    String parent = parents[i];
                    String child = childs[i];
                    parentChildMap.put(parent, child);
                }
                return parentChildMap;
            }
        }
        return null;
    }

    public static DataSet addOrdinalNumber(DataSet arrayitemcontent, String loadingdirection, int rowCount, int colCount) {
        arrayitemcontent.addColumn("ordinalnum", 1);
        for (int i = 0; i < arrayitemcontent.getRowCount(); ++i) {
            String arrayitemid = arrayitemcontent.getValue(i, "arrayitemid");
            String[] tokens = StringUtil.split(arrayitemid, "_");
            int rowNum = Integer.parseInt(tokens[1]);
            int columnNum = Integer.parseInt(tokens[2]);
            int ordinalNum = ArrayUtil.getOrdinalNumber(rowNum, columnNum, rowCount, colCount, loadingdirection);
            arrayitemcontent.setNumber(i, "ordinalnum", new BigDecimal(ordinalNum));
        }
        return arrayitemcontent;
    }

    public static int getOrdinalNumber(int rowNum, int columnNum, int rowCount, int colCount, String loadingDirection) {
        int ordinalNumber = -1;
        ordinalNumber = loadingDirection.equals("Horizontal") ? rowNum * colCount + columnNum : (loadingDirection.equals("HorizontalSnaking") ? (rowNum % 2 == 0 ? rowNum * colCount + columnNum : rowNum * colCount + (colCount - columnNum)) : (loadingDirection.equals("Vertical") ? columnNum * rowCount + rowNum : (loadingDirection.equals("VerticalSnaking") ? (columnNum % 2 == 0 ? columnNum * rowCount + rowNum : columnNum * rowCount + (rowCount - rowNum)) : rowNum * colCount + columnNum)));
        return ordinalNumber;
    }

    public static List createDistinctChildSamples(List<String> parentSampleIds, List<Integer> childcounts, String childSampleTypeId, DAMProcessor damProcessor, QueryProcessor queryProcessor, ActionProcessor actionProcessor, ConfigurationProcessor configurationProcessor) throws SapphireException {
        if (!parentSampleIds.isEmpty()) {
            String[] childs;
            ArrayList<String> parentList = new ArrayList<String>();
            ArrayList<String> childList = new ArrayList<String>();
            StringBuffer parentSamples = new StringBuffer();
            for (String parentSampleId : parentSampleIds) {
                parentSamples.append(";").append(parentSampleId);
            }
            String samples = parentSamples.substring(1);
            String rsetid = damProcessor.createRSet("Sample", samples, "", "");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s_sampleid,        sampletypeid FROM   s_sample,        rsetitems WHERE  s_sample.s_sampleid = rsetitems.keyid1        AND rsetitems.rsetid = " + safeSQL.addVar(rsetid);
            DataSet samplesTypeDS = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            StringBuffer aliquotSamples = new StringBuffer();
            String aliquotCounts = "";
            String aliquotVolumes = "";
            StringBuffer derivativeSamples = new StringBuffer();
            String derivativeCounts = "";
            String derivativeVolumes = "";
            String parentQtyCheck = "select count(*) from trackitem where linksdcid='Sample' and qtycurrent is null and linkkeyid1 = ?";
            for (int i = 0; i < samplesTypeDS.size(); ++i) {
                int parentWithoutQtyCount;
                int childCount;
                int posInInputList;
                String s_sampleid = samplesTypeDS.getValue(i, "s_sampleid");
                String parentSampleTypeId = samplesTypeDS.getValue(i, "sampletypeid");
                if (childSampleTypeId.length() == 0 || childSampleTypeId.equalsIgnoreCase(parentSampleTypeId)) {
                    aliquotSamples.append(";").append(s_sampleid);
                    aliquotVolumes = aliquotVolumes + ";";
                    posInInputList = parentSampleIds.indexOf(s_sampleid);
                    childCount = childcounts.get(posInInputList);
                    if (aliquotCounts.length() > 0) {
                        aliquotCounts = aliquotCounts + ";";
                    }
                    aliquotCounts = aliquotCounts + childCount;
                    parentWithoutQtyCount = queryProcessor.getPreparedCount(parentQtyCheck, new String[]{s_sampleid});
                    if (parentWithoutQtyCount > 0) {
                        aliquotVolumes = aliquotVolumes + "(null)";
                        continue;
                    }
                    aliquotVolumes = aliquotVolumes + "0";
                    continue;
                }
                derivativeSamples.append(";").append(s_sampleid);
                posInInputList = parentSampleIds.indexOf(s_sampleid);
                childCount = childcounts.get(posInInputList);
                derivativeVolumes = derivativeVolumes + ";";
                if (derivativeCounts.length() > 0) {
                    derivativeCounts = derivativeCounts + ";";
                }
                derivativeCounts = derivativeCounts + childCount;
                parentWithoutQtyCount = queryProcessor.getPreparedCount(parentQtyCheck, new String[]{s_sampleid});
                derivativeVolumes = parentWithoutQtyCount > 0 ? derivativeVolumes + "(null)" : derivativeVolumes + "0";
            }
            StringBuffer parentSampleids = new StringBuffer();
            StringBuffer childSampleids = new StringBuffer();
            PropertyList policy = configurationProcessor.getPolicy("ArraysPolicy", "Sapphire Custom");
            ArraysPolicy policyDef = new ArraysPolicy(policy);
            String childsamplestoragestatus = policyDef.getChildSampleStorageStatus();
            String childsamplestatus = policyDef.getChildSampleStatus();
            if (aliquotSamples.length() > 0) {
                String aliquotsample = aliquotSamples.substring(1);
                int listSize = PropertyUtil.getListItemCount(aliquotsample);
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("child_copies", PropertyUtil.repeat("1", listSize));
                actionProps.setProperty("parent_sampleid", aliquotsample);
                actionProps.setProperty("child_copies", aliquotCounts);
                actionProps.setProperty("mode", "Aliquot");
                actionProps.setProperty("child_quantity", aliquotVolumes.substring(1));
                actionProps.setProperty("childcolumn_addtrackitem", "Y");
                actionProps.setProperty("propsmatch", "Y");
                if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                    actionProps.setProperty("child_storagestatus", PropertyUtil.repeat(childsamplestoragestatus, listSize));
                    actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                    actionProps.setProperty("childcolumn_receiverequiredflag", "N");
                }
                if (childsamplestatus != null && !childsamplestatus.equals("Inherit from Parent")) {
                    actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, listSize, ";"));
                }
                actionProcessor.processAction("MultiSampleChild", "1", actionProps);
                childSampleids.append(actionProps.getProperty("newkeyid1"));
                parentSampleids.append(actionProps.getProperty("parentsampleid"));
            }
            if (derivativeSamples.length() > 0) {
                String derivativesample = derivativeSamples.substring(1);
                PropertyList actionProps = new PropertyList();
                int listSize = PropertyUtil.getListItemCount(derivativesample);
                actionProps.setProperty("child_copies", PropertyUtil.repeat("1", listSize));
                actionProps.setProperty("parent_sampleid", derivativesample);
                actionProps.setProperty("child_copies", derivativeCounts);
                actionProps.setProperty("mode", "Derivative");
                actionProps.setProperty("child_sampletypeid", PropertyUtil.repeat(childSampleTypeId, listSize));
                actionProps.setProperty("child_quantity", derivativeVolumes.substring(1));
                if (!childsamplestoragestatus.equals("Use Biobanking Policy")) {
                    actionProps.setProperty("child_storagestatus", PropertyUtil.repeat(childsamplestoragestatus, listSize));
                }
                if (childsamplestatus != null && !childsamplestatus.equals("Inherit from Parent")) {
                    actionProps.setProperty("childcolumn_samplestatus", StringUtil.repeat(childsamplestatus, listSize, ";"));
                    actionProps.setProperty("childcolumn_reviewrequiredflag", "N");
                    actionProps.setProperty("childcolumn_samplereceiptfequiredflag", "N");
                }
                actionProps.setProperty("childcolumn_addtrackitem", "Y");
                actionProps.setProperty("propsmatch", "Y");
                actionProcessor.processAction("MultiSampleChild", "1", actionProps);
                childSampleids.append(childSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("newkeyid1"));
                parentSampleids.append(parentSampleids.length() > 0 ? ";" : "").append(actionProps.getProperty("parentsampleid"));
            }
            String parentsampleids = parentSampleids.toString();
            String childsampleids = childSampleids.toString();
            String[] parents = StringUtil.split(parentsampleids, ";");
            if (parents.length == (childs = StringUtil.split(childsampleids, ";")).length) {
                for (int i = 0; i < parents.length; ++i) {
                    String parent = parents[i];
                    String child = childs[i];
                    parentList.add(parent);
                    childList.add(child);
                }
                ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
                list.add(parentList);
                list.add(childList);
                return list;
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void updateTrackItems(DataSet trackdata, ActionProcessor actionProcessor) throws ActionException {
        Boolean bl = lock;
        synchronized (bl) {
            String tracks = "";
            String updQty = "";
            String updQtyUnit = "";
            if (trackdata != null) {
                M18NUtil m18n = trackdata.getM18n();
                for (int i = 0; i < trackdata.size(); ++i) {
                    String trackitemid = trackdata.getValue(i, "trackitemid");
                    String decrvol = trackdata.getValue(i, "srcdecrvol");
                    String decrunit = trackdata.getValue(i, "srcdecrvolunits");
                    String qtycurrent = trackdata.getValue(i, "qtycurrent");
                    String qtyunits = trackdata.getValue(i, "qtyunits");
                    BigDecimal decr = BigDecimal.ZERO;
                    try {
                        decr = m18n.parseBigDecimal(decrvol);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    if (trackitemid == null || trackitemid.length() <= 0 || decrvol == null || decrvol.length() <= 0 || decr.compareTo(BigDecimal.ZERO) <= 0 || qtycurrent.length() <= 0 || qtyunits.length() <= 0) continue;
                    tracks = tracks + ";" + trackitemid;
                    updQty = updQty + ";-" + decrvol;
                    updQtyUnit = updQtyUnit + ";" + decrunit;
                }
                if (tracks.length() > 0) {
                    PropertyList properties = new PropertyList();
                    properties.setProperty("trackitemid", tracks.substring(1));
                    properties.setProperty("quantity", updQty.substring(1));
                    properties.setProperty("quantityunit", updQtyUnit.substring(1));
                    actionProcessor.processAction("AdjustTrackItemInv", "1", properties);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkExistenceInArray(String rsetid, PropertyList actionProps, QueryProcessor queryProcessor, TranslationProcessor translationProcessor, boolean isArray) throws SapphireException {
        Boolean bl = deleteLock;
        synchronized (bl) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT DISTINCT " + (isArray ? "aic.contentlabel" : "aic.contentkeyid1") + " content,                ai.arrayid array FROM   arrayitemcontent aic        LEFT OUTER JOIN arrayitem ai                     ON aic.arrayitemid = ai.arrayitemid WHERE  aic.contentkeyid1 IN (SELECT keyid1                              FROM   rsetitems                              WHERE  rsetid = " + safeSQL.addVar(rsetid) + ") ORDER  BY content";
            DataSet sqlDataSet = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (sqlDataSet != null && !sqlDataSet.isEmpty()) {
                ArrayList<DataSet> sampleArray = sqlDataSet.getGroupedDataSets("content");
                String msg = "";
                Iterator<DataSet> iterator = sampleArray.iterator();
                while (iterator.hasNext()) {
                    DataSet o;
                    DataSet ds = o = iterator.next();
                    String arrays = ds.getColumnValues("array", ";");
                    msg = msg + "; <b>" + ds.getValue(0, "content") + "</b> found in " + arrays;
                }
                throw new SapphireException(translationProcessor.translate("Found in Array"), "FAILURE", msg.substring(2));
            }
        }
    }

    public static DataSet getTransferMethodDetails(QueryProcessor queryProcessor, String transferMethodId, String transferMethodVersionId) {
        String sql = "SELECT arraytransfermethodid, arraytransfermethodversionid, numsourcearrays, numtargetarrays, sourcearraytypeid, sourcearraytypeversionid, targetarraytypeid, targetarraytypeversionid, transfermap, contenttransferrule, createchildsampleflag, sampletypeid, targetvolume, targetvolumeunits, targetconcentration, targetconcentrationunits  FROM arraytransfermethod WHERE arraytransfermethodid = ? ";
        SafeSQL safeSQL1 = new SafeSQL();
        safeSQL1.addVar(transferMethodId);
        if (transferMethodVersionId.length() > 0) {
            sql = sql + " and arraytransfermethodversionid = ?";
            safeSQL1.addVar(transferMethodVersionId);
        }
        return queryProcessor.getPreparedSqlDataSet(sql, safeSQL1.getValues(), true);
    }

    public static DataSet getArrayTypeDimensions(QueryProcessor qp, String arrayTypeId, String arrayTypeVersionId) {
        return qp.getPreparedSqlDataSet("SELECT numrows, numcolumns FROM arraytype WHERE arraytypeid=? and arraytypeversionid = ?", (Object[])new String[]{arrayTypeId, arrayTypeVersionId});
    }

    public static String getTransferMethodCurrentVersion(QueryProcessor qp, String arrayTransferMethodId) {
        String versionStatus;
        int i;
        DataSet ds = qp.getPreparedSqlDataSet("SELECT arraytransfermethodversionid, versionstatus FROM arraytransfermethod WHERE arraytransfermethodid=? order by arraytransfermethodversionid desc", (Object[])new String[]{arrayTransferMethodId});
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("C")) continue;
            return ds.getString(i, "arraytransfermethodversionid");
        }
        for (i = 0; i < ds.getRowCount(); ++i) {
            versionStatus = ds.getString(i, "versionstatus");
            if (!versionStatus.equals("P")) continue;
            return ds.getString(i, "arraytransfermethodversionid");
        }
        return "";
    }

    public static String getArrayStatus(QueryProcessor qp, String arrayid) throws SapphireException {
        DataSet ds = qp.getPreparedSqlDataSet("SELECT arrayid, arraystatus from array where arrayid = ?", (Object[])new String[]{arrayid});
        if (ds == null || ds.getRowCount() == 0) {
            throw new SapphireException("Failed to fetch array status for:" + arrayid);
        }
        return ds.getString(0, "arraystatus");
    }

    public static boolean isArrayTypeASLGrid(QueryProcessor qp, String arraytypeid) {
        DataSet ds = qp.getPreparedSqlDataSet("SELECT aslflag FROM arraytype WHERE arraytypeid = ?", (Object[])new String[]{arraytypeid});
        return ds != null && ds.size() > 0 && "Y".equalsIgnoreCase(ds.getString(0, "aslflag", "N"));
    }

    public static String[] getArrayType(QueryProcessor qp, String arrayid) throws SapphireException {
        DataSet ds = qp.getPreparedSqlDataSet("SELECT arraytypeid, arraytypeversionid from array where arrayid = ?", (Object[])new String[]{arrayid});
        if (ds == null || ds.size() == 0) {
            throw new SapphireException("Failed to fetch array status for:" + arrayid);
        }
        return new String[]{ds.getString(0, "arraytypeid"), ds.getString(0, "arraytypeversionid")};
    }
}

