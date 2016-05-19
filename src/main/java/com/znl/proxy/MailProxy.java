package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.proto.M20;
import com.znl.proto.M8;
import com.znl.service.map.WorldTile;
import com.znl.template.MailTemplate;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.log.CustomerLogger;
import com.znl.pojo.db.Mail;
import com.znl.pojo.db.Report;
import com.znl.proto.Common;
import com.znl.proto.M16;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/12/4.
 */
public class MailProxy  extends BasicProxy {

    Set<Mail> mails = new ConcurrentHashSet<>();
    Set<Report> reports = new ConcurrentHashSet<>();
    @Override
    public void shutDownProxy() {
        for (Mail mail : mails){
            mail.finalize();
        }
        for (Report report : reports){
            report.finalize();
        }
    }

    Set<Long> arenaReporttemp=new HashSet<Long>(); //全服战报缓存

    @Override
    protected void init() {

    }

    public MailProxy(Set<Long> mailIds,Set<Long> reportIds,String areaKey){
        this.areaKey = areaKey;
        for (Long id : mailIds){
            Mail mail = BaseDbPojo.get(id, Mail.class,areaKey);
            if (mail != null){
                mails.add(mail);
            }else {
                CustomerLogger.error("出现了空的邮件！！");
            }
        }
        for (Long id : reportIds){
            Report report = BaseDbPojo.get(id, Report.class,areaKey);
            if(report!=null) {
                reports.add(report);
            }else {
                CustomerLogger.error("出现了空的战报！！");
            }
        }
    }



    public long createSendingMail(SimplePlayer simplePlayer,String context,String title){
        Mail mail = BaseDbPojo.create(Mail.class,areaKey);
        mail.setTitle(title);
        mail.setContent(context);
        mail.setCreateMailTime(GameUtils.getServerDate().getTime());
        mail.setType(ChatAndMailDefine.MAIL_TYPE_SEND);
        mail.setReceiverId(simplePlayer.getId());
        mail.setReceiverName(simplePlayer.getName());
        mail.save();
        mails.add(mail);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.addMailToPlayer(mail.getId());
        return mail.getId();
    }

    public long createMail(MailTemplate mailTemplate){
        Mail mail = BaseDbPojo.create(Mail.class,areaKey);
        mail.setContent(mailTemplate.getContext());
        StringBuffer buffer = new StringBuffer();
        for (Integer[] attach : mailTemplate.getAttachments()){
            buffer.append(attach[0]);
            buffer.append(",");
            buffer.append(attach[1]);
            buffer.append(",");
            buffer.append(attach[2]);
            buffer.append("&");
        }
        mail.setAttachmentStr(buffer.toString());
        buffer = new StringBuffer();
        for (Integer rewardId : mailTemplate.getRewards()){
            buffer.append(rewardId);
            buffer.append(",");
        }
        mail.setRewardIdStr(buffer.toString());
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        mail.setPlayerId(playerProxy.getPlayerId());
        mail.setCreateMailTime(GameUtils.getServerDate().getTime());
        mail.setSenderId(mailTemplate.getSenderId());
        mail.setTitle(mailTemplate.getTitle());
        mail.setSenderName(mailTemplate.getSenderName());
        mail.setType(mailTemplate.getType());
        mail.setFriendId(mailTemplate.getFriendId());
        mails.add(mail);
        playerProxy.addMailToPlayer(mail.getId());
        mail.save();
        return mail.getId();
    }

    public int getUnreadMailNum() {
        int num = 0;
        for (Mail mail : mails){
            if (mail.getState() == ChatAndMailDefine.MAIL_STATE_UNREAD){
                num ++;
            }
        }
        return num;
    }

    public List<M16.MailShortInfo> getMailShortInfoList(){
        List<Long> fixDel = new ArrayList<>();
        List<M16.MailShortInfo> res = new ArrayList<>();
        for (Mail mail : mails){
            if (mail.getType() == ChatAndMailDefine.MAIL_TYPE_REPORT){
                if (getReportById(mail.getReportId()) == null){
                    fixDel.add(mail.getId());
                    continue;
                }
                res.add(getReportShortInfo(mail));
            }else {
                res.add(getMailShortInfo(mail));
            }
        }
        if (fixDel.size() > 0){
            deleteMail(fixDel);
        }
        return res;
    }

