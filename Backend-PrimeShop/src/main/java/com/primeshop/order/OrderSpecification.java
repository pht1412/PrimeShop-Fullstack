package com.primeshop.order;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.primeshop.user.User;
import com.primeshop.utils.SecurityUtils;
import jakarta.persistence.criteria.Predicate;

public class OrderSpecification {
    public static Specification<Order> filter(OrderFilterRequest request, User user) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!SecurityUtils.isAdmin(user)) {
                if (request.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.getUserId()));
                }
            }
            
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.getUserId()));
            }
            if (request.getOrderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getOrderId()));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            if (request.getStartDate() != null && request.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), request.getStartDate(), request.getEndDate()));
            }
            if (request.getIsDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), request.getIsDeleted()));
            }
            if (request.getMinTotalAmount() != null && request.getMaxTotalAmount() != null) {
                predicates.add(criteriaBuilder.between(root.get("totalAmount"), request.getMinTotalAmount(), request.getMaxTotalAmount()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
