package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentWithVersionDTO {

    UUID documentId;
    Long version;
    UserDTO author;
    String name;
    String type;
    String path;
    LocalDateTime createdAt;

}
