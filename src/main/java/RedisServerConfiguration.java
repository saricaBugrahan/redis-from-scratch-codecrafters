import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RedisServerConfiguration {


    public static Map<String,String> replicationInfo;

    public static int portNumber = 6379;

    private String role = "master";

    private String replicationId;

    private int replicationOffset;

    private RedisReplicaServer redisReplicaServer;


    private final RedisRDBImpl redisRDB;

    public RedisRDBImpl getRedisRDB() {
        return redisRDB;
    }

    RedisServerConfiguration(String[] args){
        replicationInfo = new HashMap<>();
        redisRDB = new RedisRDBImpl();
        parseArgs(args);
        if(role.equalsIgnoreCase("master")){
            initMasterWithDefaultArguments();
        }
        replicationInfo.put("role",role);
        replicationInfo.put("master_replid",replicationId);
        replicationInfo.put("master_repl_offset", String.valueOf(replicationOffset));
    }

    private void initMasterWithDefaultArguments(){
        replicationId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        this.replicationOffset = 0;
    }

    private void parseArgs(String[] args){
        for(int i = 0;i< args.length;i++){
            if(args[i].equalsIgnoreCase("--port")){
                try{
                    portNumber = Integer.parseInt(args[++i]);
                } catch (IndexOutOfBoundsException indexOutOfBoundsException){
                    System.out.println("Port tag used with not appropriate number of arguments");
                }
            }
            else if(args[i].equalsIgnoreCase("--replicaof")){
                try {
                    this.role = "slave";
                    redisReplicaServer = new RedisReplicaServer(new Socket(args[++i],Integer.parseInt(args[++i])));
                } catch (IndexOutOfBoundsException indexOutOfBoundsException){
                    System.out.println("Replicaof tag is used with not appropriate number of arguments");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if(args[i].equalsIgnoreCase("--dir")){
                redisRDB.setRDBDirectory(args[++i]);

            } else if(args[i].equalsIgnoreCase("--dbfilename")){
                redisRDB.setRDBFilename(args[++i]);
            }
        }
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getRole() {
        return role;
    }

    public RedisReplicaServer getRedisReplicaServer(){
        return redisReplicaServer;
    }


}