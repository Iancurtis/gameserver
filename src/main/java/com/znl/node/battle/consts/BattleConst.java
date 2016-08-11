package com.znl.node.battle.consts;

public class BattleConst {
	
	/** 阵营*/
	public enum Camp{
		/**己方阵营*/
		Left,  
		/**敌方阵营 */
		Right
	}
	
	/** 技能类型*/
	public enum SkillType{
		/**普通攻击*/
		Normal,  
		/** 技能攻击 */
		Magic
	}
	
	/** 伤害类型*/
	public enum DamageType{
		/**机枪伤害 物理伤害*/
		Cannon,  
		/** 火炮伤害 魔法伤害*/
		Missile
	}

	/** 技能范围类型*/
	public enum SkillSelectRangeType{
		/** 全体*/
		All,
		/** 最近一竖排*/
		NearestVertical,
		/** 最近一横排*/
		NearestHorizontally,
		/** 最远一竖排*/
		FurthestVertical,
		/** 最远一横排*/
		FurthestHorizontally,
		/** 自身*/
		Self
	}

	/** 技能伤害范围类型*/
	public enum SkillDamageRangeType{
		/** 全体*/
		All,
		/** 单个*/
		Single,
		/** 竖排*/
		Vertical,
		/** 横排*/
		Horizontally
	}

	/** 技能选择类型*/
	public enum SkillSelectCampType{
		/** 敌方阵营*/
		EnemyCamp,
		/** 友方阵营*/
		FriendCamp
	}
	
	public enum HurtType{
		NormalHurt(1),
		MagicHurt(2),
		CritHurt(3),
		DodgeHurt(4),
		AddHpHurt(5),
		ImmuneHurt(6),
		RefrainHurt(7),
		OtherHurt(8);
		
		private int value = 0;
		
		private HurtType(int value){
			this.value = value;
		}
		
		public int value(){
			return this.value;
		}
	}
	
	/**添加buff类型*/
	public enum AddBuffType{  
		/**技能选择目标*/
		SkillSelect, 
		/**自身*/
		Self,  
		/**己方全体*/
		SelfAll 
	}
	
	/**删除buff类型*/
	public enum DelBuffType{  
		/**技能选择目标*/
		SkillSelect, 
		/**自身*/
		Self,  
		/**己方全体*/
		SelfAll 
	}
	
	/**buff类型 debuff or buff*/
	public enum BuffType{  
		/**增益*/
		Buff, 
		/**减益*/
		Debuff,  
		Other
	}
	
	/**Buff回合触发类型*/
	public enum BuffTickType{
		/**回合开始触发计数*/
		RoundStart,
		/**回合结束触发计数*/
		RoundEnd
	}
	
	/**Buff触发类型*/
	public enum BuffTriggerType{
		/**打击时触发*/
		Hit,
		/**受击时触发*/
		BeHurt,
		/**被杀时触发*/
		AfterBeKilled,
		Killed
	}
}