    public M16.MailShortInfo getReportMailShortInfoByid(long id){
        Mail mail=getMailById(id);
        if (mail.getType() == ChatAndMailDefine.MAIL_TYPE_REPORT) {
            return getReportShortInfo(mail);
        }else{
            return getMailShortInfo(id);
        }
    }

    private M16.MailShortInfo getReportShortInfo(Mail mail) {
        M16.MailShortInfo.Builder builder = M16.MailShortInfo.newBuilder();
        builder.setId(mail.getId());
        builder.setCreateTime((int) (mail.getCreateMailTime() / 1000));
        builder.setState(mail.getState());
        builder.setTitle(mail.getTitle());
        builder.setType(mail.getType());
        Report report = getReportById(mail.getReportId());
        builder.setMailType(report.getReportType());
        builder.setSenderType(0);
        if (report.getReportType() == ReportDefine.REPORT_TYPE_BE_ATTACK){
            builder.setName(report.getAttackerName());
        }else if(report.getReportType() == ReportDefine.REPORT_TYPE_SPY) {
            builder.setName(report.getName());
        }else {
            builder.setName(report.getDefendName());
        }
        builder.setLevel(report.getLevel());
        return builder.build();
    }

    public M16.MailShortInfo getMailShortInfo(long mailId){
        Mail mail = getMailById(mailId);
        return getMailShortInfo(mail);
    }

    private M16.MailShortInfo getMailShortInfo(Mail mail){
        M16.MailShortInfo.Builder builder = M16.MailShortInfo.newBuilder();
        builder.setCreateTime((int) (mail.getCreateMailTime() / 1000));
        builder.setId(mail.getId());
        builder.setState(mail.getState());
        builder.setTitle(mail.getTitle());
        builder.setType(mail.getType());
        if (mail.getSenderId() == 0){
            builder.setSenderType(0);
        }else {
            builder.setSenderType(1);
        }
        if (mail.getType() == ChatAndMailDefine.MAIL_TYPE_SEND){
            builder.setName(mail.getReceiverName());
        }else {
            builder.setName(mail.getSenderName());
        }
        return builder.build();
    }

    private Report getReportById(long id){
        for (Report report : reports){
            if (report.getId() == id){
                return report;
            }
        }
        return null;
    }

    private Mail getMailById(long id){
        for (Mail mail : mails){
            if (mail.getId() == id){
                return mail;
            }
        }
        return null;
    }

    public List<M20.ShortInfos> getServerArenaReportShortInfo(List<Report> list){
        List<M20.ShortInfos> res = new ArrayList<>();
        for (Report report : list){
            res.add(getArenaReoprtShortInfo(report));
            arenaReporttemp.add(report.getId());
        }
        return res;
    }


    //获得新的全服竞技场战报做更新
    public List<M20.ShortInfos> getServerArenaReportShortInfoNew(List<Report> list){
        List<M20.ShortInfos> res = new ArrayList<>();
        for (Report report : list){
            if(!arenaReporttemp.contains(report.getId())) {
                res.add(getArenaReoprtShortInfo(report));
                arenaReporttemp.add(report.getId());
            }
        }
        return res;
    }

    public M20.ShortInfos getReportShinfoByid(long id){
        Report report=getReportById(id);
        if(report==null){
            return null;
        }
        return  getArenaReoprtShortInfo(report);
    }

    public List<M20.ShortInfos> getAllArenaReoprtShortInfo(){
        List<M20.ShortInfos> list = new ArrayList<>();
        for (Report report : reports){
            if (report.getReportType() == ReportDefine.REPORT_TYPE_ARENA){
                list.add(getArenaReoprtShortInfo(report));
            }
        }
        return list;
    }

    public M20.ShortInfos getArenaReoprtShortInfo(Report report){
        M20.ShortInfos.Builder builder = M20.ShortInfos.newBuilder();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long playerId = playerProxy.getPlayerId();
        if (playerId == report.getAttackerId()){
            builder.setType(1);
        }else if(playerId == report.getDefendId()){
            builder.setType(2);
        }
        builder.setAttack(report.getAttackerName());
        builder.setProtect(report.getDefendName());
        builder.setTime((int) (report.getCreateTime()/1000));
        builder.setResult(report.getResult()+1);
        builder.setId(report.getId());
        builder.setIsRead(report.getRead());
        return builder.build();
    }

