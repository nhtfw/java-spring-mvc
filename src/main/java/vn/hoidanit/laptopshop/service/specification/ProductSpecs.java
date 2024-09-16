package vn.hoidanit.laptopshop.service.specification;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;

public class ProductSpecs {

    // thao tác với model nào, thì truyền model đó vào
    public static Specification<Product> nameLike(String name) {
        // MetaModel.thuộc tính ; câu truy vấn ; ở đây so sánh Product_.NAME với
        // "%name%" ; trả về Predicate
        // root.get ... -> (trường thông tin muốn truy vấn ở database, giá trị truyền
        // vào)
        // return .... -> trả về và ghi đè function toPredicate
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

}
