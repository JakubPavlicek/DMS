package com.dms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        length = 36,
        nullable = false,
        unique = true
    )
    private UUID revisionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "userId",
        foreignKey = @ForeignKey(name = "fk_revision_user")
    )
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "document_id",
        referencedColumnName = "documentId",
        foreignKey = @ForeignKey(name = "fk_revision_document")
    )
    private Document document;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String path;

    @Column(
        length = 64,
        nullable = false
    )
    private String hash;

    @CreationTimestamp
    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

}
