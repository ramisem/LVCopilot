/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CreateInspectionLot
extends BaseAction {
    public static final String PROPERTY_ENDDATE = "enddate";
    public static final String PROPERTY_MIC = "mic";
    public static final String PROPERTY_INSPECTIONLOTORIGIN = "inspectionlotorigin";
    public static final String PROPERTY_INSPECTIONLOTQTY = "inspectionlotqty";
    public static final String PROPERTY_INSPECTIONTYPE = "inspectiontype";
    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_MATERIALID = "materialid";
    public static final String PROPERTY_NUMOFCONTAINER = "numofcontainer";
    public static final String PROPERTY_PLANT = "plantid";
    public static final String PROPERTY_PURCHASINGORG = "purchasingorg";
    public static final String PROPERTY_REVISIONLEVEL = "revisionlevel";
    public static final String PROPERTY_SHORTTEXT = "shorttext";
    public static final String PROPERTY_SPECIFICATION = "specification";
    public static final String PROPERTY_STARTDATE = "startdate";
    public static final String PROPERTY_VENDORNUMBER = "vendornumber";
    public static final String PROPERTY_INSPECTIONLOTNO = "inspectionlotnumber";
    public static final String PROPERTY_INSPOPERATIONNO = "inspoperationno";
    public static final String PROPERTY_INSPCHARNO = "inspcharno";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        propertyList.setProperty("conn", "testing");
        String inspectionLotNo = propertyList.getProperty(PROPERTY_INSPECTIONLOTNO);
        String endDate = propertyList.getProperty(PROPERTY_ENDDATE);
        String mic = propertyList.getProperty(PROPERTY_MIC);
        String inspectionLotOrigin = propertyList.getProperty(PROPERTY_INSPECTIONLOTORIGIN);
        String inspectionLotQty = propertyList.getProperty(PROPERTY_INSPECTIONLOTQTY);
        String inspectionType = propertyList.getProperty(PROPERTY_INSPECTIONTYPE);
        String manufacturer = propertyList.getProperty(PROPERTY_MANUFACTURER);
        String materialId = propertyList.getProperty(PROPERTY_MATERIALID);
        String numOfContainer = propertyList.getProperty(PROPERTY_NUMOFCONTAINER);
        String plant = propertyList.getProperty(PROPERTY_PLANT);
        String purchasingOrg = propertyList.getProperty(PROPERTY_PURCHASINGORG);
        String revisionLevel = propertyList.getProperty(PROPERTY_REVISIONLEVEL);
        String shortText = propertyList.getProperty(PROPERTY_SHORTTEXT);
        String specification = propertyList.getProperty(PROPERTY_SPECIFICATION);
        String startDate = propertyList.getProperty(PROPERTY_STARTDATE);
        String vendorNumber = propertyList.getProperty(PROPERTY_VENDORNUMBER);
        String inspOperationNo = propertyList.getProperty(PROPERTY_INSPOPERATIONNO);
        String inspCharNo = propertyList.getProperty(PROPERTY_INSPCHARNO);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Sample");
        if (materialId != null && materialId.length() > 0) {
            String sql = "SELECT count(*) FROM s_material where s_materialid='" + materialId + "'";
            if (this.database.getCount(sql) == 0) {
                throw new ActionException("MaterialID is invalid");
            }
            props.setProperty("u_materialid", materialId);
        }
        if (plant != null && plant.length() > 0) {
            props.setProperty("u_plant", plant);
        }
        if (inspectionLotOrigin != null && inspectionLotOrigin.length() > 0) {
            props.setProperty("u_inspeclotorigin", inspectionLotOrigin);
        }
        if (inspectionLotNo != null && inspectionLotNo.length() > 0) {
            props.setProperty("u_batch", inspectionLotNo);
        }
        if (specification != null && specification.length() > 0) {
            props.setProperty("u_col", specification);
        }
        if (revisionLevel != null && revisionLevel.length() > 0) {
            props.setProperty("u_revisionlevel", revisionLevel);
        }
        if (inspectionType != null && inspectionType.length() > 0) {
            props.setProperty("u_inspecttype", inspectionType);
        }
        if (inspectionLotQty != null && inspectionLotQty.length() > 0) {
            props.setProperty("u_inspecqty", inspectionLotQty);
        }
        if (numOfContainer != null && numOfContainer.length() > 0) {
            props.setProperty("u_numoflotcontainer", numOfContainer);
        }
        if (startDate != null && startDate.length() > 0) {
            props.setProperty("u_inspecstartdt", startDate);
        }
        if (endDate != null && endDate.length() > 0) {
            props.setProperty("u_inspecenddt", endDate);
        }
        if (manufacturer != null && manufacturer.length() > 0) {
            props.setProperty("u_manufacturer", manufacturer);
        }
        if (vendorNumber != null && vendorNumber.length() > 0) {
            props.setProperty("u_vendorno", vendorNumber);
        }
        if (purchasingOrg != null && purchasingOrg.length() > 0) {
            props.setProperty("u_purchasingorg", purchasingOrg);
        }
        if (shortText != null && shortText.length() > 0) {
            props.setProperty("u_shorttext", shortText);
        }
        if (inspOperationNo != null && inspOperationNo.length() > 0) {
            props.setProperty("u_inspoperationno", inspOperationNo);
        }
        props.setProperty("sampletypeid", "Research");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        String sampleId = (String)props.get("newkeyid1");
        if (sampleId == null || sampleId.length() == 0) {
            throw new SapphireException("Failed to create sample");
        }
        String[] paramListIds = mic.split(";");
        String[] inspCharNos = inspCharNo.split(";");
        for (int i = 0; i < paramListIds.length; ++i) {
            String sql = "SELECT count(*) FROM paramlist where paramlistid='" + paramListIds[i] + "'";
            if (this.database.getCount(sql) == 0) {
                throw new ActionException("ParamlistID is invalid " + paramListIds[i]);
            }
            props = new PropertyList();
            props.setProperty("keyid1", sampleId);
            props.setProperty("sdcid", "Sample");
            props.setProperty("paramlistid", paramListIds[i]);
            props.setProperty("paramlistversionid", "1");
            props.setProperty("variantid", "1");
            props.setProperty("dataset", "" + i);
            this.getActionProcessor().processAction("AddDataSet", "1", props);
            props.put("sapresponse", "Success message");
            props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", sampleId);
            props.setProperty("paramlistid", paramListIds[i]);
            props.setProperty("paramlistversionid", "1");
            props.setProperty("variantid", "1");
            props.setProperty("dataset", "" + i);
            props.setProperty("u_inspcharno", inspCharNos[i]);
            this.getActionProcessor().processAction("EditDataSet", "1", props);
        }
        propertyList.setProperty("responsemessage", "Created sample:" + sampleId + " and added paramlist(s):" + mic);
    }
}

