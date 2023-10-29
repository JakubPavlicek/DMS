package com.dms.sort;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DocumentSort {

    // valid sort format (comma-separated): <field>:<asc/desc> -> group 1: field, group 2: order by, group 3: "," or end of line
    public static final String DOCUMENT_SORT_REGEX = "(" + getFieldOptions() + "):(asc|desc)(?:,|$)";

    // valid sort fields
    private enum Field {
        NAME,
        TYPE,
        PATH,
        VERSION,
        CREATED_AT,
        UPDATED_AT
    }

    private static String getFieldOptions() {
        return Arrays.stream(Field.values())
                     .map(Enum::name)
                     .map(String::toLowerCase)
                     .collect(Collectors.joining("|"));
    }

}
