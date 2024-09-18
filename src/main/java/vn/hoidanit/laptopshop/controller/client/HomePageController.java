package vn.hoidanit.laptopshop.controller.client;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.domain.dto.ProductCriteriaDTO;
import vn.hoidanit.laptopshop.domain.dto.RegisterDTO;
import vn.hoidanit.laptopshop.service.OrderService;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class HomePageController {

    private final ProductService productService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private OrderService orderService;

    public HomePageController(ProductService productService, UserService userService, PasswordEncoder passwordEncoder) {
        this.productService = productService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String getHomePage(Model model) {
        List<Product> products = productService.showAllProduct();
        model.addAttribute("products", products);
        return "client/homepage/show";
    }

    @GetMapping("/products")
    /*
     * Bạn có thể tự động nhận các parameters vào DTO mà không cần
     * dùng @RequestParam nhờ vào cơ chế "data binding" của Spring Framework. Khi
     * Spring nhận một request với các query parameters, nó sẽ tự động ánh xạ (map)
     * các giá trị từ URL vào các thuộc tính của đối tượng DTO mà bạn truyền vào
     * controller.
     * 
     * Trong trường hợp của bạn, Spring Boot tự động ánh xạ các giá trị từ query
     * string vào các thuộc tính của ProductCriteriaDTO dựa trên tên của các
     * parameter trong URL và tên thuộc tính trong DTO. Điều này hoạt động nhờ vào
     * bộ xử lý WebDataBinder của Spring, tự động khớp các tham số request với các
     * thuộc tính trong class DTO.
     */

    /*
     * Spring biết phải đưa các tham số vào class nào thông qua cơ chế
     * "data binding" dựa trên tham số mà bạn truyền vào phương thức của controller.
     * 
     * Khi bạn khai báo một đối tượng DTO như ProductCriteriaDTO làm tham số của
     * phương thức controller, Spring sẽ sử dụng bộ xử lý
     * HandlerMethodArgumentResolver để kiểm tra các tham số của request (cả query
     * string, form data, và path variables). Sau đó, Spring sẽ tự động ánh xạ các
     * tham số này vào các thuộc tính của DTO dựa trên tên thuộc tính và loại dữ
     * liệu trong class DTO.
     */
    public String getProductsPage(Model model, ProductCriteriaDTO productCriteriaDTO, HttpServletRequest request) {
        int page = 1;

        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            }
        } catch (Exception e) {
        }

        // số trang bắt đầu (đếm từ 0), số phần tử mỗi trang
        Pageable pageable = PageRequest.of(page - 1, 3);
        if (productCriteriaDTO.getSort() != null && productCriteriaDTO.getSort().isPresent()) {
            String sort = productCriteriaDTO.getSort().get();
            if (sort.equals("gia-tang-dan")) {
                // .by() trường thông tin muôn sắp xếp
                pageable = PageRequest.of(page - 1, 3, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 3, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> prs = this.productService.showAllProductWithSpec(pageable,
                productCriteriaDTO);

        // case 0
        // String name = nameOptional.isPresent() ? nameOptional.get() : "";
        // Page<Product> prs = this.productService.showAllProduct(pageable, name);

        // case 1
        // double min = minOptional.isPresent() ? Double.parseDouble(minOptional.get())
        // : 0;
        // Page<Product> prs = this.productService.showAllProductWithMinSpec(pageable,
        // min);

        // case 2
        // double max = maxOptional.isPresent() ? Double.parseDouble(maxOptional.get())
        // : 0;
        // Page<Product> prs = this.productService.showAllProductWithMaxSpec(pageable,
        // max);

        // case 3
        // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
        // Page<Product> prs =
        // this.productService.showAllProductWithFactorySpec(pageable, factory);

        // case 4
        // split trả về mảng chuỗi
        // List<String> factory = Arrays.asList(factoryOptional.get().split(","));
        // Page<Product> prs =
        // this.productService.showAllProductWithManyFactoriesSpec(pageable, factory);

        // case 5
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        // Page<Product> prs = this.productService.showAllProductWithPriceSpec(pageable,
        // price);

        // case 6
        // convert sang list
        // List<String> price = Arrays.asList(priceOptional.get().split(","));
        // Page<Product> prs =
        // this.productService.showAllProductWithManyPriceSpec(pageable, price);

        List<Product> products = prs.getContent().size() > 0 ? prs.getContent() : new ArrayList<Product>();

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            // remove page
            qs = qs.replace("page=" + page, "");
        }

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        model.addAttribute("queryString", qs);

        return "client/product/show";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerUser", new RegisterDTO());

        return "client/auth/register";
    }

    @PostMapping("/register")
    public String handleRegisterUser(@ModelAttribute("registerUser") @Valid RegisterDTO registerDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "client/auth/register";
        }

        User user = this.userService.registerDTOtoUser(registerDTO);

        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRole(this.userService.getRoleByName("USER"));

        this.userService.handleSaveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {

        return "client/auth/login";
    }

    @GetMapping("/access_deny")
    public String getDenyPage(Model model) {

        return "client/auth/deny";
    }

    @GetMapping("/order-history")
    public String getOrderHistoryPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        long id = (long) session.getAttribute("id");
        User user = new User();
        user.setId(id);

        List<Order> orders = this.orderService.handleGetAllOrderByUser(user);

        if (orders.size() > 0) {
            model.addAttribute("orders", orders);
        } else {
            model.addAttribute("orders", new ArrayList<Order>());
        }

        return "client/cart/order";
    }
}
