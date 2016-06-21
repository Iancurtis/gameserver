import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.znl.define.DataDefine;
import com.znl.define.PlayerPowerDefine;
import com.znl.framework.socket.Request;
//import com.znl.log.admin.tbllog_player;
//import com.znl.log.admin.tbllog_player;
import com.znl.pojo.db.Player;
import com.znl.proxy.ConfigDataProxy;
import com.znl.proxy.ScriptProxy;
import com.znl.service.map.TileType;
import com.znl.utils.CreateClass;
import com.znl.utils.RandomEmitter;
import com.znl.utils.ZipUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;

import javax.script.Invocable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Administrator on 2015/10/16.
 */
public class TestMethods {

    @Test
    public void testPerJson(){
        ArrayList<JSONObject> config = ConfigDataProxy.getConfigData(DataDefine.EQUIP_POWER, false);



//        ArrayList<JSONObject> ilist2 = new ArrayList<>();
//        config.forEach( v -> {
//            ilist2.add(v);
//        });


        Long time = System.currentTimeMillis();
//        for (int i = 0; i < 1; i++) {
//            config.forEach( obj -> {
////                obj.get("ID");
//            });

//        int size = config.size();
//        for (int i = 0; i < size; i++) {
//            JSONObject obj = config.get(i);
//            obj.get("ID");
//        }
        for (int i = 0; i < 1000; i++) {
            JSONObject obj = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", 1, "lv", 20);
        }
//            ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_POWER, 1); //"quality", 1, "lv", 20
//        }

        System.out.println(System.currentTimeMillis() - time);

        ArrayList<JSONObject> ilist = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            JSONObject json = new JSONObject();
            json.put("ID", 1);
            json.put("lv1", 1);
            json.put("lv2", 1);
            json.put("lv3", 1);
            json.put("lv4", 1);
            json.put("lv5", 1);
            json.put("lv6", 1);
            json.put("lv7", 1);
            json.put("lv8", 1);
            ilist.add(json);
        }
        time = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            ilist.forEach( il -> {

            });
        }


        System.out.println(System.currentTimeMillis() - time);

    }
