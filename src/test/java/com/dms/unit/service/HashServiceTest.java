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
import org.springframework.web.multipart.MultipartFile;

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
        when(hashProperties.getAlgorithm()).thenReturn("SHA-256");

        MultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        String hash = hashService.hashFile(file);

        assertThat(hash).isNotEmpty();
    }

    @Test
    void whenInvalidHashAlgorithm_thenExceptionShouldBeThrown() {
        when(hashProperties.getAlgorithm()).thenReturn("algorithm");

        MultipartFile file = new MockMultipartFile("document.pdf", "Some text".getBytes());

        assertThatExceptionOfType(FileOperationException.class).isThrownBy(() -> hashService.hashFile(file));
    }

}