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
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_revision_user")
    )
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "document_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_revision_document")
    )
    @ToString.Exclude
    private Document document;

    @Column(
        nullable = false,
        unique = true
    )
    private String revisionId;

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

    @PrePersist
    private void generateId() {
        if (revisionId == null)
            revisionId = UUID.randomUUID().toString();
    }

}
