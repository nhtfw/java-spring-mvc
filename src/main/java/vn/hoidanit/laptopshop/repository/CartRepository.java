package vn.hoidanit.laptopshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Khi bạn truyền một đối tượng User vào hàm này, Spring sẽ không cần lấy toàn
    // bộ thông tin của đối tượng User. Thay vào đó, nó sẽ sử dụng ID của User
    Cart findByUser(User user);
}
