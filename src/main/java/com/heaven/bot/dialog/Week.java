package com.heaven.bot.dialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Week {
    private static List<String> days = new ArrayList<>();

    public static List<String> getDaysList() {
        if (days.size() == 0) {
            days.add("Понеділок");
            days.add("Вівторок");
            days.add("Середа");
            days.add("Четвер");
            days.add("Пятниця");
            days.add("Cубота");
        }
        return generateDayList();
    }

    private static List<String> generateDayList() {
        int firstListDay = 0;
        int currentDay = LocalDate.now().getDayOfWeek().getValue();
        if (LocalDateTime.now().getHour() < 19) {
            if (currentDay < 6) {
                firstListDay = currentDay;
            } else {
                firstListDay = 0;
            }
        } else {
            if (currentDay < 5) {
                firstListDay = currentDay + 1;
            } else if (currentDay == 7) {
                firstListDay = 1;
            } else {
                firstListDay = 0;
            }
        }
        List<String> result;
        if (firstListDay != 0) {
            result = new ArrayList<>(days.subList(firstListDay, days.size()));
            result.addAll(days.subList(0, firstListDay));
            return result;
        } else {
            return days;
        }
    }
}
