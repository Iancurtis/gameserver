package com.znl.node.battle.data.skillData;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.data.actions.a102.DamageAction_1;
import com.znl.node.battle.data.actions.a102.JudgeAction_1;
import com.znl.node.battle.skill.Skill;

public class Skill102 extends Skill {

public Skill102() {
      super(); 

    //单体攻击

      skillId = 102;
      skillType = BattleConst.SkillType.Normal;

      addAction(new JudgeAction_1());
      addAction(new DamageAction_1());
    //addAction(new AddBuffAction_1());
    //addAction(new AddBuffAction_2());
    //addAction(new ChangeHPAction_1());
    //addAction(new DelBuffAction_1());
   }
 }
