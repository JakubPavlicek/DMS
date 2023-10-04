package com.dms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRevision {
    @Id
    @SequenceGenerator(
        name = "revision_id_generator",
        sequenceName = "revision_id_sequence",
        allocationSize = 1
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "revision_id_generator"
    )
    private Long revisionId;

    @OneToOne(
        fetch = FetchType.EAGER,
        cascade = CascadeType.PERSIST
    )
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "userId"
    )
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "document_id",
        referencedColumnName = "documentId"
    )
    @JsonIgnore
    private Document document;

    private String name;
    private String extension;
    private String type;
    private String path;
    private String hashPointer;

    @CreationTimestamp
    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private DocumentOperation operation;
}
