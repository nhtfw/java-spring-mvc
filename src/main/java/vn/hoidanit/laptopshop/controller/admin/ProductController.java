package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadService;

@Controller
public class ProductController {

    private final UploadService uploadService;
    private final ProductService productService;

    public ProductController(UploadService uploadService, ProductService productService) {
        this.uploadService = uploadService;
        this.productService = productService;
    }

    @GetMapping("/admin/product")
    // trong trường hợp tham số page không có value, ta thiết lập value mặc định =1
    public String getProductPage(Model model,
            @RequestParam(name = "page", defaultValue = "1") String pageOptional) {
        // hash code limit vì nếu truyền limit qua query string(parameter) thì người
        // dùng có thể nhập được litmit trên query string và gây ảnh hưởng tới view

        // fix bug nếu người dùng nhập ?page= là chữ, đó cũng là lí do dùng String thay
        // vì int
        int page = 1;
        try {
            page = Integer.parseInt(pageOptional);
        } catch (Exception e) {

        }

        // config với pageable
        // .of(page,length,new sort...) (số trang bắt đầu, số limit tức số bản ghi của
        // một lần lấy, sort)
        // -1 vì đếm từ 0
        Pageable pageable = PageRequest.of(page - 1, 5);
        Page<Product> prs = this.productService.showAllProduct(pageable);

        // lấy và chuyển sang list
        List<Product> listProducts = prs.getContent();
        model.addAttribute("products", listProducts);
        // lấy tham số trang hiện tại
        model.addAttribute("currentPage", page);
        // lấy tổng số trang
        model.addAttribute("totalPages", prs.getTotalPages());
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());

        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String createProduct(Model model, @ModelAttribute("newProduct") @Valid Product newProduct,
            BindingResult newProductBindingResult,
            @RequestParam("imageFile") MultipartFile file) {

        if (newProductBindingResult.hasErrors()) {
            return "/admin/product/create";
        }

        String image = this.uploadService.handleSaveUploadFile(file, "product");

        newProduct.setImage(image);
        this.productService.handleSaveProduct(newProduct);

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/{id}")
    public String getProductDetailPage(Model model, @PathVariable("id") long id) {

        model.addAttribute("product", this.productService.getProductById(id));

        return "/admin/product/detail";
    }

    @GetMapping("/admin/product/update/{id}")
    public String getProductUpdatePage(Model model, @PathVariable("id") long id) {

        model.addAttribute("newProduct", this.productService.getProductById(id));

        return "admin/product/update";
    }

    @PostMapping("/admin/product/update")
    public String getProductUpdatePage(Model model, @ModelAttribute("newProduct") @Valid Product newProduct,
            BindingResult newProductBindingResult,
            @RequestParam("imageFile") MultipartFile file) {

        if (newProductBindingResult.hasErrors()) {
            return "/admin/product/update";
        }

        Product currentProduct = this.productService.getProductById(newProduct.getId());

        if (currentProduct != null) {
            if (!file.isEmpty()) { // neu nguoi dung khong cap nhat anh
                String img = this.uploadService.handleSaveUploadFile(file, "product"); // luu anh vao folder
                currentProduct.setImage(img);
            }

            currentProduct.setName(newProduct.getName());
            currentProduct.setPrice(newProduct.getPrice());
            currentProduct.setDetailDesc(newProduct.getDetailDesc());
            currentProduct.setShortDesc(newProduct.getShortDesc());
            currentProduct.setQuantity(newProduct.getQuantity());
            currentProduct.setFactory(newProduct.getFactory());
            currentProduct.setTarget(newProduct.getTarget());

            this.productService.handleSaveProduct(currentProduct);
        }

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String getDeleteProductPage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("product", new Product());
        return "admin/product/delete";
    }

    @PostMapping("/admin/product/delete")
    public String postDeleteProduct(Model model, @ModelAttribute("product") Product pr) {
        this.productService.deleteProductById(pr.getId());
        return "redirect:/admin/product";
    }
}
