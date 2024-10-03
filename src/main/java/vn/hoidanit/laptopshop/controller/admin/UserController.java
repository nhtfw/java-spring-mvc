package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.UploadService;
import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class UserController {

    private final UserService userService;
    private final UploadService uploadService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, UploadService uploadService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.uploadService = uploadService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/user") // http
    public String getUserPage(Model model, @RequestParam(value = "page", defaultValue = "1") String page) {
        int currentPage = 1;

        try {
            currentPage = Integer.parseInt(page);
        } catch (Exception e) {
        }

        Pageable pageable = PageRequest.of(currentPage - 1, 1);
        Page<User> prs = this.userService.getAllUsers(pageable);

        List<User> users = prs.getContent();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        // lấy tổng số trang
        model.addAttribute("totalPages", prs.getTotalPages());

        return "admin/user/show";// // return ve file view nhu html,jsp,... tuc la khi vao url "/" thi se hien
                                 // thi file view ma chung ta return
    }

    @GetMapping("/admin/user/{id}") // http
    public String getUserDetailPage(Model model, @PathVariable long id) {
        User user = this.userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("id", id);
        // model.addAttribute("id", user.getId());
        // model.addAttribute("email", user.getEmail());
        // model.addAttribute("fullname", user.getFullName());
        // model.addAttribute("address", user.getAddress());

        return "admin/user/detail";
    }

    @GetMapping("/admin/user/create") // GET
    public String getCreateUserPage(Model model) {
        model.addAttribute("newUser", new User());// khi truy cap, model se tao mot user moi, modelAttribute o ben html
                                                  // se duoc gan', va path se la bien cua user moi do
        return "admin/user/create";
    }

    @PostMapping(value = "/admin/user/create")
    public String createUserPage(Model model, @ModelAttribute("newUser") @Valid User user,
            BindingResult newUserBindingResult,
            @RequestParam("imageFile") MultipartFile file) { // khi nhan nut create, view se tra ve
                                                             // modelAttribute voi ten tuong
                                                             // ung, o day la user voi ten
                                                             // model la newUser
        // validate
        List<FieldError> errors = newUserBindingResult.getFieldErrors(); // lấy ra danh sách lỗi
        for (FieldError error : errors) { // vòng lặp lỗi
            System.out.println(">>>>" + error.getField() + " - " + error.getDefaultMessage());
        }

        // validate
        if (newUserBindingResult.hasErrors()) {
            return "admin/user/create";
        }

        //
        String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
        String hashPassword = this.passwordEncoder.encode(user.getPassword());

        user.setAvatar(avatar);
        user.setPassword(hashPassword);
        user.setRole(this.userService.getRoleByName(user.getRole().getName()));

        userService.handleSaveUser(user);
        return "redirect:/admin/user"; // redirect http, k phai ten file
    }

    @RequestMapping("/admin/user/update/{id}")
    public String getUpdateUserPage(Model model, @PathVariable long id) {
        User currentUser = this.userService.getUserById(id);
        model.addAttribute("newUser", currentUser);

        // tu lam
        if (currentUser.getAvatar() != null) {
            String avatarPath = uploadService.handleDisplayUploadedFile(currentUser.getAvatar(), "avatar");
            model.addAttribute("avatar", avatarPath);
        }

        return "admin/user/update";
    }

    @PostMapping("/admin/user/update")
    public String getUpdateUser(Model model, @ModelAttribute("newUser") User updatedUser,
            @RequestParam("imageFile") MultipartFile file) {
        User currentUser = this.userService.getUserById(updatedUser.getId());

        if (currentUser != null) {
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setPhone(updatedUser.getPhone());
            currentUser.setAddress(updatedUser.getAddress());
            currentUser.setRole(userService.getRoleByName(updatedUser.getRole().getName()));

            String avatar = this.uploadService.handleSaveUploadFile(file, "avatar");
            currentUser.setAvatar(avatar);

            this.userService.handleSaveUser(currentUser);
        }
        return "redirect:/admin/user"; // http khong phai la file
    }

    @GetMapping("admin/user/delete/{id}")
    public String getDeleteUserPage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("user", this.userService.getUserById(id));
        return "admin/user/delete";
    }

    @PostMapping("/admin/user/delete")
    public String getDeleteUser(@ModelAttribute("user") User user) {
        this.userService.deleteUserById(user.getId());
        return "redirect:/admin/user";
    }

}
