/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.pageelements.attachment.AttachmentManager;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ServiceException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseAttachmentType {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    static final String ATTACHMENT_CODE = SDIData.getDatasetCode("attachment");
    public static final int MODE_ADD = 0;
    public static final int MODE_EDIT = 1;

    public abstract String getAllTypeflagList();

    public abstract String getDisplayValue();

    public abstract String getDisplayValue(String var1);

    public abstract String getLabel(String var1);

    public abstract String getHint(String var1, DataSet var2, int var3, String var4, TranslationProcessor var5);

    public abstract String getContentValue(DataSet var1, int var2);

    public abstract void getFilenameFieldInitialRender(AttachmentManager var1, StringBuffer var2, boolean var3, boolean var4, boolean var5, int var6, String var7, String var8, PropertyList var9, String var10, String var11, TranslationProcessor var12, Browser var13);

    public abstract void getFilenameFieldTemplateRow(AttachmentManager var1, StringBuffer var2, PropertyList var3, TranslationProcessor var4, Browser var5);

    public abstract void viewAttachment(HttpServletRequest var1, HttpServletResponse var2, ServletContext var3, Attachment var4) throws IOException;

    public abstract void processGetAttachment(Attachment var1, String var2) throws ServiceException;

    public abstract String addAttachmentNormalRequest(HttpServletRequest var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, HashMap var9);

    public abstract String addAttachmentMultiPart(HttpServletRequest var1, HttpServletResponse var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, HashMap var10, List var11, String var12) throws IOException;

    public abstract String tempAttachmentMultiPart(HttpServletRequest var1, HttpServletResponse var2, String var3, String var4, String var5, String var6, String var7, List var8, JSONObject var9) throws IOException;

    public abstract byte[] getTempAttachment(String var1, String var2);

    public abstract String getOtherHtml();

    public String editAttachment(String attnum, String description, String connectionId, String sdcid, String keyid1, String keyid2, String keyid3, HashMap additionalFields, List fileItems) {
        String error = "";
        if (attnum.length() > 0) {
            HashMap map = additionalFields != null && additionalFields.size() > 0 ? (HashMap)additionalFields.clone() : new HashMap();
            map.put("sdcid", sdcid);
            map.put("keyid1", keyid1);
            if (keyid2.length() > 0) {
                map.put("keyid2", keyid2);
            }
            if (keyid3.length() > 0) {
                map.put("keyid3", keyid3);
            }
            map.put("attachmentnum", attnum);
            map.put("description", description);
            try {
                new ActionProcessor(connectionId).processActionClass("com.labvantage.sapphire.actions.sdi.EditSDIAttachment", map, true);
            }
            catch (ActionException ae) {
                Logger.logError("Could not save attachment with error:" + ae.getMessage());
                error = ae.getMessage();
            }
        } else {
            Logger.logError("No attachment number provided.");
            error = "No attachment number provided.";
        }
        return error;
    }

    public abstract String postEditMultiPart(HttpServletRequest var1, HttpServletResponse var2, List var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10, String var11, HashMap var12, String var13) throws IOException;

    protected int getWidth(PropertyList col, int correction) {
        int width;
        try {
            width = Integer.parseInt(col.getProperty("width", "140"));
        }
        catch (Exception e) {
            width = 140;
        }
        return width + correction;
    }

    public String getCopyDownModes() {
        return "N=Do Not Copy;E=Full Editable Copy;F=Full Non Editable Copy;L=Linked Copy";
    }

    public boolean isAllowedEdit(int row, DataSet attachment) {
        return attachment.getValue(row, "editableflag", "Y").equals("Y");
    }

    public abstract boolean isAllowedDownload(int var1, DataSet var2);

    public abstract String getHideContentDivJavascript();

    public abstract String getShowContentDivJavascript();

    public abstract StringBuffer renderActionColumn(int var1, String var2, String var3, boolean var4, boolean var5, boolean var6, TranslationProcessor var7, Browser var8);

    public StringBuffer renderUploadContainer(String type, PropertyListCollection uploadData, TranslationProcessor tp, String row, boolean viewOnly) {
        return new StringBuffer("&nbsp;");
    }

    public abstract StringBuffer getViewJavaScript();

    public abstract StringBuffer getEditJavaScript();

    public static StringBuffer getShowHistoryScript() {
        StringBuffer content = new StringBuffer("\n");
        content.append("function showAttachmentHistory( sAttNum, rowNum, enablePromote ){\n");
        content.append("var prefix = '" + ATTACHMENT_CODE + "';\n");
        content.append("var sdcid = document.getElementById(prefix + rowNum + '_sdcid').value;\n");
        content.append("var keyid1 = document.getElementById(prefix + rowNum + '_keyid1').value;\n");
        content.append("var keyid2 = document.getElementById(prefix + rowNum + '_keyid2').value;\n");
        content.append("var keyid3 = document.getElementById(prefix + rowNum + '_keyid3').value;\n");
        content.append("var url = 'rc?command=page&page=SDIAttachmentRevisionHistory';\n");
        content.append("sapphire.lookup.util.openWindow('attachmentHistory','Attachment History',url,700,600,false,\n");
        content.append("{'sdcid':sdcid,'keyid1':keyid1,'keyid2':keyid2,'keyid3':keyid3,'attachmentnum':sAttNum,'enablepromote':enablePromote });\n");
        content.append("}\n");
        return content;
    }
}

