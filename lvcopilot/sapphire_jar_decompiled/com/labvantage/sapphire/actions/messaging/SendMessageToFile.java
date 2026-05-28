/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.messaging;

import com.labvantage.sapphire.Trace;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SendMessageToFile
extends BaseAction
implements sapphire.action.SendMessageToFile {
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String path = propertyList.getProperty("outputpath", "");
        String messageType = propertyList.getProperty("messagetypeid");
        if (path.length() == 0) {
            String sql = "SELECT outputpath FROM sapmsgtype WHERE sapmsgtypeid=?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{messageType});
            if (ds != null && ds.getRowCount() > 0) {
                path = ds.getString(0, "outputpath", "");
            }
        }
        if (path.length() == 0) {
            Trace.log("Output path not specified for messagetype " + messageType);
        }
        String fileNamePattern = propertyList.getProperty("filenameprefix", propertyList.getProperty("messagetypeid"));
        String message = propertyList.getProperty("message");
        if (message.length() == 0) {
            throw new ActionException("PropertyList does not have message: ");
        }
        if (!(path = path.replace('\\', '/')).endsWith("/")) {
            path = path + "/";
        }
        String filename = fileNamePattern + new Date().getTime() + ".xml";
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(path + filename), "UTF8"));
            out.write(message);
            ((Writer)out).flush();
            ((Writer)out).close();
        }
        catch (IOException e) {
            throw new ActionException("Failed to create message file: " + e.getMessage());
        }
    }
}

