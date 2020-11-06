package com.heaven.bot;

import com.heaven.bot.viber4j.ViberBot;
import com.heaven.bot.viber4j.ViberBotManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Application {

    private static String yourBotToken;

    private static String yourServerUrl;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        ViberBot viberBot = ViberBotManager.viberBot(yourBotToken);
        viberBot.removeWebHook();
        viberBot.setWebHook(yourServerUrl);
    }
}
