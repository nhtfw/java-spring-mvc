package vn.hoidanit.laptopshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetails;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailsRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.repository.UserRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailsRepository cartDetailsRepository, UserRepository userRepository,
            OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public List<Product> showAllProduct() {
        return this.productRepository.findAll();
    }

    public Page<Product> showAllProduct(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Page<Product> showAllProduct(Pageable page, String name) {
        // this.nameLike() trả về Specification
        return this.productRepository.findAll(ProductSpecs.nameLike(name), page);
    }

    public Product getProductById(long id) {
        return this.productRepository.findTop1ById(id);
    }

    public void deleteProductById(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {

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
                    cartDetails.setQuantity(quantity);

                    this.cartDetailsRepository.save(cartDetails);

                    // update sum của cart,lưu ý chỉ update khi sản phẩm chưa tồn tại trong giỏ hàng
                    int sum = cart.getSum() + 1;
                    cart.setSum(sum);
                    this.cartRepository.save(cart);

                    // cập nhật session sum
                    session.setAttribute("sum", sum);
                } else {
                    // nếu đã có, tăng số lượng lên 1 và cập nhật
                    oldCartDetails.setQuantity(oldCartDetails.getQuantity() + quantity);
                    this.cartDetailsRepository.save(oldCartDetails);
                }
            }
        }
    }

    // tu lam
    public List<CartDetails> getAllCartDetails(String email) {
        User user = this.userRepository.findByEmail(email);

        if (user != null) {
            Cart cart = this.cartRepository.findByUser(user);

            if (cart != null) {
                List<CartDetails> listCartDetails = this.cartDetailsRepository.findAllByCart(cart);

                return listCartDetails;
            } else {
                //
            }
        }

        return null;
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handlerDeleteCartProduct(long id, HttpSession session) {
        Optional<CartDetails> cartDetails = this.cartDetailsRepository.findById(id);
        if (cartDetails.isPresent()) {
            CartDetails otherCartDetails = cartDetails.get();

            Cart cart = otherCartDetails.getCart();

            this.cartDetailsRepository.deleteById(id);

            int sum = cart.getSum();
            if (sum > 1) {
                sum -= 1;
                cart.setSum(sum);
                this.cartRepository.save(cart);
                session.setAttribute("sum", sum);
            } else if (sum <= 1) {
                this.cartRepository.deleteById(cart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetails> cartDetails) {
        for (CartDetails cartDetail : cartDetails) {
            Optional<CartDetails> cdOptional = this.cartDetailsRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetails currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailsRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(User user, HttpSession session, String receiverName, String receiverAddress,
            String receiverPhone) {

        // create orderDetail
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            // get all products in cart
            List<CartDetails> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {
                // create order
                Order order = new Order();
                // set user_id
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double sum = 0;
                for (CartDetails cd : cartDetails) {
                    sum += cd.getPrice();
                }
                order.setTotalPrice(sum);
                // lấy ID, ID được tạo tự động sau khi save
                order = this.orderRepository.save(order);

                for (CartDetails cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    // set order_id
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice((long) cd.getPrice());
                    orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // delete cart detail
                for (CartDetails cd : cartDetails) {
                    this.cartDetailsRepository.delete(cd);
                }
                // delete cart
                this.cartRepository.deleteById(cart.getId());

                // update sesssion
                session.setAttribute("sum", 0);
            }
        }
    }
}
