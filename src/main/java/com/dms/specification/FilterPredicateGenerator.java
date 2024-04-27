package com.dms.specification;

import com.dms.entity.Document_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@code FilterPredicateGenerator} class generates JPA criteria predicates for filtering entities.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class FilterPredicateGenerator {

    /** Private constructor to prevent instantiation of this utility class. */
    private FilterPredicateGenerator() {
    }

    /**
     * Generates a list of predicates for filtering entities based on filter criteria.
     *
     * @param filters the filter criteria
     * @param root the root entity
     * @param criteriaBuilder the criteria builder
     * @return a list of predicates for filtering entities
     */
    public static <E> List<Predicate> getLikePredicates(Map<String, String> filters, Root<E> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String field = filter.getKey();
            String value = filter.getValue();

            // special case -> parse boolean from string
            if (field.equals(Document_.IS_ARCHIVED)) {
                Predicate predicate = criteriaBuilder.equal(root.get(field), Boolean.parseBoolean(value));
                predicates.add(predicate);
                continue;
            }

            String pattern = "%" + Normalizer.normalize(value, Normalizer.Form.NFKD) + "%";
            Predicate predicate = criteriaBuilder.like(root.get(field), pattern);

            predicates.add(predicate);
        }

        return predicates;
    }

}