    public int deleteArenaReport(List<Long> ids){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        StringBuffer sb = new StringBuffer();
        sb.append("已删除战报；");
        for (Long id : ids){
            Report report = getReportById(id);
            if (report.getReportType() != ReportDefine.REPORT_TYPE_ARENA){
                continue;
            }
            reports.remove(report);
            playerProxy.removeReport(id);
            sb.append(id);
            sb.append(",");
            report.del();
        }
        sendFunctionLog(FunctionIdDefine.DELETE_ARENA_REPORT_FUNCTION_ID,playerProxy.getPlayerId(),0,0,sb.toString());
        return 0;
    }

    public M20.PerDetailInfos getArenaReoprtDetailInfo(Report report){
        M20.PerDetailInfos.Builder builder = M20.PerDetailInfos.newBuilder();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long playerId = playerProxy.getPlayerId();
        if (playerId == report.getAttackerId()){
            builder.setType(1);
        }else if(playerId == report.getDefendId()){
            builder.setType(2);
        }else {
            builder.setType(3);
        }
        builder.setTime((int) (report.getCreateTime()/1000));
        builder.setResult(report.getResult()+1);
        builder.setFirst(report.getFirstHand());
        //进攻信息
        M20.PersionInfo.Builder attack = M20.PersionInfo.newBuilder();
        attack.setName(report.getAttackerName());
        attack.setVip(report.getAttackVip());
        attack.setTeam(report.getAttackLegion());
        attack.setBoss("");
        attack.setBossSkill("");
        for (int i=0;i<report.getAttackSoldierNums().size();i++){
            M20.LostInfos.Builder lost = M20.LostInfos.newBuilder();
            lost.setNum(report.getAttackSoldierNums().get(i));
            lost.setTypeid(report.getAttackSoldierTypeIds().get(i));
            attack.addLost(lost.build());
        }
        builder.setAttack(attack);

        //防守信息
        M20.PersionInfo.Builder defend = M20.PersionInfo.newBuilder();
        defend.setName(report.getDefendName());
        defend.setVip(report.getDefendVip());
        defend.setTeam(report.getDefendLegion());
        defend.setBoss("");
        defend.setBossSkill("");
        for (int i=0;i<report.getDefendSoldierNums().size();i++){
            M20.LostInfos.Builder lost = M20.LostInfos.newBuilder();
            lost.setNum(report.getDefendSoldierNums().get(i));
            lost.setTypeid(report.getDefendSoldierTypeIds().get(i));
            defend.addLost(lost.build());
        }
        builder.setProtect(defend);
        List<Common.RewardInfo> rewardInfos = getRewarInfoByString(report.getReward());
        builder.addAllReward(rewardInfos);
        builder.setBattleId(report.getMessageId());
        builder.setId(report.getId());
        if (report.getRead() == 2){
            report.setRead(1);
            report.save();
        }
        return builder.build();
    }

    public boolean isReadReport(Long id){
        Report report = getReportById(id);
        if (report == null){
            return false;
        }
        if (report.getRead() == 2){
            return false;
        }
        return true;
    }

    public M20.PerDetailInfos getArenaReoprtDetailInfoById(Long id){
        Report report = getReportById(id);
        if (report == null){
            return null;
        }
        return getArenaReoprtDetailInfo(report);
    }

    private PlayerReward packetMailReward(Mail mail){
        PlayerReward reward = new PlayerReward();
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (mail.getRewardIdStr() != null && mail.getRewardIdStr().length() > 0){
            String[] rewardStrs = mail.getRewardIdStr().split(",");
            for (String str : rewardStrs){
                Integer rewardId = Integer.parseInt(str);
                rewardProxy.getPlayerReward(rewardId,reward);
            }
        }
        if (mail.getAttachmentStr() != null && mail.getAttachmentStr().length() > 0){
            for (String attachMent: mail.getAttachmentStr().split("&")){
                String[] attStrs = attachMent.split(",");
                rewardProxy.getRewardContent(reward,Integer.parseInt(attStrs[0]),Integer.parseInt(attStrs[1]),Integer.parseInt(attStrs[2]));
            }
        }
        return reward;
    }

