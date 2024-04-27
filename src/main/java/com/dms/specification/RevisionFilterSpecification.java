package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.DocumentRevision;
import com.dms.entity.DocumentRevision_;
import com.dms.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

/**
 * The {@code RevisionFilterSpecification} class provides specifications for filtering {@link DocumentRevision} entities.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
public class RevisionFilterSpecification {

    /** Private constructor to prevent instantiation of this utility class. */
    private RevisionFilterSpecification() {
    }

    /**
     * Generates a specification to filter document revisions by user.
     *
     * @param filters the filter criteria
     * @param user the user for filtering
     * @return the specification for filtering document revisions by user
     */
    public static Specification<DocumentRevision> filter(Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate userPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.AUTHOR), user);
            predicates.add(userPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Generates a specification to filter document revisions by document and user.
     *
     * @param document the document for filtering
     * @param filters the filter criteria
     * @param user the user for filtering
     * @return the specification for filtering document revisions by document and user
     */
    public static Specification<DocumentRevision> filterByDocumentAndUser(Document document, Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate documentPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.DOCUMENT), document);
            Predicate userPredicate = criteriaBuilder.equal(root.get(DocumentRevision_.AUTHOR), user);
            predicates.addAll(List.of(documentPredicate, userPredicate));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
