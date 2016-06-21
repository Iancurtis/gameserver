package com.znl.node.battle.data.actions.a102;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.skill.skillActions.JudgeAction;

public class JudgeAction_1 extends JudgeAction {

      public JudgeAction_1() {
      super();
      skillSelectCampType = BattleConst.SkillSelectCampType.EnemyCamp;
      skillSelectRangeType = BattleConst.SkillSelectRangeType.NearestHorizontally;
      skillDamageRangeType = BattleConst.SkillDamageRangeType.Single;
   }
 }
