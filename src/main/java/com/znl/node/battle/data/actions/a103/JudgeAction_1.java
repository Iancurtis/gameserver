package com.znl.node.battle.data.actions.a103;


import com.znl.node.battle.consts.BattleConst;
import com.znl.node.battle.skill.skillActions.JudgeAction;

public class JudgeAction_1 extends JudgeAction {

      public JudgeAction_1() {
      super();
      skillSelectCampType = BattleConst.SkillSelectCampType.EnemyCamp;
      skillSelectRangeType = BattleConst.SkillSelectRangeType.NearestVertical;
      skillDamageRangeType = BattleConst.SkillDamageRangeType.Vertical;
   }
 }