    public int getDetalInfo(long id, M16.MailDetalInfo.Builder builder){
        Mail mail = getMailById(id);
        if (mail == null){
            return ErrorCodeDefine.M160001_1;
        }
        if (mail.getType() == ChatAndMailDefine.MAIL_TYPE_REPORT){
            getReportDetalInfo(mail,builder);
            String name = builder.getInfos().getInfoPanel().getName();
            builder.setName(name);
        }else {
            getMailDetalInfo(mail,builder);
        }

        if (mail.getState() == ChatAndMailDefine.MAIL_STATE_UNREAD){
            mail.setState(ChatAndMailDefine.MAIL_STATE_READ);
            mail.save();
        }
        return 0;
    }

    private void getMailDetalInfo(Mail mail, M16.MailDetalInfo.Builder builder) {
        builder.setContext(mail.getContent());
        builder.setCreateTime((int) (mail.getCreateMailTime() / 1000));
        builder.setId(mail.getId());
        builder.setFriendId(mail.getFriendId());
        builder.setTitle(mail.getTitle());
        builder.setType(mail.getType());
        if (mail.getType() == ChatAndMailDefine.MAIL_TYPE_SEND){
            builder.setName(mail.getReceiverName());
            builder.setPlayerId(mail.getReceiverId());
            builder.setSenderType(1);
        }else {
            builder.setName(mail.getSenderName());
            if (mail.getSenderId() == 0){
                builder.setSenderType(0);
            }else {
                builder.setSenderType(1);
            }
        }
        if(mail.getSenderId()>0) {
            builder.setFriendId(mail.getSenderId());
            builder.setPlayerId(mail.getSenderId());
        }
        builder.setExtracted(mail.getExtracted());
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        PlayerReward reward = packetMailReward(mail);
        List<Common.RewardInfo> infos = new ArrayList<>();
        rewardProxy.getRewardInfoByReward(reward,infos);
        builder.addAllReward(infos);
    }

    private void getReportDetalInfo(Mail mail, M16.MailDetalInfo.Builder builder) {
        builder.setCreateTime((int) (mail.getCreateMailTime() / 1000));
        builder.setId(mail.getId());
        builder.setType(mail.getType());
        builder.setSenderType(0);
        builder.setInfos(getReportInfo(mail,builder));
    }

