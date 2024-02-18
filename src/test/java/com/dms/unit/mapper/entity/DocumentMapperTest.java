package com.dms.unit.mapper.entity;

import com.dms.entity.Document_;
import com.dms.mapper.entity.DocumentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DocumentMapperTest {

    private static Stream<Arguments> documentFieldNames() {
        return Stream.of(
            Arguments.of(DocumentMapper.Field.DOCUMENT_ID, Document_.DOCUMENT_ID),
            Arguments.of(DocumentMapper.Field.NAME, Document_.NAME),
            Arguments.of(DocumentMapper.Field.TYPE, Document_.TYPE),
            Arguments.of(DocumentMapper.Field.PATH, Document_.PATH),
            Arguments.of(DocumentMapper.Field.HASH, Document_.HASH),
            Arguments.of(DocumentMapper.Field.VERSION, Document_.VERSION),
            Arguments.of(DocumentMapper.Field.CREATED_AT, Document_.CREATED_AT),
            Arguments.of(DocumentMapper.Field.UPDATED_AT, Document_.UPDATED_AT)
        );
    }

    @ParameterizedTest
    @MethodSource("documentFieldNames")
    void shouldReturnCorrectDocumentField(DocumentMapper.Field field, String expectedFieldName) {
        String documentFieldName = DocumentMapper.getMappedDocumentField(field.name());
        assertThat(documentFieldName).isEqualTo(expectedFieldName);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDocumentFieldIsInvalid() {
        assertThatIllegalArgumentException().isThrownBy(() -> DocumentMapper.getMappedDocumentField("null"));
    }

}