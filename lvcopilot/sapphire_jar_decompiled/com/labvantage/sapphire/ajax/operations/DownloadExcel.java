/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.array.ExcelUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;

public class DownloadExcel
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public boolean acceptContentType(String contentType) {
        return contentType == null || contentType.equalsIgnoreCase("application/x-www-form-urlencoded") || contentType.equalsIgnoreCase("application/json");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String transfermethodid = request.getParameter("transfermethodid");
        String transfermethodversionid = request.getParameter("transfermethodversionid");
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ArrayTransferMethod");
        sdiRequest.setKeyid1List(transfermethodid);
        sdiRequest.setKeyid2List(transfermethodversionid);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setExtendedDataTypes(true);
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet dataset = sdiData.getDataset("primary");
        String transfermap = dataset.getValue(0, "transfermap");
        String s = StringUtil.replaceAll(transfermap, "|", ";");
        String s1 = StringUtil.replaceAll(s, ",", "|");
        String data = StringUtil.replaceAll(s1, "-", "|");
        String columns = "Target Array Index;Target Row Index;Target Column Index;Source Array Index;Source Row Index;Source Column Index";
        DataSet transferDS = new DataSet();
        transferDS.setColidCaseSensitive(true);
        transferDS.setColumnRowString(columns, data);
        String colormap = dataset.getValue(0, "colormap");
        String sc = StringUtil.replaceAll(colormap, ";", "#");
        String sc1 = StringUtil.replaceAll(sc, "|", ";");
        String sc2 = StringUtil.replaceAll(sc1, "#", "|");
        String datac = StringUtil.replaceAll(sc2, "-", "|");
        String colorcols = "Source Array Index;Source Row Index;Source Column Index;Group";
        DataSet colorDS = new DataSet();
        colorDS.setColidCaseSensitive(true);
        colorDS.setColumnRowString(colorcols, datac);
        HashMap<String, DataSet> sheetDSMap = new HashMap<String, DataSet>();
        sheetDSMap.put("Source Target Mapping", transferDS);
        sheetDSMap.put("Color Map", colorDS);
        BufferedInputStream input = null;
        FilterOutputStream output = null;
        File file = null;
        String uploadLocation = "";
        try {
            int length;
            String temp = Configuration.getInstance().getSapphireHome() + "/temp/";
            if (!new File(temp).exists()) {
                new File(temp).mkdir();
            }
            uploadLocation = temp + this.getConnectionProcessor().getConnectionInfo(this.getConnectionProcessor().getConnectionid()).getSysuserId() + System.currentTimeMillis();
            file = ExcelUtil.createExcelWorkbook(uploadLocation, "TransferMap.xls", sheetDSMap);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + transfermethodid + file.getName() + "\"");
            response.setHeader("Content-Length", String.valueOf(file.length()));
            input = new BufferedInputStream(new FileInputStream(file), 8192);
            output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);
            byte[] buffer = new byte[8192];
            while ((length = input.read(buffer)) > -1) {
                ((BufferedOutputStream)output).write(buffer, 0, length);
            }
            ((BufferedOutputStream)output).flush();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        finally {
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {}
            }
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {}
            }
            if (file != null && file.exists()) {
                file.delete();
            }
            if (new File(uploadLocation).exists()) {
                new File(uploadLocation).delete();
            }
        }
    }
}

