package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import com.dms.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public class RevisionFilterSpecification {

    public static Specification<DocumentRevision> filter(Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate userPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.AUTHOR), user);
            predicates.add(userPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<DocumentRevision> filterByDocument(Document document, Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.DOCUMENT), document);
            Predicate userPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.AUTHOR), user);
            predicates.addAll(List.of(documentPredicate, userPredicate));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
