package wan;

import cn.hyperchain.sdk.rpc.returns.NodeInfoReturn;
import net.sf.json.JSONObject;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.sql.Timestamp;
import java.sql.Time;

/**
 * Created by dong on 2017/6/26.
 */


public class InfluxWriter {

    InfluxDB influxDB;
    String dbName;

    public InfluxWriter() throws Exception {
        try {
            InputStream e = this.getClass().getClassLoader().getResourceAsStream("influx.properties");
//            File file = new File("/Users/md760/wanda/projects/influx.properties");
//            InputStream e =  new FileInputStream(file);

            Properties properties = new Properties();
            properties.load(e);

//
//            String node = properties.getProperty("node");
//            JSONObject nodes = JSONObject.fromObject(node);
//            String nodeUrl = nodes.getString("nodes");
//            influxDB.deleteDatabase(dbName);

            String url = properties.getProperty("influx_url");
            String u = properties.getProperty("influx_username");
            String p = properties.getProperty("influx_password");
            String db = properties.getProperty("dbname");

            influxDB = InfluxDBFactory.connect(url );
            dbName = db;
            influxDB.deleteDatabase(dbName);
            influxDB.createDatabase(dbName);

        } catch (Exception var18) {
            var18.printStackTrace();
        }

    }

    public void flushDB(){
        influxDB.deleteDatabase(dbName);
        influxDB.createDatabase(dbName);
    }

    public void write_nodes(ArrayList<NodeInfoReturn> nodeInfoReturns){
        this.deleteMeasurement("nodes");
        for (NodeInfoReturn nodeInfoReturn : nodeInfoReturns){
            System.out.println(nodeInfoReturn.getIp());
//            System.out.println(nodeInfoReturn.getId());
//            System.out.println(nodeInfoReturn.getPrimary());
//            System.out.println(nodeInfoReturn.getDelay());
            this.write_node(nodeInfoReturn.getIp(), nodeInfoReturn.getId(), nodeInfoReturn.getPrimary(), nodeInfoReturn.getDelay());
        }
    }
    public void write_node(String ip, int id, boolean primary, BigInteger delay){
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("nodes")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("node", ip)
                .addField("ip", ip)
                .addField("id", id)
                .addField("primary", primary)
                .addField("delay", delay)
                .build();
        batchPoints.point(point1);
//        this.deleteMeasurement()
        influxDB.write(batchPoints);
    }

    public void write_blocks(String bk) {
        JSONObject bkjson = JSONObject.fromObject(bk);
        String number = bkjson.getString("number");

    }

    public void write_block(String bk){

        JSONObject bkjson = JSONObject.fromObject(bk);
        String version = bkjson.getString("version");
        String number = bkjson.getString("number");
        String hash = bkjson.getString("hash");
        String parentHash = bkjson.getString("parentHash");
        Long time = bkjson.getLong("writeTime");
        String avgTime = bkjson.getString("avgTime");
        String txcounts = bkjson.getString("txcounts");
        String merkleRoot = bkjson.getString("merkleRoot");

        int txcountsInt = Integer.parseInt(txcounts.replaceAll("^0[x|X]", ""), 16);
        int numberInt = Integer.parseInt(number.replaceAll("^0[x|X]", ""), 16);


//        System.out.println(System.currentTimeMillis());
//        System.out.println(time);
//        System.out.println( System.currentTimeMillis());

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("blocks")
                .time(time/1000000, TimeUnit.MILLISECONDS)
                .tag("blockhash", hash)
                .addField("version", version)
                .addField("number", numberInt)
                .addField("hash", hash)
                .addField("parentHash", parentHash)
                .addField("avgTime", avgTime)
                .addField("txcounts", txcountsInt)
                .addField("merkleRoot", merkleRoot)
                .build();
        batchPoints.point(point1);

        this.deleteByField("blocks", "blockhash", hash);
        influxDB.write(batchPoints);
    }

    public void write_avgTime(String t){
        //delete the former one

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("avgBlockTime")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("avgblocktime", t)
                .build();
        batchPoints.point(point1);

        this.deleteMeasurement("avgBlockTime");
        influxDB.write(batchPoints);
    }



    public void write_trans(String transStr){
        JSONObject trjson = JSONObject.fromObject(transStr);

        String version = trjson.getString("version");
        String hash = trjson.getString("hash");
        String blockNumber = trjson.getString("blockNumber");      // 0x
        String blockHash =  trjson.getString("blockHash");
        String txIndex =  trjson.getString("txIndex");           //0x
        String from = trjson.getString("from");
        String to = trjson.getString("to");
        String amount = trjson.getString("amount");       //0x
        Long   timestamp = trjson.getLong("timestamp");
        Long   nonce = trjson.getLong("nonce");
        String executeTime = trjson.getString("executeTime");     //0x
        String payload = trjson.getString("payload");

        int blockInt = Integer.parseInt(blockNumber.replaceAll("^0[x|X]", ""), 16);
        int txIndexInt = Integer.parseInt(txIndex.replaceAll("^0[x|X]", ""), 16);
        int amountInt = Integer.parseInt(amount.replaceAll("^0[x|X]", ""), 16);
        int executeTimeInt = Integer.parseInt(executeTime.replaceAll("^0[x|X]", ""), 16);

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point1 = Point.measurement("transactions")
                .time(timestamp, TimeUnit.MILLISECONDS)
                .tag("tranHash", hash)
                .addField("version", version)
                .addField("blockNumber", blockInt)
                .addField("hash", hash)
                .addField("blockHash", blockHash)
                .addField("txIndex", txIndexInt)
                .addField("from", from)
                .addField("to", to)
                .addField("amount", amountInt)
                .addField("nonce", nonce)
                .addField("executeTime", executeTimeInt)
                .addField("payload", payload)
                .build();
        batchPoints.point(point1);

        this.deleteByField("transactions", "tranHash", hash);
        influxDB.write(batchPoints);

    }


    public void deleteMeasurement(String measurement){

        String command = "DELETE FROM " + measurement;

        final Query query = new Query(command, dbName, true);

        try {
            QueryResult.Result result = influxDB.query(query).getResults().get(0);

            if (result.hasError()) {
                throw new RuntimeException(result.getError());
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void deleteByField(String measurement, String tag, String value){

        String command = "DELETE FROM " + measurement + " WHERE " +  tag + " = " + value ;

        final Query query = new Query(command, dbName, true);

        try {
            QueryResult.Result result = influxDB.query(query).getResults().get(0);

            if (result.hasError()) {
                throw new RuntimeException(result.getError());
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
