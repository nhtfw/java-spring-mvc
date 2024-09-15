package vn.hoidanit.laptopshop.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import vn.hoidanit.laptopshop.service.UserService;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String getDashboard(Model model) {
        model.addAttribute("countUsers", this.userService.getCountUsers());
        model.addAttribute("countProducts", this.userService.getCountProducts());
        model.addAttribute("countOrders", this.userService.getCountOrders());

        return "admin/dashboard/show";
    }
}
