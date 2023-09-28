package com.dms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

@Entity
@RevisionEntity
@Getter
@Setter
@NoArgsConstructor
public class DocumentFileRevisionInfo {

    @Id
    @RevisionNumber
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

    @RevisionTimestamp
    private LocalDateTime createdAt;
}
