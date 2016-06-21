package com.znl.node.battle.data.actions.a103;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.skill.skillActions.AddBuffAction;

public class AddBuffAction_1 extends AddBuffAction {

      public AddBuffAction_1() {
          super.buffId = 0;
          super.addBuffType = BattleConst.AddBuffType.Self;
          super.buffType = BattleConst.BuffType.Debuff;
   }

      @Override
      protected boolean canAdd(PuppetEntity target) {
           return super.roll(1000);
   }
 }
