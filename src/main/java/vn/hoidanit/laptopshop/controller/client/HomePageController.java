package vn.hoidanit.laptopshop.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import vn.hoidanit.laptopshop.domain.User;
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
    public String getProductsPage(Model model, @RequestParam(name = "page", defaultValue = "1") String pageOptional,
            @RequestParam("name") Optional<String> nameOptional) {
        int page;
        try {
            page = Integer.parseInt(pageOptional);
        } catch (Exception e) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, 6);

        String name = nameOptional.isPresent() ? nameOptional.get() : "";
        Page<Product> prs = this.productService.showAllProduct(pageable, name);

        List<Product> products = prs.getContent();
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());

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
