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
    void whenValidFile_thenHashShouldBeCreated() {
        MockMultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        when(hashProperties.getAlgorithm()).thenReturn("SHA-256");

        String hash = hashService.hashFile(file);

        assertThat(hash).isNotEmpty();
    }

    @Test
    void whenInvalidHashAlgorithm_thenExceptionShouldBeThrown() {
        MockMultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        when(hashProperties.getAlgorithm()).thenReturn("algorithm");

        assertThatExceptionOfType(FileOperationException.class).isThrownBy(() -> hashService.hashFile(file));
    }

}