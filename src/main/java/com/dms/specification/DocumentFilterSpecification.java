package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.Document_;
import com.dms.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * The {@code DocumentFilterSpecification} class provides specifications for filtering {@link Document} entities.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class DocumentFilterSpecification {

    /** Private constructor to prevent instantiation of this utility class. */
    private DocumentFilterSpecification() {
    }

    /**
     * Generates a specification to filter documents by user.
     *
     * @param filters the filter criteria
     * @param user the user for filtering
     * @return the specification for filtering documents by user
     */
    public static Specification<Document> filterByUser(Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate userPredicate = criteriaBuilder.equal(root.get(Document_.AUTHOR), user);
            predicates.add(userPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Generates a specification to filter documents.
     *
     * @param filters the filter criteria
     * @return the specification for filtering documents
     */
    public static Specification<Document> filter(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
