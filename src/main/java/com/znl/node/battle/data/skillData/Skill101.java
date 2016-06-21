package com.znl.node.battle.data.skillData;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.data.actions.a101.DamageAction_1;
import com.znl.node.battle.data.actions.a101.JudgeAction_1;
import com.znl.node.battle.skill.Skill;

public class Skill101 extends Skill {

public Skill101() {
      super(); 

    //横排攻击

      skillId = 101;
      skillType = BattleConst.SkillType.Normal;

      addAction(new JudgeAction_1());
      addAction(new DamageAction_1());
    //addAction(new AddBuffAction_1());
    //addAction(new AddBuffAction_2());
    //addAction(new ChangeHPAction_1());
    //addAction(new DelBuffAction_1());
   }
 }
