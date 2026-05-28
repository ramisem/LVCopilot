/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.BaseCustom;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;

public class DDTLabelsUtil
extends BaseCustom {
    static String[] tablenames = new String[]{"sdiattachment", "sdidata", "sdidataitem", "sdidataitemlimits", "sdidataapproval", "sdidatarelation", "sdidataitemspec", "sdispec", "sdispecrule", "sdiaddress", "sdiworkitem", "sdicoc", "sdipricelist", "sdicategoryitem", "sdirole", "sdiworkitemitem", "sdiworkitemrelation", "sdiapproval", "sdiapprovalstep", "sdidocument", "sdiformrule", "sdieventplan", "sdieventplanitem", "sdieventplanitemproperty", "sdiworkflowrule", "sdinote", "sdiattribute", "sdialias", "sdicalendar", "sdiworksheetrule", "sdicaptureoperation", "sdidatacapture", "sdiresourcerequirement", "sdiattachmentoperation", "sdiworkitemattribute", "datasetattribute", "dataitemattribute", "attachmentattribute"};
    static String[] tablelabels = new String[]{"Attachment", "DataSet", "DataItem", "DataItemLimits", "DataApproval", "DataRelation", "DataItemSpec", "Spec", "SpecRule", "Address", "Test Method", "COC", "PriceList", "Category", "Role", "Test Method Item", "Test Method Relation", "Approval", "Approval Step", "Document", "Form Rule", "Event Plan", "Event Plan Item", "Event Plan Item Property", "Workflow Rule", "Note", "Attribute", "Alias", "Calendar", "Worksheet Rule", "Capture Operation", "Data Capture", "Resource Requirement", "Attachment Operations", "Test Attribute", "DataSet Attribute", "DataItem Attribute", "Attachment Attribute"};
    static String[] itemdisplays = new String[]{"[attachmentnum]", "[paramlistid], [paramlistversionid], [variantid], [dataset]", "[paramlistid], [paramlistversionid], [variantid], [dataset], [paramid], [paramtype], [replicateid]", "[paramlistid], [paramlistversionid], [variantid], [dataset], [paramid], [paramtype], [replicateid], [limittypeid]", "[paramlistid], [paramlistversionid], [variantid], [dataset], [approvalstep]", "[paramlistid], [paramlistversionid], [variantid], [dataset], [relationid]", "[paramlistid], [paramlistversionid], [variantid], [dataset], [paramid], [paramtype], [replicateid], [specid], [specversionid]", "[specid], [specversionid]", "[specid], [specversionid], [ruleno]", "[addressid], [addresstype], [contactfunction]", "[workitemid], [workiteminstance]", "[cocid]", "[pricelistid]", "[categoryid]", "[roleid]", "[workitemid], [workiteminstance], [workitemitemid]", "[workitemid], [workiteminstance], [relationid]", "[approvaltypeid]", "[approvaltypeid], [approvalstep], [approvalstepinstance]", "[documentid], [documentversionid]", "[formid], [forminstance]", "[eventplanid], [eventplanversionid], [eventplaceinstance]", "[eventplanid], [eventplanversionid], [eventplaninstance], [eventplanitemid]", "[eventplanid], [eventplanversionid], [eventplaninstance], [eventplanitemid], [propertyid]", "[workflowdefid], [workflowdefversionid], [workflowdefvariantid], [taskdefitemid], [ioitemid], [workflowexecid]", "[notenum]", "[attributeid], [attributesdcid], [attributeinstance]", "[aliasid], [aliastype]", "[calenderid]", "[worksheetid], [worksheetinstance]", "[captureoperationid]", "[datacaptureid]", "[resourcenum]", "[operationkeyid1]", "[attributeid], [attributesdcid], [attributeinstance]", "[attributeid], [attributesdcid], [attributeinstance]", "[attributeid], [attributesdcid], [attributeinstance]", "[attributeid], [attributesdcid], [attributeinstance]"};

    public static HashMap<String, String> getColumnTitleMap(SDCProcessor sdcProcessor, String tableid, String[] cols) {
        HashMap<String, String> ret = new HashMap<String, String>();
        DataSet columnData = sdcProcessor.getTableColumnData(tableid);
        for (int i = 0; i < cols.length; ++i) {
            DataSet currCol;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("columnid", cols[i]);
            String currLabel = cols[i];
            if (columnData != null && (currCol = columnData.getFilteredDataSet(filter)).getRowCount() > 0) {
                currLabel = currCol.getString(0, "columnlabel", cols[i]);
            }
            ret.put(cols[i], currLabel);
        }
        return ret;
    }

    public static String getSDIDetailTableLabel(String tableid) throws SapphireException {
        if (tableid.startsWith("sdi")) {
            for (int i = 0; i < tablenames.length; ++i) {
                if (!tableid.equals(tablenames[i])) continue;
                return tablelabels[i];
            }
            throw new SapphireException("Missing sdi detail table mapping");
        }
        return tableid;
    }

    public static String getSDIDetailItemDisplay(String tableid) throws SapphireException {
        if (tableid.startsWith("sdi")) {
            for (int i = 0; i < tablenames.length; ++i) {
                if (!tableid.equals(tablenames[i])) continue;
                return itemdisplays[i];
            }
            throw new SapphireException("Missing sdi detail table mapping");
        }
        return "";
    }

    public static String getLinkItemDisplay(SDCProcessor sdcProcessor, String sdcid, String linkid, String detailtableid) throws SapphireException {
        if (linkid == null) {
            if (detailtableid.startsWith("sdi")) {
                return DDTLabelsUtil.getSDIDetailItemDisplay(detailtableid);
            }
            if (detailtableid.equals("categoryitem")) {
                return "[categoryid]";
            }
            if (detailtableid.equals("sdcsecurity")) {
                return "[sdcid], [operationid], [sysuserid]";
            }
            if (detailtableid.equals("sdcjobtypesecurity")) {
                return "[sdcid], [operationid], [jobtypeid]";
            }
            throw new SapphireException("Unexpected null linkid");
        }
        HashMap linkProps = sdcProcessor.getLinkProperties(sdcid, linkid);
        if (linkProps != null && linkProps.get("linkitemdisplay") != null) {
            String label = (String)linkProps.get("linkitemdisplay");
            if (label == null || label.length() == 0) {
                return "";
            }
            return label;
        }
        return "";
    }

    public static String getLinkTableLabel(SDCProcessor sdcProcessor, String sdcid, String linkid, String tableid) throws SapphireException {
        HashMap linkProps;
        if (linkid == null || linkid.length() == 0) {
            if (tableid.startsWith("sdi")) {
                return DDTLabelsUtil.getSDIDetailTableLabel(tableid);
            }
            if (tableid.equals("categoryitem")) {
                return "Categories";
            }
            return tableid;
        }
        if (linkid.startsWith(sdcid + ";")) {
            linkid = linkid.substring(linkid.indexOf(";") + 1);
        }
        if ((linkProps = sdcProcessor.getLinkProperties(sdcid, linkid)) != null && linkProps.get("linktablelabel") != null) {
            String label = (String)linkProps.get("linktablelabel");
            if (label == null || label.length() == 0) {
                return linkid;
            }
            return label;
        }
        return tableid;
    }
}

