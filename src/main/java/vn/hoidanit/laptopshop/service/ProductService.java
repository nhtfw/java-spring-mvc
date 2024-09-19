package vn.hoidanit.laptopshop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
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

    // case 0
    public Page<Product> showAllProduct(Pageable page, String name) {
        // this.nameLike() trả về Specification
        return this.productRepository.findAll(ProductSpecs.nameLike(name), page);
    }

    // case 1
    public Page<Product> showAllProductWithMinSpec(Pageable page, double min) {
        return this.productRepository.findAll(ProductSpecs.minPrice(min), page);
    }

    // case 2
    public Page<Product> showAllProductWithMaxSpec(Pageable page, double max) {
        return this.productRepository.findAll(ProductSpecs.maxPrice(max), page);
    }

    // case 3
    public Page<Product> showAllProductWithFactorySpec(Pageable page, String factory) {
        return this.productRepository.findAll(ProductSpecs.matchFactory(factory), page);
    }

    // case 4
    public Page<Product> showAllProductWithManyFactoriesSpec(Pageable page, List<String> factory) {
        return this.productRepository.findAll(ProductSpecs.matchManyFactories(factory), page);
    }

    // case 5
    public Page<Product> showAllProductWithPriceSpec(Pageable page, String price) {

        if (price.equals("10-toi-15-trieu")) {
            double min = 10000000;
            double max = 15000000;
            return this.productRepository.findAll(ProductSpecs.matchPrice(min, max), page);
        } else if (price.equals("15-toi-30-trieu")) {
            double min = 15000000;
            double max = 30000000;
            return this.productRepository.findAll(ProductSpecs.matchPrice(min, max), page);
        }

        return this.productRepository.findAll(page);
    }

    // case 6
    public Page<Product> showAllProductWithManyPriceSpec(Pageable page, List<String> price) {
        // gộp nhiều query lại thành 1 query bằng disjunction
        // trong lần chạy đầu tiên = null
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        int count = 0;
        // lặp các phần tử trong mảng
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "10-toi-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    count++;
                    break;
                case "15-toi-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    count++;
                    break;
                case "20-toi-30-trieu":
                    min = 20000000;
                    max = 30000000;
                    count++;
                    break;
                // Add more cases as needed
            }

            if (min != 0 && max != 0) {
                //
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                // cộng gộp
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        // Check if any price ranges were added (combinedSpec is empty)
        if (count == 0) {
            return this.productRepository.findAll(page);
        }

        return this.productRepository.findAll(combinedSpec, page);
    }

    public Page<Product> showAllProductWithSpec(Pageable pageable, ProductCriteriaDTO productCriteriaDTO) {
        if (productCriteriaDTO.getFactory() == null
                && productCriteriaDTO.getTarget() == null
                && productCriteriaDTO.getPrice() == null) {
            return this.productRepository.findAll(pageable);
        }

        Specification<Product> combinedSpec = Specification.where(null);

        if (productCriteriaDTO.getTarget() != null && productCriteriaDTO.getTarget().isPresent()) {
            // vì truyền vào một list nên tái sử dụng hàm matchManyFactories
            Specification<Product> currentSpec = ProductSpecs
                    .matchManyTarget(productCriteriaDTO.getTarget().get());
            combinedSpec = combinedSpec.and(currentSpec);
        }

        if (productCriteriaDTO.getFactory() != null && productCriteriaDTO.getFactory().isPresent()) {
            Specification<Product> currentSpec = ProductSpecs
                    .matchManyFactories(productCriteriaDTO.getFactory().get());
            combinedSpec = combinedSpec.and(currentSpec);
        }

        if (productCriteriaDTO.getPrice() != null && productCriteriaDTO.getPrice().isPresent()) {
            Specification<Product> currentSpecs = this.buildPriceSpecification(productCriteriaDTO.getPrice().get());
            combinedSpec = combinedSpec.and(currentSpecs);
        }

        return this.productRepository.findAll(combinedSpec, pageable);
    }

    public Specification<Product> buildPriceSpecification(List<String> price) {
        // khởi tạo biến rỗng
        Specification<Product> combinedSpec = Specification.where(null);
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "duoi-10-trieu":
                    min = 1;
                    max = 10000000;
                    break;
                case "10-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    break;
                case "15-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    break;
                case "tren-20-trieu":
                    min = 20000000;
                    max = 200000000;
                    break;
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        return combinedSpec;
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
                    session.setAttribute("sum", cart.getSum());
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