    private M16.Report getReportInfo(Mail mail,M16.MailDetalInfo.Builder mailInfo){
        Report report = getReportById(mail.getReportId());
        M16.Report.Builder builder = M16.Report.newBuilder();
        builder.setMailType(report.getReportType());
        if (report.getDefendId() <= 0){
            builder.setIsPerson(1);
        }else {
            builder.setIsPerson(0);
          /*  if(report.getResourceMapId()>0){
                builder.setIsPerson(1);
            }*/
        }
        if (report.getMessageId() > 0){
            builder.setHaveBattle(1);
            builder.setBattleId(report.getMessageId());
        }else {
            builder.setHaveBattle(0);
        }
        M16.TargetInfo.Builder targetInfo = M16.TargetInfo.newBuilder();
        targetInfo.setName(report.getName());
        targetInfo.setTime((int) (mail.getCreateMailTime() / 1000));
        targetInfo.setPosX(report.getX());
        targetInfo.setPosY(report.getY());
        targetInfo.setPosSoldier(report.getGarrisonName());
        targetInfo.setHonner(report.getHonner());
        targetInfo.setLevel(report.getDefendLevel());
        targetInfo.setResult(report.getResult());
        targetInfo.setAim(report.getAim());
        targetInfo.setLegionName(report.getDefendLegion());
        M16.Resource.Builder resourceInfo = M16.Resource.newBuilder();
        if (report.getReportType() != ReportDefine.REPORT_TYPE_SPY){
            resourceInfo.setType(2);
            M16.fivePos.Builder fiveInfo = M16.fivePos.newBuilder();
            fiveInfo.addAllPosCount(report.getPosResource());
            resourceInfo.setInfo(fiveInfo.build());
            builder.setLostSerPanel(getFightLostSoldiers(report));
            builder.setCityPanel(getCityInfo(report));
            resourceInfo.setMostGet(report.getResourceGet());
        }else {
           // targetInfo.setPosSoldier(report.getDefendName());
            targetInfo.setProsper(report.getDefentCurrBoom());
            targetInfo.setTotalprosper(report.getDefentTotalBoom());

            if (report.getDefendId() <= 0){
                resourceInfo.setType(1);
                resourceInfo.setResourceId(report.getResourceMapId());
            }else {
                resourceInfo.setType(1);
                if (report.getResourceMapId() <= 0){
                    //对方是玩家主城
                    resourceInfo.setType(0);
                }else{
                    resourceInfo.setGet(report.getResourceGet());
                }
                resourceInfo.setResourceId(report.getResourceMapId());
                M16.fivePos.Builder fiveInfo = M16.fivePos.newBuilder();
                fiveInfo.addAllPosCount(report.getPosResource());
                resourceInfo.setInfo(fiveInfo.build());
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                if (playerProxy.getPlayerId() == report.getDefendId()){
                    mailInfo.setPlayerId(report.getAttackerId());
                }else {
                    mailInfo.setPlayerId(report.getDefendId());
                }
            }
            builder.setWatchSerPanel(getSpySoldierInfo(report));
        }
        builder.setInfoPanel(targetInfo.build());
        if(report.getDefentIcon()>0) {
            resourceInfo.setCityIcon(report.getDefentIcon());
          /*  if(report.getDefentIcon()>=50){
                builder.setIsPerson(0);
            }*/
        }
        builder.setResourcePanel(resourceInfo.build());
        builder.addAllReward(getRewarInfoByString(report.getReward()));
        return builder.build();
    }

    private List<Common.RewardInfo> getRewarInfoByString(String rewardStr){
        List<Common.RewardInfo> list = new ArrayList<>();
        if(rewardStr.length() == 0){
            return list;
        }
        for (String str : rewardStr.split("&")){
            String[] rewards = str.split(",");
            Common.RewardInfo.Builder info = Common.RewardInfo.newBuilder();
            info.setPower(Integer.parseInt(rewards[0]));
            info.setTypeid(Integer.parseInt(rewards[1]));
            info.setNum(Integer.parseInt(rewards[2]));
            list.add(info.build());
        }
        return list;
    }

    private M16.CityInfo getCityInfo(Report report) {
        M16.CityInfo.Builder builder = M16.CityInfo.newBuilder();
        builder.setAttackIcon(report.getAttackCityIcon());
        builder.setAttackAddBoom(report.getAttackAddBoom());
        builder.setAttackTotalBoom(report.getAttackTotalBoom());
        builder.setAttackCurrBoom(report.getAttackCurrBoom());
        builder.setDefentAddBoom(report.getDefentAddBoom());
        builder.setDefentTotalBoom(report.getDefentTotalBoom());
        builder.setDefentCurrBoom(report.getDefentCurrBoom());
        builder.setDefentIcon(report.getDefentIcon());
        builder.setDefenPox(report.getDefendX());
        builder.setDefenPoy(report.getDefendY());
        if (report.getReportType() == ReportDefine.REPORT_TYPE_BE_ATTACK){
            builder.setOName(report.getAttackerName());
            builder.setMyName(report.getDefendName());
        }else {
            builder.setOName(report.getDefendName());
            builder.setMyName(report.getAttackerName());
        }
        return builder.build();
    }

