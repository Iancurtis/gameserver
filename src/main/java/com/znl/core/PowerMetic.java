package com.znl.core;

import com.znl.define.PlayerPowerDefine;
import com.znl.proxy.GameProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/21.
 */
public class PowerMetic {
    interface Formula {
        Object calc(GameProxy proxy, int power);
    }

    private Map<Integer, Formula> _mapMetic;

    private static PowerMetic metic = null;
    public static PowerMetic getPowerMetic(){
        if (metic == null){
            metic = new PowerMetic();
        }
        return metic;
    }

    private PowerMetic() {
        _mapMetic = new HashMap<>();
        initPowerMetic();
    }

    private void initPowerMetic() {
        Formula formula = null;


        //通用公式
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(power);
            return A;
        };
        _mapMetic.put(100, formula);



        //银两容量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(51);
            long B = proxy.getExpandPowerValueForMetic(63);
            long C = proxy.getExpandPowerValueForMetic(56);
            long D = proxy.getExpandPowerValueForMetic(68);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(101, formula);


        //铁锭容量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(52);
            long B = proxy.getExpandPowerValueForMetic(64);
            long C = proxy.getExpandPowerValueForMetic(56);
            long D = proxy.getExpandPowerValueForMetic(68);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(102, formula);

        //木材容量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(53);
            long B = proxy.getExpandPowerValueForMetic(65);
            long C = proxy.getExpandPowerValueForMetic(56);
            long D = proxy.getExpandPowerValueForMetic(68);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(103, formula);

        //石料容量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(54);
            long B = proxy.getExpandPowerValueForMetic(66);
            long C = proxy.getExpandPowerValueForMetic(56);
            long D = proxy.getExpandPowerValueForMetic(68);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(104, formula);

        //粮食容量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(55);
            long B = proxy.getExpandPowerValueForMetic(67);
            long C = proxy.getExpandPowerValueForMetic(56);
            long D = proxy.getExpandPowerValueForMetic(68);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(105, formula);

        //银两产量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(57);
            long B = proxy.getExpandPowerValueForMetic(69);
            long C = proxy.getExpandPowerValueForMetic(78);
            long D = proxy.getExpandPowerValueForMetic(79);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(106, formula);

        //铁锭产量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(58);
            long B = proxy.getExpandPowerValueForMetic(70);
            long C = proxy.getExpandPowerValueForMetic(78);
            long D = proxy.getExpandPowerValueForMetic(79);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(107, formula);

        //木材产量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(59);
            long B = proxy.getExpandPowerValueForMetic(71);
            long C = proxy.getExpandPowerValueForMetic(78);
            long D = proxy.getExpandPowerValueForMetic(79);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(108, formula);

        //石料产量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(60);
            long B = proxy.getExpandPowerValueForMetic(72);
            long C = proxy.getExpandPowerValueForMetic(78);
            long D = proxy.getExpandPowerValueForMetic(79);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(109, formula);

        //粮食产量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(61);
            long B = proxy.getExpandPowerValueForMetic(73);
            long C = proxy.getExpandPowerValueForMetic(78);
            long D = proxy.getExpandPowerValueForMetic(79);
            return (long)((A + C) * (1 + B/100.0 + D/100.0));
        };
        _mapMetic.put(110, formula);

        //仓库保护量
        formula = (GameProxy proxy, int power) -> {
            long A = proxy.getExpandPowerValueForMetic(62);
            long B = proxy.getExpandPowerValueForMetic(74);
            return (long)(A * (1 + B/100.0));
        };
        _mapMetic.put(111, formula);


    }

    @SuppressWarnings("unchecked")
    public<T> T getMeticValue(int meticId, GameProxy proxy, int power)
    {
//        if(_mapMetic.containsKey(meticId)==false){
//            meticId = 100;
//        }
        Object result = _mapMetic.get(meticId).calc(proxy, power);
        return (T)result;
    }
}
