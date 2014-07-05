package com.tuplejump.stargate;

import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * User: satya
 * A simple wrapper around {@link org.joda.time.format.DateTimeFormatter} that retains the
 * format that was used to create it.
 */
public class FormatDateTimeFormatter {

    private final String format;

    private final DateTimeFormatter parser;

    private final DateTimeFormatter printer;

    private final Locale locale;

    public FormatDateTimeFormatter(String format, DateTimeFormatter parser, Locale locale) {
        this(format, parser, parser, locale);
    }

    public FormatDateTimeFormatter(String format, DateTimeFormatter parser, DateTimeFormatter printer, Locale locale) {
        this.format = format;
        this.locale = locale;
        this.printer = locale == null ? printer.withDefaultYear(1970) : printer.withLocale(locale).withDefaultYear(1970);
        this.parser = locale == null ? parser.withDefaultYear(1970) : parser.withLocale(locale).withDefaultYear(1970);
    }

    public String format() {
        return format;
    }

    public DateTimeFormatter parser() {
        return parser;
    }

    public DateTimeFormatter printer() {
        return this.printer;
    }

    public Locale locale() {
        return locale;
    }
}