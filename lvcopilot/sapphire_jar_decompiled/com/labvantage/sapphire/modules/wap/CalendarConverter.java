/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap;

import com.labvantage.sapphire.DateTimeUtil;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarConverter {
    private final DateTimeUtil dtu;
    private final SimpleDateFormat actionDateFormatter;

    public CalendarConverter(DateTimeUtil dtu) {
        this.dtu = dtu;
        this.actionDateFormatter = (SimpleDateFormat)DateFormat.getDateTimeInstance(2, 2, dtu.getLocale());
    }

    public static ZonedDateTime getStartOfWeek(ZonedDateTime zonedDateTime, Locale locale) {
        ZonedDateTime startofweek = zonedDateTime.with(LocalTime.of(0, 0, 0)).with(WeekFields.of(locale).getFirstDayOfWeek());
        if (startofweek.isAfter(zonedDateTime)) {
            startofweek = startofweek.minus(7L, ChronoUnit.DAYS);
        }
        return startofweek;
    }

    public Calendar convertUtcInstantToUtcCalendar(Instant instantUTC) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(instantUTC.toEpochMilli());
        return c;
    }

    public Instant convertDatabaseCalendarToInstantUtc(Calendar databaseCalendar) {
        if (databaseCalendar == null) {
            return null;
        }
        return databaseCalendar.toInstant();
    }

    public Instant convertDummyIsoToUtcInstant(String dummyIso) {
        TemporalAccessor parse = DateTimeFormatter.ISO_INSTANT.parse(dummyIso);
        LocalDateTime localDateTime = LocalDateTime.of(parse.get(ChronoField.YEAR), parse.get(ChronoField.MONTH_OF_YEAR), parse.get(ChronoField.DAY_OF_MONTH), parse.get(ChronoField.HOUR_OF_DAY), parse.get(ChronoField.MINUTE_OF_HOUR), parse.get(ChronoField.SECOND_OF_MINUTE));
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    public Calendar convertInstantUtcToDatabaseCalendar(Instant instantUTC) {
        return this.convertInstantUtcToCalendarWithTimezone(instantUTC, TimeZone.getDefault());
    }

    public Calendar convertInstantUtcToUtcCalendar(Instant instantUTC, TimeZone timezone) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instantUTC, timezone.toZoneId());
        Calendar toDatabase = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        toDatabase.set(zonedDateTime.getYear(), zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(), zonedDateTime.getSecond());
        toDatabase.set(14, 0);
        return toDatabase;
    }

    public Calendar convertInstantUtcToCalendarWithTimezone(Instant instantUTC, TimeZone timezone) {
        return this.convertInstantUtcToCalendarWithZoneId(instantUTC, timezone.toZoneId());
    }

    public Calendar convertInstantUtcToCalendarWithZoneId(Instant instantUTC, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instantUTC, zoneId);
        Calendar toDatabase = Calendar.getInstance();
        toDatabase.set(zonedDateTime.getYear(), zonedDateTime.getMonthValue() - 1, zonedDateTime.getDayOfMonth(), zonedDateTime.getHour(), zonedDateTime.getMinute(), zonedDateTime.getSecond());
        toDatabase.set(14, 0);
        return toDatabase;
    }

    public Timestamp convertInstantUtcToDatabaseTimestamp(Instant instantUTC) {
        Calendar calendar = this.convertInstantUtcToDatabaseCalendar(instantUTC);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public String convertInstantUtcToUserActionDateString(Instant startInstantUTC) {
        if (startInstantUTC == null) {
            return "";
        }
        Calendar calendar = this.convertInstantUtcToCalendarWithTimezone(startInstantUTC, this.dtu.getTimeZone());
        return this.actionDateFormatter.format(calendar.getTime());
    }

    public String convertInstantUtcToSystemDateString(Instant startInstantUTC) {
        if (startInstantUTC == null) {
            return "";
        }
        Calendar calendar = this.convertInstantUtcToDatabaseCalendar(startInstantUTC);
        return this.actionDateFormatter.format(calendar.getTime());
    }

    public Instant getIsoInstant(String isoInstant) {
        if (isoInstant == null || isoInstant.length() == 0) {
            return null;
        }
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(isoInstant));
    }

    public static ZonedDateTime getZonedDateTimeFromClientInstantUTC(String value, ZoneId displayTimeZone) {
        if (value == null || value.length() == 0) {
            return null;
        }
        if (value.endsWith("Z")) {
            return ZonedDateTime.ofInstant(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(value)), displayTimeZone);
        }
        if (value.matches("^\\d+$")) {
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), displayTimeZone);
        }
        return null;
    }

    public static ZonedDateTime getZonedDateTimeFromClientValue(String value, ZoneId displayTimeZone, int clientOffsetMinutes) {
        if (value == null || value.length() == 0) {
            return null;
        }
        if (value.endsWith("Z")) {
            return CalendarConverter.getZonedDateTimeFromClientIso(value, displayTimeZone, clientOffsetMinutes);
        }
        if (value.matches("^\\d+$")) {
            return CalendarConverter.getZonedDateTimeFromClientMillis(value, displayTimeZone, clientOffsetMinutes);
        }
        return null;
    }

    public static ZonedDateTime getZonedDateTimeFromClientIso(String value, ZoneId displayTimeZone, int clientOffsetMinutes) {
        Instant apparentInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(value));
        apparentInstant = apparentInstant.minus(clientOffsetMinutes, ChronoUnit.MINUTES);
        return CalendarConverter.getZonedDateTimeFromApparentInstant(apparentInstant, displayTimeZone);
    }

    public static ZonedDateTime getZonedDateTimeFromClientMillis(String value, ZoneId displayTimeZone, int clientOffsetMinutes) {
        Instant apparentInstant = Instant.ofEpochMilli(Long.parseLong(value));
        apparentInstant = apparentInstant.minus(clientOffsetMinutes, ChronoUnit.MINUTES);
        return CalendarConverter.getZonedDateTimeFromApparentInstant(apparentInstant, displayTimeZone);
    }

    public Instant getInstantUTCFromApparentMillis(String millis, ZoneId displayTimeZone) {
        Instant apparentInstant = Instant.ofEpochMilli(Long.parseLong(millis));
        ZonedDateTime apparentZoned = CalendarConverter.getZonedDateTimeFromApparentInstant(apparentInstant, displayTimeZone);
        int offset = TimeZone.getDefault().getOffset(apparentZoned.toEpochSecond());
        apparentZoned = apparentZoned.plus(offset, ChronoUnit.MILLIS);
        return apparentZoned.toInstant();
    }

    public static ZonedDateTime getZonedDateTimeFromApparentInstant(Instant apparentInstant, ZoneId toZoneId) {
        int year = apparentInstant.atZone(ZoneOffset.UTC).getYear();
        int month = apparentInstant.atZone(ZoneOffset.UTC).getMonthValue();
        int day = apparentInstant.atZone(ZoneOffset.UTC).getDayOfMonth();
        int hour = apparentInstant.atZone(ZoneOffset.UTC).getHour();
        int minute = apparentInstant.atZone(ZoneOffset.UTC).getMinute();
        int second = apparentInstant.atZone(ZoneOffset.UTC).getSecond();
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, toZoneId);
    }

    public static Instant rewindToStartOfThisDay(Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }

    public static LocalDateTime rewindToStartOfThisDay(LocalDateTime localDateTime) {
        return localDateTime.truncatedTo(ChronoUnit.DAYS);
    }

    public static Instant forwardToStartOfNextDayAfter(Instant instant, Instant after) {
        if (!(instant = instant.truncatedTo(ChronoUnit.DAYS)).isAfter(after)) {
            instant = instant.plus(1L, ChronoUnit.DAYS);
        }
        return instant;
    }

    public static LocalDateTime forwardToStartOfNextDayAfter(LocalDateTime localDateTime, LocalDateTime localDateTimeAfter) {
        if (!(localDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS)).isAfter(localDateTimeAfter)) {
            localDateTime = localDateTime.plus(1L, ChronoUnit.DAYS);
        }
        return localDateTime;
    }
}