//    @Test
//    public void testRedis(){
//        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
//        jedisClusterNodes.add(new HostAndPort("192.168.10.190", 7001));
//        JedisCluster jc = new JedisCluster(jedisClusterNodes);
//    }

    private Connection getDbConnection(){
        String mysql_ip = "192.168.10.190";
        String mysql_db = "log_android_cn_s9994";
        String mysql_user = "root";
        String mysql_pwd = "AAxx0011!!@@#~~~~~~~~~~~~~~~~~~~~~~~~~";
        try {
            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass("com.mysql.jdbc.Driver");
            cpds.setJdbcUrl(String.format("jdbc:mysql://%s/%s",mysql_ip, mysql_db));
            cpds.setUser(mysql_user);
            cpds.setPassword(mysql_pwd);

            cpds.setMinPoolSize(5);
            cpds.setAcquireIncrement(5);
            cpds.setMaxPoolSize(20);

            return cpds.getConnection();
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

//    @Test
    public void exportDBClass(){

        try {
            ArrayList<String> tableList = new ArrayList<String>();
            Connection conn = getDbConnection();
            DatabaseMetaData dmd = (DatabaseMetaData) conn.getMetaData();
            ResultSet rs=dmd.getTables(null, null, "%", null);
            while(rs.next()){
                tableList.add(rs.getString("TABLE_NAME"));
            }

//            CreateClass create = new CreateClass();
//            create.setConnection(getDbConnection());
//            create.setStrpackage("com.znl.log.admin");
//            create.setTableName("tbllog_online");
//            create.execute();

            tableList.forEach( tableName -> {
                CreateClass create = new CreateClass();
                create.setConnection(getDbConnection());
                create.setStrpackage("com.znl.log.admin");
                create.setTableName(tableName);
                create.execute();
            });


        }catch (Exception e){

        }


    }
//    @Test
//    public void testDBLog(){
//        tbllog_player tbllog_player = new tbllog_player();
////        tbllog_player.setLogTime(GameUtils.getServerTime());
////        tbllog_player.setLogType("tbllog_player");
//        tbllog_player.setPlatform("3k");
//        tbllog_player.setDid("sdfdsf");
//        JSONObject json = new JSONObject(tbllog_player);
////        System.out.println(json.keys());
//        Iterator<String> iter = json.keys();
//
//        String sql = "insert into tbllog_player set ";
//        while (iter.hasNext()){
//            String key = iter.next();
//            Object value = json.get(key);
//            if(value instanceof String){
////                System.out.println(key + " string: " + json.get(key));
//                if(iter.hasNext()){
//                    sql = sql + String.format("%s='%s', ", key, value);
//                }else{
//                    sql = sql + String.format("%s='%s'", key, value);
//                }
//
//            }else{
////                System.out.println(key + " int: " + json.get(key));
//
//                if(iter.hasNext()){
//                    sql = sql + String.format("%s=%d, ", key, json.getLong(key));
//                }else {
//                    sql = sql + String.format("%s=%d ", key, json.getLong(key));
//                }
//            }
//
//        }
//        sql = sql + ";";
//        System.out.println(sql);
//
//        try {
//            Connection conn = getDbConnection();
//            Statement statement = conn.createStatement();
//            statement.addBatch(sql);
//
//            statement.executeBatch();
//            statement.close();
//            conn.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//    }
//    @Test
    public void testUUID(){
        for (int i = 0; i < 10; i++) {
            long id = UUID.randomUUID().getMostSignificantBits();
            System.out.println(id);

        }

        long l = 9088695328979372755L;


    }

//    @Test
    public void testZip(){
        String zipStr = ZipUtils.gzip("hellollllllllllll2");
        String uzipStr = ZipUtils.gunzip(zipStr);

        System.out.println(uzipStr);
    }

//    @Test
    public void testBase64(){
//        byte[] bytes = Base64.getDecoder().decode("hello");
//        String encode = Base64.getEncoder().encodeToString(bytes);

        try {

//            byte[] bytes = com.znl.framework.socket.websocket.Base64.decode("hello");
//            String encode = com.znl.framework.socket.websocket.Base64.encodeBytes(bytes);

            byte[] bytes = {1,2,3};
            String encode = com.znl.framework.socket.websocket.Base64.encodeBytes(bytes);
            byte[] bytes2 = com.znl.framework.socket.websocket.Base64.decode(encode);

            System.out.println(bytes2);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(encode);
    }

//    @Test
    public void testMapJson(){
//        Map<String, Object> map = new HashMap<>();
//        map.put("1", "2");
//
//        JSONObject object = new JSONObject(map);
//        System.out.println(object.toString());
        ConfigDataProxy.loadAllConfig();
        JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MONSTER_GROUP, "ID", 2048);
        JSONArray ary = info.optJSONArray("position1");

        System.out.println(ary.get(0));
    }

//    @Test
    public void test(){
        System.out.println("hello world");
//        Player player = new Player();
//        player.setter("level", "1");
//        player.save();
//
//        Long id = player.getter("id");
//        System.out.println(id);

//        String name = player.getKey();
//        System.out.println(name);
        String userDir = System.getProperty("user.dir");
        System.out.println(userDir);
        String gamePropertiesPath = userDir + File.separator + "properties" + File.separator + "game.properties";

//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("game.properties");
        Properties p = new Properties();
        try {
            InputStream inputStream = new FileInputStream(gamePropertiesPath);
            p.load(inputStream);

            String ip = p.getProperty("ip");
            String port = p.getProperty("port");
            System.out.println(ip + " " + port);
        }catch (IOException e){
            e.printStackTrace();
        }


        TestMethods self = this;
        AbstractFunction1 f1 = new AbstractFunction1<Request, Object>(){
            @Override
            public Object apply(Request v1) {
                self.handleM1(v1);
                return null;
            }
        };

//        BasicModule bm = new BasicModule();
//        bm.registerNetEvent(1, f1 );

//        bm.onNetEvent(1, Request.valueOf(1, 1, 1));

    }

//    @Test
    public void testRedis(){
        String redisIp = "192.168.10.190";
        int redisPort = 7001;
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort(redisIp, redisPort));
        JedisCluster jc = new JedisCluster(jedisClusterNodes);

        Map<String, String> map = new HashMap<>();
        map.put("key1", "2");
//        map.put("key2", "4");
        map.put("key3", "5");
        jc.hmset("TestDB", map);

//        jc.setbit()
//        Map<String, String> testPlayer = jc.hgetAll("TestPlayer");
//        Iterator<Map.Entry<String, String>> it = testPlayer.entrySet().iterator();
//        while (it.hasNext()){
//            Map.Entry<String, String> entry = it.next();
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
//
//        Set<Long> set = new HashSet<Long>();
////        set.
//        set.add(1L);
//        set.add(2L);
//        set.add(3L);
//        String str = GameUtils.set2str(set);
//        System.out.println(str);
//
//        Set<Long> set2 = GameUtils.str2set(str);
//        System.out.println(set2);
    }

//    @Test
    public void testValue(){
        System.out.println(TileType.Empty().id());
        System.out.println(TileType.Building().id());
        System.out.println(TileType.Resource().id());
    }

//    @Test
    public void testRandomEmitter(){
        ArrayList<Tuple2<Integer, Integer>> list = new ArrayList<Tuple2<Integer, Integer>>();
        list.add(new Tuple2(1, 80));
        list.add(new Tuple2(2, 20));
        list.add(new Tuple2(3, 80));
        list.add(new Tuple2(4, 20));
        list.add(new Tuple2(5, 80));
        list.add(new Tuple2(6, 20));
        list.add(new Tuple2(7, 80));
        list.add(new Tuple2(8, 20));
        list.add(new Tuple2(9, 80));
        list.add(new Tuple2(10, 20));
        list.add(new Tuple2(11, 500));
        RandomEmitter randomEmitter = new RandomEmitter(list, 10);
        for (int i = 0; i < 100 ; i++) {
            int id = randomEmitter.emitter();
            System.out.println(id);
        }

    }

//    @Test
    public void testGetConfig(){
        Map<String, String> map = new HashMap<String, String>(){
            {
                put("sort", "117");
            }
        };
//        JSONObject info = ConfigDataProxy.getConfigInfoFindByField(DataDefine.MAP_GENERATE, map);
        JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MAP_GENERATE, "sort", 117);
        int playMax = info.getInt("playmax");
        System.out.println(playMax);

        List<JSONObject> infos = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.MAP_GENERATE, "sort", 117);
        for(JSONObject info2 : infos){
            playMax = info2.getInt("playmax");
            System.out.println(playMax);
        }

        Long time = System.currentTimeMillis();
        for (int i = 0; i < 600 * 600; i++) {

        }
        System.out.println(System.currentTimeMillis() - time);
    }

