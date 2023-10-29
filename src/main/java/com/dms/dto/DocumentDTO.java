package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    UUID documentId;
    UserDTO author;
    Long version;
    String name;
    String type;
    String path;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