    private M16.lostSoldiers getFightLostSoldiers(Report report) {
        //进攻信息
        M16.lostSoldiers.Builder builder = M16.lostSoldiers.newBuilder();
        M16.lostItem.Builder attackItem = M16.lostItem.newBuilder();
        attackItem.setName(report.getAttackerName());
        attackItem.setFtvip(report.getAttackVip());
        attackItem.setFtTeam(report.getAttackLegion());
        attackItem.setFightExp(report.getAttackExp());
        M16.FightItem.Builder attackAdviser = M16.FightItem.newBuilder();
        attackAdviser.setIconId(report.getAttackAdviserIcondId());
        attackAdviser.setName(report.getAttackAdviserName());
        if (report.getAttackAdviserSkillId() > 0){
            attackAdviser.setSkillId(report.getAttackAdviserSkillId());
            attackAdviser.setSkillName(report.getAttackAdviserSkillName());
        }
        attackItem.setFightSr(attackAdviser.build());
        for (int i=0;i<report.getAttackSoldierNums().size();i++){
            M16.PosInfo.Builder info = M16.PosInfo.newBuilder();
            info.setNum(report.getAttackSoldierNums().get(i).intValue());
            info.setTypeid(report.getAttackSoldierTypeIds().get(i).intValue());
            attackItem.addFtLost(info.build());
        }
        builder.setAttackItem(attackItem.build());

        //防守信息
        M16.lostItem.Builder defentItem = M16.lostItem.newBuilder();
        defentItem.setName(report.getDefendName());
        defentItem.setFtvip(report.getDefendVip());
        defentItem.setFtTeam(report.getDefendLegion());
        defentItem.setFightExp(report.getDefendExp());
        M16.FightItem.Builder defendAdviser = M16.FightItem.newBuilder();
        defendAdviser.setIconId(report.getDefendAdviserIcondId());
        defendAdviser.setName(report.getDefendAdviserName());
        if (report.getDefendAdviserSkillId() > 0){
            defendAdviser.setSkillId(report.getDefendAdviserSkillId());
            defendAdviser.setSkillName(report.getDefendAdviserSkillName());
        }
        defentItem.setFightSr(defendAdviser);
        for (int i=0;i<report.getDefendSoldierNums().size();i++){
            M16.PosInfo.Builder info = M16.PosInfo.newBuilder();
            info.setNum(report.getDefendSoldierNums().get(i).intValue());
            info.setTypeid(report.getDefendSoldierTypeIds().get(i).intValue());
            defentItem.addFtLost(info.build());
        }
        builder.setFirstHand(report.getFirstHand());
        builder.setDefentItem(defentItem);
        return builder.build();
    }

    private M16.SolidierInfo getSpySoldierInfo(Report report) {
        M16.SolidierInfo.Builder builder = M16.SolidierInfo.newBuilder();
        for (int i=0;i<6;i++){
            M16.PosInfo.Builder info = M16.PosInfo.newBuilder();
            info.setPost(i+1);
            info.setNum(report.getDefendSoldierNums().get(i).intValue());
            info.setTypeid(report.getDefendSoldierTypeIds().get(i).intValue());
            builder.addInfo(info.build());
        }
        return builder.build();
    }

