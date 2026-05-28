/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlTransient
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlTransient
public class Header
extends Element {
    @XmlElement(name="Created", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String created = "";
    @XmlElement(name="Description", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private String description = "";

    public String getCreated() {
        return this.created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

