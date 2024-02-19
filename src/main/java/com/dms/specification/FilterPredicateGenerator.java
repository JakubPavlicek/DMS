package com.dms.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterPredicateGenerator {

    private FilterPredicateGenerator()
    {
    }

    public static <E> List<Predicate> getLikePredicates(Map<String, String> filters, Root<E> root, CriteriaBuilder criteriaBuilder) {
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
