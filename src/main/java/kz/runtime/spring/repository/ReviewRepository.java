package kz.runtime.spring.repository;

import kz.runtime.spring.entity.Product;
import kz.runtime.spring.entity.Review;
import kz.runtime.spring.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserAndProduct(User user, Product product);

    Optional<List<Review>> findAllByPublishedAndProduct(Boolean published, Product product);
}
