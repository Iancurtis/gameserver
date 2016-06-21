package com.znl.template;

import com.znl.proto.M5;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/7.
 */
public class MailTemplate {
    private String title;
    private String context;
    private long senderId;
    private String senderName;
    private int type;
    private List<Integer> rewards = new ArrayList<>();
    private List<Integer[]> attachments = new ArrayList<>();
    private M5.M50000.S2C mess;
    private long battlePackId;
    private long friendId;
    public MailTemplate(String title, String context, long senderId, String senderName, int type) {
        this.title = title;
        this.context = context;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;

    }

    public MailTemplate(String title, String context, long senderId, String senderName, int type, long friendId) {
        this.title = title;
        this.context = context;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.friendId = friendId;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getRewards() {
        return rewards;
    }

    public void setRewards(List<Integer> rewards) {
        this.rewards = rewards;
    }

    public List<Integer[]> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Integer[]> attachments) {
        this.attachments = attachments;
    }

    public M5.M50000.S2C getMess() {
        return mess;
    }

    public void setMess(M5.M50000.S2C mess) {
        this.mess = mess;
    }

    public Long getBattlePackId() {
        return battlePackId;
    }

    public void setBattlePackId(Long battlePackId) {
        this.battlePackId = battlePackId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
