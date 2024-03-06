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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Document {

    @Id
    @SequenceGenerator(
        name = "document_id_generator",
        sequenceName = "document_id_sequence",
        allocationSize = 1
    )
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "document_id_generator"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_document_user")
    )
    private User author;

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "document"
    )
    @ToString.Exclude
    private List<DocumentRevision> revisions;

    @Column(
        nullable = false,
        unique = true
    )
    private String documentId;

    @Column(nullable = false)
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Long size;

    @Column(
        length = 64,
        nullable = false
    )
    private String hash;

    @Column(nullable = false)
    private Boolean isArchived;

    @CreationTimestamp
    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deleteAt;

    @PrePersist
    private void generateId() {
        if (documentId == null) {
            documentId = UUID.randomUUID().toString();
        }
    }

}
