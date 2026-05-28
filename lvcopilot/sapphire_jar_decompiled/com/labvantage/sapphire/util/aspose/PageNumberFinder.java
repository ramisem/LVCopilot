/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentVisitor
 *  com.aspose.words.LayoutCollector
 *  com.aspose.words.Node
 */
package com.labvantage.sapphire.util.aspose;

import com.aspose.words.Document;
import com.aspose.words.DocumentVisitor;
import com.aspose.words.LayoutCollector;
import com.aspose.words.Node;
import com.labvantage.sapphire.util.aspose.SectionSplitter;
import java.util.ArrayList;
import java.util.Hashtable;

public class PageNumberFinder {
    private Hashtable mNodeStartPageLookup = new Hashtable();
    private Hashtable mNodeEndPageLookup = new Hashtable();
    private Hashtable mReversePageLookup;
    private LayoutCollector mCollector;

    public PageNumberFinder(LayoutCollector collector) {
        this.mCollector = collector;
    }

    public int GetPage(Node node) throws Exception {
        if (this.mNodeStartPageLookup.containsKey(node)) {
            return (Integer)this.mNodeStartPageLookup.get(node);
        }
        return this.mCollector.getStartPageIndex(node);
    }

    public int GetPageEnd(Node node) throws Exception {
        if (this.mNodeEndPageLookup.containsKey(node)) {
            return (Integer)this.mNodeEndPageLookup.get(node);
        }
        return this.mCollector.getEndPageIndex(node);
    }

    public int PageSpan(Node node) throws Exception {
        return this.GetPageEnd(node) - this.GetPage(node) + 1;
    }

    public ArrayList RetrieveAllNodesOnPages(int startPage, int endPage, int nodeType) throws Exception {
        if (startPage < 1 || startPage > this.getDocument().getPageCount()) {
            throw new Exception("startPage");
        }
        if (endPage < 1 || endPage > this.getDocument().getPageCount() || endPage < startPage) {
            throw new Exception("endPage");
        }
        this.CheckPageListsPopulated();
        ArrayList<Node> pageNodes = new ArrayList<Node>();
        for (int page = startPage; page <= endPage; ++page) {
            if (!this.mReversePageLookup.containsKey(page)) continue;
            for (Node node : (ArrayList)this.mReversePageLookup.get(page)) {
                if (node.getParentNode() == null || nodeType != 0 && nodeType != node.getNodeType() || pageNodes.contains(node)) continue;
                pageNodes.add(node);
            }
        }
        return pageNodes;
    }

    public void SplitNodesAcrossPages() throws Exception {
        this.getDocument().accept((DocumentVisitor)new SectionSplitter(this));
    }

    public Document getDocument() {
        return this.mCollector.getDocument();
    }

    void AddPageNumbersForNode(Node node, int startPage, int endPage) {
        if (startPage > 0) {
            this.mNodeStartPageLookup.put(node, startPage);
        }
        if (endPage > 0) {
            this.mNodeEndPageLookup.put(node, endPage);
        }
    }

    private void CheckPageListsPopulated() throws Exception {
        if (this.mReversePageLookup != null) {
            return;
        }
        this.mReversePageLookup = new Hashtable();
        for (Node node : this.getDocument().getChildNodes(0, true)) {
            if (PageNumberFinder.IsHeaderFooterType(node)) continue;
            int startPage = this.GetPage(node);
            int endPage = this.GetPageEnd(node);
            for (int page = startPage; page <= endPage; ++page) {
                if (!this.mReversePageLookup.containsKey(page)) {
                    this.mReversePageLookup.put(page, new ArrayList());
                }
                ((ArrayList)this.mReversePageLookup.get(page)).add(node);
            }
        }
    }

    private static boolean IsHeaderFooterType(Node node) {
        return node.getNodeType() == 4 || node.getAncestor(4) != null;
    }
}

