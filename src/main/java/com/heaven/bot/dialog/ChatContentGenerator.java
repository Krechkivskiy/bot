package com.heaven.bot.dialog;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.heaven.bot.model.*;
import com.heaven.bot.viber4j.keyboard.BtnActionType;
import com.heaven.bot.viber4j.keyboard.RichMedia;
import com.heaven.bot.viber4j.keyboard.ViberButton;
import com.heaven.bot.viber4j.keyboard.ViberKeyboard;
import org.springframework.stereotype.Component;

@Component
public class ChatContentGenerator {

    private Gson gson = new Gson();

    private static final int CAROUSEL_ITEM_WIDTH = 4;
    private static final int CAROUSEL_ITEM_HEIGHT = 4;

    public ViberKeyboard generateCategoriesKeyboard(List<String> categories) {
        ViberKeyboard keyboard = new ViberKeyboard();
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(categories.size() + 1);
        for (String category : categories) {
            String actionBody = createProductActionBody(ChatSteps.SHOW_ALL_PRODUCT_TYPES, category);
            keyboard.addButton(new ViberButton(actionBody).setText(category).setRows(1).setColumns(6));
        }
        return keyboard;
    }

    public ViberKeyboard generateRecsKeyboard(Map<String, List<Product>> recs,
                                              ViberKeyboard keyboard) {
        ViberKeyboard resultKeyboard = new ViberKeyboard();
        if (keyboard != null) {
            resultKeyboard = keyboard;
        }
        for (Map.Entry<String, List<Product>> entry : recs.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                generateProductsKeyboard(entry.getValue(), resultKeyboard);
            }
        }
        return resultKeyboard;
    }

    public ViberKeyboard generateProductsKeyboard(List<Product> products,
                                                  ViberKeyboard providedKeyboard) {
        ViberKeyboard result;
        String stepName = "";
        if (providedKeyboard != null) {
            result = providedKeyboard;
        } else {
            result = new ViberKeyboard();
        }
        stepName = ChatSteps.BASKET_MENU;
        result.setButtonsGroupColumns(6).
                setButtonsGroupRows(products.size() + 1);
        for (
                Product product : products) {
            String name = product.getName();
            String id = product.getId();
            String actionBodyViewProduct = createProductActionBody(stepName, id, ChatSteps.SHOW_FULL_INFO_ABOUT_PRODUCT);
            String actionBodyBuyProduct = createProductActionBody(stepName, id, ChatSteps.ADD_TO_BASKET);
            result
                    .addButton(new ViberButton(actionBodyViewProduct).setRows(1).setColumns(1).setImage(product.getPicture()))
                    .addButton(new ViberButton(actionBodyViewProduct).setText(name).setRows(1).setColumns(3))
                    .addButton(new ViberButton(actionBodyBuyProduct).setText("В корзину").setRows(1).setColumns(2));
        }
        return result;
    }

    public ViberKeyboard generateProductsKeyboardForBasket(Long count, List<Product> products,
                                                           ViberKeyboard providedKeyboard) {
        ViberKeyboard result;
        if (providedKeyboard != null) {
            result = providedKeyboard;
        } else {
            result = new ViberKeyboard();
        }
        if (count == null) {
            count = 0l;
        }
        result.setButtonsGroupColumns(6).
                setButtonsGroupRows(products.size() + 1);
        String basketBody = createProductActionBody(ChatSteps.STEP_BASKET, ChatSteps.PRESSED_BUTTON, ChatSteps.PRESSED_BUTTON);
        String changeCountBody = createProductActionBody(ChatSteps.STEP_BASKET, ChatSteps.SHOW_FULL_INFO_ABOUT_PRODUCT);
        for (Product product : products) {
            String productActionBody = createProductActionBody(ChatSteps.BASKET_MENU,
                    product.getId(),
                    ChatSteps.SHOW_FULL_INFO_ABOUT_PRODUCT);
            String delete = createProductActionBody(ChatSteps.REMOVE_FROM_BASKET, product.getId(), product.getId());
            result
                    .addButton(new ViberButton(productActionBody).setRows(1).setColumns(3).setText(product.getName()))
                    .addButton(new ViberButton(delete).setText("Видалити").setRows(1).setColumns(3));
        }
        result.addButton(new ViberButton(changeCountBody).setText("Змінити кількість(" + count + ")")
                .setRows(1).setColumns(6));
        result.addButton(new ViberButton(basketBody).
                setText("Назад").
                setBgColor("").
                setColumns(6).
                setRows(1));
        return result;
    }

    public ViberKeyboard generateStartMenu() {
        ViberKeyboard keyboard = new ViberKeyboard();
        keyboard.setButtonsGroupRows(2).setButtonsGroupColumns(6);
        String showProducts = createProductActionBody(ChatSteps.SHOW_DAYS,
                ChatSteps.SHOW_DAYS);
        keyboard.addButton(new ViberButton(showProducts).setText("Меню").setColumns(3).setRows(1));
        String registration = createProductActionBody(ChatSteps.STEP_CATEGORY_REGISTRATION, ChatSteps.STEP_CATEGORY_REGISTRATION);
        keyboard.addButton(new ViberButton(registration).setText("Авторизация").setColumns(3).setRows(1));
        String connectWithUs = createProductActionBody(ChatSteps.STEP_CONNECT_WITH_US, ChatSteps.STEP_CONNECT_WITH_US);
        keyboard.addButton(new ViberButton(connectWithUs).setText("Зв'язатись з нами").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public ViberKeyboard generateStartMenuForRegisteredUser() {
        ViberKeyboard keyboard = new ViberKeyboard();
        keyboard.setButtonsGroupRows(2).setButtonsGroupColumns(6);
        String showProducts = createProductActionBody(ChatSteps.SHOW_DAYS,
                ChatSteps.SHOW_DAYS);
        keyboard.addButton(new ViberButton(showProducts).setText("Меню").setColumns(3).setRows(1));
        String connectWithUs = createProductActionBody(ChatSteps.STEP_CONNECT_WITH_US, ChatSteps.STEP_CONNECT_WITH_US);
        keyboard.addButton(new ViberButton(connectWithUs).setText("Зв'язатись з нами").setColumns(3).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public RichMedia generateReachMediaBasket(List<Product> basket) {
        RichMedia richMedia = new RichMedia("Carousel example");
        richMedia.setButtonsGroupColumns(CAROUSEL_ITEM_WIDTH).setButtonsGroupRows(CAROUSEL_ITEM_HEIGHT + 1 + 1);
        for (Product content : basket) {
            String image = "/image-not-found.PNG";
            if (content.getPicture() != null) {
                if (content.getPrice() != null) {
                    image = content.getPicture();
                }
            }
            richMedia.addButton(new ViberButton(content.getId()).setActionType(BtnActionType.NONE)
                    .setImage(content.getPicture())
                    .setColumns(CAROUSEL_ITEM_WIDTH).setRows(CAROUSEL_ITEM_HEIGHT));
            richMedia.addButton(new ViberButton(content.getId())
                    .setText("<font color=#323232><b>"
                            + content.getName() + " </b></font>").setActionType(BtnActionType.NONE)
                    .setColumns(CAROUSEL_ITEM_WIDTH).setRows(1)
            );
        }
        return richMedia;
    }

    public ViberKeyboard generateProductRoleKeyboard(List<String> roles, ViberKeyboard keyboard) {
        for (String role : roles) {
            String actionBody = createProductActionBody(ChatSteps.SHOW_PRODUCT_LIST, role);
            keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
            keyboard.addButton(new ViberButton(actionBody).setText(role).setColumns(6)
                    .setRows(1));
            keyboard.setDefaultHeight(false);
        }
        return keyboard;
    }

    public ViberKeyboard addBackButtonKeyboard(ViberKeyboard keyboard) {
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        keyboard.addButton(new ViberButton(ChatSteps.STEP_BACK)
                .setText("Назад").setColumns(6)
                .setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public ViberKeyboard generateOrderButton(ViberKeyboard keyboard, String action) {
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        String actionBody = createProductActionBody(ChatSteps.STEP_CREATE_ORDER, action, ChatSteps.PRESSED_BUTTON);
        keyboard.addButton(new ViberButton(actionBody).setText("Замовити").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public ViberKeyboard addEditBasketButton(ViberKeyboard keyboard, Long basketId) {
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        String actionBody = createProductActionBody(ChatSteps.STEP_EDIT_BASKET, String.valueOf(basketId), "edit");
        keyboard.addButton(new ViberButton(actionBody).setText("Редагувати").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public ViberKeyboard generateConfirmButton(ViberKeyboard keyboard, String action) {
        if (keyboard == null) {
            keyboard = new ViberKeyboard();
        }
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        String actionBody = createProductActionBody(ChatSteps.CONFIRM_DATA, ChatSteps.CONFIRM_DATA, action);
        keyboard.addButton(new ViberButton(actionBody).setText("Підтвердити").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        return keyboard;
    }

    public ViberKeyboard generateEditButton(ViberKeyboard keyboard) {
        if (keyboard == null) {
            keyboard = new ViberKeyboard();
        }
        String actionBody1 = createProductActionBody(ChatSteps.EDIT_INFO, null, ChatSteps.PRESSED_BUTTON);
        keyboard.addButton(new ViberButton(actionBody1).setText("Редагувати").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }


    public ViberKeyboard addBackAndBasketButtonKeyboard(ViberKeyboard keyboard, Long basketId, String day) {
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        String actionBody = createProductActionBody(ChatSteps.STEP_BASKET, basketId.toString(), day);
        String actionBody1 = createProductActionBody(ChatSteps.STEP_BACK, day);
        keyboard.addButton(new ViberButton(actionBody1).setText("<font color=#F0F8FF>Назад</font>")
                .setBgColor("#696969").setColumns(3).setRows(1));
        keyboard.addButton(new ViberButton(actionBody).setText("<font>Корзина</font>")
                .setBgColor("#E9967A").setColumns(3).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    public ViberKeyboard addBasketButtonKeyboard(ViberKeyboard keyboard, String day) {
        if (keyboard == null) {
            keyboard = new ViberKeyboard();
        }
        keyboard.setButtonsGroupColumns(6).setButtonsGroupRows(1);
        String actionBody1 = createProductActionBody(ChatSteps.STEP_BASKET, day);
        keyboard.addButton(new ViberButton(actionBody1).setText("Корзина").setColumns(6).setRows(1));
        keyboard.setDefaultHeight(false);
        return keyboard;
    }

    private String createProductActionBody(String stepName, String paramValue) {
        Step step = new Step();
        step.setStep(stepName);
        step.setProductType(paramValue);
        return gson.toJson(step);
    }

    private String createProductActionBody(String stepName, String paramValue, String action) {
        Step step = new Step();
        step.setStep(stepName);
        step.setProductType(paramValue);
        step.setAction(action);
        return gson.toJson(step);
    }

    public RichMedia createDetailedProductMessage(Product content) {
        RichMedia richMedia = new RichMedia("Carousel example");
        int HEIGHT = 5;
        int WIDTH = 5;
        richMedia.setButtonsGroupColumns(WIDTH).setButtonsGroupRows(HEIGHT + 1 + 1);
        String image = "image-not-found.PNG";
        if (content.getPicture() != null) {
            image = content.getPicture();
        }
        richMedia.addButton(new ViberButton(content.getId())
                .setActionType(BtnActionType.NONE)
                .setImage(image)
                .setColumns(WIDTH).setRows(HEIGHT)
        );
        richMedia.addButton(new ViberButton(content.getId())
                .setText("<font color=#323232><b>" + content.getName() + "</b></font>")
                .setActionType(BtnActionType.NONE)
                .setColumns(WIDTH).setRows(1)
        );
        richMedia.addButton(new ViberButton(content.getId())
                .setText("<font color=#323232><b>" + content.getName() + " </b></font>")
                .setActionType(BtnActionType.NONE)
                .setColumns(WIDTH).setRows(1)
        );
        return richMedia;
    }
}
