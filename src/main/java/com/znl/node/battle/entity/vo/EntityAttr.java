package com.znl.node.battle.entity.vo;

import java.util.HashMap;
import java.util.Map;

public class EntityAttr {
	
	private Map<String, Object> _attrMap;
	public EntityAttr()
	{
		_attrMap = new HashMap<String, Object>();
		
		initAttrMap();
	}
	
	private void initAttrMap()
	{
		_attrMap.put("id", 0);
		_attrMap.put("model", 0);
		_attrMap.put("index", 0);
		_attrMap.put("camp", 0);
		_attrMap.put("level", 0);
		
		_attrMap.put("power", 0);
		_attrMap.put("agility", 0);
		_attrMap.put("intelligence", 0);
		_attrMap.put("power_grow", 0);
		_attrMap.put("agility_grow", 0);
		_attrMap.put("intelligence_grow", 0);
		
		_attrMap.put("hp", 0);
		_attrMap.put("hp_max", 0);
		_attrMap.put("p_atk", 0);
		_attrMap.put("m_atk", 0);
		_attrMap.put("p_def", 0);
		_attrMap.put("m_def", 0);
		_attrMap.put("mp", 0);
		_attrMap.put("mp_max", 0);
		_attrMap.put("atk_spd", 0);
		_attrMap.put("atk_spd_max", 0);
		_attrMap.put("spd", 0);
		
		_attrMap.put("p_critical", 0);
		_attrMap.put("m_critical", 0);
		_attrMap.put("accu", 0);
		_attrMap.put("dodge", 0);
		_attrMap.put("Armor_penet", 0);
		_attrMap.put("Spell_penet", 0);
		_attrMap.put("vimpire_level", 0);
		_attrMap.put("counter_level", 0);
		_attrMap.put("cridam_level", 0);
		
		_attrMap.put("hp_shield", 0);
		_attrMap.put("hp_recover", 0);
		_attrMap.put("mp_recover", 0);
		_attrMap.put("treat_effect", 0);
		_attrMap.put("mark_fell", 0);
		_attrMap.put("dam_add_per", 0);
		_attrMap.put("dam_dec_per", 0);
		
		_attrMap.put("is_vertigo", 0);
		_attrMap.put("is_god", 0);
		_attrMap.put("is_silence", 0);
		_attrMap.put("is_disarm", 0);
		_attrMap.put("is_taunt", 0);
		_attrMap.put("is_immune", 0);
		_attrMap.put("is_charm", 0);
		_attrMap.put("is_dam_miss", 0);

		_attrMap.put("x", 0);
		_attrMap.put("y", 0);
		_attrMap.put("gridX", 0);
		_attrMap.put("gridY", 0);
		_attrMap.put("spawX", 0);
		_attrMap.put("spawY", 0);
		_attrMap.put("entityType", 0);
		
		//----以下是缓存属性
		_attrMap.put("cur_spd", 0);
		_attrMap.put("curRoundCount", 0);
		_attrMap.put("cur_wait_count", 0);
		_attrMap.put("relativeDisGridX", 0);
		_attrMap.put("relativeDisGridY", 0);
	}
	
	public Object getValue(String key)
	{
		return _attrMap.get(key);
	}
	
	public void setValue(String key, Object value)
	{
		_attrMap.replace(key, value);
	}
	
}
