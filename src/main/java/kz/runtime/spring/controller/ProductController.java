package kz.runtime.spring.controller;

import kz.runtime.spring.entity.*;
import kz.runtime.spring.repository.*;
import kz.runtime.spring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private CharacteristicRepository characteristicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping(path = "/products") // ГЛАВНАЯ СТРАНИЦА
    public String productResource(@RequestParam(name = "prevPage", required = false) Integer prevPage,
                                  @RequestParam(name = "nextPage", required = false) Integer nextPage,
                                  @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                                  @RequestParam(name = "prevPack", required = false) Integer prevPack,
                                  @RequestParam(name = "nextPack", required = false) Integer nextPack,
                                  Model model) {

        // Все категории в алфавитном порядке
        Sort sortCategories = Sort.by(Sort.Order.asc("name"));
        List<Category> categories = categoryRepository.findAll(sortCategories);
        model.addAttribute("categories", categories);

        User user = userService.getCurrentUser();
        boolean userEntered = user != null;
        boolean isAdmin = user != null && user.getRole().equals(UserRole.ADMIN);

        int pageIndex = 0;
        final int PACK_NUMBER = 4;
        final int PAGE_ELEMENTS_NUMBER = 7;
        if (pageNumber != null) pageIndex = pageNumber;
        if (prevPage != null) pageIndex--;
        if (nextPage != null) pageIndex++;
        if (prevPack != null) pageIndex = (pageIndex + 1) - ((pageIndex + 1) % PACK_NUMBER == 0 ?
                2 * PACK_NUMBER : (PACK_NUMBER + (pageIndex + 1) % PACK_NUMBER));
        if (nextPack != null) pageIndex = (pageIndex + 1) + ((pageIndex + 1) % PACK_NUMBER == 0 ?
                0 : ((PACK_NUMBER - (pageIndex + 1) % PACK_NUMBER)));


        Sort sort = Sort.by(Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(pageIndex, PAGE_ELEMENTS_NUMBER, sort);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Product> products = productPage.getContent();

        int totalPages = productPage.getTotalPages();
        boolean isPageLast = pageIndex + 1 == totalPages;

        if (userEntered) {
            model.addAttribute("user", user);
        }

        // 1 2 3 4
        // 5 6 7 8
        // 9 10 11 12

        int startPage;
        int lastPage;
        if (nextPack == null && prevPack == null) {
            startPage = 1;
            if (pageIndex + 1 >= PACK_NUMBER) {
                startPage = (pageIndex + 1) % PACK_NUMBER == 0 ?
                        (pageIndex + 1) - 3 : (pageIndex + 1) - (pageIndex + 1) % PACK_NUMBER + 1;
            }
        } else {
            startPage = pageIndex + 1;
        }

        lastPage = Math.min(startPage + 3, totalPages);


        model.addAttribute("products", products);
        model.addAttribute("user_entered", userEntered);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("pageNumber", pageIndex + 1);
        model.addAttribute("isPageLast", isPageLast);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("totalPages", totalPages);

        return "product_product_resource_page";
    }

    @GetMapping(path = "/products/create/category") // СОЗДАТЬ ТОВАР -выбрать категорию
    public String chooseCategoryResource(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        return "product_select_category_page";
    }

    @GetMapping(path = "/products/create") // СОЗДАТЬ ТОВАР -заполнить характеристики
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

    @PostMapping(path = "/products/create") // СОЗДАТЬ ТОВАР -сохранить в бд
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

    @GetMapping(path = "/products/change") // ИЗМЕНИТЬ ТОВАР -изменить категорию, имя, цену
    public String changeProduct(@RequestParam(name = "productId", required = false) Long productId,
                                Model model) {

        Product product = productRepository.findById(productId).orElseThrow();
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "product_change_product_page";
    }

    @PostMapping(path = "/products/change") // ИЗМЕНИТЬ ТОВАР -сохранить новые категорию, имя, цену
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

    @GetMapping(path = "/products/change_characteristics") // ИЗМЕНИТЬ ТОВАР -изменить характеристики
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
//            List<Characteristic> characteristics = product.getCategory().getCharacteristics();
            List<Characteristic> characteristics = characteristicRepository.findAllByCategoryOrderById(product.getCategory());
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

    @PostMapping(path = "/products/change_characteristics") // ИЗМЕНИТЬ ТОВАР -сохранить характеристики
    public String saveChangedCharacteristics(
            Model model,
            @RequestParam(name = "productId", required = false) Long productId,
            @RequestParam(name = "description", required = false) List<String> description,
            @RequestParam(name = "characteristicId", required = false) List<Long> characteristicIds
    ) {
        Product product = productRepository.findById(productId).orElseThrow();
        List<ProductCharacteristic> characteristicDescriptions = product.getProductCharacteristics();

        for (int i = 0; i < characteristicIds.size(); i++) {
            boolean exists = false;

            for (ProductCharacteristic productCharacteristic : characteristicDescriptions) {
                if (productCharacteristic.getCharacteristic().getId().equals(characteristicIds.get(i)) &&
                        !description.get(i).isEmpty()) {
                    exists = true;
                    productCharacteristic.setDescription(description.get(i));
                    productCharacteristicRepository.save(productCharacteristic);
                }
            }

            if (!exists && !description.get(i).isEmpty()) {
                ProductCharacteristic productCharacteristic = new ProductCharacteristic();
                productCharacteristic.setProduct(product);
                productCharacteristic.setCharacteristic(characteristicRepository.findById(characteristicIds.get(i)).orElseThrow());
                productCharacteristic.setDescription(description.get(i));
                productCharacteristicRepository.save(productCharacteristic);
            }
        }

        return "redirect:/products";
    }

    @GetMapping(path = "/products/view") // ПРОСМОТР ТОВАРА
    public String viewProduct(Model model,
                              @RequestParam(name = "productId", required = true) Long productId) {

        Product product = productRepository.findById(productId).orElseThrow();
        List<ProductCharacteristic> productCharacteristics = product.getProductCharacteristics();

        // Список отзывов к товару
        List<Review> reviews = reviewRepository.findAllByPublishedAndProduct(true, product);

        // Рейтинг товара
        double averageRating = 0;
        if (!reviews.isEmpty()) {
            for (Review review : reviews) {
                if (review.getPublished()) {
                    averageRating += review.getRating();
                }
            }
            averageRating = averageRating / reviews.size();
        } else {
            averageRating = -1;
        }

        // Существует ли отзыв по комбинации юзер-продукт
        User currentUser = userService.getCurrentUser();
        Review review = reviewRepository.findByUserAndProduct(currentUser, product).orElse(null);
        boolean reviewExists = review != null;

        boolean userAuthorized = currentUser != null;
        boolean isAdmin = currentUser != null && currentUser.getRole().equals(UserRole.ADMIN);

        model.addAttribute("product", product);
        model.addAttribute("characteristics", productCharacteristics);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewExists", reviewExists);
        model.addAttribute("userAuthorized", userAuthorized);
        model.addAttribute("isAdmin", isAdmin);

        return "product_view_page";
    }

    @PostMapping(path = "/products/saveReview") // СОХРАНИТЬ ОТЗЫВ О ТОВАРЕ
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

    @GetMapping(path = "/products/addToCart") // ДОБАВИТЬ ТОВАР В КОРЗИНУ
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

    @GetMapping(path = "/products/cart") // КОРЗИНА
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

    @GetMapping(path = "/products/cart/increase") // КОРЗИНА -увеличить количество
    public String increaseAmountInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        cart.setAmount(cart.getAmount() + 1);
        cartRepository.save(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/cart/decrease") // КОРЗИНА -уменьшить количество
    public String decreaseAmountInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        if (cart.getAmount() > 0) {
            cart.setAmount(cart.getAmount() - 1);
        }
        cartRepository.save(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/cart/delete") // КОРЗИНА -удалить товар
    public String deleteProductInCart(@RequestParam(name = "cartId", required = true) Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();
        cartRepository.delete(cart);
        return "redirect:/products/cart";
    }

    @GetMapping(path = "/products/update_cart") // КОРЗИНА -обновление списка
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

    @GetMapping(path = "/products/place_order") // СДЕЛАТЬ ЗАКАЗ
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

    @GetMapping(path = "/products/orders") // ЗАКАЗЫ
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

    @GetMapping(path = "/products/moderate_reviews") // МОДЕРАЦИЯ ОТЗЫВОВ
    public String moderateReviews(Model model) {
        List<Review> reviews = reviewRepository.findAllByPublished(false);
        model.addAttribute("reviews", reviews);

        return "moderate_reviews_page";
    }

    @GetMapping(path = "/products/moderate_reviews/post") // МОДЕРАЦИЯ ОТЗЫВОВ -опубликовать
    public String postReview(@RequestParam(name = "reviewId", required = true) Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        review.setPublished(true);
        reviewRepository.save(review);
        return "redirect:/products/moderate_reviews";
    }

    @GetMapping(path = "/products/moderate_reviews/delete") // МОДЕРАЦИЯ ОТЗЫВОВ -удалить
    public String deleteReview(@RequestParam(name = "reviewId", required = true) Long reviewId,
                               @RequestParam(name = "productId", required = false) Long productId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        reviewRepository.delete(review);

        if (productId == null) return "redirect:/products/moderate_reviews";
        else return "redirect:/products/view?productId=" + productId;
    }

    @GetMapping(path = "/products/moderate_reviews/hide") // МОДЕРАЦИЯ ОТЗЫВОВ -скрыть
    public String hideReview(@RequestParam(name = "productId", required = true) Long productId,
                             @RequestParam(name = "reviewId", required = true) Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        review.setPublished(false);
        reviewRepository.save(review);
        return "redirect:/products/view?productId=" + productId;
    }

    @GetMapping(path = "/products/moderate_orders") // МОДЕРАЦИЯ ЗАКАЗОВ
    public String moderateOrders(Model model) {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Order.asc("id")));
        model.addAttribute("orders", orders);

        OrderStatus[] statuses = OrderStatus.values();
        model.addAttribute("statuses", statuses);
        return "moderate_orders_page";
    }

    @GetMapping(path = "/products/moderate_orders/change_status") // МОДЕРАЦИЯ ЗАКАЗОВ -поменять статус
    public String moderateOrderStatus(@RequestParam(name = "orderId", required = true) Long orderId,
                                      @RequestParam(name = "status", required = true) OrderStatus status) {
//        System.out.println(status);
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);

        return "redirect:/products/moderate_orders";
    }

    @GetMapping(path = "/products/moderate_orders/filter_by_status") // МОДЕРАЦИЯ ЗАКАЗОВ -сортировка по статусам
    public String moderateOrdersFilterByStatus(Model model,
                                               @RequestParam(name = "status", required = false) OrderStatus status) {
        List<Order> orders = status == null ? orderRepository.findAll() : orderRepository.findAllByStatusOrderById(status);
//        List<Order> orders = orderRepository.findAllByStatusOrderById(status);
        model.addAttribute("orders", orders);

        OrderStatus[] statuses = OrderStatus.values();
        model.addAttribute("statuses", statuses);
        return "moderate_orders_page";
    }

    @GetMapping(path = "/user/sign_up") // РЕГИСТРАЦИЯ
    public String signUp() {
        return "sign_up_page";
    }

    @PostMapping(path = "/user/sign_up/save") // РЕГИСТРАЦИЯ -сохранение нового пользователя
    public String saveNewUser(@RequestParam(name = "login", required = true) String login,
                              @RequestParam(name = "password", required = true) String password,
                              @RequestParam(name = "firstName", required = true) String firstName,
                              @RequestParam(name = "lastName", required = true) String lastName,
                              Model model) {
        String msg = null;
        User user = userRepository.findByLogin(login).orElse(null);
        if (user != null) {
            msg = "Пользователь с таким логином уже существует";
            model.addAttribute("msg", msg);
            return "sign_up_page";
        } else {
            user = new User();
            user.setRole(UserRole.USER);
            user.setLogin(login);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setSignUpDate(LocalDateTime.now());
            userRepository.save(user);
            return "redirect:/login";
        }
    }

    @GetMapping(path = "/login")
    public String signIn(Model model,
                         @RequestParam(name = "error", required = false) String error) {
        if (error != null)
            model.addAttribute("error_msg", "Неверные данные");

        return "sign_in_page";
    }

    @GetMapping(path = "/products/category")
    public String productsByCategory(@RequestParam(name = "categoryId", required = true) Long categoryId,
                                     Model model) {

        List<Characteristic> characteristics = characteristicRepository.findAllByCategoryIdOrderById(categoryId);
        model.addAttribute("characteristics", characteristics);

        Map<Long, List<ProductCharacteristic>> descriptions = new HashMap<>();
        for (Characteristic characteristic : characteristics) {
            List<ProductCharacteristic> descriptionsList = productCharacteristicRepository.findAllByCharacteristic(characteristic);
            descriptions.put(characteristic.getId(), descriptionsList);
        }
        model.addAttribute("descriptionsMap", descriptions);

        List<Product> products = productRepository.findAllByCategoryId(categoryId);
        model.addAttribute("products", products);

        return "products_categories_page";
    }
}