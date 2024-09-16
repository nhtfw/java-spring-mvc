package vn.hoidanit.laptopshop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.User;

//crud: create, read, update, delete
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // day la function, kphai la bien'
    User save(User user); // == insert into user value()

    User findById(long id);

    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    List<User> findOneByEmail(String email);

    void deleteById(long id);

    boolean existsByEmail(String email);

    User findByEmail(String email);
}
