package com.dms.storage;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BlobStorage {

    @Value("${storage.path}")
    private String storagePath;

    @Value("${storage.directory-prefix-length}")
    private int directoryPrefixLength;

}
