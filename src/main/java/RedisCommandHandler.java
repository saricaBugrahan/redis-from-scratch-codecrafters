import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class RedisCommandHandler implements CommandHandler{
    private RedisEncoder redisEncoder;
    public RedisCommandHandler(){
        redisEncoder = new RedisEncoder();
    }

    @Override
    public List<String> inputHandler(BufferedReader bufferedReader) {
        return null;
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
    public void outputHandler(DataOutputStream dataOutputStream, List<String> list) throws IOException {
        String command = list.get(0);
        switch (command){
            case "ping":
                sendResponsePing(dataOutputStream,list);
                break;
            case "echo":
                sendResponse(dataOutputStream,list.get(1));
                break;
            case "set":
                RedisClient.redisKeyValuePair.put(list.get(1),list.get(2));
                if(list.size()>3 && list.get(3).equalsIgnoreCase("px")){
                    RedisClient.redisKeyTimeoutPair.put(list.get(1),
                            new Long[]{Long.parseLong(list.get(4)),new Date().getTime()});
                }
                sendResponse(dataOutputStream,"OK");
                break;
            case "get":
                String value = RedisClient.redisKeyValuePair.getOrDefault(list.get(1),"null");
                if(value.equalsIgnoreCase("null")){

                    sendResponse(dataOutputStream,"null");
                }else{
                    sendResponse(dataOutputStream,value);
                }
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

        }
    }

    
}
