package vn.hoidanit.laptopshop.service;

import org.springframework.stereotype.Service;

import java.util.List;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public List<Product> showAllProduct() {
        return this.productRepository.findAll();
    }

    public Product getProductById(long id) {
        return this.productRepository.findTop1ById(id);
    }

    public void deleteProductById(long id) {
        this.productRepository.deleteById(id);
    }
}
