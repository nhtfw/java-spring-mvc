package vn.hoidanit.laptopshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetails;
import vn.hoidanit.laptopshop.domain.Product;

@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails, Long> {
    // check đã tồn tại bởi cart và product hay chưa
    boolean existsByCartAndProduct(Cart cart, Product product);

    CartDetails findByCartAndProduct(Cart cart, Product product);

    List<CartDetails> findAllByCart(Cart cart);

    void deleteById(long id);
}
