package com.dms.filter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DocumentFilter {

    // valid filter format (comma-separated): <field>:<value> -> group 1: field, group 2: value
    public static final String FILTER_REGEX = "(" + getFieldOptions() + "):\"([^,]*)\"(?:,|$)";

    // valid filter fields
    private enum Field {
        NAME,
        TYPE,
        PATH
    }

    private static String getFieldOptions() {
        return Arrays.stream(Field.values())
                     .map(Enum::name)
                     .map(String::toLowerCase)
                     .collect(Collectors.joining("|"));
    }

}
