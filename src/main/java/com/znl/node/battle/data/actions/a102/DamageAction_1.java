package com.znl.node.battle.data.actions.a102;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.skill.skillActions.DamageAction;

public class DamageAction_1 extends DamageAction {

  public DamageAction_1() {

      super();
      damageType = BattleConst.DamageType.Cannon;
      extra = 0;
 } 

      @Override
          protected double getRatio() {
          double s = 100;
          return s/100;
   }
 }
