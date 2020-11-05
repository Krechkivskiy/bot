package com.heaven.bot;

import com.heaven.bot.viber4j.ViberBot;
import com.heaven.bot.viber4j.ViberBotManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        ViberBot viberBot = ViberBotManager.viberBot("4c367b1d49800ecf-26826b1517659043-635e852722e03961");
        viberBot.removeWebHook();
        viberBot.setWebHook("https://23465f24e2c3.ngrok.io/");
    }
}
