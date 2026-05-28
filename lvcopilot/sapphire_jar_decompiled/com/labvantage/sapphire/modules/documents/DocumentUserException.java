/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import sapphire.xml.PropertyList;

public class DocumentUserException
extends Exception {
    private String status = "DUE_Default";
    private PropertyList data;

    public DocumentUserException(String message) {
        super(message);
    }

    public DocumentUserException(String message, String status) {
        super(message);
        this.status = status;
    }

    public DocumentUserException(String message, String status, PropertyList data) {
        super(message);
        this.status = status;
        this.data = data;
    }

    public String getStatus() {
        return this.status;
    }

    public PropertyList getData() {
        return this.data;
    }
}

