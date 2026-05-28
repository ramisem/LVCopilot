/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.actions.BaseDataCapture;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.xml.PropertyList;

public class UpdateDataCapture
extends BaseDataCapture
implements SDMSConstants {
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_DATACAPTUREID = "datacaptureid";
    public static final String PROPERTY_DATACAPTURELOG = "datacapturelog";
    public static final String PROPERTY_CAPTUREDT = "capturedt";
    public static final String PROPERTY_TRIGGERDT = "triggerdt";

    public static void markCaptured(String datacaptureid, String datacaptureLog, String capturedt, ActionProcessor ap) throws SapphireException {
        PropertyList p = new PropertyList();
        p.setProperty(PROPERTY_STATUS, "Captured");
        p.setProperty(PROPERTY_DATACAPTUREID, datacaptureid);
        p.setProperty(PROPERTY_DATACAPTURELOG, datacaptureLog);
        if (capturedt != null && capturedt.length() > 0) {
            p.setProperty(PROPERTY_CAPTUREDT, capturedt);
        }
        ap.processActionClass(UpdateDataCapture.class.getName(), p);
    }

    public static void markFailure(String datacaptureid, String datacaptureLog, ActionProcessor ap) throws SapphireException {
        PropertyList p = new PropertyList();
        p.setProperty(PROPERTY_STATUS, "Failure");
        p.setProperty(PROPERTY_DATACAPTUREID, datacaptureid);
        p.setProperty(PROPERTY_DATACAPTURELOG, datacaptureLog);
        ap.processActionClass(UpdateDataCapture.class.getName(), p);
    }

    @Override
    public void addMetaData(PropertyList propertyList, String datacaptureid, boolean updateable) throws SapphireException {
        PropertyList props = new PropertyList();
        Iterator it = propertyList.keySet().iterator();
        DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
        while (it.hasNext()) {
            String key = it.next().toString();
            if (key.equalsIgnoreCase(PROPERTY_STATUS) || key.equalsIgnoreCase(PROPERTY_DATACAPTUREID) || key.equalsIgnoreCase("tempfileid") || key.equalsIgnoreCase("filepath") || key.equalsIgnoreCase("filemetadata") || key.equalsIgnoreCase(PROPERTY_DATACAPTURELOG) || key.equalsIgnoreCase(PROPERTY_CAPTUREDT) || key.equalsIgnoreCase(PROPERTY_TRIGGERDT) || key.equalsIgnoreCase("filereference")) continue;
            String value = propertyList.getProperty(key, "");
            if (value.length() > 0) {
                try {
                    Calendar c = SDMSUtil.parseCalendar(value);
                    if (c != null) {
                        value = dtu.getDefaultDateFormat().format(c.getTime());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            props.put(key, value);
        }
        super.addMetaData(props, datacaptureid, updateable);
    }

    public static void addMetaData(PropertyList propertyList, String[] ignore, String datacaptureid, boolean updateable, SDCProcessor sdc, ActionProcessor ap) throws SapphireException {
        ArrayList ignoreList;
        PropertyList props = new PropertyList();
        Iterator it = propertyList.keySet().iterator();
        ArrayList<Object> arrayList = ignoreList = ignore == null ? new ArrayList() : new ArrayList<String>(Arrays.asList(ignore));
        while (it.hasNext()) {
            String key = it.next().toString();
            if (ignoreList.contains(key)) continue;
            props.put(key, propertyList.get(key));
        }
        BaseSDIAttributeAction.addMetaData(props, "LV_DataCapture", datacaptureid, null, null, updateable, sdc, ap);
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block12: {
            String datacaptureid = properties.getProperty(PROPERTY_DATACAPTUREID);
            try {
                if (datacaptureid.length() > 0) {
                    this.addMetaData(properties, datacaptureid, true);
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
                    this.addAttachments(properties, datacaptureid, attachmentMetaData, this.connectionInfo.getConnectionId());
                    if (properties.getProperty(PROPERTY_STATUS).length() > 0) {
                        PropertyList editSDI = new PropertyList();
                        editSDI.setProperty("sdcid", "LV_DataCapture");
                        editSDI.setProperty("keyid1", datacaptureid);
                        editSDI.setProperty("datacapturestatus", properties.getProperty(PROPERTY_STATUS));
                        String log = properties.getProperty(PROPERTY_DATACAPTURELOG);
                        if (log.length() > 0) {
                            editSDI.setProperty(PROPERTY_DATACAPTURELOG, log);
                        }
                        if (properties.getProperty(PROPERTY_CAPTUREDT).length() > 0) {
                            editSDI.setProperty(PROPERTY_CAPTUREDT, properties.getProperty(PROPERTY_CAPTUREDT));
                        }
                        if (properties.getProperty(PROPERTY_TRIGGERDT).length() > 0) {
                            editSDI.setProperty(PROPERTY_TRIGGERDT, properties.getProperty(PROPERTY_TRIGGERDT));
                        }
                        this.getActionProcessor().processAction("EditSDI", "1", editSDI);
                    }
                    break block12;
                }
                throw new SapphireException("No data capture provided.");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to add data capture sdi", e);
            }
        }
    }
}

