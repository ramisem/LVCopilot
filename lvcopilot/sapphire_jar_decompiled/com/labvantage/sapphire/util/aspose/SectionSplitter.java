/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.Cell
 *  com.aspose.words.CompositeNode
 *  com.aspose.words.ControlChar
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentVisitor
 *  com.aspose.words.Field
 *  com.aspose.words.FieldSeparator
 *  com.aspose.words.FieldStart
 *  com.aspose.words.HeaderFooter
 *  com.aspose.words.List
 *  com.aspose.words.ListLevel
 *  com.aspose.words.Node
 *  com.aspose.words.Paragraph
 *  com.aspose.words.Row
 *  com.aspose.words.Run
 *  com.aspose.words.Section
 *  com.aspose.words.SmartTag
 *  com.aspose.words.StructuredDocumentTag
 *  com.aspose.words.Table
 */
package com.labvantage.sapphire.util.aspose;

import com.aspose.words.Cell;
import com.aspose.words.CompositeNode;
import com.aspose.words.ControlChar;
import com.aspose.words.Document;
import com.aspose.words.DocumentVisitor;
import com.aspose.words.Field;
import com.aspose.words.FieldSeparator;
import com.aspose.words.FieldStart;
import com.aspose.words.HeaderFooter;
import com.aspose.words.List;
import com.aspose.words.ListLevel;
import com.aspose.words.Node;
import com.aspose.words.Paragraph;
import com.aspose.words.Row;
import com.aspose.words.Run;
import com.aspose.words.Section;
import com.aspose.words.SmartTag;
import com.aspose.words.StructuredDocumentTag;
import com.aspose.words.Table;
import com.labvantage.sapphire.util.aspose.PageNumberFinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class SectionSplitter
extends DocumentVisitor {
    private Hashtable mListLevelToListNumberLookup = new Hashtable();
    private Hashtable mListToReplacementListLookup = new Hashtable();
    private Hashtable mListLevelToPageLookup = new Hashtable();
    private PageNumberFinder mPageNumberFinder;
    private int mSectionCount;

    public SectionSplitter(PageNumberFinder pageNumberFinder) {
        this.mPageNumberFinder = pageNumberFinder;
    }

    public int visitParagraphStart(Paragraph paragraph) throws Exception {
        Paragraph prevParagraph;
        if (paragraph.isListItem()) {
            List paraList = paragraph.getListFormat().getList();
            ListLevel currentLevel = paragraph.getListFormat().getListLevel();
            int currentListLevelNumber = paragraph.getListFormat().getListLevelNumber();
            for (int i = currentListLevelNumber + 1; i < paraList.getListLevels().getCount(); ++i) {
                ListLevel paraLevel = paraList.getListLevels().get(i);
                if (paraLevel.getRestartAfterLevel() < currentListLevelNumber) continue;
                this.mListLevelToListNumberLookup.put(paraLevel, paraLevel.getStartAt());
            }
            if (this.ContainsListLevelAndPageChanged(paragraph)) {
                List copyList = paragraph.getDocument().getLists().addCopy(paraList);
                this.mListLevelToListNumberLookup.put(currentLevel, paragraph.getListLabel().getLabelValue());
                for (int i = 0; i < paraList.getListLevels().getCount(); ++i) {
                    ListLevel paraLevel = paraList.getListLevels().get(i);
                    if (!this.mListLevelToListNumberLookup.containsKey(paraLevel)) continue;
                    copyList.getListLevels().get(i).setStartAt(((Integer)this.mListLevelToListNumberLookup.get(paraLevel)).intValue());
                }
                this.mListToReplacementListLookup.put(paraList, copyList);
            }
            if (this.mListToReplacementListLookup.containsKey(paraList)) {
                paragraph.getListFormat().setList((List)this.mListToReplacementListLookup.get(paraList));
                paragraph.getListFormat().setListLevelNumber(paragraph.getListFormat().getListLevelNumber() + 0);
            }
            this.mListLevelToPageLookup.put(currentLevel, this.mPageNumberFinder.GetPage((Node)paragraph));
            this.mListLevelToListNumberLookup.put(currentLevel, paragraph.getListLabel().getLabelValue());
        }
        Section prevSection = (Section)paragraph.getParentSection().getPreviousSibling();
        Paragraph prevBodyPara = null;
        if (paragraph.getPreviousSibling() != null && paragraph.getPreviousSibling().getNodeType() == 8) {
            prevBodyPara = (Paragraph)paragraph.getPreviousSibling();
        }
        Paragraph prevSectionPara = prevSection != null && paragraph == paragraph.getParentSection().getBody().getFirstChild() ? prevSection.getBody().getLastParagraph() : null;
        Paragraph paragraph2 = prevParagraph = prevBodyPara != null ? prevBodyPara : prevSectionPara;
        if (paragraph.isEndOfSection() && !paragraph.hasChildNodes()) {
            paragraph.remove();
        }
        if (prevParagraph != null && this.mPageNumberFinder.GetPage((Node)paragraph) != this.mPageNumberFinder.GetPageEnd((Node)prevParagraph)) {
            if (paragraph.isListItem() && prevParagraph.isListItem() && !prevParagraph.isEndOfSection()) {
                prevParagraph.getParagraphFormat().setSpaceAfter(0.0);
            } else if (prevParagraph.getParagraphFormat().getStyleName() == paragraph.getParagraphFormat().getStyleName() && paragraph.getParagraphFormat().getNoSpaceBetweenParagraphsOfSameStyle()) {
                paragraph.getParagraphFormat().setSpaceBefore(0.0);
            } else if (paragraph.getParagraphFormat().getPageBreakBefore() || prevParagraph.isEndOfSection() && prevSection.getPageSetup().getSectionStart() != 1) {
                paragraph.getParagraphFormat().setSpaceBefore(Math.max(paragraph.getParagraphFormat().getSpaceBefore() - prevParagraph.getParagraphFormat().getSpaceAfter(), 0.0));
            } else {
                paragraph.getParagraphFormat().setSpaceBefore(0.0);
            }
        }
        return 0;
    }

    public int visitSectionStart(Section section) throws Exception {
        ++this.mSectionCount;
        Section previousSection = (Section)section.getPreviousSibling();
        if (previousSection != null) {
            if (!section.getPageSetup().getRestartPageNumbering()) {
                section.getPageSetup().setRestartPageNumbering(true);
                section.getPageSetup().setPageStartingNumber(previousSection.getPageSetup().getPageStartingNumber() + this.mPageNumberFinder.PageSpan((Node)previousSection));
            }
            for (HeaderFooter previousHeaderFooter : previousSection.getHeadersFooters()) {
                if (section.getHeadersFooters().getByHeaderFooterType(previousHeaderFooter.getHeaderFooterType()) != null) continue;
                HeaderFooter newHeaderFooter = (HeaderFooter)previousSection.getHeadersFooters().getByHeaderFooterType(previousHeaderFooter.getHeaderFooterType()).deepClone(true);
                section.getHeadersFooters().add((Node)newHeaderFooter);
            }
        }
        for (HeaderFooter headerFooter : section.getHeadersFooters()) {
            for (Field field : headerFooter.getRange().getFields()) {
                if (field.getType() != 65 && field.getType() != 66) continue;
                field.setResult(field.getType() == 65 ? Integer.toString(this.mSectionCount) : Integer.toString(this.mPageNumberFinder.PageSpan((Node)section)));
                field.isLocked(true);
            }
        }
        for (Field field : section.getBody().getRange().getFields()) {
            field.isLocked(true);
        }
        return 0;
    }

    public int visitDocumentEnd(Document doc) throws Exception {
        doc.updateFields();
        for (HeaderFooter headerFooter : doc.getChildNodes(4, true)) {
            for (Field field : headerFooter.getRange().getFields()) {
                field.isLocked(true);
            }
        }
        return 0;
    }

    public int visitSmartTagEnd(SmartTag smartTag) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)smartTag)) {
            this.SplitComposite((CompositeNode)smartTag);
        }
        return 0;
    }

    public int visitStructuredDocumentTagEnd(StructuredDocumentTag sdt) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)sdt)) {
            this.SplitComposite((CompositeNode)sdt);
        }
        return 0;
    }

    public int visitCellEnd(Cell cell) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)cell)) {
            this.SplitComposite((CompositeNode)cell);
        }
        return 0;
    }

    public int visitRowEnd(Row row) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)row)) {
            this.SplitComposite((CompositeNode)row);
        }
        return 0;
    }

    public int visitTableEnd(Table table) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)table)) {
            Row[] rows = table.getRows().toArray();
            for (Table cloneTable : this.SplitComposite((CompositeNode)table)) {
                for (Row row : rows) {
                    if (!row.getRowFormat().getHeadingFormat()) continue;
                    cloneTable.prependChild(row.deepClone(true));
                }
            }
        }
        return 0;
    }

    public int visitParagraphEnd(Paragraph paragraph) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)paragraph)) {
            for (Paragraph clonePara : this.SplitComposite((CompositeNode)paragraph)) {
                if (paragraph.isListItem()) {
                    double textPosition = clonePara.getListFormat().getListLevel().getTextPosition();
                    clonePara.getListFormat().removeNumbers();
                    clonePara.getParagraphFormat().setLeftIndent(textPosition);
                }
                clonePara.getParagraphFormat().setSpaceBefore(0.0);
                paragraph.getParagraphFormat().setSpaceAfter(0.0);
            }
        }
        return 0;
    }

    public int visitSectionEnd(Section section) throws Exception {
        if (this.IsCompositeAcrossPage((CompositeNode)section)) {
            for (FieldStart start : section.getChildNodes(22, true)) {
                if (start.getFieldType() != 13) continue;
                Field field = start.getField();
                FieldSeparator node = field.getSeparator();
                while ((node = node.nextPreOrder((Node)section)) != field.getEnd()) {
                    if (node.getNodeType() != 21) continue;
                    ((Run)node).getFont().clearFormatting();
                }
            }
            for (Section cloneSection : this.SplitComposite((CompositeNode)section)) {
                cloneSection.getPageSetup().setSectionStart(2);
                cloneSection.getPageSetup().setRestartPageNumbering(true);
                cloneSection.getPageSetup().setPageStartingNumber(section.getPageSetup().getPageStartingNumber() + (section.getDocument().indexOf((Node)cloneSection) - section.getDocument().indexOf((Node)section)));
                cloneSection.getPageSetup().setDifferentFirstPageHeaderFooter(false);
                this.RemovePageBreaksFromParagraph(cloneSection.getBody().getLastParagraph());
            }
            this.RemovePageBreaksFromParagraph(section.getBody().getLastParagraph());
            this.mPageNumberFinder.AddPageNumbersForNode((Node)section.getBody(), this.mPageNumberFinder.GetPage((Node)section), this.mPageNumberFinder.GetPageEnd((Node)section));
        }
        return 0;
    }

    private boolean IsCompositeAcrossPage(CompositeNode composite) throws Exception {
        return this.mPageNumberFinder.PageSpan((Node)composite) > 1;
    }

    private boolean ContainsListLevelAndPageChanged(Paragraph para) throws Exception {
        return this.mListLevelToPageLookup.containsKey(para.getListFormat().getListLevel()) && ((Integer)this.mListLevelToPageLookup.get(para.getListFormat().getListLevel())).intValue() != this.mPageNumberFinder.GetPage((Node)para);
    }

    private void RemovePageBreaksFromParagraph(Paragraph para) {
        if (para != null) {
            for (Run run : para.getRuns()) {
                try {
                    run.setText(run.getText().replace(ControlChar.PAGE_BREAK, ""));
                }
                catch (Exception exception) {}
            }
        }
    }

    private ArrayList SplitComposite(CompositeNode composite) throws Exception {
        ArrayList<CompositeNode> splitNodes = new ArrayList<CompositeNode>();
        for (Node splitNode : this.FindChildSplitPositions(composite)) {
            splitNodes.add(this.SplitCompositeAtNode(composite, splitNode));
        }
        return splitNodes;
    }

    private ArrayList FindChildSplitPositions(CompositeNode node) throws Exception {
        Node[] childNodes;
        ArrayList<Node> splitList = new ArrayList<Node>();
        int startingPage = this.mPageNumberFinder.GetPage((Node)node);
        for (Node childNode : childNodes = node.getNodeType() == 2 ? ((Section)node).getBody().getChildNodes().toArray() : node.getChildNodes().toArray()) {
            int pageNum = this.mPageNumberFinder.GetPage(childNode);
            if (pageNum > startingPage) {
                splitList.add(childNode);
                startingPage = pageNum;
            }
            if (this.mPageNumberFinder.PageSpan(childNode) <= 1) continue;
            this.mPageNumberFinder.AddPageNumbersForNode(childNode, pageNum, pageNum);
        }
        Collections.reverse(splitList);
        return splitList;
    }

    private CompositeNode SplitCompositeAtNode(CompositeNode baseNode, Node targetNode) throws Exception {
        CompositeNode cloneNode = (CompositeNode)baseNode.deepClone(false);
        Node node = targetNode;
        int currentPageNum = this.mPageNumberFinder.GetPage((Node)baseNode);
        if (baseNode.getNodeType() != 6) {
            CompositeNode composite = cloneNode;
            if (baseNode.getNodeType() == 2) {
                cloneNode = (CompositeNode)baseNode.deepClone(true);
                Section section = (Section)cloneNode;
                section.getBody().removeAllChildren();
                composite = section.getBody();
            }
            while (node != null) {
                Node nextNode = node.getNextSibling();
                composite.appendChild(node);
                node = nextNode;
            }
        } else {
            Node[] childNodes;
            int targetPageNum = this.mPageNumberFinder.GetPage(targetNode);
            for (Node childNode : childNodes = baseNode.getChildNodes().toArray()) {
                int pageNum = this.mPageNumberFinder.GetPage(childNode);
                if (pageNum == targetPageNum) {
                    cloneNode.getLastChild().remove();
                    cloneNode.appendChild(childNode);
                    continue;
                }
                if (pageNum != currentPageNum) continue;
                cloneNode.appendChild(childNode.deepClone(false));
                if (cloneNode.getLastChild().getNodeType() == 7) continue;
                ((CompositeNode)cloneNode.getLastChild()).appendChild(((CompositeNode)childNode).getFirstChild().deepClone(false));
            }
        }
        baseNode.getParentNode().insertAfter((Node)cloneNode, (Node)baseNode);
        int currentEndPageNum = this.mPageNumberFinder.GetPageEnd((Node)baseNode);
        this.mPageNumberFinder.AddPageNumbersForNode((Node)baseNode, currentPageNum, currentEndPageNum - 1);
        this.mPageNumberFinder.AddPageNumbersForNode((Node)cloneNode, currentEndPageNum, currentEndPageNum);
        for (Node childNode : cloneNode.getChildNodes(0, true)) {
            this.mPageNumberFinder.AddPageNumbersForNode(childNode, currentEndPageNum, currentEndPageNum);
        }
        return cloneNode;
    }
}

