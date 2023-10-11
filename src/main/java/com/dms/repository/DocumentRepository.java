package com.dms.repository;

import com.dms.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    @Query("SELECT COUNT(document.hashPointer) > 1 FROM Document document WHERE document.hashPointer = :hashPointer")
    boolean duplicateHashPointerExists(String hashPointer);

}
