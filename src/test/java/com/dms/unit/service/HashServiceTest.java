package com.dms.unit.service;

import com.dms.config.HashProperties;
import com.dms.exception.FileOperationException;
import com.dms.service.HashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashServiceTest {

    @Mock
    private HashProperties hashProperties;

    @InjectMocks
    private HashService hashService;

    @Test
    void shouldReturnHash() {
        String expectedHash = "4c2e9e6da31a64c70623619c449a040968cdbea85945bf384fa30ed2d5d24fa3";
        MockMultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        when(hashProperties.getAlgorithm()).thenReturn("SHA-256");

        String actualHash = hashService.hashFile(file);

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test
    void shouldThrowFileOperationExceptionWhenAlgorithmDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        when(hashProperties.getAlgorithm()).thenReturn("algorithm");

        assertThatExceptionOfType(FileOperationException.class).isThrownBy(() -> hashService.hashFile(file));
    }

}