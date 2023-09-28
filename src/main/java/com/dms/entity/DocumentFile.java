package com.dms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;

@Entity
@Audited
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String fileId;

    private String fileName;
    private String fileType;
    private String filePath;

    @NotAudited
    @CreationTimestamp
    private LocalDateTime createdAt;

    @NotAudited
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String author;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private byte[] data;
}
