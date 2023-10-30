package com.dms.mapper;

import com.dms.entity.Document_;

public class DocumentMapper {

    public enum Field {
        DOCUMENT_ID,
        NAME,
        TYPE,
        PATH,
        HASH,
        VERSION,
        CREATED_AT,
        UPDATED_AT
    }

    public static String getMappedDocumentField(String field) {
        Field documentField = Field.valueOf(field.toUpperCase());

        return switch (documentField) {
            case DOCUMENT_ID -> Document_.DOCUMENT_ID;
            case NAME -> Document_.NAME;
            case TYPE -> Document_.TYPE;
            case PATH -> Document_.PATH;
            case HASH -> Document_.HASH;
            case VERSION -> Document_.VERSION;
            case CREATED_AT -> Document_.CREATED_AT;
            case UPDATED_AT -> Document_.UPDATED_AT;
        };
    }

}
