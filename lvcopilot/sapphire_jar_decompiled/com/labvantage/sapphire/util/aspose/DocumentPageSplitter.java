/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.Document
 *  com.aspose.words.LayoutCollector
 *  com.aspose.words.Node
 *  com.aspose.words.Section
 */
package com.labvantage.sapphire.util.aspose;

import com.aspose.words.Document;
import com.aspose.words.LayoutCollector;
import com.aspose.words.Node;
import com.aspose.words.Section;
import com.labvantage.sapphire.util.aspose.PageNumberFinder;

public class DocumentPageSplitter {
    private PageNumberFinder mPageNumberFinder;

    public DocumentPageSplitter(LayoutCollector collector) throws Exception {
        this.mPageNumberFinder = new PageNumberFinder(collector);
        this.mPageNumberFinder.SplitNodesAcrossPages();
    }

    public Document getDocumentOfPage(int pageIndex) throws Exception {
        return this.getDocumentOfPageRange(pageIndex, pageIndex);
    }

    public Document getDocumentOfPageRange(int startIndex, int endIndex) throws Exception {
        Document result = (Document)this.getDocument().deepClone(false);
        for (Section section : this.mPageNumberFinder.RetrieveAllNodesOnPages(startIndex, endIndex, 2)) {
            result.appendChild(result.importNode((Node)section, true));
        }
        return result;
    }

    private Document getDocument() {
        return this.mPageNumberFinder.getDocument();
    }
}

