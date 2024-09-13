package vn.hoidanit.laptopshop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public List<Order> handleShowAllOrders() {
        return this.orderRepository.findAll();
    }

    public Order handleGetOrderDetails(long id) {
        return this.orderRepository.findTop1ById(id);
    }

    public Order handleGetOrderByID(long id) {
        return this.orderRepository.findTop1ById(id);
    }

    public void handleSaveOrder(Order order) {
        this.orderRepository.save(order);
    }

    public void handleDeleteOrder(long id) {
        Order currentOrder = this.handleGetOrderByID(id);

        // this.orderDetailRepository.deleteAllByOrder(currentOrder);
        // xoa order detail truoc
        List<OrderDetail> orderDetailList = currentOrder.getOrderDetails();
        for (OrderDetail orderDetail : orderDetailList) {
            this.orderDetailRepository.deleteById(orderDetail.getId());
        }

        this.orderRepository.deleteById(currentOrder.getId());
    }
}
