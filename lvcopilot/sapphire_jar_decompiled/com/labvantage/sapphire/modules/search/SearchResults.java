/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.util.regex.RegexUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import sapphire.util.StringUtil;

public class SearchResults {
    private SearchRequest searchRequest;
    private LinkedList<SearchDocument> searchResults;
    private int totalHits;
    private ArrayList<String> queryparts;
    private long searchTime;
    private long processingTime;
    private String queryClass;
    private String query;

    public SearchResults(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
        this.queryparts = SearchResults.createQueryParts(searchRequest.getEnteredQuery());
    }

    public static ArrayList<String> createQueryParts(String enteredQuery) {
        ArrayList<String> queryParts = new ArrayList<String>();
        String[] enteredParts = StringUtil.split(enteredQuery, " ");
        for (int i = 0; i < enteredParts.length; ++i) {
            String querypart = enteredParts[i].toLowerCase();
            if (querypart.contains(":")) {
                querypart = querypart.substring(querypart.indexOf(":") + 1);
            }
            if (querypart.length() <= 0 || querypart.equals("and") || querypart.equals("or") || querypart.equals("not") || querypart.equals("to")) continue;
            queryParts.add(StringUtil.replaceAll(querypart, "__", "-"));
        }
        return queryParts;
    }

    public void setSearchDocuments(LinkedList<SearchDocument> searchResults) {
        this.searchResults = searchResults;
    }

    public void setStats(int totalHits, long searchTime, long processingTime) {
        this.totalHits = totalHits;
        this.searchTime = searchTime;
        this.processingTime = processingTime;
    }

    public int getTotalHits() {
        return this.totalHits;
    }

    public List<SearchDocument> getSearchDocuments() {
        return this.searchResults;
    }

    public long getSearchTime() {
        return this.searchTime;
    }

    public long getProcessingTime() {
        return this.processingTime;
    }

    public String highlight(String value) {
        return this.highlight(value, -1);
    }

    public String highlight(String value, int maxlen) {
        return SearchResults.highlight(value, maxlen, this.queryparts, "<b><i>", "</i></b>", true, false);
    }

    public static String highlight(String value, String query, String start, String end) {
        return SearchResults.highlight(value, -1, new ArrayList<String>(Arrays.asList(query)), start, end, false, true);
    }

    public static String highlight(String value, ArrayList<String> queryparts, String start, String end) {
        return SearchResults.highlight(value, -1, queryparts, start, end, true, false);
    }

    private static String highlight(String value, int maxlen, ArrayList<String> queryparts, String start, String end, boolean splitValue, boolean ignoreSpaces) {
        String[] stringArray;
        StringBuffer output = new StringBuffer();
        if (splitValue) {
            stringArray = StringUtil.split(value, " ");
        } else {
            String[] stringArray2 = new String[1];
            stringArray = stringArray2;
            stringArray2[0] = value;
        }
        String[] valueparts = stringArray;
        for (int i = 0; i < valueparts.length; ++i) {
            boolean match = false;
            String querypart = "";
            for (int j = 0; j < queryparts.size(); ++j) {
                if (!RegexUtil.wildcardMatch(ignoreSpaces ? valueparts[i].toLowerCase() : valueparts[i].toLowerCase().replaceAll("\\s", ""), "*" + queryparts.get(j).toLowerCase() + "*")) continue;
                match = true;
                querypart = queryparts.get(j);
            }
            String valuepart = valueparts[i];
            if (match) {
                int pos = valueparts[i].toLowerCase().indexOf(querypart.toLowerCase());
                valuepart = pos >= 0 ? (pos > 0 ? valueparts[i].substring(0, pos) : "") + start + valueparts[i].substring(pos, pos + querypart.length()) + end + (valueparts[i].length() > pos + querypart.length() ? valueparts[i].substring(pos + querypart.length()) : "") : start + valueparts[i] + end;
                if (maxlen > 0) {
                    maxlen += start.length() + end.length();
                }
            }
            output.append(" ").append(valuepart);
        }
        if (maxlen > 0 && output.length() > maxlen) {
            int pos = output.indexOf(start);
            if (pos > -1) {
                int lastpos = output.lastIndexOf(end);
                int range = lastpos - pos;
                if (range <= maxlen) {
                    int extra;
                    if (lastpos + extra + (pos - (extra = (maxlen - range) / 2) > 0 ? 0 : extra - pos) < output.length()) {
                        output.delete(lastpos + extra + (pos - extra > 0 ? 0 : extra - pos), output.length());
                    }
                    if (pos - extra > 0) {
                        output.delete(0, pos - extra);
                    }
                } else {
                    int nextpos = output.indexOf(end, pos);
                    range = nextpos - pos;
                    if (range <= maxlen) {
                        int extra;
                        if (nextpos + extra + (pos - (extra = (maxlen - range) / 2) > 0 ? 0 : extra - pos) < output.length()) {
                            output.delete(nextpos + extra + (pos - extra > 0 ? 0 : extra - pos), output.length());
                        }
                        if (pos - extra > 0) {
                            output.delete(0, pos - extra);
                        }
                    } else {
                        output.delete(maxlen, output.length());
                    }
                }
            } else {
                output.delete(maxlen, output.length());
            }
        }
        return output.substring(1);
    }

    public String highlightMatchLines(String content, String lineSeparator, int maxMatchesShown) {
        StringBuffer output = new StringBuffer();
        String[] lines = StringUtil.split(content, lineSeparator);
        String previousLine = "";
        int hits = 0;
        for (int i = 0; i < lines.length; ++i) {
            if (hits >= maxMatchesShown) {
                output.append(lineSeparator).append("....").append(lineSeparator).append(".... .....");
                break;
            }
            String processedline = this.highlight(lines[i]);
            if (processedline.contains("<b><i>")) {
                ++hits;
                if (i != 0 && !previousLine.contains("<b><i>")) {
                    output.append(lineSeparator).append("....").append(lineSeparator).append(previousLine);
                }
                output.append(lineSeparator).append(processedline).append(lineSeparator).append(i < lines.length - 1 ? lines[i + 1] + lineSeparator + "...." : "");
            }
            previousLine = lines[i];
        }
        return output.toString();
    }

    public String getQueryClass() {
        return this.queryClass;
    }

    public void setQueryClass(String queryClass) {
        this.queryClass = queryClass;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}

