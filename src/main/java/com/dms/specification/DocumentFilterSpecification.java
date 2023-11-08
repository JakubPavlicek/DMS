package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import com.dms.entity.Document_;
import com.dms.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public class DocumentFilterSpecification {

    public static Specification<Document> filter(Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = CommonSpecification.getLikePredicates(filters, user, root, criteriaBuilder);

            Predicate userPredicate = criteriaBuilder.equal(root.get(Document_.AUTHOR), user);
            predicates.add(userPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<DocumentRevision> filterByDocument(Document document, User user, Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = CommonSpecification.getLikePredicates(filters, user, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.DOCUMENT), document);
            Predicate userPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.AUTHOR), user);
            predicates.addAll(List.of(documentPredicate, userPredicate));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
