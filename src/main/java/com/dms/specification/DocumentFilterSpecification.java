package com.dms.specification;

import com.dms.filter.FilterItem;
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

public class DocumentFilterSpecification {

    public static <E> Specification<E> filterByItems(List<FilterItem> filterItems) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getLikePredicatesFromFilterItems(filterItems, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<DocumentRevision> filterByDocumentAndFilterItems(Document document, List<FilterItem> filterItems) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getLikePredicatesFromFilterItems(filterItems, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.DOCUMENT), document);
            predicates.add(documentPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static <E> List<Predicate> getLikePredicatesFromFilterItems(List<FilterItem> filterItems, Root<E> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        for (FilterItem filterItem : filterItems) {
            String field = filterItem.getField();
            String value = filterItem.getValue();

            String pattern = "%" + Normalizer.normalize(value, Normalizer.Form.NFKD) + "%";
            Predicate predicate = criteriaBuilder.like(root.get(field), pattern);

            predicates.add(predicate);
        }

        return predicates;
    }

}
