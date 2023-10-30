package com.dms.mapper;

import com.dms.entity.DocumentRevision_;

public class RevisionMapper {

    public enum Field {
        REVISION_ID,
        NAME,
        TYPE,
        PATH,
        HASH,
        CREATED_AT,
        VERSION
    }

    public static String getMappedRevisionField(String field) {
        Field revisionField = Field.valueOf(field.toUpperCase());

        return switch (revisionField) {
            case REVISION_ID -> DocumentRevision_.REVISION_ID;
            case NAME -> DocumentRevision_.NAME;
            case TYPE -> DocumentRevision_.TYPE;
            case PATH -> DocumentRevision_.PATH;
            case HASH -> DocumentRevision_.HASH;
            case CREATED_AT -> DocumentRevision_.CREATED_AT;
            case VERSION -> DocumentRevision_.VERSION;
        };
    }

}
