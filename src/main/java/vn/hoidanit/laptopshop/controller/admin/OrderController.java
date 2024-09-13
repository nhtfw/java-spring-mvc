package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.service.OrderService;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/admin/order")
    public String getDashboard(Model model) {
        List<Order> orders = this.orderService.handleShowAllOrders();

        model.addAttribute("orders", orders);
        return "admin/order/show";
    }

    @GetMapping("/admin/order/{id}")
    public String getOrderDetailsPage(Model model, @PathVariable long id) {
        Order order = this.orderService.handleGetOrderDetails(id);

        model.addAttribute("id", id);
        model.addAttribute("orderDetails", order.getOrderDetails());
        return "admin/order/detail";
    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {

        Order order = this.orderService.handleGetOrderByID(id);

        model.addAttribute("newOrder", order);

        return "admin/order/update";
    }

    @PostMapping("/admin/order/update")
    public String updateOrder(@ModelAttribute("newOrder") @Valid Order order, BindingResult bindingResult) {
        Order newOrder = this.orderService.handleGetOrderByID(order.getId());
        newOrder.setStatus(order.getStatus());
        this.orderService.handleSaveOrder(newOrder);

        return "redirect:/admin/order";
    }

    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrder(Model model, @PathVariable long id) {
        Order newOrder = this.orderService.handleGetOrderByID(id);

        model.addAttribute("id", id);
        model.addAttribute("newOrder", newOrder);

        return "admin/order/delete";
    }

    @PostMapping("/admin/order/delete")
    public String deleteOrder(@ModelAttribute("newOrder") Order order) {
        Order newOrder = this.orderService.handleGetOrderByID(order.getId());

        this.orderService.handleDeleteOrder(newOrder.getId());

        return "redirect:/admin/order";
    }

}
