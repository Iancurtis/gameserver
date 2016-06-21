package com.znl.node.battle.entity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.znl.define.SoldierDefine;
import com.znl.proto.M5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.znl.base.BasicBattleAttr;
import com.znl.node.battle.buff.Buff;
import com.znl.node.battle.consts.BattleConst.BuffTriggerType;
import com.znl.node.battle.controller.SkillController;
import com.znl.node.battle.skill.Skill;

public class PuppetEntity extends Entity {
	
	private Logger log = LoggerFactory.getLogger(PuppetEntity.class);
	
	private SkillController _skillController;
	protected Map<Integer, Buff> _buffMap;
	
	private List<M5.Bloods> _roundBloosList = null; //一个回合的血量变化列表，回合开始时需要先清除 TODO
	private List<Integer> _removeBuffList = null; //删除的Buff缓存列表， 在回合开始、回合战斗、回合结束 结算Buff前，都需要清空
	private Map<Integer, List<Integer>> _buffAttrMap = null; //buff属性更改表，每次打包后则需要清空

	public PuppetEntity(BasicBattleAttr attr) {
		super(attr);
		
		_skillController = new SkillController(this);
		_buffMap = new HashMap<>();
		_roundBloosList = new ArrayList<>();
		_removeBuffList = new ArrayList<>();
		_buffAttrMap = new HashMap<>();
	}
	
	@Override
	public void depose() {
		super.depose();
		_skillController.depose();
		
		Iterator<Map.Entry<Integer, Buff>> it = _buffMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Integer, Buff> entry = it.next();
			Buff buff = entry.getValue();
			buff.depose();
		}
		
		_buffMap.clear();
		_roundBloosList.clear();
		_removeBuffList.clear();
		
		_buffMap = null;
		_skillController = null;
		_roundBloosList = null;
		_removeBuffList = null;
	}

	public SkillController getSkillController() {
		return _skillController;
	}
	
	public Skill getCurUseSkill(){
		return _skillController.getCurUseSkill();
	}
	
	//===================
	//addbuff
	public Buff addBuff(int buffId, int casterId, int buffLayer)
	{
		Buff buff = null;
		Class<?> ownerClass;
		try {
			ownerClass = Class
					.forName("com.znl.node.battle.data.buffData.Buff" + buffId);
			
			Constructor<?> c0 = ownerClass.getDeclaredConstructor(new Class[]{PuppetEntity.class});
			c0.setAccessible(true);
			buff = (Buff)c0.newInstance(this);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if( buff == null)
		{
			log.error("========添加的buff不存在=============:" + buffId);
			return null;
		}
		
		
		boolean hasBuff = this.hasBuff(buffId);
		if(hasBuff == true)  //有buff
		{
			removeBuff(buffId);
		}
		else
		{
			
		}
		
		_buffMap.put(buffId, buff);
		buff.onOccur();
		
		return buff;
	}
	//身上有被删除的Buff后，再重新重置一下剩余的buff
	public void otherBuffTrigger()
	{
		List<Buff> buffList = getBuffList();
		for (Buff buff : buffList) {
			buff.onOtherRemoved();
		}
	}
	
	public void onTriggerRemoveBuff(int buffId){
		if(_buffMap.containsKey(buffId) == true ){
			_buffMap.get(buffId).onRemoved();
		}
	}
	
	public void removeBuff(int buffId)
	{
//		_buffMap.remove(buffId);
		
		Iterator<Integer> iterator = _buffMap.keySet().iterator();
		while (iterator.hasNext()) { 
			Integer key = iterator.next();
			if(key.equals(buffId) == true)
			{
				iterator.remove(); 
				_buffMap.remove(buffId);
				_removeBuffList.add(buffId);
			}
		}
		
		this.otherBuffTrigger();
	}
	
	/**
	 * Buff伤害参数，进行伤害计算
	 * @param buffId
	 * @return
	 */
	public int getBuffParam(int buffId)
	{
		boolean hasBuff = hasBuff(buffId);
		if( hasBuff == true )
			return 1;
		
		return 0;
	}
	
	public boolean hasBuff(int buffId)
	{
		return _buffMap.containsKey(buffId);
	}
	
	public boolean hasBuff()
	{
		return _buffMap.size() > 0;
	}
	
	public Buff getBuff(int buffId)
	{
		if(hasBuff(buffId) == false)
			return null;
		
		return _buffMap.get(buffId);
	}
	
	public List<Buff> getBuffList()
	{
		List<Buff> list = new  ArrayList<Buff>(_buffMap.values());  
		return list;
	}
	
	//buff触发
	//caster 来源
	//target 受击者
	public void buffTrigger(BuffTriggerType triggerType, PuppetEntity caster, PuppetEntity target)
	{
		Iterator<Integer> iterator = _buffMap.keySet().iterator();
		while (iterator.hasNext()) { 
			Integer key = iterator.next();
			Buff buff = _buffMap.get(key);
			switch(triggerType){
			case BeHurt:
				buff.onBeHurt(caster);
				break;
			case Hit:
				buff.onHit(target);
				break;
			case AfterBeKilled:
				buff.onAfterBeKilled();
			case Killed:
				buff.onKilled();
		    default:
		    	break;
			}
		}
	}
	
	@Override
	public void setAttrValue(Integer key, Object value) {
		
		super.setAttrValue(key, value);
		
		
		if(isDead() == true)
    	{
			buffTrigger(BuffTriggerType.AfterBeKilled, null, null);
    	}
	}
	
	public void setBuffAttrValue(Integer key, Object value){
		
		if(key >= 1 && key <= 48) //buff属性的改变
    	{
			Object ovalue = super.getAttrValue(key);
			if(ovalue != null){
				int oldValue = super.getAttrValue(key);
	    		int dt = (int)value - oldValue;
	    		
	    		if(dt != 0){
	    			if(_buffAttrMap.containsKey(key)){
//		    			_buffAttrMap.replace(key, dt);
		    		}else{
		    			_buffAttrMap.put(key, new ArrayList<Integer>());
		    		}
	    			
	    			_buffAttrMap.get(key).add(dt);
	    		}
	    		
	    		
//	    		if ( getAttrValue(Define.POWER_CAMP) == Camp.Right && key == 1){
//	    			
//	    			System.err.println("右边BUFF当前血量值得：" + value + " dt:" + dt + " size: " + _buffAttrMap.size());
//	    		}
			}
    	}
		setAttrValue(key, value);
	}
	
	public void addBloos(int delta, int hurtType)
	{
		M5.Bloods.Builder bloodsBuilder = M5.Bloods.newBuilder();
		bloodsBuilder.setDelta(delta);
//		bloodsBuilder.setHp(super.getAttrValue(SoldierDefine.POWER_hp));
		bloodsBuilder.setState(hurtType);
		
		_roundBloosList.add(bloodsBuilder.build());
	}
	
	public void clearBloosList()
	{
		_roundBloosList.clear();
	}
	
	public void clearRemoveBuffList()
	{
		_removeBuffList.clear();
	}
	
	public void clearBuffAttrMap()
	{
		_buffAttrMap.clear();
	}
	
	public List<M5.Bloods> getRoundBloosList()
	{
		return _roundBloosList;
	}

	public List<Integer> getRemoveBuffList() {
		return _removeBuffList;
	}
	
	public Map<Integer, List<Integer>> getBuffAttrMap(){
		return _buffAttrMap;
	}

	public int getBuffAttrMapSize(){
		return _buffAttrMap.size();
	}
	
	public int getSkillFixdam(int skillId){
		return _attr.getSkillFixdam(skillId);
	}

}
