package kz.runtime.spring.controller;

import kz.runtime.spring.entity.Cart;
import kz.runtime.spring.entity.Category;
import kz.runtime.spring.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/data_controller")
public class DataController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(path = "/resource_1")
    public Object firstResource() {
        Category category = categoryRepository.findById(1L).orElseThrow();
        return category.getName();
    }

    @GetMapping(path = "/resource_2")
    public Object secondResource() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(Category::getName).toArray();
    }
}
