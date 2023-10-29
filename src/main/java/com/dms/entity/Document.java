package com.dms.entity;

import jakarta.persistence.CascadeType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        length = 36,
        nullable = false,
        unique = true
    )
    private UUID documentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "userId",
        foreignKey = @ForeignKey(name = "fk_document_user")
    )
    private User author;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.REMOVE,
        mappedBy = "document"
    )
    private List<DocumentRevision> revisions;

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

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