//    @Test
    public void testRandom(){
        SecureRandom random = new SecureRandom();
        String algorithm = random.getAlgorithm();
        System.out.println(algorithm);
        int i = 0;
        Set<Integer> ranIntSet = new HashSet<Integer>();

        while(i < 200){
            int num = random.nextInt(1000);
            boolean flag = ranIntSet.add(num);
            if(flag == false){
                System.out.println(num);
            }
            i++;
        }

    }


//    @Test
    public void testJson(){
//        String str = "{" +
//                    " 1 : {name : woko, list : [1, 2, 4]}" +
//                "}";
//        JSONObject jsonObject = new JSONObject(str);
//        JSONObject obj = jsonObject.getJSONObject("1");
//        JSONArray jary = obj.getJSONArray("list");
//
//        System.out.println(obj.get("name"));
//
//        Map<Integer, JSONObject> map = ConfigDataProxy.getDungeonConfig();
//        JSONObject value = map.get(127);
//        System.out.println(value.getInt("id"));
//
//        JSONObject value2 = ConfigDataProxy.getDungeonInfoById(128);
//        System.out.println(value.getString("name"));

        Player player = new Player();
        player.setAccountName("woko");
        player.setIcon(1);
        JSONObject json = new JSONObject(player);
        System.out.println(json.toString());


        int time = (int)(System.currentTimeMillis() / 1000);

        Field[] fields = PlayerPowerDefine.class.getFields();
        for(Field field : fields){
            try {
                System.out.println(field.get(null));
                System.out.println(field.getName().replace("POWER_", ""));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private int summ(int a, int b){
        return a + b;
    }


    public void testJavaScript(){

        ScriptProxy.loadScript("LoginModuleHandler");

//        System.out.println("sum:" + ScriptProxy.runScript("sum(1,2);"));
        Invocable invocable = (Invocable)ScriptProxy.engine();
        try {


//            invocable.invokeFunction("OnTriggerNet10000Event", Request.valueOf(1, 2, 3));
//            long time = System.currentTimeMillis();
//            invocable.invokeFunction("OnTriggerNet10000Event", Request.valueOf(1, 2, 4));
//            System.out.println(System.currentTimeMillis() - time);

//            Thread.sleep(10000);
//            ScriptProxy.loadScript("LoginModuleHandler");
//            time = System.currentTimeMillis();
//            invocable.invokeFunction("OnTriggerNet10000Event", Request.valueOf(1, 2, 4));
//            System.out.println(System.currentTimeMillis() - time);
//            summ(1 , 2);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    private void handleM1(Request request) {
        System.out.println("------hello----" + request.getCmd());
    }


//    @Test
    public void testDemo(){
        long value=1524000014l;
        int h=(int)(value/10000000000l);
        int t=(int)(value/1000000000l)%10;
        int b=(int)(value%1000000000);
        System.out.println(t);
        System.out.println(h);
        System.out.println(b);
    }

//    @Test
    public void testDemo1(){
       List<Integer> list=new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(1);
        list.add(3);


    }

    interface Handler{
        void onNetEvent(int cmd, Request request);
    }
}
