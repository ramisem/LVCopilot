/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.actions.BaseDataCapture;
import java.util.Calendar;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class CreateDataCapture
extends BaseDataCapture
implements SDMSConstants {
    public static final String PROPERTY_INSTRUMENTID = "instrumentid";
    public static final String PROPERTY_DATACAPTURELOG = "datacapturelog";
    public static final String PROPERTY_CAPTUREDT = "capturedt";
    public static final String PROPERTY_TRIGGERDT = "triggerdt";
    public static final String PROPERTY_AUTOCAPTURE = "autocapture";
    public static final String PROPERTY_SDMSCOLLECTORID = "sdmscollectorid";
    public static final String RETURN_DATACAPTUREID = "datacaptureid";

    @Override
    public void addMetaData(PropertyList propertyList, String datacaptureid, boolean updateable) throws SapphireException {
        PropertyList props = new PropertyList();
        Iterator it = propertyList.keySet().iterator();
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        while (it.hasNext()) {
            String key = it.next().toString();
            if (key.equalsIgnoreCase(PROPERTY_INSTRUMENTID) || key.equalsIgnoreCase(PROPERTY_SDMSCOLLECTORID) || key.equalsIgnoreCase(PROPERTY_AUTOCAPTURE) || key.equalsIgnoreCase("tempfileid") || key.equalsIgnoreCase("filepath") || key.equalsIgnoreCase("filemetadata") || key.equalsIgnoreCase(RETURN_DATACAPTUREID) || key.equalsIgnoreCase(PROPERTY_DATACAPTURELOG) || key.equalsIgnoreCase(PROPERTY_CAPTUREDT) || key.equalsIgnoreCase(PROPERTY_TRIGGERDT) || key.equalsIgnoreCase("filereference")) continue;
            if (key.equalsIgnoreCase("creationdt") || key.equalsIgnoreCase("lastaccessdt") || key.equalsIgnoreCase("lastmodifieddt")) {
                String value = propertyList.getProperty(key, "");
                if (value.length() > 0) {
                    try {
                        Calendar c = SDMSUtil.parseCalendar(value);
                        value = dtu.getDefaultDateFormat().format(c.getTime());
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                props.put(key, value);
                continue;
            }
            props.put(key, propertyList.get(key));
        }
        super.addMetaData(props, datacaptureid, updateable);
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        PropertyList addSDI = new PropertyList();
        addSDI.setProperty("sdcid", "LV_DataCapture");
        addSDI.setProperty("copies", "1");
        addSDI.setProperty("datacapturestatus", "Capturing");
        if (properties.getProperty(PROPERTY_INSTRUMENTID).length() > 0) {
            addSDI.setProperty(PROPERTY_INSTRUMENTID, properties.getProperty(PROPERTY_INSTRUMENTID));
        }
        if (properties.getProperty(PROPERTY_SDMSCOLLECTORID).length() > 0) {
            addSDI.setProperty(PROPERTY_SDMSCOLLECTORID, properties.getProperty(PROPERTY_SDMSCOLLECTORID));
        }
        if (properties.getProperty(PROPERTY_DATACAPTURELOG).length() > 0) {
            addSDI.setProperty(PROPERTY_DATACAPTURELOG, properties.getProperty(PROPERTY_DATACAPTURELOG));
        }
        if (properties.getProperty(PROPERTY_CAPTUREDT).length() > 0) {
            addSDI.setProperty(PROPERTY_CAPTUREDT, properties.getProperty(PROPERTY_CAPTUREDT));
        }
        if (properties.getProperty(PROPERTY_TRIGGERDT).length() > 0) {
            addSDI.setProperty(PROPERTY_TRIGGERDT, properties.getProperty(PROPERTY_TRIGGERDT));
        }
        if (properties.getProperty("datacapturedesc").length() > 0) {
            addSDI.setProperty("datacapturedesc", properties.getProperty("datacapturedesc"));
            properties.remove("datacapturedesc");
        }
        try {
            this.getActionProcessor().processAction("AddSDI", "1", addSDI);
            String newid = addSDI.getProperty("newkeyid1");
            properties.setProperty(RETURN_DATACAPTUREID, newid);
            this.addMetaData(properties, newid, false);
            PropertyList attachmentMetaData = null;
            if (properties.containsKey("filemetadata")) {
                if (properties.get("filemetadata") instanceof PropertyList) {
                    attachmentMetaData = (PropertyList)properties.get("filemetadata");
                } else if (properties.getProperty("filemetadata").length() > 0) {
                    attachmentMetaData = new PropertyList();
                    String[] items = StringUtil.split(properties.getProperty("filemetadata"), ";");
                    for (int i = 0; i < items.length; ++i) {
                        String[] parts = StringUtil.split(items[i], "=");
                        attachmentMetaData.setProperty(parts[0], parts[1]);
                    }
                }
            }
            this.addAttachments(properties, newid, attachmentMetaData, this.connectionInfo.getConnectionId());
            if (properties.getProperty(PROPERTY_AUTOCAPTURE, "N").equalsIgnoreCase("Y")) {
                PropertyList editSDI = new PropertyList();
                editSDI.setProperty("sdcid", "LV_DataCapture");
                editSDI.setProperty("keyid1", newid);
                editSDI.setProperty("datacapturestatus", "Captured");
                this.getActionProcessor().processAction("EditSDI", "1", editSDI);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to add data capture sdi", e);
        }
    }
}

