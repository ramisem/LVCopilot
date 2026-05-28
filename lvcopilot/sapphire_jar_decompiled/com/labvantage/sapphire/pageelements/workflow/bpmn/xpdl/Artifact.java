/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.bind.annotation.XmlAccessType
 *  javax.xml.bind.annotation.XmlAccessorType
 *  javax.xml.bind.annotation.XmlAttribute
 *  javax.xml.bind.annotation.XmlElement
 *  javax.xml.bind.annotation.XmlElementWrapper
 *  javax.xml.bind.annotation.XmlType
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl;

import com.labvantage.sapphire.pageelements.workflow.bpmn.Element;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.Group;
import com.labvantage.sapphire.pageelements.workflow.bpmn.xpdl.NodeGraphicsInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(value=XmlAccessType.FIELD)
@XmlType(name="Artifact", namespace="http://www.wfmc.org/2009/XPDL2.2")
public class Artifact
extends Element {
    @XmlAttribute(name="ArtifactType")
    private String artifactType = "";
    @XmlAttribute(name="TextAnnotation")
    private String textAnnotation = "";
    @XmlElement(name="Group", type=Group.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    private Group group = new Group();
    @XmlElement(name="NodeGraphicsInfo", type=NodeGraphicsInfo.class, namespace="http://www.wfmc.org/2009/XPDL2.2")
    @XmlElementWrapper(name="NodeGraphicsInfos", namespace="http://www.wfmc.org/2009/XPDL2.2")
    private List<NodeGraphicsInfo> nodeGraphicsInfos = new ArrayList<NodeGraphicsInfo>();

    public List<NodeGraphicsInfo> getNodeGraphicsInfos() {
        return this.nodeGraphicsInfos;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setNodeGraphicsInfos(List<NodeGraphicsInfo> nodeGraphicsInfos) {
        this.nodeGraphicsInfos = nodeGraphicsInfos;
    }

    public String getArtifactType() {
        return this.artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getTextAnnotation() {
        return this.textAnnotation;
    }

    public void setTextAnnotation(String textAnnotation) {
        this.textAnnotation = textAnnotation;
    }
}

