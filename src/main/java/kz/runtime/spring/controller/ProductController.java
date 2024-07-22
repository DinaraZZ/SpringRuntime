package kz.runtime.spring.controller;

import kz.runtime.spring.entity.Category;
import kz.runtime.spring.entity.Product;
import kz.runtime.spring.repository.CategoryRepository;
import kz.runtime.spring.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(path = "/products")
    public String productResource(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                  Model model) {

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElseThrow();
            List<Product> categoryProducts = category.getProducts();
            model.addAttribute("products", categoryProducts);
        } else {
            List<Product> products = productRepository.findAll();
            model.addAttribute("products", products);
        }
        return "product_product_resource_page";
    }

    @GetMapping(path = "/products/create")
    public String createProductResource(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "product_select_category_page";
    }

    @PostMapping(path = "/products/create")
    public String saveProductResource(@RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "price", required = false) Integer price,
                                      @RequestParam(name = "category", required = false) Long categoryId,
                                      Model model) {
        if (categoryId > 0) {
            Category category = categoryRepository.findById(categoryId).orElseThrow();
            model.addAttribute("category", category);
        } else {
            Product newProduct = new Product();
            newProduct.setName(name);
            newProduct.setPrice(price);
            Category category = categoryRepository.findById(categoryId).orElseThrow();
            newProduct.setCategory(category);
            newProduct.setVisibility(true);
            productRepository.save(newProduct);
        }
        return "product_create_resource_page";
    }
    // страница "выбрать категорию" -> страница создания товара с характеристиками выбранной категории
}
