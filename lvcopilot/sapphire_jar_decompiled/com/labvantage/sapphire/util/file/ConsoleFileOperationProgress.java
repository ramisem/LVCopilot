/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.JspWriter
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.util.file.FileOperationListener;
import com.labvantage.sapphire.util.file.ZipFileListener;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import javax.servlet.jsp.JspWriter;

public class ConsoleFileOperationProgress
implements ZipFileListener,
FileOperationListener {
    private JspWriter out;
    private int nextpercent = 10;
    private double size = 0.0;
    private double count = 0.0;
    private int elementcount = 0;
    private String elementid;

    public ConsoleFileOperationProgress(JspWriter out) {
        this.out = out;
    }

    public void startProgress() {
        this.initProgress("");
    }

    public void startProgress(String message) {
        this.initProgress(message);
    }

    public void startProgress(String message, File dirToCount) {
        try {
            this.initProgress(message);
            this.size = FileUtil.fileCount(dirToCount);
            this.count = 0.0;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void startProgress(double size) {
        this.initProgress("");
        this.size = size;
        this.count = 0.0;
    }

    private void initProgress(String message) {
        this.elementid = "_p_" + this.elementcount++;
        this.nextpercent = 10;
        try {
            if (this.out != null) {
                if (message.length() > 0) {
                    this.out.println(message + "<br id=\"" + this.elementid + "__msg\"/><script>document.getElementById( \"" + this.elementid + "__msg\" ).scrollIntoView( true )</script>");
                }
                this.out.println("<input id=\"" + this.elementid + "\" style=\"width:1px;background:#C1D1E0;border:none\"/>");
                this.out.flush();
            } else {
                System.out.println(message);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void endProgress() {
        try {
            if (this.out != null) {
                this.out.println("<script>document.getElementById('" + this.elementid + "').style.display='none';</script>");
                this.out.flush();
            } else {
                System.out.println("Completed");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    @Override
    public void fileExtracted(ZipEntry zipEntry, int percent) {
        this.updateProgress(percent);
    }

    @Override
    public void fileAdded(ZipEntry zipEntry) {
        this.fileOperation();
    }

    @Override
    public void fileDeleted(File file) {
        this.fileOperation();
    }

    @Override
    public void fileCopied(File from, File to) {
        this.fileOperation();
    }

    @Override
    public void fileRenamed(File from, File to) {
        this.fileOperation();
    }

    private void fileOperation() {
        this.count += 1.0;
        int percent = (int)(this.count / this.size * 100.0);
        this.updateProgress(percent);
    }

    private void updateProgress(int percent) {
        try {
            if (percent > 100) {
                percent = 100;
            }
            if (percent >= this.nextpercent) {
                this.nextpercent += 10;
                if (this.out != null) {
                    this.out.println("<script>document.getElementById('" + this.elementid + "').value='" + percent + "%';document.getElementById('" + this.elementid + "').style.width=" + (double)percent / 100.0 * 400.0 + ";</script>");
                    this.out.flush();
                } else {
                    System.out.println(percent + "% Done");
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

