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
public class DocumentRevisionDTO {

    Long revisionId;
    UserDTO author;
    String name;
    String type;
    LocalDateTime createdAt;

}
