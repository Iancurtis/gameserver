package com.znl.node.battle.data.buffData;

import java.util.ArrayList;
import java.util.List;

import com.znl.define.SoldierDefine;
import com.znl.node.battle.buff.Buff;
import com.znl.node.battle.consts.BattleConst.BuffTickType;
import com.znl.node.battle.consts.BattleConst.BuffType;
import com.znl.node.battle.entity.PuppetEntity;


public class Buff101 extends Buff {

    public Buff101 (PuppetEntity role) {
       super(role);//3回合减伤40%

       super.id = 101;
       buffType = BuffType.Buff;
       super.lastRound = 100;
       tickType = BuffTickType.RoundEnd;
}

   @Override
   public void onOccur() {
          int value = role.getAttrValue(SoldierDefine.POWER_hitRate);
          super.modifyAttr(SoldierDefine.POWER_hitRate, value+400, true, value);
          super.onOccur();}

   @Override
   public void onRoundTick() {
           
           super.onRoundTick();
   }
   
   @Override
   public void onOtherRemoved() {

       super.onOtherRemoved();
   }
   
   

 }
