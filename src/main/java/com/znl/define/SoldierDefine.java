package com.znl.define;

import java.util.HashMap;

/**
 * Created by Administrator on 2015/11/6.
 */
public class SoldierDefine {
    //
    public static final int DEFAULT_SOLDIER_TYPE_ID = 101;
    public static final int DEFAULT_SOLDIER_NUM = 100;

    //战斗属性
    /*血量上限*/
    public static final int POWER_hpMax = 1;
    /*血量*/
    public static final int POWER_hp = 2;
    /*攻击*/
    public static final int POWER_atk = 3;
    /*命中率*/
    public static final int POWER_hitRate = 4;
    /*闪避率*/
    public static final int POWER_dodgeRate = 5;
    /*暴击率*/
    public static final int POWER_critRate = 6;
    /*抗暴率*/
    public static final int POWER_defRate = 7;
    /*穿刺*/
    public static final int POWER_wreck = 8;
    /*防护*/
    public static final int POWER_defend = 9;
    /*先手值*/
    public static final int POWER_initiative = 10;
    /*血量百分比*/
    public static final int POWER_hpMaxRate = 11;
    /*攻击百分比*/
    public static final int POWER_atkRate = 12;
    /*步兵血量百分比*/
    public static final int POWER_infantryHpMax = 13;
    /*步兵攻击百分比*/
    public static final int POWER_infantryAtk = 14;
    /*骑兵血量百分比*/
    public static final int POWER_cavalryHpMax = 15;
    /*骑兵攻击百分比*/
    public static final int POWER_cavalryAtk = 16;
    /*枪兵血量百分比*/
    public static final int POWER_pikemanHpMax = 17;
    /*枪兵攻击百分比*/
    public static final int POWER_pikemanAtk = 18;
    /*弓兵血量百分比*/
    public static final int POWER_archerHpMax = 19;
    /*弓兵攻击百分比*/
    public static final int POWER_archerHpatk = 20;
    /*载重*/
    public static final int POWER_load = 21;
    /*载重百分比*/
    public static final int POWER_loadRate = 22;
    /*行军速度加成比*/
    public static final int POWER_speedRate = 23;
    /*PVE伤害加成*/
    public static final int POWER_pveDamAdd = 24;
    /*PVE伤害减免*/
    public static final int POWER_pveDamDer = 25;
    /*PVP伤害加成*/
    public static final int POWER_pvpDamAdd = 26;
    /*PVP伤害减免*/
    public static final int POWER_pvpDamDer = 27;
    /*伤害加成*/
    public static final int POWER_damadd = 28;
    /*伤害减免*/
    public static final int POWER_damder = 29;


    /*总战斗属性数量*/
    public static final int TOTAL_FIGHT_POWER = 29;



    //非战斗属性
    /*兵种*/
    public static final int NOR_POWER_TYPE = 50;
    /*出战位置*/
    public static final int NOR_POWER_INDEX = 51;
    /*出战数量*/
    public static final int NOR_POWER_NUM = 52;
    /*出战数量*/
    public static final int NOR_POWER_ICON = 53;
    /*技能id列表*/
    public static final int NOR_POWER_SKILL = 54;
    /*阵营（友方还是敌方）*/
    public static final int NOR_POWER_CAMP = 55;
    /*名字*/
    public static final int NOR_POWER_NAME = 56;
    /*X*/
    public static final int NOR_POWER_GRIDX = 57;
    /*Y*/
    public static final int NOR_POWER_GRIDY = 58;
    /*BUFF列表*/
    public static final int NOR_POWER_BUFF = 59;
    /*是否晕眩 */
    public static final int NOR_POWER_IS_VERTIGO = 60;
    /*是否沉默 */
    public static final int NOR_POWER_IS_SILENCE = 61;
    /*是否缴械 */
    public static final int NOR_POWER_IS_DISARM = 62;
    /*是否免疫一切DEBUFF */
    public static final int NOR_POWER_IS_IMMUNE = 63;
    /*是否免疫物理伤害 */
    public static final int NOR_POWER_IS_IMMUNE_NORMAL = 64;
    /*是否免疫魔法伤害 */
    public static final int NOR_POWER_IS_IMMUNE_MAGIC = 65;
    /*上一次伤害*/
    public static final int NOR_POWER_LAST_DAMAGE = 66;
    public static final int NOR_POWER_RELATIVEDISGRIDX = 67;
    public static final int NOR_POWER_RELATIVEDISGRIDY = 68;
    /*单只佣兵hp*/
    public static final int NOR_POWER_SOLDIER_TYPE_HP = 69;
    /*单只佣兵攻击*/
    public static final int NOR_POWER_SOLDIER_TYPE_ATK = 70;
    /*克制*/
    public static final int NOR_POWER_SOLDIER_RESTRAIN = 71;
    /*模板id*/
    public static final int NOR_POWER_TYPE_ID = 72;
    /*光环列表*/
    public static final int NOR_POWER_TYPE_AURAS = 73;
    /*攻击计数器*/
    public static final int NOR_POWER_TYPE_ATK_COUNT = 74;
    /*暴击计数器*/
    public static final int NOR_POWER_TYPE_CIRT_COUNT = 75;
    /*被攻击计数器*/
    public static final int NOR_POWER_TYPE_BE_ATKED_COUNT = 76;
    /*闪避计数器*/
    public static final int NOR_POWER_TYPE_DODGE_COUNT = 77;



    public static HashMap<Integer,String> NameMap = new HashMap<>();


    public static final int SOLDIER_TYPE_CAVALRY = 1;//骑兵
    public static final int SOLDIER_TYPE_INFANTRY = 2;//步兵
    public static final int SOLDIER_TYPE_PIKEMAN = 3;//枪兵
    public static final int SOLDIER_TYPE_ARCHER = 4;//弓兵

    public static final int FORMATION_DUNGEO = 1;//副本阵型
    public static final int FORMATION_DEFEND = 2;//防守阵型
    public static final int FORMATION_ARENA = 3;//竞技场阵型

    //军团副本怪物阵型
    public static final int FORMATION_LEGION_DUNGEO = 4;
}
