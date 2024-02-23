package com.dms.specification;

import com.dms.entity.Document;
import com.dms.entity.Document_;
import com.dms.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public class DocumentFilterSpecification {

    private DocumentFilterSpecification() {
    }

    public static Specification<Document> filter(Map<String, String> filters, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = FilterPredicateGenerator.getLikePredicates(filters, root, criteriaBuilder);

            Predicate userPredicate = criteriaBuilder.equal(root.get(Document_.AUTHOR), user);
            predicates.add(userPredicate);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