    public int deleteMail(List<Long> ids) {
        List<Long> reportIds = new ArrayList<>();
        StringBuffer sb = new StringBuffer();

        for (Long id : ids){
            Mail mail = getMailById(id);
            if (mail != null){
                mail.del();
                sb.append(id);
                sb.append(",");
                mails.remove(mail);
                if (mail.getReportId() > 0){
                    reportIds.add(mail.getReportId());
                }
            }
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.removeMailToPlayer(ids);
        for (Long id : reportIds){
            Report report = getReportById(id);
            if (report != null){
                report.del();
                sb.append(id);
                sb.append(",");
                reports.remove(report);
            }
            playerProxy.removeReport(id);
        }
        sendFunctionLog(FunctionIdDefine.DELETE_MAIL_FUNCTION_ID,0,0,0,sb.toString());
        return 0;
    }

    public Long createReport(Report report) {
        Mail mail = BaseDbPojo.create(Mail.class,areaKey);
        mail.setType(ChatAndMailDefine.MAIL_TYPE_REPORT);
        mail.setReportId(report.getId());
        mail.setState(ChatAndMailDefine.MAIL_STATE_UNREAD);
        mail.setCreateMailTime(GameUtils.getServerDate().getTime());
        mail.setSenderName(report.getName());
        mail.save();
        mails.add(mail);
        reports.add(report);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.addMailToPlayer(mail.getId());
        return mail.getId();
    }

    public Set<Mail> getMailByType(int type){
        Set<Mail> res = new HashSet<>();
        for (Mail mail : mails){
            if (mail.getType() == type){
                res.add(mail);
            }
        }
        return res;
    }

    public boolean checkMailFullHandle() {
        boolean rs = false;
        for (int type=ChatAndMailDefine.MAIL_TYPE_SYSTEM;type<=ChatAndMailDefine.MAIL_TYPE_REPORT;type++){
            Set<Mail> mailTypes = getMailByType(type);
            List<Long> deleteIds = new ArrayList<>();
                while (mailTypes.size() > ChatAndMailDefine.MAIL_SIZE){
                    //获取最古老的邮件
                    Mail oldestMail = null;
                    for (Mail mail : mailTypes){
                        if (oldestMail == null || mail.getCreateMailTime() < oldestMail.getCreateMailTime()){
                            oldestMail = mail;
                        }
                    }
                    deleteIds.add(oldestMail.getId());
                    mailTypes.remove(oldestMail);
                }
            //删除邮件
            if (deleteIds.size() > 0){
                deleteMail(deleteIds);
                rs = true;
            }
        }
        return rs;
    }

    public long getBattleId(long mailId) {
        Mail mail = getMailById(mailId);
        if (mail == null){
            return ErrorCodeDefine.M160005_1;
        }
        if (mail.getType() != ChatAndMailDefine.MAIL_TYPE_REPORT || mail.getReportId() <= 0){
            return ErrorCodeDefine.M160005_2;
        }
        Report report = getReportById(mail.getReportId());
        if (report.getMessageId()<=0){
            return ErrorCodeDefine.M160005_3;
        }
        return report.getMessageId();
    }

    public int extractMail(long mailId, PlayerReward reward) {
        Mail mail = getMailById(mailId);
        if (mail.getType() != ChatAndMailDefine.MAIL_TYPE_SYSTEM){
            return ErrorCodeDefine.M160006_1;
        }
        if (mail.getAttachmentStr().length() == 0 && mail.getRewardIdStr().length() == 0){
            return ErrorCodeDefine.M160006_1;
        }
        if (mail.getExtracted() == ChatAndMailDefine.MAIL_HAVE_EXTRACT){
            return ErrorCodeDefine.M160006_2;
        }
        StringBuffer sb = new StringBuffer();
        EquipProxy equipProxy=getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (mail.getAttachmentStr().length() > 0){
            for (String att : mail.getAttachmentStr().split("&")){
                try {
                    String[] str = att.split(",");
                    int power = Integer.parseInt(str[0]);
                    int typeId = Integer.parseInt(str[1]);
                    int value = Integer.parseInt(str[2]);
                    rewardProxy.getRewardContent(reward,power,typeId,value);
                }catch (Exception e){
                    CustomerLogger.error("解析邮件附件的时候出错",e);
                }
            }
        }
        if (mail.getRewardIdStr().length() > 0){
            for (String id : mail.getRewardIdStr().split(",")){
                rewardProxy.getPlayerReward(Integer.parseInt(id),reward);
                sb.append(Integer.parseInt(id));
                sb.append(",");
            }
        }
        if(equipProxy.getEquipBagLesFree()<reward.generalMap.size()){
            return ErrorCodeDefine.M160006_3;
        }
        AdviserProxy adviserProxy=getGameProxy().getProxy(ActorDefine.ADVISER_PROXY_NAME);
        if(gerewardAdvisernum(reward)+adviserProxy.getAdviserMaxNum()>EquipDefine.ADVISER_JUNSHIFU_MAXNUM){
            return ErrorCodeDefine.M160006_4;
        }
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_MAIL);
        mail.setExtracted(ChatAndMailDefine.MAIL_HAVE_EXTRACT);
        mail.save();
        sendFunctionLog(FunctionIdDefine.EXTRACT_MAIL_FUNCTION_ID,mailId,0,0,sb.toString());
        return 0;
    }

    //获得奖励军师的数量
    public int gerewardAdvisernum(PlayerReward reward){
        int n=0;
        for(int num:reward.counsellorMap.values()){
           n+=num;
        }
        return n;
    }

    public int unReadMail(){
        int num=0;
        for(Mail mail:mails){
            if(mail.getState()==ChatAndMailDefine.MAIL_STATE_UNREAD){
                num++;
            }
        }
        return num;
    }

    public Long createArenaReport(Report report) {
        reports.add(report);
        return report.getId();
    }


}
