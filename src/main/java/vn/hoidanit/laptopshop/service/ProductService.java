package vn.hoidanit.laptopshop.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetails;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailsRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailsRepository cartDetailsRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.userRepository = userRepository;
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public List<Product> showAllProduct() {
        return this.productRepository.findAll();
    }

    public Product getProductById(long id) {
        return this.productRepository.findTop1ById(id);
    }

    public void deleteProductById(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session) {
        User user = this.userRepository.findByEmail(email);
        if (user != null) {
            Cart cart = this.cartRepository.findByUser(user);
            // nếu người dùng chưa có cart
            if (cart == null) {
                // tạo mới cart
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);

                // vừa lưu và gán
                cart = this.cartRepository.save(otherCart);
            }

            Optional<Product> productOptional = this.productRepository.findById(productId);

            // kiểm tra product có tồn tại hay không
            // if (productOptional != null)
            if (productOptional.isPresent()) {
                Product realProduct = productOptional.get();

                // check sản phẩm đã được thêm vào giỏ hàng trước đó hay chưa
                CartDetails oldCartDetails = this.cartDetailsRepository.findByCartAndProduct(cart, realProduct);
                if (oldCartDetails == null) { // nếu chưa có, tạo một đối tượng mới và lưu
                    CartDetails cartDetails = new CartDetails();
                    cartDetails.setCart(cart);
                    cartDetails.setProduct(realProduct);
                    cartDetails.setPrice(realProduct.getPrice());
                    cartDetails.setQuantity(1);

                    this.cartDetailsRepository.save(cartDetails);

                    // update sum của cart,lưu ý chỉ update khi sản phẩm chưa tồn tại trong giỏ hàng
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);

                    // cập nhật session sum
                    session.setAttribute("sum", sum);
                } else {
                    // nếu đã có, tăng số lượng lên 1 và cập nhật
                    oldCartDetails.setQuantity(oldCartDetails.getQuantity() + 1);
                    this.cartDetailsRepository.save(oldCartDetails);
                }
            }
        }
    }

}
