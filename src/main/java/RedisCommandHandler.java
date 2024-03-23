import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RedisCommandHandler implements CommandHandler{
    private RedisEncoder redisEncoder;

    private final String b64EmptyRDB ="UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";

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
    public void sendResponsePing(DataOutputStream dataOutputStream, List<String> response) throws IOException {
        dataOutputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendRDB(DataOutputStream dataOutputStream,String prefix ,byte[] rdb) throws IOException {
        dataOutputStream.write(prefix.getBytes(StandardCharsets.UTF_8));
        dataOutputStream.write(rdb);
    }

    @Override
    public int getCommandLength(String command){
        return Integer.parseInt(command.substring(1).replace("\r\n",""));
    }


    @Override
    public void outputHandler(DataOutputStream dataOutputStream, List<String> list, boolean isReplica, ConcurrentHashMap<String,String> keyValuePair) throws IOException {
        String command = list.get(0);
        switch (command){
            case "ping":
                sendResponsePing(dataOutputStream,list);
                break;
            case "echo":
                sendResponse(dataOutputStream,list.get(1));
                break;
            case "set":
                keyValuePair.put(list.get(1),list.get(2));
                if(list.size()>3 && list.get(3).equalsIgnoreCase("px")){
                    RedisClient.redisKeyTimeoutPair.put(list.get(1),
                            new Long[]{Long.parseLong(list.get(4)),new Date().getTime()});
                }
                if(!isReplica)sendResponse(dataOutputStream,"OK");
                break;
            case "get":
                String value = keyValuePair.getOrDefault(list.get(1),"null");
                if(value.equalsIgnoreCase("null")){

                    sendResponse(dataOutputStream,"null");
                }else{
                    sendResponse(dataOutputStream,value);
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
                sendResponse(dataOutputStream,"OK");
                break;
            case "psync":
                sendResponse(dataOutputStream,"FULLRESYNC %s %s"
                        .formatted(RedisServerConfiguration.replicationInfo.get("master_replid"),
                                RedisServerConfiguration.replicationInfo.get("master_repl_offset")));
                byte[] rdbBytes = Base64.getDecoder().decode(b64EmptyRDB);
                sendRDB(dataOutputStream,"$"+rdbBytes.length+"\r\n",rdbBytes);
                break;
        }
    }

    
}
