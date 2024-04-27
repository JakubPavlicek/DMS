package com.dms.mapper.entity;

import com.dms.entity.Document;
import com.dms.entity.Document_;

/**
 * The {@code DocumentMapper} class provides mapping between field names in the {@link Document} and their corresponding fields in the {@link Document_} entity.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class DocumentMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private DocumentMapper() {
    }

    /** Enum defining the fields in the {@link Document} entity. */
    public enum Field {
        DOCUMENT_ID,
        NAME,
        TYPE,
        PATH,
        SIZE,
        HASH,
        VERSION,
        IS_ARCHIVED,
        CREATED_AT,
        UPDATED_AT,
        DELETE_AT
    }

    /**
     * Retrieves the mapped field in the {@link Document_} entity corresponding to the given field name.
     *
     * @param field the field name for which to retrieve the mapped field
     * @return the mapped field in the {@link Document_} entity
     */
    public static String getMappedDocumentField(String field) {
        Field documentField = Field.valueOf(field.toUpperCase());

        return switch (documentField) {
            case DOCUMENT_ID -> Document_.DOCUMENT_ID;
            case NAME -> Document_.NAME;
            case TYPE -> Document_.TYPE;
            case PATH -> Document_.PATH;
            case SIZE -> Document_.SIZE;
            case HASH -> Document_.HASH;
            case VERSION -> Document_.VERSION;
            case IS_ARCHIVED -> Document_.IS_ARCHIVED;
            case CREATED_AT -> Document_.CREATED_AT;
            case UPDATED_AT -> Document_.UPDATED_AT;
            case DELETE_AT -> Document_.DELETE_AT;
        };
    }

}
