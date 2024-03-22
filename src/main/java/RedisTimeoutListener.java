import java.util.Date;
import java.util.Map;

public class RedisTimeoutListener implements Runnable{

    private Date currentDate;
    RedisTimeoutListener(){
        this.currentDate = new Date();
    }

    @Override
    public void run() {
        while (true){
            if(!RedisClient.redisKeyTimeoutPair.isEmpty()){
                this.currentDate = new Date();
                for(Map.Entry<String,Long[]> pair: RedisClient.redisKeyTimeoutPair.entrySet()){
                    long timeDifference = this.currentDate.getTime() - pair.getValue()[1];
                    if(timeDifference > pair.getValue()[0]){
                        RedisClient.redisKeyValuePair.remove(pair.getKey());
                    }
                }
            }
        }
    }
}
