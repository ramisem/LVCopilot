/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 */
package com.labvantage.sapphire.modules.search;

import com.labvantage.sapphire.modules.search.IndexerListener;
import java.io.IOException;
import javax.servlet.jsp.JspWriter;

public class IndexerOperationProgress
implements IndexerListener {
    private JspWriter out;
    private long jspScrollCounter = 0L;
    private int nextpercent = 1;
    private double size = 0.0;
    private double count = 0.0;
    private int elementcount = 0;
    private String elementid;

    public IndexerOperationProgress(JspWriter out) {
        this.out = out;
    }

    public void startProgress(String message, double size) {
        this.initProgress(message);
        this.size = size;
        this.count = 0.0;
    }

    private void initProgress(String message) {
        this.elementid = "_p_" + this.elementcount++;
        this.nextpercent = 10;
        try {
            if (message.length() > 0) {
                this.out.println(message + "<br id=\"" + this.elementid + "__msg\"/><script>document.getElementById( \"" + this.elementid + "__msg\" ).scrollIntoView( true )</script>");
            }
            this.out.println("<input id=\"" + this.elementid + "_indexed\" style=\"width:300px;border:none\"/><br/>");
            this.out.println("<input id=\"" + this.elementid + "\" style=\"width:1px;background:#C1D1E0;border:none\"/>");
            this.out.flush();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void endProgress(String message) {
        try {
            this.out.println("<script>document.getElementById('" + this.elementid + "').style.display='none';</script>");
            this.out.println("<script>document.getElementById('" + this.elementid + "_indexed').value='" + message + "';</script>");
            this.out.flush();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    @Override
    public void indexedSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        try {
            this.out.println("<script>document.getElementById('" + this.elementid + "_indexed').value='- indexed " + keyid1 + " " + keyid2 + " " + keyid3 + "';</script>");
            this.count += 1.0;
            int percent = (int)(this.count / this.size * 100.0);
            this.updateProgress(percent);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void updateProgress(int percent) {
        try {
            if (percent > 100) {
                percent = 100;
            }
            if (percent >= this.nextpercent) {
                ++this.nextpercent;
                this.out.println("<script>document.getElementById('" + this.elementid + "').value='" + String.valueOf(percent) + "%';document.getElementById('" + this.elementid + "').style.width=" + String.valueOf((double)percent / 100.0 * 400.0) + ";</script>");
                this.out.flush();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void jspLiveLog(String message) {
        try {
            if (this.out != null) {
                this.out.println(message + "<br id=\"" + this.jspScrollCounter + "\"/><script>document.getElementById( \"" + this.jspScrollCounter + "\" ).scrollIntoView( true )</script>");
                this.out.flush();
                ++this.jspScrollCounter;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

