package com.znl.node.battle.data.buffData;

import java.util.ArrayList;
import java.util.List;

import com.znl.define.SoldierDefine;
import com.znl.node.battle.buff.Buff;
import com.znl.node.battle.consts.BattleConst.BuffTickType;
import com.znl.node.battle.consts.BattleConst.BuffType;
import com.znl.node.battle.entity.PuppetEntity;

public class Buff1012 extends Buff {

    public Buff1012 (PuppetEntity role) {
       super(role);//增加己方所有部队10%伤害

       super.id = 1012;
       super.iconId = 0;
       super.lastRound = 100;
       tickType = BuffTickType.RoundEnd;
}

   @Override
   public void onOccur() {
           int value = role.getAttrValue(SoldierDefine.POWER_damadd);
           super.modifyAttr(SoldierDefine.POWER_damadd, value+1000, false, 0);
           super.onOccur();}

   @Override
   public void onRoundTick() {
           
           super.onRoundTick();
   }

 }
