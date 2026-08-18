package com.primeshop.product;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.primeshop.product.Product.ProductStatus;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {

    public static Specification<Product> filter(ProductFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ Seller View
            if (Boolean.TRUE.equals(request.getSellerView()) 
                    && request.getSellerId() != null) {
                predicates.add(
                    cb.equal(root.get("seller").get("id"), request.getSellerId())
                );

            } else {
                // ✅ Public View
                predicates.add(cb.equal(root.get("status"), ProductStatus.APPROVED));
                predicates.add(cb.isTrue(root.get("active")));
            }

            // ✅ Search chứa từ khóa - Case insensitive
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String keyword = "%" + request.getSearch().trim().toLowerCase() + "%";
                predicates.add(
                    cb.like(cb.lower(root.get("name")), keyword)
                );
            }

            // ✅ Category filter bằng slug
            if (request.getCategory() != null) {
                predicates.add(
                    cb.equal(root.get("category").get("slug"), request.getCategory())
                );
            }

            // ✅ Brand filter
            if (request.getBrand() != null) {
                predicates.add(
                    cb.equal(root.get("brand"), request.getBrand())
                );
            }

            // ✅ Min price
            if (request.getMinPrice() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice())
                );
            }

            // ✅ Max price
            if (request.getMaxPrice() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice())
                );
            }

            // ✅ Sort default (nếu không truyền sort từ pageable)
            // ví dụ: sort theo createdAt desc mới nhất lên đầu
            if (query.getOrderList().isEmpty()) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

