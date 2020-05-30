package com.sandy.agorachatsandy.model;


import com.sandy.agorachatsandy.rtm.AgoraChatManager;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtm.RtmMessage;

public class MessageListBean {
    private String accountOther;
    private List<AgoraMessageBean> messageBeanList;

    public MessageListBean(String account, List<AgoraMessageBean> messageBeanList) {
        this.accountOther = account;
        this.messageBeanList = messageBeanList;
    }

    /**
     * Create message list bean from offline messages
     * @param account peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    public MessageListBean(String account, AgoraChatManager chatManager) {
        accountOther = account;
        messageBeanList = new ArrayList<>();

        List<RtmMessage> messageList = chatManager.getAllOfflineMessages(account);
        for (RtmMessage m : messageList) {
            // All offline messages are from peer users
            AgoraMessageBean bean = new AgoraMessageBean(account, m.getText(), false);
            messageBeanList.add(bean);
        }
    }

    public String getAccountOther() {
        return accountOther;
    }

    public void setAccountOther(String accountOther) {
        this.accountOther = accountOther;
    }

    public List<AgoraMessageBean> getMessageBeanList() {
        return messageBeanList;
    }

    public void setMessageBeanList(List<AgoraMessageBean> messageBeanList) {
        this.messageBeanList = messageBeanList;
    }
}
