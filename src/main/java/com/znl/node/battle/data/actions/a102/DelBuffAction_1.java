package com.znl.node.battle.data.actions.a102;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.skill.skillActions.DelBuffAction;

public class DelBuffAction_1 extends DelBuffAction {

      public DelBuffAction_1() {
           delBuffType = BattleConst.BuffType.Other;
           delBuffSelectType = BattleConst.DelBuffType.SkillSelect;
   }
 }
