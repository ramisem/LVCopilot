/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetValuesForSelection
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 70125 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String parameters = request.getParameter("parameter");
        String[] paramArray = StringUtil.split(parameters, ";");
        this.logger.info("in ajax class GetValuesForSelection");
        String str = "";
        try {
            for (int i = 0; i < paramArray.length; ++i) {
                String query;
                String singleParam = paramArray[i];
                String[] queries = StringUtil.split(singleParam, "^^^");
                String queryParam = queries[0];
                String sql = queries[1];
                this.logger.info("EncryptDecrypt.isObfuscating()::" + EncryptDecrypt.isObfuscating());
                this.logger.info("SQL before obfuscate::" + sql);
                if (EncryptDecrypt.isObfuscating()) {
                    if (!EncryptDecrypt.isObfuscated(sql)) throw new SapphireException("The sql query is not obfuscated.");
                    query = EncryptDecrypt.unobfsql(sql);
                } else {
                    query = sql;
                }
                this.logger.info("SQL after obfuscate::" + query);
                DataSet set = this.getQueryProcessor().getSqlDataSet(query);
                String values = set.getColumnValues(set.getColumnId(0), ";");
                str = str + "%3B" + queryParam + "||" + values;
            }
            str = str.substring(3);
            this.logger.info("str=" + str);
            PrintWriter out = response.getWriter();
            out.print(str.length() > 0 ? str : "");
            out.flush();
            out.close();
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

