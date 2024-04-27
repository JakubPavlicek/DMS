package com.dms.mapper.entity;

import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;

/**
 * The {@code RevisionMapper} class provides mapping between field names in the {@link DocumentRevision} and their corresponding fields in the {@link DocumentRevision_} entity.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class RevisionMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private RevisionMapper() {
    }

    /** Enum defining the fields in the {@link DocumentRevision} entity. */
    public enum Field {
        REVISION_ID,
        NAME,
        TYPE,
        SIZE,
        HASH,
        CREATED_AT,
        VERSION
    }

    /**
     * Retrieves the mapped field in the {@link DocumentRevision_} entity corresponding to the given field name.
     *
     * @param field the field name for which to retrieve the mapped field
     * @return the mapped field in the {@link DocumentRevision_} entity
     */
    public static String getMappedRevisionField(String field) {
        Field revisionField = Field.valueOf(field.toUpperCase());

        return switch (revisionField) {
            case REVISION_ID -> DocumentRevision_.REVISION_ID;
            case NAME -> DocumentRevision_.NAME;
            case TYPE -> DocumentRevision_.TYPE;
            case SIZE -> DocumentRevision_.SIZE;
            case HASH -> DocumentRevision_.HASH;
            case CREATED_AT -> DocumentRevision_.CREATED_AT;
            case VERSION -> DocumentRevision_.VERSION;
        };
    }

}
