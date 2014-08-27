/*
 * Copyright 2014, Tuplejump Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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