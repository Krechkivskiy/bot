package com.heaven.bot.viber4j;

import java.util.List;

import com.heaven.bot.viber4j.account.AccountInfo;
import com.heaven.bot.viber4j.account.UserDetails;
import com.heaven.bot.viber4j.account.UserOnline;
import com.heaven.bot.viber4j.outgoing.Outgoing;

/**
 * @author n.zvyagintsev
 */
public interface ViberBot {

    /**
     * Registers endpoint for receiving messages from the Viber
     *
     * @param webHookUrl endpoint for receiving messages from the Viber.
     * @return post message result
     */
    boolean setWebHook(String webHookUrl);

    boolean setWebHook(String webHookUrl, List<CallbackEvent> events);

    boolean removeWebHook();

    Outgoing messageForUser(String receiverId);

    Outgoing broadcastMessage(List<String> receiverIds);

    Outgoing publicMessage(String fromId);

    AccountInfo getAccountInfo();

    UserDetails getUserDetails(String userId);

    List<UserOnline> getUserOnline(List<String> receiverIds);

    enum CallbackEvent {
        delivered, seen, failed, subscribed, unsubscribed, conversation_started
    }
}
