/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Node;
import java.util.ArrayList;
import java.util.Iterator;
import sapphire.util.StringUtil;

public class NodeList
extends ArrayList<Node>
implements Cloneable {
    private Node parentNode;

    public Node getParentNode() {
        return this.parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public String toXMLString(int level) {
        StringBuffer out = new StringBuffer(StringUtil.repeat("\t", level) + "<nodelist>\n");
        for (int i = 0; i < this.size(); ++i) {
            out.append(((Node)this.get(i)).toXMLString(level + 1));
        }
        return out.toString() + StringUtil.repeat("\t", level) + "</nodelist>\n";
    }

    public void getAllNodes(ArrayList list) {
        for (int i = 0; i < this.size(); ++i) {
            Node node = (Node)this.get(i);
            list.add(node);
            NodeList nodeList = node.getNodeList();
            if (nodeList == null) continue;
            nodeList.getAllNodes(list);
        }
    }

    public Node findNode(String nodeid) {
        return this.findNode(nodeid, true);
    }

    public Node findNode(String nodeid, boolean caseSensitive) {
        for (Node node : this) {
            Node foundNode;
            if (caseSensitive ? node.getId().equals(nodeid) : node.getId().equalsIgnoreCase(nodeid)) {
                return node;
            }
            NodeList nodeList = node.getNodeList();
            if (nodeList == null || (foundNode = nodeList.findNode(nodeid, caseSensitive)) == null) continue;
            return foundNode;
        }
        return null;
    }

    public Node deleteNode(String nodeid) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Node node = (Node)iterator.next();
            if (node.getId().equals(nodeid)) {
                iterator.remove();
                continue;
            }
            NodeList nodeList = node.getNodeList();
            if (nodeList == null) continue;
            nodeList.deleteNode(nodeid);
        }
        return null;
    }

    public void mergeNodes(NodeList newList) {
        if (newList != null) {
            for (Node newnode : newList) {
                Node currentNode = this.findNode(newnode.getId());
                if (currentNode != null) continue;
                this.add(newnode);
            }
        }
    }
}

