/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.xml.PropertyList;

public class FieldSetter {
    public static void setValue(Field field, PropertyList instance) {
        field.setValue(instance);
    }

    public static void setConnectionid(Field field, String connectionid) {
        field.setConnectionid(connectionid);
    }

    public static void setDocument(Field field, String documentid, String documentversionid) {
        field.setDocumentid(documentid, documentversionid);
    }

    public static void deleteTempFile(Field field) {
        field.deleteTempFile();
    }

    public static void defineFieldMap(FormValue formValue, ConnectionInfo connectionInfo) {
        formValue.defineFieldMap(connectionInfo);
    }
}

