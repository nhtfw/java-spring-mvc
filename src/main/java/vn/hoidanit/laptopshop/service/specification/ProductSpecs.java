package vn.hoidanit.laptopshop.service.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;

public class ProductSpecs {

    // thao tác với model nào, thì truyền model đó vào
    public static Specification<Product> nameLike(String name) {
        // .like(...) -> (MetaModel.thuộc tính ; câu truy vấn) ; ở đây so sánh
        // Product_.NAME với "%name%" ; trả về Predicate
        // Predicate trả về boolean
        // root.get ... -> (trường thông tin muốn truy vấn ở database, giá trị truyền
        // vào)
        // return .... -> trả về và ghi đè function toPredicate
        // Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder
        // criteriaBuilder);
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

    // case 1
    public static Specification<Product> minPrice(double min) {
        // ge -> greater & equal -> lớn hơn hoặc bằng
        return (root, query, criteriaBuilder) -> criteriaBuilder.ge(root.get(Product_.PRICE), min);
    }

    // case 2
    public static Specification<Product> maxPrice(double max) {
        // le -> less than & equal -> nhỏ hơn hoặc bằng
        return (root, query, criteriaBuilder) -> criteriaBuilder.le(root.get(Product_.PRICE), max);
    }

    // case 3
    public static Specification<Product> matchFactory(String factory) {
        // equal -> bằng
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Product_.FACTORY), factory);
    }

    // case 4
    public static Specification<Product> matchManyFactories(List<String> factory) {
        // truyền vào trường thông tin của đối tượng ứng với table trong databse
        // in(...) trả về CriteriaBuilder.In không phải Predicate
        // thêm giá trị vào một list

        /*
         * root.get(Product_.FACTORY) truy cập trường FACTORY trong bảng tương ứng với
         * đối tượng Product.
         */

        /*
         * criteriaBuilder.in(...) tạo điều kiện IN trong SQL. Điều kiện IN sẽ kiểm tra
         * xem giá trị của cột factory có thuộc danh sách factory (danh sách các nhà sản
         * xuất) không.
         * 
         * Ví dụ List factory có APPLE và ASUS
         * SELECT * FROM Customers
         * WHERE factory IN ('APPLE', 'ASUS');
         */
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get(Product_.FACTORY)).value(factory);
    }

    public static Specification<Product> matchManyTarget(List<String> target) {
        // truyền vào trường thông tin của đối tượng ứng với table trong databse
        // in(...) trả về CriteriaBuilder.In không phải Predicate
        // thêm giá trị vào một list
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get(Product_.TARGET)).value(target);
    }

    // case 5
    public static Specification<Product> matchPrice(double min, double max) {
        // Predicate trả về boolean
        // .and() -> trả về Predicate
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.gt(root.get(Product_.PRICE), min),
                criteriaBuilder.le(root.get(Product_.PRICE), max));
    }

    // case 6
    public static Specification<Product> matchMultiplePrice(double min, double max) {
        // nằm giữa hai khoảng
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(
                root.get(Product_.PRICE), min, max);
    }

    // nói chung là: Các criteriaBuilder.gt()le()equal()... trả về Predicate và các
    // criteriaBuilder.between()and()or()... cũng trả về Predicate nốt.
    // Predicate thì trả về boolean.
    // and(true,false) -> false ; or(true,false) -> true,...
    // Các (root, query, criteriaBuilder)... trả về và ghi đè function toPredicate

    /*
     * root: đại diện cho đối tượng (bảng) mà bạn đang truy vấn.
     * query: đại diện cho câu truy vấn.
     * criteriaBuilder: cung cấp các phương thức để xây dựng các điều kiện lọc
     * (Predicate).
     */
}
