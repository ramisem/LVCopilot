/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetFieldValue
extends BaseAction
implements DocumentConstants {
    public static final String ID = "SetFieldValue";
    public static final String VERSION = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        StringBuffer modtoolList;
        StringBuffer moddtList;
        StringBuffer modbyList;
        String documentid = properties.getProperty("documentid");
        String documentversionid = properties.getProperty("documentversionid");
        String[] fieldid = StringUtil.split(properties.getProperty("fieldid"), "$SEP$");
        String[] fieldinstance = StringUtil.split(properties.getProperty("fieldinstance"), "$SEP$");
        String[] value = StringUtil.split(properties.getProperty("value"), "$SEP$");
        if (fieldid.length != fieldinstance.length && fieldid.length != value.length) {
            throw new SapphireException("Fieldid, instance and value property lists do not match!");
        }
        this.database.createPreparedResultSet("SELECT * FROM document WHERE documentid = ? AND documentversionid = ?", new Object[]{documentid, documentversionid});
        if (this.database.getNext()) {
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            Form form = Form.getInstance(sapphireConnection, this.database.getValue("formid"), this.database.getValue("formversionid"));
            modbyList = new StringBuffer();
            moddtList = new StringBuffer();
            modtoolList = new StringBuffer();
            String now = DateFormatter.formatDateTime(DateTimeUtil.getNowCalendar(), "[shortdate] [time]");
            for (int i = 0; i < fieldid.length; ++i) {
                PropertyList field = form.getField(fieldid[i]);
                if (field.getProperty("identityfield", "N").equals("Y")) {
                    throw new SapphireException("Field '" + fieldid[i] + "' cannot be updated because it is an identity field");
                }
                if (field.getProperty("repeatable", "N").equals("Y")) {
                    throw new SapphireException("Field '" + fieldid[i] + "' cannot be updated because it is in a repeating section");
                }
                if (field.getCollection("precedents") != null && field.getCollection("precedents").size() > 0) {
                    throw new SapphireException("Field '" + fieldid[i] + "' cannot be updated because it has precedents!");
                }
                if (field.getCollection("dependents") != null && field.getCollection("dependents").size() > 0) {
                    throw new SapphireException("Field '" + fieldid[i] + "' cannot be updated because it has dependents!");
                }
                modbyList.append("$SEP$").append(sapphireConnection.getSysuserId());
                moddtList.append("$SEP$").append(now);
                modtoolList.append("$SEP$").append(ID);
            }
        } else {
            throw new SapphireException("Document " + documentid + " not found!");
        }
        HashMap<String, String> fieldEditProps = new HashMap<String, String>();
        fieldEditProps.put("linkid", "documentfield_link");
        fieldEditProps.put("sdcid", "LV_Document");
        fieldEditProps.put("keyid1", documentid);
        fieldEditProps.put("keyid2", documentversionid);
        fieldEditProps.put("separator", "$SEP$");
        fieldEditProps.put("fieldid", properties.getProperty("fieldid"));
        fieldEditProps.put("fieldinstance", properties.getProperty("fieldinstance"));
        fieldEditProps.put("modby", modbyList.substring("$SEP$".length()));
        fieldEditProps.put("moddt", moddtList.substring("$SEP$".length()));
        fieldEditProps.put("modtool", modtoolList.substring("$SEP$".length()));
        fieldEditProps.put("enteredtext", properties.getProperty("value"));
        fieldEditProps.put("numericvalue", properties.getProperty("value"));
        fieldEditProps.put("datevalue", properties.getProperty("value"));
        this.getActionProcessor().processAction("EditSDIDetail", VERSION, fieldEditProps);
    }
}

