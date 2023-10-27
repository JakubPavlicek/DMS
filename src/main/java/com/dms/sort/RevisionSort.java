package com.dms.sort;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RevisionSort {

    // valid sort format for revisions (comma-separated): <field>:<asc/desc> -> group 1: field, group 2: order by, group 3: "," or end of line
    public static final String REVISION_SORT_REGEX = "(" + getFieldOptions() + "):(asc|desc)(?:,|$)";

    // valid sort fields
    private enum Field {
        NAME,
        TYPE,
        PATH,
        CREATED_AT,
        VERSION
    }

    private static String getFieldOptions() {
        return Arrays.stream(Field.values())
                     .map(Enum::name)
                     .map(String::toLowerCase)
                     .collect(Collectors.joining("|"));
    }

}