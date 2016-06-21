package com.znl.node.battle.message;


public class BattleMessage {
	public enum BattleMessageType{
		START_BATTLE,
		START_ROUND,
		NEXT_ROUND,
		END_BATTLE_VICTORY,
		END_BATTLE_FAILURE,
		BATTLE_ERROR //战斗出错了
	}
	
	
	private final BattleMessageType type;
	private final Object data;

	public BattleMessage(BattleMessageType type, Object data) {
		this.type = type;
		this.data = data;
	}

	
	public BattleMessageType getType() {
		return this.type;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public static BattleMessage valueOf(BattleMessageType type, Object data)
	{
		return new BattleMessage(type, data);
	}
	
	public static BattleMessage valueOf(BattleMessageType type)
	{
		return new BattleMessage(type, null);
	}
}
