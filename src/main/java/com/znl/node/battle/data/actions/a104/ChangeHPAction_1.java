package com.znl.node.battle.data.actions.a104;


import com.znl.define.SoldierDefine;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.skill.skillActions.ChangeHPAction;

public class ChangeHPAction_1 extends ChangeHPAction {

      @Override
      public int changeHP(PuppetEntity caster, PuppetEntity target)  {
          int hp = 0;
          int forceTotal = target.getAttrValue(SoldierDefine.POWER_hpMax);
          hp = (10*forceTotal)/100;    
          return hp;
   }
 }
