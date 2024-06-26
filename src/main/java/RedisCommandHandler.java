import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RedisCommandHandler implements CommandHandler{
    private final RedisEncoder redisEncoder;

    public static int ACK = 0;
    public static int noCommandReplicaCounter =0;

    public static boolean noCommandFromMaster = true;

    public static int commandReplicaCounter = 0;

    public String ERROR_MESSAGE = "ERR The ID specified in XADD is equal or smaller than the target stream top item";

    public String ERROR_EQUAL_MESSAGE ="ERR The ID specified in XADD must be greater than 0-0";

    public RedisRDBImpl redisRDB;

    public RedisCommandHandler(){
        redisEncoder = new RedisEncoder();
    }


    @Override
    public void sendResponse(DataOutputStream dataOutputStream, String response) throws IOException {
        String dataToSend = redisEncoder.parseResponseIntoRESPBulk(response);
        dataOutputStream.write(dataToSend.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendResponse(DataOutputStream dataOutputStream, List<String> response) throws IOException {
        String dataToSend = redisEncoder.parseResponseIntoRESPBulk(response);
        dataOutputStream.write(dataToSend.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendResponse(DataOutputStream dataOutputStream, int number) throws IOException {
        String dataToSend = redisEncoder.parseResponseIntoRESPBulk(number);
        dataOutputStream.write(dataToSend.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendResponsePing(DataOutputStream dataOutputStream, List<String> response) throws IOException {
        dataOutputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendRDB(DataOutputStream dataOutputStream,String prefix ,byte[] rdb) throws IOException {
        dataOutputStream.write(prefix.getBytes(StandardCharsets.UTF_8));
        dataOutputStream.write(rdb);
    }

    @Override
    public void sendResponseSimple(DataOutputStream dataOutputStream, String response, boolean isError) throws IOException {
        dataOutputStream.write(redisEncoder.simpleString(response,isError).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getCommandLength(String command){
        return Integer.parseInt(command.substring(1).replace("\r\n",""));
    }


    @Override
    public void outputHandler(DataOutputStream dataOutputStream, List<String> list, boolean isReplica) throws IOException {
        String command = list.get(0);
        String b64EmptyRDB = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
        switch (command){
            case "ping":
                if(!isReplica)sendResponsePing(dataOutputStream,list);
                break;
            case "echo":
                sendResponse(dataOutputStream,list.get(1));
                break;
            case "set":
                //RedisClient.redisKeyValuePair.put(list.get(1),list.get(2));
                if(list.size()>3 && list.get(3).equalsIgnoreCase("px")){
                    RedisRDBImpl.redisRBDMap.put(list.get(1),new RedisRDBEntryRecord(
                            Long.parseLong(list.get(4)), new Date().getTime(), 0, list.get(1), list.get(2)
                    ));
                } else{
                    RedisRDBImpl.redisRBDMap.put((list.get(1)),new RedisRDBEntryRecord(
                            0L, new Date().getTime(), 0, list.get(1), list.get(2)
                    ));
                }
                if(!isReplica){
                    sendResponse(dataOutputStream,"OK");
                    noCommandFromMaster = false;
                }
                break;
            case "get":
                String redisRDBValue= redisRDB.getValue(list.get(1));
                if(redisRDBValue.equalsIgnoreCase("null")){
                    sendResponse(dataOutputStream,"null");
                }else{
                    sendResponse(dataOutputStream,redisRDBValue);
                }
                break;
            case "info":
                if(list.get(1).equalsIgnoreCase("replication")){
                    String replication = """
                                            role:%s
                                            
                                            master_replid:%s
                                            
                                            master_repl_offset:%s
                                            
                                            """.formatted(RedisServerConfiguration.replicationInfo.get("role"),
                            RedisServerConfiguration.replicationInfo.get("master_replid"),
                            RedisServerConfiguration.replicationInfo.get("master_repl_offset"));
                    sendResponse(dataOutputStream,replication);
                }
                break;
            case "replconf":
                if (list.get(1).equalsIgnoreCase("getack")){
                    sendResponse(dataOutputStream,List.of("REPLCONF","ACK",String.valueOf(ACK)));
                } else if (list.get(1).equalsIgnoreCase("ack")){
                    System.out.println("ack retrieved from slave");
                    commandReplicaCounter++;
                } else{
                    sendResponse(dataOutputStream,"OK");
                }
                break;
            case "psync":
                sendResponse(dataOutputStream,"FULLRESYNC %s %s"
                        .formatted(RedisServerConfiguration.replicationInfo.get("master_replid"),
                                RedisServerConfiguration.replicationInfo.get("master_repl_offset")));
                byte[] rdbBytes = Base64.getDecoder().decode(b64EmptyRDB);
                sendRDB(dataOutputStream,"$"+rdbBytes.length+"\r\n",rdbBytes);
                break;
            case "wait":
                int expectedNumberOfSlaves = Integer.parseInt(list.get(1));
                long expectedWaitDuration  = Long.parseLong(list.get(2));
                if(noCommandFromMaster){
                    sendResponse(dataOutputStream,noCommandReplicaCounter);
                } else{
                    new RedisReplicaACKWaitHandler(expectedNumberOfSlaves,expectedWaitDuration).waitForACK();
                    sendResponse(dataOutputStream,commandReplicaCounter);
                    commandReplicaCounter = 0;
                }
                break;
            case "config":
                if(list.get(1).equalsIgnoreCase("get") && list.get(2).equalsIgnoreCase("dir")){
                    sendResponse(dataOutputStream,List.of("dir",redisRDB.getRDBDirectory()));
                } else if(list.get(1).equalsIgnoreCase("get") && list.get(2).equalsIgnoreCase("dbfilename")){
                    sendResponse(dataOutputStream,List.of("dbfilename",redisRDB.getRDBFilename()));
                } else{
                    System.out.println("Unknown parameter after config command");
                }
                break;
            case "keys":
                if (list.get(1).equalsIgnoreCase("*")){
                    sendResponse(dataOutputStream, Collections.list(redisRDB.getKeys()));
                }
                break;


            case "type":
                String value = redisRDB.getValue(list.get(1));
                if (value.equalsIgnoreCase("null")){
                    if (redisRDB.checkRedisStreamKey(list.get(1))){
                        sendResponseSimple(dataOutputStream,"stream",false);
                    }
                    sendResponseSimple(dataOutputStream,"none",false);
                }

                else
                    sendResponseSimple(dataOutputStream,"string",false);
                break;

            case "xadd":
                String ID = list.get(2);
                if (!redisRDB.checkRedisStreamKey(list.get(1))){
                    if (ID.equalsIgnoreCase("0-0")){
                        sendResponseSimple(dataOutputStream,ERROR_EQUAL_MESSAGE,true);
                        return;
                    } else if (ID.endsWith("*")){
                        if (ID.length() == 1){
                            ID = System.currentTimeMillis()+"-0";
                        } else{
                            ID = redisRDB.getIDFromStar(ID,true,null);
                            LinkedList<RedisStreamEntryRecord> entry = new LinkedList<>();
                            entry.add(new RedisStreamEntryRecord(ID,list.get(3),list.get(4)));
                            RedisRDBImpl.redisStream.put(list.get(1),entry);

                        }
                        sendResponse(dataOutputStream,ID);
                        return;
                    }
                    LinkedList<RedisStreamEntryRecord> entry = new LinkedList<>();
                    entry.add(new RedisStreamEntryRecord(ID,list.get(3),list.get(4)));
                    RedisRDBImpl.redisStream.put(list.get(1),entry);

                    sendResponseSimple(dataOutputStream,list.get(2),false);
                } else{
                    if (redisRDB.checkValidityRedisStreamKeyID(list.get(1),list.get(2)) == 0){
                        sendResponseSimple(dataOutputStream,ERROR_MESSAGE,true);
                    } else if (redisRDB.checkValidityRedisStreamKeyID(list.get(1),list.get(2)) == -1){
                        sendResponseSimple(dataOutputStream,ERROR_EQUAL_MESSAGE,true);
                    } else{
                        if (ID.endsWith("*")){
                            if (ID.length() == 1)
                                ID = System.currentTimeMillis()+"-0";
                            else
                                ID = redisRDB.getIDFromStar(ID,redisRDB.checkInitial(list.get(1),ID),list.get(1));
                            RedisRDBImpl.redisStream.get(list.get(1)).add(
                                    new RedisStreamEntryRecord(ID,list.get(3),list.get(4))
                            );
                            sendResponse(dataOutputStream,ID);
                            return;
                        }
                        RedisRDBImpl.redisStream.get(list.get(1)).add(
                                new RedisStreamEntryRecord(list.get(2),list.get(3),list.get(4))
                        );
                        sendResponseSimple(dataOutputStream,list.get(2),false);
                    }
                }
                break;

            case "xrange":
                String masterKey = list.get(1);
                String IDStart = list.get(2);
                String IDEnd = list.get(3);

                LinkedList<RedisStreamEntryRecord> streamEntryRecordsInRange = redisRDB.getStreamRecordsInRange(masterKey,IDStart,IDEnd);
                String test = redisEncoder.respStreamEncoder(streamEntryRecordsInRange);
                System.out.println(test);
                dataOutputStream.write(test.getBytes(StandardCharsets.UTF_8));
                break;
            default:
                System.out.println("Unknown Command "+command);
                break;
        }
    }

    public RedisRDBImpl getRedisRDB() {
        return redisRDB;
    }

    public void setRedisRDB(RedisRDBImpl redisRDB) {
        this.redisRDB = redisRDB;
    }


}