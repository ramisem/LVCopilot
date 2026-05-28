/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRDefaultScriptlet
 *  net.sf.jasperreports.engine.fill.JRFillParameter
 *  org.apache.commons.codec.binary.Base64
 */
package sapphire.report;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.fill.JRFillParameter;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;

public class JasperReportScriptlet
extends JRDefaultScriptlet {
    private TranslationProcessor tp = null;
    private Locale userLocale = null;
    private TimeZone userTimeZone = null;
    private TimeZone reportTimeZone = null;
    private HashMap fieldvalueMap = new HashMap();
    private ArrayList fieldvaluecollectionlist;
    private ConnectionInfo connectionInfo;
    private M18NUtil m18NUtil = new M18NUtil();
    private DataSet address;
    private DataSet logoattachment;
    private static Properties text = null;
    private ByteArrayInputStream logoByte;

    public void setLogo(ByteArrayInputStream logoByte) {
        this.logoByte = logoByte;
    }

    public ByteArrayInputStream getLogoByte() {
        return this.logoByte;
    }

    public HashMap getFieldValueMap() {
        return this.fieldvalueMap;
    }

    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    public void beforeReportInit() {
        Map paramsMap = (Map)((JRFillParameter)this.parametersMap.get("REPORT_PARAMETERS_MAP")).getValue();
        text = new Properties();
        String databaseid = (String)paramsMap.get("SAPPHIRE_DatabaseName");
        try {
            File textpropsFile = new File(paramsMap.get("SAPPHIRE_ReportRoot") + "/reporttexts_" + databaseid + ".txt");
            if (!(textpropsFile.exists() || (textpropsFile = new File(paramsMap.get("SAPPHIRE_ReportRoot") + "/OOB/reporttexts_" + databaseid + ".txt")).exists() || (textpropsFile = new File(paramsMap.get("SAPPHIRE_ReportRoot") + "/reporttexts.txt")).exists())) {
                textpropsFile = new File(paramsMap.get("SAPPHIRE_ReportRoot") + "/OOB/reporttexts.txt");
            }
            if (textpropsFile.exists()) {
                InputStreamReader reader = new InputStreamReader((InputStream)new FileInputStream(textpropsFile), "UTF-8");
                text.load(reader);
                ((Reader)reader).close();
            } else {
                Logger.logInfo("Cannot find reporttexts_" + databaseid + ".txt or reporttexts.txt file");
            }
        }
        catch (Exception e) {
            Logger.logInfo("Cannot load reporttexts_" + databaseid + ".txt or reporttexts.txt file");
        }
        String connectionid = null;
        JRFillParameter connectionidparam = (JRFillParameter)this.parametersMap.get("SAPPHIRE_CONNECTIONID");
        if (connectionidparam != null) {
            connectionid = (String)connectionidparam.getValue();
        }
        if (connectionid != null) {
            this.tp = (TranslationProcessor)paramsMap.get("translationProcessor");
            if (this.tp == null) {
                this.tp = new TranslationProcessor(connectionid);
            }
            this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
            this.m18NUtil = new M18NUtil(this.connectionInfo);
        } else if (this.tp == null) {
            this.tp = (TranslationProcessor)paramsMap.get("translationProcessor");
            if (this.tp != null) {
                this.connectionInfo = new ConnectionProcessor(this.tp.getConnectionid()).getConnectionInfo(this.tp.getConnectionid());
                this.m18NUtil = new M18NUtil(this.connectionInfo);
            }
        }
        String texttype = (String)paramsMap.get("SAPPHIRE_SDCID");
        if (texttype == null) {
            texttype = (String)paramsMap.get("SAPPHIRE_ReportID");
        }
        if (texttype != null && this.tp != null) {
            this.tp.setTextType(texttype);
        }
        if (this.userLocale == null) {
            this.userLocale = I18nUtil.getConnectionLocale(this.connectionInfo);
        }
        if (this.userTimeZone == null) {
            this.userTimeZone = I18nUtil.getConnectionTimeZone(this.connectionInfo);
        }
        if (this.reportTimeZone == null) {
            this.reportTimeZone = paramsMap.get("SAPPHIRE_REPORT_TIMEZONE") != null ? TimeZone.getTimeZone(paramsMap.get("SAPPHIRE_REPORT_TIMEZONE").toString().trim()) : I18nUtil.getConnectionTimeZone(this.connectionInfo);
            this.m18NUtil.setTimeZone(this.reportTimeZone);
        }
        this.address = paramsMap.get("addressid") != null ? ((SDIData)paramsMap.get("addressid")).getDataset("primary") : null;
        this.logoattachment = paramsMap.get("addressid") != null ? ((SDIData)paramsMap.get("addressid")).getDataset("attachment") : null;
        this.fieldvaluecollectionlist = (ArrayList)paramsMap.get("fieldvaluecollectionlist");
    }

    public void beforeDetailEval() {
        if (this.fieldvaluecollectionlist != null && this.fieldvaluecollectionlist.size() > 0) {
            try {
                for (int i = 0; i < this.fieldvaluecollectionlist.size(); ++i) {
                    try {
                        String fieldname = ((String)this.fieldvaluecollectionlist.get(i)).toUpperCase();
                        String fieldvalue = (String)this.getFieldValue(fieldname);
                        if (this.fieldvalueMap.get(fieldname) == null) {
                            this.fieldvalueMap.put(fieldname, new StringBuffer());
                            ((StringBuffer)this.fieldvalueMap.get(fieldname)).append(fieldvalue);
                            continue;
                        }
                        ((StringBuffer)this.fieldvalueMap.get(fieldname)).append(";" + fieldvalue);
                        continue;
                    }
                    catch (Exception ee) {
                        Logger.logWarn(ee.getMessage());
                    }
                }
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
    }

    public String translate(String text) {
        if (this.tp != null) {
            text = this.tp.translate(text);
        }
        return text;
    }

    public String translate(String text, String context) {
        return this.translate(text, context, null);
    }

    public String translate(String text, String context, String languageid) {
        if (this.tp != null) {
            String textType = this.tp.getTextType();
            String language = this.tp.getLanguage();
            if (languageid != null) {
                this.tp.setLanguage(languageid);
            }
            this.tp.setTextType(context);
            text = this.tp.translate(text);
            this.tp.setTextType(textType);
            this.tp.setLanguage(language);
        }
        return text;
    }

    public String formatDate(Calendar datetime) {
        if (datetime == null) {
            return null;
        }
        return this.format(datetime);
    }

    public String formatDateOnly(Calendar datetime) {
        if (datetime == null) {
            return null;
        }
        return this.m18NUtil.formatDateOnly(datetime);
    }

    public String formatDateOnly(Calendar datetime, int datePattern) {
        if (datetime == null) {
            return null;
        }
        return this.formatDateOnly(datetime.getTime(), datePattern);
    }

    public String formatDate(Calendar datetime, int datePattern, int timePattern) {
        if (datetime == null) {
            return null;
        }
        return this.formatDate(datetime.getTime(), datePattern, timePattern);
    }

    public String formatDateOnly(Date datetime) {
        if (datetime == null) {
            return null;
        }
        return this.m18NUtil.getDefaultDateOnlyFormat().format(datetime);
    }

    public String formatDate(Date datetime) {
        if (datetime == null) {
            return null;
        }
        return this.format(datetime);
    }

    public String formatDateOnly(Date datetime, int datePattern) {
        if (datetime == null) {
            return null;
        }
        return DateFormat.getDateInstance(datePattern, this.userLocale).format(datetime);
    }

    public String formatDate(Date datetime, int datePattern, int timePattern) {
        if (datetime == null) {
            return null;
        }
        return DateFormat.getDateTimeInstance(datePattern, timePattern, this.userLocale).format(datetime);
    }

    public String formatDateOnly(Timestamp datetime) {
        return this.m18NUtil.getDefaultDateOnlyFormat().format(datetime);
    }

    public String formatDate(Timestamp datetime) {
        return this.format(datetime);
    }

    public String formatDateOnly(Timestamp datetime, int datePattern) {
        if (datetime == null) {
            return null;
        }
        return this.formatDateOnly((Date)datetime, datePattern);
    }

    public String formatDate(Timestamp datetime, int datePattern, int timePattern) {
        return this.formatDate((Date)datetime, datePattern, timePattern);
    }

    public String formatNumber(BigDecimal num) {
        return this.formatNumber(num, false);
    }

    public String formatNumber(BigDecimal num, boolean group) {
        return this.m18NUtil.format(num, group);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String format(Object valueobject) {
        String formatedout = null;
        if (valueobject == null) return formatedout;
        if (valueobject instanceof Calendar) {
            return this.m18NUtil.format((Calendar)valueobject);
        }
        if (valueobject instanceof BigDecimal) {
            return this.formatNumber((BigDecimal)valueobject);
        }
        if (valueobject instanceof Integer) {
            return this.formatNumber(new BigDecimal((Integer)valueobject));
        }
        if (valueobject instanceof String) {
            return this.translate((String)valueobject);
        }
        if (valueobject instanceof Date) {
            return this.m18NUtil.getDefaultDateFormat().format((Date)valueobject);
        }
        String strvalue = valueobject.toString();
        if (!(valueobject instanceof Clob)) return strvalue;
        Clob clob = (Clob)valueobject;
        if (clob == null) return "";
        try {
            int length = (int)clob.length();
            if (length <= 0) return formatedout;
            return clob.getSubString(1L, length);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String formatDataEntryDisplay(String displayvalue, String datatype, Object transformvalue, Object transformdt, String displayformat, String displayvalueformat) {
        BigDecimal value = null;
        Calendar dt = null;
        value = transformvalue instanceof Integer ? new BigDecimal((Integer)transformvalue) : (BigDecimal)transformvalue;
        if (transformdt instanceof Date) {
            dt = Calendar.getInstance();
            dt.setTime((Date)transformdt);
        } else {
            dt = (Calendar)transformdt;
        }
        return I18nUtil.formatDataEntryDisplay(displayvalue, datatype, value, dt, displayformat, displayvalueformat, this.userLocale, this.userTimeZone, this.m18NUtil);
    }

    public String text(String key) {
        String value = "";
        value = key.equalsIgnoreCase("address") ? this.getAddress() : ((value = text.getProperty(key)) == null ? key : value);
        return value;
    }

    public static Calendar getOffsetDate(Timestamp offsetfromDt, String periodUnit, BigDecimal period) {
        if (offsetfromDt == null) {
            return null;
        }
        Calendar c = null;
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(offsetfromDt.getTime());
        if (period == null) {
            return fromCal;
        }
        try {
            c = DateTimeUtil.getOffsetDate(fromCal, periodUnit, period);
        }
        catch (SapphireException e) {
            Trace.logError(e.getMessage());
        }
        return c;
    }

    public ByteArrayInputStream getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, String connectonId) {
        FileManager.TempFile attachment = FileManager.getAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum, connectonId);
        return this.getByteArrayInputStream(attachment);
    }

    public ByteArrayInputStream getAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentClass, String connectonId) {
        FileManager.TempFile attachment = FileManager.getAttachment(sdcid, keyid1, keyid2, keyid3, attachmentClass, connectonId);
        return this.getByteArrayInputStream(attachment);
    }

    private ByteArrayInputStream getByteArrayInputStream(FileManager.TempFile attachment) {
        FileTypeGroup type = FileTypeGroup.getFileTypeGroupByType(attachment.getMimeType(), this.connectionInfo.getConnectionId());
        byte[] bytes = null;
        if (type == FileTypeGroup.IMAGE) {
            bytes = attachment.getData().getData();
            return new ByteArrayInputStream(bytes);
        }
        FileManager.FileData preview = FileManager.generateThumbnail(attachment.getData(), -1, -1, new Logger(new LogContext()), this.connectionInfo.getConnectionId());
        return new ByteArrayInputStream(Base64.decodeBase64((byte[])preview.getBase64().getBytes()));
    }

    public String getAddress(String columnname) {
        String addressinfo = "";
        if (OpalUtil.isNotEmpty(this.address)) {
            addressinfo = this.address.getString(0, columnname);
        }
        return addressinfo;
    }

    public String getAddress() {
        StringBuilder addressinfo = new StringBuilder();
        if (OpalUtil.isNotEmpty(this.address)) {
            if (OpalUtil.isNotEmpty(this.address.getString(0, "firstname"))) {
                addressinfo.append(this.address.getString(0, "firstname"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "middlename"))) {
                addressinfo.append(this.address.getString(0, "middlename"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "lastname"))) {
                addressinfo.append(" ");
                addressinfo.append(this.address.getString(0, "lastname"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "address1")) || OpalUtil.isNotEmpty(this.address.getString(0, "address2")) || OpalUtil.isNotEmpty(this.address.getString(0, "address3"))) {
                addressinfo.append("\r");
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "address1"))) {
                addressinfo.append(this.address.getString(0, "address1"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "address2"))) {
                addressinfo.append(" ");
                addressinfo.append(this.address.getString(0, "address2"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "address3"))) {
                addressinfo.append(" ");
                addressinfo.append(this.address.getString(0, "address3"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "city")) || OpalUtil.isNotEmpty(this.address.getString(0, "state")) || OpalUtil.isNotEmpty(this.address.getString(0, "postalcode"))) {
                addressinfo.append("\r");
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "city"))) {
                addressinfo.append(this.address.getString(0, "city"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "state"))) {
                addressinfo.append(", ");
                addressinfo.append(this.address.getString(0, "state"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "postalcode"))) {
                addressinfo.append(" ");
                addressinfo.append(this.address.getString(0, "postalcode"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "country"))) {
                addressinfo.append("\r");
                addressinfo.append(this.address.getString(0, "country"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "phone"))) {
                addressinfo.append("\r");
                addressinfo.append("Phone: ").append(this.address.getString(0, "phone"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "fax"))) {
                addressinfo.append("\r");
                addressinfo.append(this.address.getString(0, "fax"));
            }
            if (OpalUtil.isNotEmpty(this.address.getString(0, "email"))) {
                addressinfo.append("\r");
                addressinfo.append(this.address.getString(0, "email"));
            }
        }
        return addressinfo.toString();
    }

    public ByteArrayInputStream getLogo() {
        if (this.logoByte == null) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("attachmentclass", "ReportLogo");
            DataSet logods = this.logoattachment.getFilteredDataSet(filter);
            FileManager.TempFile attachment = FileManager.getAttachment(logods.getString(0, "sdcid"), logods.getString(0, "keyid1"), logods.getString(0, "keyid2"), logods.getString(0, "keyid3"), logods.getString(0, "attachmentclass"), this.connectionInfo.getConnectionId());
            this.logoByte = this.getByteArrayInputStream(attachment);
        }
        return this.logoByte;
    }
}

