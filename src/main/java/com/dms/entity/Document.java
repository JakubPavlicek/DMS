package com.dms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String documentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "userId"
    )
    private User author;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.REMOVE,
        mappedBy = "document"
    )
    @JsonIgnore
    private List<DocumentRevision> revisions;

    private String name;
    private String extension;
    private String type;
    private String hash;

    @CreationTimestamp
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    @Column(
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

}
