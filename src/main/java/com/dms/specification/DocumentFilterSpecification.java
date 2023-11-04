package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentFilterSpecification {

    public static <E> Specification<E> filterByItems(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getLikePredicatesFromFilterItems(filters, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<DocumentRevision> filterByDocumentAndFilterItems(Document document, Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getLikePredicatesFromFilterItems(filters, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.DOCUMENT), document);
            predicates.add(documentPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static <E> List<Predicate> getLikePredicatesFromFilterItems(Map<String, String> filters, Root<E> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String field = filter.getKey();
            String value = filter.getValue();

            String pattern = "%" + Normalizer.normalize(value, Normalizer.Form.NFKD) + "%";
            Predicate predicate = criteriaBuilder.like(root.get(field), pattern);

            predicates.add(predicate);
        }

        return predicates;
    }

}
