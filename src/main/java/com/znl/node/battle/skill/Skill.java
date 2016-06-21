package com.znl.node.battle.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.znl.pub.LinkList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.znl.node.battle.consts.BattleConst.SkillType;
import com.znl.define.BattleDefine;
import com.znl.node.battle.entity.PuppetEntity;
import com.znl.node.battle.factory.EntityFactory;
import com.znl.node.battle.interfaces.IDepose;
import com.znl.node.battle.skill.skillActions.SkillAction;

public class Skill implements IDepose, Comparable{
	
	private Logger log = LoggerFactory.getLogger(Skill.class);
	
	public JSONObject skillDefine;
	public int skillShowId;
	protected int skillId;

	protected SkillType skillType = SkillType.Normal;
	protected int expendMp = 0; //技能花费的MP值
	
	protected int maxCoolingRound = 1;  //技能的冷却回合
	
	private PuppetEntity _owner;
	private LinkList<SkillAction> _skillActionLink;
	private LinkList<SkillAction>.Node _curSkillNode;
	
	private List<PuppetEntity> _targets = null;
	private EntityFactory _factory;
	private Map<Short, Object> cacheMap = null;
	
	private int totalNum = 0;
	private int curUseCount = 0;
	
	private boolean isCooling = false; //技能是否处于冷却中，true则不能释放
	private int curCoolingRound = 0; //当前冷却回合
	private int useCount = 0; //使用次数，通过这个次数才进行动态排序
	private int index = 0; //技能顺序
	
	public int getUseCount() {
		return useCount;
	}

	public Skill()
	{
		_skillActionLink = new LinkList<SkillAction>();
		_targets = new ArrayList<PuppetEntity>();
		
		cacheMap = new HashMap<Short, Object>();
	}
	@Override
	public String toString(){
		return ""+skillId;
	}
	@Override
	public void depose() {
		skillType = null;
		_owner = null;
		
		_skillActionLink = null; //TODO
		_curSkillNode = null;
		_targets.clear();
		_targets = null;
		cacheMap.clear();
		cacheMap = null;
		
		_factory = null;
	}
	
	@Override
	public int compareTo(Object arg0) {
		Skill skill = (Skill)arg0;
		
		return useCount > skill.getUseCount() ? 1 : (useCount == skill.getUseCount() ? 0 : -1 );
	}

	public void init(PuppetEntity owner)
	{
		_owner = owner;
	}
	
	public void use()
	{
		useCount++;
		curCoolingRound = maxCoolingRound;
		onEnter();
	}
	
	public void reset()
	{
		curCoolingRound = 0;
		cacheMap.clear();
		_targets.clear();
	}
	
	//处理技能的冷却回合
	public void endRound(){
		curCoolingRound--;
		if(curCoolingRound < 0){
			curCoolingRound = 0;
		}
	}
	
	public void coolingRound(){
		curCoolingRound--;
		if(curCoolingRound < 0){
			curCoolingRound = 0;
		}
	}
	
	
	protected void addAction(SkillAction action)
	{
		action.setCacheMap(cacheMap);
		_skillActionLink.addNode(action);
	}
	
	public void insertAction(SkillAction action)
	{
		_skillActionLink.insertNode(_curSkillNode, action);
	}
	
	public void nextAction()
	{
	
		_curSkillNode = _curSkillNode.getNextNode();
		if(_curSkillNode != null)
		{
			onEnterAction(_curSkillNode.getValue());
		}
		else
		{
			log.info("===技能action完结===");
		}
	}
	
	private void onEnter()
	{
		_curSkillNode =  _skillActionLink.getRootNode();
		onEnterAction(_curSkillNode.getValue());
	}
	
	private void onEnterAction(SkillAction action)
	{
		if(action.getCaster() == null)
		{
			action.setCaster(_owner);
		}
		action.onEnter(this);
		
		List<PuppetEntity> list = action.getCacheData(BattleDefine.ACTION_CACHE_TARGETS);
		if(list != null)
		{
			for (PuppetEntity puppetEntity : list) {
				if(_targets.indexOf(puppetEntity) < 0){
					_targets.add(puppetEntity);
				}
			}
		}
	}
	
	
	
	//===========================
	
	public PuppetEntity getOwner() 
	{
		return _owner;
	}

	public List<PuppetEntity> getTargets() {
		return _targets;
	}

	public EntityFactory getFactory() {
		return _factory;
	}

	public void setFactory(EntityFactory factory) {
		this._factory = factory;
	}
	
	public int getSkillId()
	{
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}

	public SkillType getSkillType() {
		return skillType;
	}

	public void setSkillType(SkillType skillType) {
		this.skillType = skillType;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getCurUseCount() {
		return curUseCount;
	}

	public void setCurUseCount(int curUseCount) {
		this.curUseCount = curUseCount;
	}

	public boolean isCooling() {
		isCooling = curCoolingRound > 0;
		return isCooling;
	}

	public int getCurCoolingRound() {
		return curCoolingRound;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setMaxCoolingRound(int maxCoolingRound) {
		this.maxCoolingRound = maxCoolingRound;
	}
	
	public int getSkillFixdam(){
		return _owner.getSkillFixdam(skillId);
	}

}
