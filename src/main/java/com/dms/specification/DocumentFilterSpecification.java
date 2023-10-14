package com.dms.specification;

import com.dms.dto.FilterItem;
import com.dms.entity.Document;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentFilterSpecification {

    public static <E> Specification<E> filterByItems(List<FilterItem> filterItems) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicatesFromFilterItems(filterItems, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static <E> Specification<E> filterByDocumentAndFilterItems(Document document, List<FilterItem> filterItems) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicatesFromFilterItems(filterItems, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get("document"), document);
            predicates.add(documentPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static <E> List<Predicate> getPredicatesFromFilterItems(List<FilterItem> filterItems, Root<E> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        for (FilterItem filterItem : filterItems) {
            String field = filterItem.getField();
            String value = filterItem.getValue();

            Predicate predicate = criteriaBuilder.like(root.get(field), "%" + value + "%");
            predicates.add(predicate);
        }

        return predicates;
    }

}