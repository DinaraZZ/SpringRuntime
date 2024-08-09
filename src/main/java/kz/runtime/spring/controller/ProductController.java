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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

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

        // Список отзывов к товару
        List<Review> reviews = reviewRepository.findAllByPublishedAndProduct(true, product);

        // Рейтинг товара
        double averageRating = 0;
        for (Review review : reviews) {
            if (review.getPublished()) {
                averageRating += review.getRating();
            }
        }
        averageRating /= reviews.size();

        DecimalFormat df = new DecimalFormat("#.#");
        String formattedNumber = df.format(averageRating);

        // Существует ли отзыв по комбинации юзер-продукт
        User currentUser = userService.getCurrentUser();
        Review review = reviewRepository.findByUserAndProduct(currentUser, product).orElse(null);
        boolean reviewExists = review != null;

        model.addAttribute("product", product);
        model.addAttribute("characteristics", productCharacteristics);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", formattedNumber);
        model.addAttribute("reviewExists", reviewExists);

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
        review.setPublished(false);
        review.setRating(rating);
        review.setCommentary(commentary);
        review.setReviewDate(LocalDateTime.now());

        reviewRepository.save(review);

        return "redirect:/products";
    }

    @GetMapping(path = "/products/addToCart")
    public String addToCart(@RequestParam(name = "productId", required = true) Long productId) {
        User user = userService.getCurrentUser();
        Product product = productRepository.findById(productId).orElseThrow();
        Cart cart = cartRepository.findByUserAndProduct(user, product).orElse(new Cart());
        cart.setUser(user);
        cart.setProduct(product);
        if (cart.getAmount() == null) {
            cart.setAmount(1);
        } else {
            cart.setAmount(cart.getAmount() + 1);
        }
        cartRepository.save(cart);

        return "redirect:/products";
    }

    @GetMapping(path = "/products/cart")
    public String cart(Model model) {
        User user = userService.getCurrentUser();
        List<Cart> carts = cartRepository.findAllByUserOrderById(user);

        double sum = 0;
        for (Cart cart : carts) {
            sum += cart.getProduct().getPrice() * cart.getAmount();
        }

        model.addAttribute("carts", carts);
        model.addAttribute("sum", sum);

        return "cart_page";
    }

    @GetMapping(path = "/products/cart/increase")
    public String increaseAmountInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        cart.setAmount(cart.getAmount() + 1);
        cartRepository.save(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/cart/decrease")
    public String decreaseAmountInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        if (cart.getAmount() > 0) {
            cart.setAmount(cart.getAmount() - 1);
        }
        cartRepository.save(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/cart/delete")
    public String deleteProductInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        cartRepository.delete(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/update_cart")
    public String updateCart() {
        User user = userService.getCurrentUser();
        List<Cart> carts = user.getCarts();
        for (Cart cart : carts) {
            if (cart.getAmount() == 0) {
                cartRepository.delete(cart);
            }
        }
        return "redirect:/products";
    }

    @GetMapping(path = "/products/place_order")
    public String placeOrder() {
        User user = userService.getCurrentUser();
        List<Cart> carts = user.getCarts();
        if (carts.size() > 0) {
            Order order = new Order();
            order.setUser(user);
            order.setStatus(OrderStatus.CREATED);
            order.setOrderDate(LocalDateTime.now());
            orderRepository.save(order);
            for (Cart cart : carts) {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setOrder(order);
                orderProduct.setProduct(cart.getProduct());
                orderProduct.setAmount(cart.getAmount());
                orderProductRepository.save(orderProduct);
            }
            cartRepository.deleteAll(carts);
        }

        return "redirect:/products/orders";
    }

    @GetMapping(path = "/products/orders")
    public String showOrders(Model model) {
        User user = userService.getCurrentUser();

        List<Order> orders = user.getOrders();
        System.out.println(orders.size());
        Map<Long, Integer> orderCosts = new HashMap<>();

        for (Order order1 : orders) {
            System.out.println(order1.getId());
            int sum = 0;
            for (OrderProduct orderProduct : order1.getOrderProducts()) {
                sum += orderProduct.getProduct().getPrice() * orderProduct.getAmount();
            }
            orderCosts.put(order1.getId(), sum);
            System.out.println(order1.getOrderProducts().size());
        }
        model.addAttribute("orders", orders);
        model.addAttribute("orderCosts", orderCosts);
        return "order_page";
    }

    @GetMapping(path = "/products/moderate_reviews")
    public String moderateReviews(Model model) {
        List<Review> reviews = reviewRepository.findAllByPublished(false);
        model.addAttribute("reviews", reviews);

        return "moderate_reviews_page";
    }

    @GetMapping(path = "/products/moderate_reviews/post")
    public String postReview(@RequestParam(name = "reviewId", required = true) Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        review.setPublished(true);
        reviewRepository.save(review);
        return "redirect:/products/moderate_reviews";
    }

    @GetMapping(path = "/products/moderate_reviews/delete")
    public String deleteReview(@RequestParam(name = "reviewId", required = true) Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        reviewRepository.delete(review);
        return "redirect:/products/moderate_reviews";
    }
}