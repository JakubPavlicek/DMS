package com.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    String documentId;
    UserDTO author;
    String name;
    String type;
    String path;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

}
