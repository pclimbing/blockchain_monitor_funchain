package wan;

import cn.hyperchain.sdk.crypto.ECPriv;
import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.function.FunctionDecode;
import cn.hyperchain.sdk.rpc.function.FunctionEncode;
import cn.hyperchain.sdk.rpc.returns.*;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * goo
 *
 */

public class App
{

    final static String SOLC_CODE = "contract Accumulator{ uint32 sum = 0; function increment(){ sum = sum + 1;\n" +
            "} function getSum() returns(uint32){ return sum; } function add(uint32 num1,uint32 num2) { sum = sum+num1+num2; } }";

    public static void main( String[] args )
    {

        try {
//            HyperchainAPI hyperchain = new HyperchainAPI();
            HyperchainAPI hyperchain = new HyperchainAPI("/opt/hpc.properties");
            InfluxWriter influxdb = new InfluxWriter();


//            ECPriv ecKey = createAccount(hyperchain);
//            CompileReturn complieResult = compile(hyperchain, SOLC_CODE);
//            String contractAddress = deploy(hyperchain, complieResult.getBin().get(0), ecKey);
////            invokeNoParamFunc(hyperchain, contractAddress, complieResult.getAbi().get(0), ecKey, "increment");
//            invokeNoParamFunc(hyperchain, contractAddress, complieResult.getAbi().get(0), ecKey, "getSum");
//            query(hyperchain);



            StdReturn re = hyperchain.getTx( "1", "");
            System.out.println(re.getResult());

            insertTrans(hyperchain, influxdb);
            insertBlocks(hyperchain, influxdb);
//            insertBlocktoInflux(hyperchain, influxdb);  //abandoned
            insertNodestoInflux(hyperchain, influxdb);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void insertTrans(HyperchainAPI hyperchain, InfluxWriter influxdb) {

        StdReturn re = hyperchain.getTx( "1", "");
//        System.out.println(re.getResult());
        JSONArray transArr = JSONArray.fromObject(re.getResult());

        Iterator<Object> it = transArr.iterator();
        while (it.hasNext()) {
            JSONObject ob = (JSONObject) it.next();
            System.out.println(ob);
            influxdb.write_trans(ob.toString());

        }
    }

    public static void insertBlocks(HyperchainAPI hyperchain, InfluxWriter influxdb){

        //get newest block
        BlockReturn blockReturn = hyperchain.getLatestBlock();
        JSONObject bkjson = JSONObject.fromObject(blockReturn.getResult());
        String number = bkjson.getString("number");
        int numberInt = Integer.parseInt(number.replaceAll("^0[x|X]", ""), 16);


        //get and insert all blocks
        ArrayList<BlockReturn> blockReturns = hyperchain.getBlocks( new BigInteger( "1"),
                new BigInteger( "" + numberInt));

        JSONArray blockArr = JSONArray.fromObject(blockReturns.get(0).getResult());

        Iterator<Object> it = blockArr.iterator();
        while (it.hasNext()) {
            JSONObject ob = (JSONObject) it.next();
            //System.out.println(ob);
            influxdb.write_block(ob.toString());

        }

        //insert avg block time
        SingleValueReturn singleValueReturn = hyperchain.getAvgGenTimeByBlkNum( new BigInteger( "1"),
                new BigInteger( "" + numberInt));
        int arvTimeInt = Integer.parseInt(singleValueReturn.getResult().replaceAll("^0[x|X]", ""), 16);
        System.out.println("平均出块时间： " + arvTimeInt);
        influxdb.write_avgTime( arvTimeInt + "");
    }

    public static void insertBlocktoInflux(HyperchainAPI hyperchain, InfluxWriter influxdb) {
        BlockReturn blockReturn = hyperchain.getLatestBlock();
        influxdb.write_block(blockReturn.getResult());

    }

    //insert all node into influxdb
    //will flush nodes first to ensore all nodes remain only one in db
    public static void insertNodestoInflux(HyperchainAPI hyperchain, InfluxWriter influxdb){
//        influxdb.removeNodes(); //move to influxWriter
        ArrayList<NodeInfoReturn> nodeInfoReturns= hyperchain.getNodes();
        influxdb.write_nodes(nodeInfoReturns);
    }





    // test create account, compile code, deploy code, and invoke code
    public static void test(HyperchainAPI hyperchain){
//        ECPriv ecKey = createAccount(hyperchain);
//        CompileReturn complieResult = compile(hyperchain, SOLC_CODE);
//        System.out.println(complieResult.getAbi());
//        String contractAddress = deploy(hyperchain, complieResult.getBin().get(0), ecKey);
//        invokeNoParamFunc(hyperchain, contractAddress, complieResult.getAbi().get(0), ecKey, "increment");
//        invokeNoParamFunc(hyperchain, contractAddress, complieResult.getAbi().get(0), ecKey, "getSum");
//        query(hyperchain);

    }

    public static void query(HyperchainAPI hyperchain) {
        SingleValueReturn r = hyperchain.getTx("1", "");
        System.out.println(r.getResult());
    }

    public static CompileReturn compile(HyperchainAPI hyperchain, String solcCode) {
        CompileReturn complieResult = hyperchain.compileContract(solcCode);
        return complieResult;
    }

    public static String deploy(HyperchainAPI hyperchain, String bin, ECPriv ecKey) throws Exception {
        Transaction transaction = new Transaction(ecKey.address(),
                bin,
                false);
        transaction.sign(ecKey);

        SingleValueReturn result = hyperchain.deployContract(transaction);
        ReceiptReturn deployReceipt = hyperchain.getTransactionReceipt(result.getResult());

        return deployReceipt.getContractAddress();
    }

    public static void invokeNoParamFunc(HyperchainAPI hyperchain, String contractAddress, String abi, ECPriv ecKey, String func) throws Exception {
        String input = FunctionEncode.encodeFunction(func);

        Transaction transaction = new Transaction(ecKey.address(),
                contractAddress,
                input,
                false);
        transaction.sign(ecKey);

        SingleValueReturn result = hyperchain.invokeContract(transaction);

        ReceiptReturn solcReceipt = hyperchain.getTransactionReceipt(result.getResult());
        System.out.println(func + "  " + solcReceipt.getResult());
        System.out.println(FunctionDecode.resultDecode(func,abi,solcReceipt.getRet()));
    }

    public static void sendTx(HyperchainAPI hyperchain, ECPriv ecKey) throws Exception {
        Transaction transaction = new Transaction(ecKey.address(),
                "000f1a7a08ccc48e5d30f80850cf1cf283aa3abd",
                100,
                false);
        transaction.sign(ecKey);

        StdReturn result = hyperchain.sendTx(transaction);
        System.out.println(result.getResult());
    }

    public static ECPriv createAccount(HyperchainAPI hyperchain) throws Exception {
        String accountJson = hyperchain.newAccount( "123");
        System.out.println(accountJson);

        ECPriv ecKey = hyperchain.decryptAccount(accountJson, "123"); //获取地址

        return ecKey;
    }

    public static void invoke(HyperchainAPI hyperchain, String contractAddress, ECPriv ecKey) throws Exception {
        FuncParamReal param1 = new FuncParamReal("uint32", 1);
        FuncParamReal param2 = new FuncParamReal("uint32", 2);
        String input = FunctionEncode.encodeFunction("add", param1, param2);

        Transaction transaction = new Transaction(ecKey.address(),
                contractAddress,
                input,
                false);
        transaction.sign(ecKey);

        SingleValueReturn result = hyperchain.invokeContract(transaction);
        ReceiptReturn solcReceipt = hyperchain.getTransactionReceipt(result.getResult());
    }
}
