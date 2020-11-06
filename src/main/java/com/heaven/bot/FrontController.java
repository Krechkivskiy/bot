package com.heaven.bot;

import com.heaven.bot.dialog.ChatContentGenerator;
import com.heaven.bot.dialog.ChatSteps;
import com.heaven.bot.dialog.MessagesTemplates;
import com.heaven.bot.dialog.Week;
import com.heaven.bot.model.*;
import com.heaven.bot.service.*;
import com.heaven.bot.viber4j.SenderInfo;
import com.heaven.bot.viber4j.http.DefaultViberClient;
import com.heaven.bot.viber4j.http.ViberClient;
import com.heaven.bot.viber4j.incoming.Incoming;
import com.heaven.bot.viber4j.incoming.IncomingImpl;
import com.heaven.bot.viber4j.keyboard.RichMedia;
import com.heaven.bot.viber4j.keyboard.ViberKeyboard;
import com.heaven.bot.viber4j.outgoing.Outgoing;
import com.heaven.bot.viber4j.outgoing.OutgoingImpl;
import net.bytebuddy.utility.RandomString;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FrontController {

    private final ProductService productService;
    private final ChatContentGenerator chatContentGenerator;
    private final ViberClient viberClient;
    private final UserService userService;
    private final StepService stepService;
    private final BasketService basketService;
    private final OrderService orderService;
    private final Map<String, String> lastResponses = new HashMap<>();


    public FrontController(ProductService productService, ChatContentGenerator chatContentGenerator,
                           UserService userService, StepService stepService,
                           BasketService basketService, OrderService orderService) {
        this.productService = productService;
        this.chatContentGenerator = chatContentGenerator;
        this.userService = userService;
        this.stepService = stepService;
        this.basketService = basketService;
        this.orderService = orderService;
        viberClient = new DefaultViberClient("4c367b1d49800ecf-26826b1517659043-635e852722e03961");
    }

    @PostConstruct
    public void asd() {
        stepService.removeAll();
    }


    @RequestMapping(method = RequestMethod.POST, path = "/bot")
    ResponseEntity<?> callbackHandle(@RequestBody String text) {
        Incoming incoming = IncomingImpl.fromString(text);
        String userId = incoming.getSenderId();
        String messageText = incoming.getMessageText();
        String lastMessage = lastResponses.get(userId);
        if (userId.equals("") || (!Objects.isNull(lastMessage) && messageText.equals(lastMessage))) {
            return ResponseEntity.ok(HttpStatus.OK);
        } else {
            lastResponses.put(userId, messageText);
        }
        Outgoing outgoing = OutgoingImpl
                .outgoingForReceiver(userId, viberClient, new SenderInfo(incoming.getSenderName(),
                        incoming.getSenderAvatar()));
        String[] operands = messageText.replaceAll("[{}\"]", "").split("[:,]");
        if (operands.length > 1) {
            ResponseEntity<?> responseEntity = defineActionByButton(userId, messageText, outgoing, operands);
            if (messageText.contains(ChatSteps.STEP_BACK) || messageText.contains(ChatSteps.CONFIRM_DATA)) {
                lastResponses.remove(userId);
            }
            return responseEntity;
        } else {
            ResponseEntity<Object> responseEntity = defineActionByMessageText(userId, messageText, outgoing);
            lastResponses.remove(userId);
            return responseEntity;
        }
    }

    private ResponseEntity<?> defineActionByButton(String userId, String messageText,
                                                   Outgoing outgoing, String[] operands) {
        User user = userService.findById(userId);
        switch (operands[1]) {
            case ChatSteps.STEP_CATEGORY_REGISTRATION:
                return processRegistration(outgoing, userId);
            case ChatSteps.SHOW_DAYS:
                return processDays(outgoing, user);
            case ChatSteps.SHOW_ALL_PRODUCT_TYPES:
                outgoing.postText(" ", showAllProductTypes(operands[3], userId));
                return ResponseEntity.ok(HttpStatus.OK);
            case ChatSteps.SHOW_PRODUCT_LIST:
                return getProductListByType(userId, outgoing, operands[3], user);
            case ChatSteps.BASKET_MENU:
                return processBasketMenu(outgoing, operands, user);
            case ChatSteps.STEP_CREATE_ORDER:
                return processCreateOrder(outgoing, user);
            case ChatSteps.CONFIRM_DATA:
                return processConfirmData(userId, outgoing, messageText, user);
            case ChatSteps.EDIT_INFO:
                outgoing.postText("Введіть вашу адресу і назву підприємства");
                generateAndSaveStepForUser(ChatSteps.STEP_CREATE_ORDER, ChatSteps.STEP_REGISTRATION_ENTERED_ADDRESS,
                        null, null, null, userId);
                return ResponseEntity.ok(HttpStatus.OK);
            case ChatSteps.STEP_BASKET:
                return showBasket(user, outgoing);
            case ChatSteps.STEP_EDIT_BASKET:
                outgoing.postText(" ", processBasketEditMenu(userId));
                return ResponseEntity.ok(HttpStatus.OK);
            case ChatSteps.REMOVE_FROM_BASKET:
                outgoing.postText(" ", processRemoveFromBasket(userId, operands[3], user));
                break;
            case ChatSteps.STEP_BACK:
                return checkBack(stepService
                                .findLastUserAction(new User(userId)).orElseThrow(NoSuchElementException::new),
                        userId, outgoing);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ViberKeyboard processRemoveFromBasket(String userId, String productId, User user) {
        basketService.remove(Long.valueOf(productId), userId);
        if (!basketService.getBasketByUser(user).getProducts().isEmpty()) {
            return processBasketEditMenu(userId);
        } else {
            String day = stepService
                    .findLastUserActionByType(user, ChatSteps.SHOW_ALL_PRODUCT_TYPES)
                    .orElseThrow(NoSuchElementException::new)
                    .getDay();
            return showAllProductTypes(day, user.getId());
        }
    }

    private ResponseEntity<HttpStatus> processConfirmData(String userId, Outgoing outgoing, String step, User user) {
        if (step.contains(ChatSteps.STEP_CATEGORY_REGISTRATION)) {
            outgoing.postText("Введіть вашу адресу і назву підприємства");
            generateAndSaveStepForUser(ChatSteps.STEP_CREATE_ORDER, ChatSteps.STEP_REGISTRATION_ENTERED_ADDRESS, null, null, null, userId);
        } else {
            processOrder(user, outgoing);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<HttpStatus> processCreateOrder(Outgoing outgoing, User user) {
        Basket basketByUser = basketService.getBasketByUser(new User(user.getId()));
        if (checkIsComplexOrder(basketByUser)) {
            if (basketByUser.getCount() != null && basketByUser.getCount() > 0) {
                if (checkIsFullyRegistered(user.getId())) {
                    String confirmMessage = prepareConfirmAddressMessages(user);
                    ViberKeyboard keyboard = chatContentGenerator.generateConfirmButton(new ViberKeyboard(), null);
                    chatContentGenerator.generateEditButton(keyboard);
                    chatContentGenerator.addBackButtonKeyboard(keyboard);
                    outgoing.postText(confirmMessage, keyboard);
                    generateAndSaveStepForUser(ChatSteps.STEP_CREATE_ORDER,
                            ChatSteps.PRESSED_BUTTON, null, null, null, user.getId());
                    return ResponseEntity.ok(HttpStatus.OK);
                } else {
                    prepareConfirmOrderMessageForUnregisteredUsers(user, outgoing);
                    generateAndSaveStepForUser(ChatSteps.STEP_CREATE_ORDER,
                            ChatSteps.PRESSED_BUTTON, null, null, null, user.getId());
                    return ResponseEntity.ok(HttpStatus.OK);
                }
            } else {
                generateAndSaveStepForUser(ChatSteps.CHANGE_COUNT,
                        ChatSteps.CHANGE_COUNT, null, null, null, user.getId());
                outgoing.postText("Введіть кількість порцій", chatContentGenerator.addBackButtonKeyboard(new ViberKeyboard()));
            }
        } else {
            Step step = getLastUserStepWithInfoAboutDayAndProductTypeByUser(user.getId());
            ViberKeyboard viberKeyboard = showAllProductTypes(step.getDay(), user.getId());
            outgoing.postText(MessagesTemplates.NON_FULLY_BASKET_COMPLETED, viberKeyboard);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private boolean checkIsComplexOrder(Basket basketByUser) {
        ArrayList<String> roles = new ArrayList<>();
        roles.add("Основна страва");
        roles.add("Суп");
        roles.add("Салат");
        List<String> basketRoles = basketByUser.getProducts()
                .stream()
                .map(Product::getRole)
                .distinct()
                .collect(Collectors.toList());
        for (String basketRole : basketRoles) {
            roles.remove(basketRole);
        }
        if (roles.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private ResponseEntity<?> processBasketMenu(Outgoing outgoing, String[] operands, User user) {
        if (Arrays.asList(operands).contains(ChatSteps.ADD_TO_BASKET)) {
            return processBasketMenu(user, operands[3], outgoing);
        } else if (Arrays.asList(operands).contains(ChatSteps.SHOW_FULL_INFO_ABOUT_PRODUCT)) {
            if (stepService.findLastUserAction(user).get().getStep().equals(ChatSteps.STEP_BASKET)) {
                RichMedia detailedProductMessage = chatContentGenerator.createDetailedProductMessage(productService.findById(operands[3]));
                ViberKeyboard viberKeyboard = processBasketEditMenu(user.getId());
                outgoing.postCarousel(detailedProductMessage, viberKeyboard);
                return ResponseEntity.ok(HttpStatus.OK);
            } else {
                RichMedia detailedProductMessage = chatContentGenerator.createDetailedProductMessage(productService.findById(operands[3]));
                Step step = stepService
                        .findLastUserActionByType(user, ChatSteps.SHOW_ALL_PRODUCTS_BY_ROLE_AND_DAY).get();
                List<Product> products = processProductsByDayAndType(step.getDay(), step.getProductType2(), user.getId());
                outgoing.postCarousel(detailedProductMessage);
                showAllDaysProducts(outgoing, user, products);
                return ResponseEntity.ok(HttpStatus.OK);
            }
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<?> getProductListByType(String userId, Outgoing outgoing, String type, User user) {
        Optional<Step> lastAction = stepService
                .findLastUserActionByType(user, ChatSteps.SHOW_ALL_PRODUCT_TYPES);
        if (lastAction.isPresent()) {
            String day = lastAction.get().getDay();
            List<Product> products = processProductsByDayAndType(day, type, userId);
            showAllDaysProducts(outgoing, user, products);
            generateAndSaveStepForUser(ChatSteps.SHOW_ALL_PRODUCTS_BY_ROLE_AND_DAY, ChatSteps.PRESSED_BUTTON, null, type, day, userId);
            return ResponseEntity.ok(HttpStatus.OK);
        } else {
            return processDays(outgoing, user);
        }
    }

    private ResponseEntity<?> prepareConfirmOrderMessageForUnregisteredUsers(User user, Outgoing outgoing) {
        String message = "Підтвердити?";
        ViberKeyboard viberKeyboard = new ViberKeyboard();
        chatContentGenerator.generateConfirmButton(viberKeyboard, ChatSteps.STEP_CATEGORY_REGISTRATION);
        chatContentGenerator.addBackButtonKeyboard(viberKeyboard);
        outgoing.postText(message, viberKeyboard);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<Object> defineActionByMessageText(String userId, String messageText, Outgoing
            outgoing) {
        Optional<Step> optionalStep = stepService.findLastUserAction(new User(userId));
        if (optionalStep.isPresent()) {
            Step lastUserAction = optionalStep.get();
            if (messageText.equals(ChatSteps.STEP_BACK)) {
                checkBack(lastUserAction, userId, outgoing);
            } else if (lastUserAction.getStep().equals(ChatSteps.STEP_CATEGORY_REGISTRATION)) {
                checkIsRegistrationProcess(userId, outgoing, messageText, lastUserAction);
                return ResponseEntity.ok(HttpStatus.OK);
            } else if (lastUserAction.getStep().equals(ChatSteps.STEP_BASKET)) {
                if (lastUserAction.getAction() != null && lastUserAction.getAction().equals(ChatSteps.PRESSED_BUTTON)) {
                    addProductCountToBasket(new User(userId), messageText, outgoing);
                }
            } else if (lastUserAction.getStep().equals(ChatSteps.STEP_CREATE_ORDER)) {
                if (lastUserAction.getAction()
                        .equals(ChatSteps.STEP_REGISTRATION_ENTERED_ADDRESS)) {
                    User byId = userService.findById(userId);
                    byId.setAddress(messageText);
                    userService.save(byId);
                    outgoing.postText("Введіть номер телефону");
                    generateAndSaveStepForUser(ChatSteps.STEP_CREATE_ORDER,
                            ChatSteps.STEP_REGISTRATION_ENTERED_PHONE_NUMBER, null, null, null, userId);
                } else if (lastUserAction.getAction()
                        .equals(ChatSteps.STEP_REGISTRATION_ENTERED_PHONE_NUMBER)) {
                    User byId = userService.findById(userId);
                    byId.setMobile(messageText);
                    userService.save(byId);
                    processOrder(byId, outgoing);
                }
            } else if (lastUserAction.getStep().equals(ChatSteps.CHANGE_COUNT)) {
                try {
                    basketService.setCount(userId, Long.valueOf(messageText));
                    processCreateOrder(outgoing, new User(userId));
                    generateAndSaveStepForUser(ChatSteps.STEP_BASKET,
                            ChatSteps.CHANGE_COUNT, null, null, null, userId);
                } catch (Exception e) {
                    outgoing.postText("Введіть нову кількість");
                    e.printStackTrace();
                }
            }
        } else {
            if (checkIsFirstComeToBot(userId)) {

                if (checkIsFullyRegistered(userId)) {
                    outgoing.postText(" ", chatContentGenerator.generateStartMenuForRegisteredUser());
                    return ResponseEntity.ok(HttpStatus.OK);
                } else {
                    outgoing.postText(" ", chatContentGenerator.generateStartMenu());
                }
            } else {
                outgoing.postText(MessagesTemplates.GREETING, chatContentGenerator.generateStartMenu());
            }
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }


    private ResponseEntity<HttpStatus> processChangeCount(String operand, Outgoing outgoing, String userId) {
        outgoing.postText("Введіть кількість", chatContentGenerator.addBackButtonKeyboard(new ViberKeyboard()));

        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<?> processProductsInfo(Outgoing outgoing, String productId, User user) {
        Product byId = productService.findById(productId);
        RichMedia detailedProductMessage = chatContentGenerator.createDetailedProductMessage(byId);
        outgoing.postCarousel(detailedProductMessage);
        List<Product> products = processProductsByDayAndType(byId.getDay(), byId.getRole(), user.getId());
        showAllDaysProducts(outgoing, user, products);
        return ResponseEntity.ok(null);
    }

    private ResponseEntity<HttpStatus> checkBack(Step last, String userId, Outgoing outgoing) {
        switch (last.getStep()) {
            case ChatSteps.SHOW_FULL_INFO_ABOUT_PRODUCT:
            case ChatSteps.STEP_BASKET:
            case ChatSteps.SHOW_ALL_PRODUCTS_BY_ROLE_AND_DAY:
                outgoing.postText(" ", showAllProductTypes(getLastUserStepWithInfoAboutDayAndProductTypeByUser(userId).getDay(), userId));
                break;
            case ChatSteps.SHOW_DAYS:
                if (checkIsFirstComeToBot(userId))
                    if (!checkIsFullyRegistered(userId)) {
                        processMain(outgoing, MessagesTemplates.GREETING);
                    } else {
                        outgoing.postText(" ", chatContentGenerator.generateStartMenuForRegisteredUser());
                    }
                break;
            case ChatSteps.STEP_CATEGORY_REGISTRATION:
                outgoing.postText(" ", chatContentGenerator.generateStartMenu());
                break;
            case ChatSteps.STEP_CREATE_ORDER:
            case ChatSteps.CONFIRM_DATA:
            case ChatSteps.CHANGE_COUNT:
                showBasket(new User(userId), outgoing);
                break;
            default:
                processDays(outgoing, new User(userId));
        }
        lastResponses.remove(userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private boolean checkIsFirstComeToBot(String userId) {
        try {
            userService.findById(userId);
            return true;
        } catch (NoSuchElementException e) {
            userService.save(new User(userId));
            return false;
        }
    }


    private Step getLastUserStepWithInfoAboutDayAndProductTypeByUser(String userId) {
        return stepService.findLastUserActionByType(new User(userId), ChatSteps.SHOW_ALL_PRODUCTS_BY_ROLE_AND_DAY)
                .orElseThrow(NoSuchElementException::new);
    }

    private void generateAndSaveStepForUser(String step, String action,
                                            String paramValue, String type, String day, String userId) {
        Step userStep = new Step();
        userStep.setStep(step);
        userStep.setAction(action);
        userStep.setParamValue(paramValue);
        userStep.setProductType(type);
        userStep.setDay(day);
        stepService.save(userStep, new User(userId));
    }

    private String prepareConfirmAddressMessages(User user) {
        User byId = userService.findById(user.getId());
        return "Адреса доставки   " + byId.getAddress() + ", контактний номер "
                + byId.getMobile();
        //" Сума покупки " + totalPrice + " грн";
    }

    private ResponseEntity<?> showBasket(User user, Outgoing outgoing) {
        Basket basketByUser = basketService.getBasketByUser(user);
        List<Product> products = basketByUser.getProducts()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        RichMedia richMedia = chatContentGenerator.generateReachMediaBasket(products);
        ViberKeyboard keyboard = chatContentGenerator.generateOrderButton(new ViberKeyboard(), ChatSteps.PRESSED_BUTTON);
        chatContentGenerator.addEditBasketButton(keyboard, basketByUser.getId());
        chatContentGenerator.addBackButtonKeyboard(keyboard);
        outgoing.postCarousel(richMedia, keyboard);
        generateAndSaveStepForUser(ChatSteps.STEP_BASKET, ChatSteps.SHOW, null, null, null, user.getId());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private List<Product> processProductsByDayAndType(String day, String type, String userId) {
        List<Product> basket = basketService.getBasketByUser(new User(userId)).getProducts();
        List<Product> all = productService.findProductsByDayAndType(day, type);
        for (Product product : basket) {
            all.remove(product);
        }
        return all;
    }

    private ViberKeyboard showAllProductTypes(String day, String userId) {
        generateAndSaveStepForUser(ChatSteps.SHOW_ALL_PRODUCT_TYPES, ChatSteps.PRESSED_BUTTON, null, null, day, userId);
        List<String> collect = Arrays.stream(Role.values()).map(r -> r.label).collect(Collectors.toList());
        ViberKeyboard keyboard = chatContentGenerator.generateProductRoleKeyboard(collect, new ViberKeyboard());
        Basket basketByUser = basketService.getBasketByUser(new User(userId));
        List<Product> products = basketByUser.getProducts();
        if (products != null && !products.isEmpty()) {
            chatContentGenerator.addBasketButtonKeyboard(keyboard, basketByUser.getId().toString());
            chatContentGenerator.addBackButtonKeyboard(keyboard);
        } else {
            chatContentGenerator.addBackButtonKeyboard(keyboard);
        }
        return keyboard;
    }


    private ViberKeyboard processBasketEditMenu(String userId) {
        Basket basket = basketService.getBasketByUser(new User(userId));
        return chatContentGenerator.
                generateProductsKeyboardForBasket(basket.getCount(), basket.getProducts(), new ViberKeyboard());
    }

    private ResponseEntity<?> showAllDaysProducts(Outgoing outgoing, User user, List<Product> list) {
        HashMap<String, List<Product>> map = new HashMap<>();
        map.put(" ", list);
        ViberKeyboard keyboard = chatContentGenerator.generateRecsKeyboard(map, new ViberKeyboard());
        List<Product> products = basketService.getBasketByUser(user).getProducts();
        if (products != null && products.size() != 0 && list.size() > 0) {
            chatContentGenerator.addBackAndBasketButtonKeyboard(keyboard, basketService.getBasketByUser(user).getId(),
                    list.get(0).getDay());
        } else {
            chatContentGenerator.addBackButtonKeyboard(keyboard);
        }
        outgoing.postText(" ", keyboard);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<?> checkIsRegistrationProcess(String userId, Outgoing outgoing,
                                                         String operation, Step lastUserAction) {
        if (lastUserAction.getAction().equals(ChatSteps.PRESSED_BUTTON)) {
            User byId = userService.findById(userId);
            byId.setAddress(operation);
            userService.save(byId);
            outgoing.postText("Введіть контактний номер телефону",
                    chatContentGenerator.addBackButtonKeyboard(new ViberKeyboard()));
            generateAndSaveStepForUser(ChatSteps.STEP_CATEGORY_REGISTRATION,
                    ChatSteps.STEP_REGISTRATION_ENTERED_ADDRESS, null, null, null, userId);
        } else if (lastUserAction.getAction().equals(ChatSteps.STEP_REGISTRATION_ENTERED_ADDRESS)) {
            User byId = userService.findById(userId);
            byId.setMobile(operation);
            userService.save(byId);
            generateAndSaveStepForUser(ChatSteps.STEP_CATEGORY_REGISTRATION,
                    ChatSteps.STEP_REGISTRATION_ENTERED_PHONE_NUMBER, null, null, null, userId);
            outgoing.postText("Реєстрацію успішно завершено",
                    chatContentGenerator.generateStartMenuForRegisteredUser());
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    void addProductCountToBasket(User user, String operation, Outgoing outgoing) {
        Step lastUserAction = stepService.findLastUserAction(user).get();
        if (lastUserAction.getStep().equals(ChatSteps.STEP_BASKET) &&
                lastUserAction.getAction().equals(ChatSteps.PRESSED_BUTTON)) {
            try {
                long count = Long.parseLong(operation);
                Basket basketByUser = basketService.getBasketByUser(new User(user.getId()));
                basketByUser.setCount(count);
                basketService.save(basketByUser);
                showBasket(user, outgoing);
            } catch (RuntimeException e) {
                outgoing.postText("Введіть кількість", chatContentGenerator.addBackButtonKeyboard(new ViberKeyboard()));
            }
        }
    }

    private boolean checkIsFullyRegistered(String userId) {
        User user = userService.findById(userId);
        if (user.getMobile() != null && user.getAddress() != null) {
            {
                return true;
            }
        } else return false;
    }

    private ResponseEntity<?> processRegistration(Outgoing outgoing, String userId) {
        outgoing.postText("Введіть вашу адресу і назву підприємства", chatContentGenerator.addBackButtonKeyboard(new ViberKeyboard()));
        generateAndSaveStepForUser(ChatSteps.STEP_CATEGORY_REGISTRATION,
                ChatSteps.PRESSED_BUTTON, null, null, null, userId);
        return ResponseEntity.ok(null);
    }

    private ResponseEntity<?> processDays(Outgoing outgoing, User user) {
        generateAndSaveStepForUser(ChatSteps.SHOW_DAYS,
                ChatSteps.PRESSED_BUTTON, null, null, null, user.getId());
        ViberKeyboard keyboard = chatContentGenerator.generateCategoriesKeyboard(new ArrayList<>(Week.getDaysList()));
        Basket basketByUser = basketService.getBasketByUser(user);
        List<Product> productBasket = basketByUser.getProducts();
        if (productBasket != null && !productBasket.isEmpty()) {
            chatContentGenerator.addBasketButtonKeyboard(keyboard, basketByUser.getId().toString());
        }
        outgoing.postText(" ", chatContentGenerator.addBackButtonKeyboard(keyboard));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private ResponseEntity<?> processMain(Outgoing outgoing, String greeting) {
        ViberKeyboard menu = chatContentGenerator.generateStartMenu();
        outgoing.postText(greeting, menu);
        return ResponseEntity.ok(true);
    }

    private ResponseEntity<?> processOrder(User user, Outgoing outgoing) {
        Basket basketByUser = basketService.getBasketByUser(user);
        Order order = new Order();
        order.setUser(user);
        List<Product> products = basketByUser.getProducts();
        if (!products.isEmpty()) {
            basketByUser.setFinished(true);
            basketService.setComplete(basketByUser.getId());
            order.setId(RandomString.make());
            order.setBasket(basketByUser);
            Order save = orderService.save(order);
            basketService.createNewBasketForUser(user);
            String insert = MessagesTemplates.getFinishOrderMessage(products.get(products.size() - 1).getDay());
            outgoing.postText(insert,
                    chatContentGenerator.generateStartMenuForRegisteredUser());
            return ResponseEntity.ok(save);
        }
        return ResponseEntity.ok(null);
    }

    private ResponseEntity<?> processBasketMenu(User user, String productId, Outgoing outgoing) {
        basketService.addProduct(productService.findById(productId), user);
        Step last = getLastUserStepWithInfoAboutDayAndProductTypeByUser(user.getId());
        getProductListByType(user.getId(), outgoing, last.getProductType(), user);
        generateAndSaveStepForUser(ChatSteps.STEP_BASKET,
                ChatSteps.PRESSED_BUTTON, null, null, null, user.getId());
        return ResponseEntity.ok(null);
    }
}
