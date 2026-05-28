/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.util;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.jsp.PageContext;
import sapphire.util.FormatUtil;
import sapphire.util.HttpUtil;

public class M18NUtil
implements Serializable {
    private DateTimeUtil dtu;
    private FormatUtil fu;
    private DateFormat df;
    private DateFormat dof;
    private TimeZone userTimeZone;
    private Locale userLocale;

    public M18NUtil() {
        this.userLocale = Locale.getDefault();
        this.userTimeZone = TimeZone.getDefault();
        this.dtu = new DateTimeUtil(this.userTimeZone, this.userLocale);
    }

    public M18NUtil(ConnectionInfo connectionInfo) {
        this.dtu = new DateTimeUtil(connectionInfo);
        this.userTimeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
        this.userLocale = I18nUtil.getConnectionLocale(connectionInfo);
    }

    public M18NUtil(File rakFile, ConnectionInfo connectionInfo) {
        this.dtu = new DateTimeUtil(rakFile, connectionInfo);
        this.userTimeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
        this.userLocale = I18nUtil.getConnectionLocale(connectionInfo);
    }

    public M18NUtil(PageContext pageContext) {
        ConnectionInfo connectionInfo = HttpUtil.getConnectionInfo(pageContext);
        this.dtu = new DateTimeUtil(connectionInfo);
        this.userTimeZone = I18nUtil.getSessionTimeZone(pageContext);
        this.userLocale = I18nUtil.getConnectionLocale(connectionInfo);
    }

    public M18NUtil(Locale locale, TimeZone timezone) {
        this.dtu = new DateTimeUtil(timezone, locale);
        this.userTimeZone = timezone;
        this.userLocale = locale;
    }

    public Calendar parseCalendar(String datetime) {
        return this.parseCalendar(datetime, true);
    }

    public Calendar parseCalendar(String datetime, boolean userTimeZoneSensitive) {
        if (userTimeZoneSensitive) {
            this.dtu.setTimeZone(this.userTimeZone);
        } else {
            this.dtu.setTimeZone(TimeZone.getDefault());
        }
        return this.dtu.getCalendar(datetime);
    }

    public String format(Calendar calendar) {
        return this.format(calendar, true);
    }

    public String formatDateOnly(Calendar calendar) {
        return this.formatDateOnly(calendar, true);
    }

    public String format(Calendar calendar, boolean userTimeZoneSensitive) {
        if (this.df == null) {
            this.df = this.dtu.getDefaultDateFormat();
        }
        if (userTimeZoneSensitive) {
            this.df.setTimeZone(this.userTimeZone);
        } else {
            this.df.setTimeZone(TimeZone.getDefault());
        }
        return this.df.format(calendar.getTime());
    }

    public String formatDateOnly(Calendar calendar, boolean userTimeZoneSensitive) {
        if (this.dof == null) {
            this.dof = this.dtu.getDefaultDateOnlyFormat();
        }
        if (userTimeZoneSensitive) {
            this.dof.setTimeZone(this.userTimeZone);
        } else {
            this.dof.setTimeZone(TimeZone.getDefault());
        }
        return this.dof.format(calendar.getTime());
    }

    public DateTimeFormatter getDateTimeFormatter(ZoneId displayZone) {
        try {
            return DateTimeFormatter.ofPattern(((SimpleDateFormat)this.dtu.getDefaultDateFormat()).toPattern()).withZone(displayZone).withLocale(this.getLocale());
        }
        catch (Exception e) {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(displayZone).withLocale(this.getLocale());
        }
    }

    public DateTimeFormatter getDateFormatter(ZoneId displayZone) {
        try {
            return DateTimeFormatter.ofPattern(((SimpleDateFormat)this.dtu.getDefaultDateOnlyFormat()).toPattern()).withZone(displayZone).withLocale(this.getLocale());
        }
        catch (Exception e) {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(displayZone).withLocale(this.getLocale());
        }
    }

    public DateTimeFormatter getTimeFormatter(ZoneId displayZone) {
        try {
            return DateTimeFormatter.ofPattern("HH:mm").withZone(displayZone).withLocale(this.getLocale());
        }
        catch (Exception e) {
            return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withZone(displayZone).withLocale(this.getLocale());
        }
    }

    public DateFormat getDefaultDateFormat() {
        return this.dtu.getDefaultDateFormat();
    }

    public DateFormat getDefaultDateOnlyFormat() {
        return this.dtu.getDefaultDateOnlyFormat();
    }

    public DateFormat getSysQueryDateFormat() {
        return this.dtu.getSysQueryDateFormat();
    }

    public DateFormat getDefaultDateFormat(boolean userTimeZoneSensitive) {
        DateFormat df = this.dtu.getDefaultDateFormat();
        if (!userTimeZoneSensitive) {
            df.setTimeZone(TimeZone.getDefault());
        }
        return df;
    }

    public DateFormat getDefaultDateOnlyFormat(boolean userTimeZoneSensitive) {
        DateFormat df = this.dtu.getDefaultDateOnlyFormat();
        if (!userTimeZoneSensitive) {
            df.setTimeZone(TimeZone.getDefault());
        }
        return df;
    }

    public Calendar getNowCalendar() {
        return Calendar.getInstance(this.dtu.getTimeZone(), this.dtu.getLocale());
    }

    public String format(BigDecimal bigDecimal) {
        if (this.fu == null) {
            this.initFormatUtil();
        }
        return this.fu.format(bigDecimal, false);
    }

    public String format(BigDecimal bigDecimal, boolean group, boolean stripTrailingZeros) {
        if (this.fu == null) {
            this.initFormatUtil();
        }
        return this.fu.format(bigDecimal, group, stripTrailingZeros);
    }

    public String format(BigDecimal bigDecimal, boolean group) {
        if (this.fu == null) {
            this.initFormatUtil();
        }
        return this.fu.format(bigDecimal, group);
    }

    public BigDecimal parseBigDecimal(String value) {
        if (this.fu == null) {
            this.initFormatUtil();
        }
        return this.fu.parseBigDecimal(value);
    }

    public Locale getLocale() {
        return this.userLocale;
    }

    public TimeZone getTimezone() {
        return this.userTimeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.userTimeZone = timeZone;
        this.dtu.setTimeZone(timeZone);
    }

    public boolean isRelDate(String date) {
        return this.dtu.isRelDate(date);
    }

    public boolean isRelDate(String date, boolean dateonly) {
        return this.dtu.isRelDate(date, dateonly);
    }

    private void initFormatUtil() {
        this.fu = FormatUtil.getInstance(this.userLocale);
    }

    public Timestamp parseTimestamp(String datetime, boolean userTimeZoneSensitive) {
        Timestamp ts = null;
        Calendar cal = this.parseCalendar(datetime, userTimeZoneSensitive);
        if (cal != null) {
            ts = new Timestamp(cal.getTime().getTime());
        }
        return ts;
    }

    public Timestamp parseTimestamp(String datetime) {
        return this.parseTimestamp(datetime, true);
    }
}

