/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.definition;

import com.labvantage.sapphire.pageelements.workflow.bpmn.definition.ProcessElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="group", namespace="http://www.omg.org/spec/BPMN/20100524/MODEL")
public class Group
extends ProcessElement {
    @XmlAttribute
    private String categoryValueRef = "";

    public String getCategoryValueRef() {
        return this.categoryValueRef;
    }

    public void setCategoryValueRef(String categoryValueRef) {
        this.categoryValueRef = categoryValueRef;
    }
}

