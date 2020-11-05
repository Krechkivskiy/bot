package com.heaven.bot.viber4j.keyboard;

import com.heaven.bot.viber4j.outgoing.MessageType;

public class RichMedia extends ButtonContainer {

    public RichMedia() {
        this.type = MessageType.CAROUSEL.getKeyName();
    }

    public RichMedia(String altText) {
        this.type = MessageType.CAROUSEL.getKeyName();
        this.altText = altText;
    }
}
