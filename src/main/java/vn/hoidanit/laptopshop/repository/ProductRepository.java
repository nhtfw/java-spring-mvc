package vn.hoidanit.laptopshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

import vn.hoidanit.laptopshop.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Product save(Product product);

    List<Product> findAll();

    Product findTop1ById(long id);

    void deleteById(long id);

    Page<Product> findAll(Pageable page);

    // pagination và filter
    // Page<T> findAll(Specification<T> spec, Pageable pageable);
    Page<Product> findAll(Specification<Product> specification, Pageable page);
}
