package kz.runtime.spring.controller;

import kz.runtime.spring.entity.*;
import kz.runtime.spring.repository.*;
import kz.runtime.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductCharacteristicRepository productCharacteristicRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserService userService;

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
    public String saveProduct(@RequestParam(name = "categoryId", required = false) Long categoryId,
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

        return "redirect:/products";
    }

    @GetMapping(path = "/products/change")
    public String changeProduct(@RequestParam(name = "productId", required = false) Long productId,
                                Model model) {

        Product product = productRepository.findById(productId).orElseThrow();
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "product_change_product_page";
    }

    @PostMapping(path = "/products/change")
    public String saveChangedProduct(@RequestParam(name = "productId", required = false) Long productId,
                                     @RequestParam(name = "categoryId", required = false) Long categoryId,
                                     @RequestParam(name = "name", required = false) String name,
                                     @RequestParam(name = "price", required = false) Integer price) {

        Product product = productRepository.findById(productId).orElseThrow();
        Long oldCategoryId = product.getCategory().getId();

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElseThrow();
            product.setCategory(category);
        }
        if (name != null && !name.isEmpty()) {
            product.setName(name);
        }
        if (price != null) {
            product.setPrice(price);
        }
        productRepository.save(product);

        return "redirect:/products/change_characteristics?productId="
                + product.getId()
                + "&oldCategoryId=" + oldCategoryId;
    }

    @GetMapping(path = "/products/change_characteristics")
    public String changeCharacteristics(
            Model model,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "oldCategoryId", required = false) Long oldCategoryId
    ) {
        Product product = productRepository.findById(productId).orElseThrow();
        Category category = product.getCategory();
        Long categoryId = category.getId();
        Map<Characteristic, ProductCharacteristic> map = new HashMap<>();

        if (categoryId.equals(oldCategoryId)) {
            List<Characteristic> characteristics = product.getCategory().getCharacteristics();
            List<ProductCharacteristic> characteristicDescriptions = product.getProductCharacteristics();


            for (Characteristic characteristic : characteristics) {
                boolean exists = false;
                for (ProductCharacteristic characteristicDescription : characteristicDescriptions) {
                    if (characteristicDescription.getCharacteristic().getName().equals(characteristic.getName())) {
                        map.put(characteristic, characteristicDescription);
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    ProductCharacteristic cd = new ProductCharacteristic();
                    cd.setDescription("");
                    map.put(characteristic, cd);
                }
            }
        } else {
            List<Characteristic> characteristics = category.getCharacteristics();
            for (Characteristic characteristic : characteristics) {
                map.put(characteristic, new ProductCharacteristic());
            }
            List<ProductCharacteristic> characteristicDescriptions = product.getProductCharacteristics();
            productCharacteristicRepository.deleteAll(characteristicDescriptions);
        }


        model.addAttribute("product", product);
        model.addAttribute("map", map);

        return "characteristics_describe";
    }

    @PostMapping(path = "/products/change_characteristics")
    public String saveChangedCharacteristics(
            Model model,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "description", required = false) String... description
    ) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<ProductCharacteristic> characteristicDescriptions = product.getProductCharacteristics();
        for (int i = 0; i < description.length; i++) {
            if (description[i] != null && !description[i].isEmpty()) {
                boolean exists = false;
                ProductCharacteristic characteristicDescription = new ProductCharacteristic();
                if (!characteristicDescriptions.isEmpty()) {
                    for (ProductCharacteristic characteristicDescription1 : characteristicDescriptions) {
                        if (characteristicDescription1.getCharacteristic().getName().equals(
                                product.getCategory().getCharacteristics().get(i).getName()
                        )) {
                            exists = true;
                            characteristicDescription = characteristicDescription1;
                            break;
                        }
                    }
                }
                if (!exists) {
                    characteristicDescription.setDescription(description[i]);
                    characteristicDescription.setProduct(product);
                    characteristicDescription.setCharacteristic(product.getCategory().getCharacteristics().get(i));
                    productCharacteristicRepository.save(characteristicDescription);
                } else {
                    characteristicDescription.setDescription(description[i]);
                    productCharacteristicRepository.save(characteristicDescription);
                }

            }
        }
        return "redirect:/products";
    }

    @GetMapping(path = "/products/view")
    public String viewProduct(Model model,
                              @RequestParam(name = "productId", required = true) Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<ProductCharacteristic> productCharacteristics = product.getProductCharacteristics();
        List<Review> reviews = product.getReviews();

        double averageRating = 0;
        for (Review review : reviews) {
            averageRating += review.getRating();
        }
        averageRating /= reviews.size();

        DecimalFormat df = new DecimalFormat("#.#");
        String formattedNumber = df.format(averageRating);

        model.addAttribute("product", product);
        model.addAttribute("characteristics", productCharacteristics);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", formattedNumber);

        return "product_view_page";
    }

    @PostMapping(path = "/products/saveReview")
    public String saveReview(@RequestParam(name = "productId", required = true) Long productId,
                             @RequestParam(name = "rating", required = true) Integer rating,
                             @RequestParam(name = "commentary", required = true) String commentary) {

        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(productId).orElseThrow();

        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(product);
        review.setPublished(true);
        review.setRating(rating);
        review.setCommentary(commentary);
        review.setReviewDate(LocalDateTime.now());

        reviewRepository.save(review);

        return "redirect:/products";
    }

    @GetMapping(path = "/products/addToCart")
    public String addToCart(@RequestParam(name = "productId", required = true) Long productId) {
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(productId).orElseThrow();

//        Cart cart = currentUser.
//
//        Cart cart = new Cart();
//        cart.setUser(currentUser);
//        cart.setProduct(product);
//        cart.setAmount(1);
        //Найти корзину по комбинации Продкута + Пользователя

        return "redirect:/products";
    }

}