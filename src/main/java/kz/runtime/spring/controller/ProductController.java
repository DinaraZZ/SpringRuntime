package kz.runtime.spring.controller;

import kz.runtime.spring.entity.Category;
import kz.runtime.spring.entity.Characteristic;
import kz.runtime.spring.entity.Product;
import kz.runtime.spring.entity.ProductCharacteristic;
import kz.runtime.spring.repository.CategoryRepository;
import kz.runtime.spring.repository.ProductCharacteristicRepository;
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

    @Autowired
    private ProductCharacteristicRepository productCharacteristicRepository;

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

    @GetMapping(path = "/products/create/category")
    public String chooseCategoryResource(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "product_select_category_page";
    }

    @GetMapping(path = "/products/create")
    public String fillProduct(
            Model model,
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElseThrow();
            model.addAttribute("category", category);
            model.addAttribute("characteristics", category.getCharacteristics());
        }
        return "product_create_resource_page";
    }

    @PostMapping(path = "/products/create")
    public String saveProduct(Model model,
                              @RequestParam(name = "categoryId", required = false) Long categoryId,
                              @RequestParam(name = "product", required = false) String product,
                              @RequestParam(name = "price", required = false) Integer price,
                              @RequestParam(name = "description", required = false) String... description) {
        if (categoryId != null && product != null && price != null && description != null) {
            Category category = categoryRepository.findById(categoryId).orElseThrow();

            Product newProduct = new Product();
            newProduct.setName(product);
            newProduct.setPrice(price);
            newProduct.setVisibility(true);
            newProduct.setCategory(category);
            productRepository.save(newProduct);


            for (int i = 0; i < description.length; i++) {
                Characteristic characteristic = category.getCharacteristics().get(i);
                ProductCharacteristic characteristicDescription = new ProductCharacteristic();
                characteristicDescription.setDescription(description[i]);
                characteristicDescription.setProduct(newProduct);
                characteristicDescription.setCharacteristic(characteristic);
                productCharacteristicRepository.save(characteristicDescription);
            }
        }
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        return "redirect:/products";
    }
}
