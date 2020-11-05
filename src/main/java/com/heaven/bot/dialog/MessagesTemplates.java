package com.heaven.bot.dialog;

public class MessagesTemplates {

    private static final String FINISH_ORDER = "Дякуємо за довіру, твоє замовлення буде доставлено в insert з 12:00-15:00"
            + " Приємного дня, чекаю на тебе знову";

    public static final String GREETING = "" +
            "Вітаю тебе в онлайн-сервісі доставки їжі\n" +
            "Тут ти можеш легко вибрати улюблену їжу та замовити доставку\n" +
            "Для того, щоб залишитись з нами тисни - «приєднатись»\n" +
            "Ми працюємо для тебе з Понеділка по Суботу\uD83D\uDE0A\n" +
            "Доставка обідів здійснюється з 12:00-15:00\n" +
            "Замовлення приймаємо до 19:00 на наступний день\n" +
            "Ти маєш можливість харчуватись кожного дня смачною та корисною іжею\n" +
            "Для зв’язку з нами телефонуй - +38063-762-04-37\n";

    public static final String NON_FULLY_BASKET_COMPLETED = "Схоже, для замовлення тобі не вистачає чогось\n" +
            "\n" +
            "В комплексі має бути : «основна страва, суп та салат»\n" +
            "\n";

    public static String getFinishOrderMessage(String insert) {
        String toReplace = "insert";
        switch (insert) {
            case "Середа":
                return FINISH_ORDER.replace(toReplace, "Середу");
            case "Пятниця":
                return FINISH_ORDER.replace(toReplace, "П'ятницю");
            case "Cубота":
                return FINISH_ORDER.replace(toReplace, "Суботу");
            default:
                return FINISH_ORDER.replace(toReplace, insert);
        }
    }
}
