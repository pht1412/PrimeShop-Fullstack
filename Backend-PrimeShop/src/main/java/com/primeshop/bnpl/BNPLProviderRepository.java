package com.primeshop.bnpl;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BNPLProviderRepository extends JpaRepository<BNPLProvider, Long> {
    Optional<BNPLProvider> findByName(String name);
}
