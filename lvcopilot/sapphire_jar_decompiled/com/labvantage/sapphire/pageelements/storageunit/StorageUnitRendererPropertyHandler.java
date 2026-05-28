/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.storageunit;

import com.labvantage.opal.elements.BasePropertyHandler;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class StorageUnitRendererPropertyHandler
extends BasePropertyHandler {
    @Override
    public void processProperties(HashMap elementProps) throws SapphireException {
        String elementid = (String)elementProps.get("__propertyhandler_elementid");
        String data = (String)elementProps.get("__" + elementid + "_data");
        if (OpalUtil.isNotEmpty(data)) {
            try {
                DataSet ds = new DataSet();
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String trackitemid = jsonObject.getString("trackitemid");
                    String targetid = jsonObject.getString("targetid");
                    if (!OpalUtil.isNotEmpty(trackitemid) || !OpalUtil.isNotEmpty(targetid)) continue;
                    int row = ds.addRow();
                    ds.setString(row, "trackitemid", trackitemid);
                    ds.setString(row, "currentstorageunitid", targetid);
                }
                if (ds.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
                    props.setProperty("currentstorageunitid", ds.getColumnValues("currentstorageunitid", ";"));
                    String __pr_extraprops = (String)elementProps.get("__pr_extraprops");
                    if (OpalUtil.isNotEmpty(__pr_extraprops)) {
                        String[] extraprops;
                        for (String text : extraprops = StringUtil.split(__pr_extraprops, ";")) {
                            String[] s;
                            if (!text.contains("=") || (s = StringUtil.split(text, "=")).length != 2) continue;
                            String key = s[0];
                            String value = s[1];
                            if (!OpalUtil.isNotEmpty(key) || !OpalUtil.isNotEmpty(value)) continue;
                            props.setProperty(key, value);
                        }
                    }
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
            catch (JSONException e) {
                throw new SapphireException("VALIDATION", "StorageUnitRendererPropertyHandler", this.getTranslationProcessor().translate("Unable to persist data.") + "<hr>" + e.getMessage() + "<hr>" + this.getTranslationProcessor().translate("If problem persists, please contact your Administrator."));
            }
        }
    }
}

