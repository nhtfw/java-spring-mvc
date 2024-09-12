package vn.hoidanit.laptopshop.controller.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetails;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.ProductService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {

    private final ProductService productService;

    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{id}")
    public String getProductDetailPage(Model model, @PathVariable long id) {
        model.addAttribute("product", this.productService.getProductById(id));

        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        long productId = id;
        // trả về object nên phải ép kiểu string
        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, productId, session);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {

        // tu lam
        // HttpSession session = request.getSession(false);

        // String email = (String) session.getAttribute("email");

        // List<CartDetails> listCartDetails =
        // this.productService.getAllCartDetails(email);

        // model.addAttribute("listCartDetails", listCartDetails);

        User currentUser = new User();
        // khong tao moi session
        HttpSession session = request.getSession(false);

        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);

        // return list CartDetails
        List<CartDetails> cartDetails = cart.getCartDetails();

        double toltalPrice = 0;
        for (CartDetails cd : cartDetails) {
            toltalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("listCartDetails", cartDetails);
        model.addAttribute("totalPrice", toltalPrice);

        return "client/cart/show";
    }

}
