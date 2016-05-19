package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2015/10/29.
 */
public class Mail extends BaseDbPojo {
    private long playerId = 0l;
    private String title = "";
    private String content = "";
    private String rewardIdStr = "";
    private String attachmentStr = "";
    private long senderId = 0l;
    private String senderName = "";
    private long createMailTime = 0l;
    private int type = 0;
    private int state = 0;
    private long receiverId = 0l;
    private String receiverName = "";
    private long reportId = 0l;
    private int extracted = 0;
    private long friendId = 0l;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRewardIdStr() {
        return rewardIdStr;
    }

    public void setRewardIdStr(String rewardIdStr) {
        this.rewardIdStr = rewardIdStr;
    }

    public String getAttachmentStr() {
        return attachmentStr;
    }

    public void setAttachmentStr(String attachmentStr) {
        this.attachmentStr = attachmentStr;
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

    public long getCreateMailTime() {
        return createMailTime;
    }

    public void setCreateMailTime(long createMailTime) {
        this.createMailTime = createMailTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public int getExtracted() {
        return extracted;
    }

    public void setExtracted(int extracted) {
        this.extracted = extracted;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }
}
