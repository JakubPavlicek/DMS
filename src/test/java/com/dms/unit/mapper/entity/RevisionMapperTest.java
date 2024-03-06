package com.dms.unit.mapper.entity;

import com.dms.entity.DocumentRevision_;
import com.dms.mapper.entity.RevisionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RevisionMapperTest {

    private static Stream<Arguments> revisionFieldNames() {
        return Stream.of(
            Arguments.of(RevisionMapper.Field.REVISION_ID, DocumentRevision_.REVISION_ID),
            Arguments.of(RevisionMapper.Field.NAME, DocumentRevision_.NAME),
            Arguments.of(RevisionMapper.Field.TYPE, DocumentRevision_.TYPE),
            Arguments.of(RevisionMapper.Field.SIZE, DocumentRevision_.SIZE),
            Arguments.of(RevisionMapper.Field.HASH, DocumentRevision_.HASH),
            Arguments.of(RevisionMapper.Field.CREATED_AT, DocumentRevision_.CREATED_AT),
            Arguments.of(RevisionMapper.Field.VERSION, DocumentRevision_.VERSION)
        );
    }

    @ParameterizedTest
    @MethodSource("revisionFieldNames")
    void shouldReturnCorrectRevisionField(RevisionMapper.Field field, String expectedFieldName) {
        String revisionFieldName = RevisionMapper.getMappedRevisionField(field.name());
        assertThat(revisionFieldName).isEqualTo(expectedFieldName);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenRevisionFieldIsInvalid() {
        assertThatIllegalArgumentException().isThrownBy(() -> RevisionMapper.getMappedRevisionField("null"));
    }

}