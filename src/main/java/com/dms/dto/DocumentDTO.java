package com.dms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    LocalDateTime updatedAt;

}
