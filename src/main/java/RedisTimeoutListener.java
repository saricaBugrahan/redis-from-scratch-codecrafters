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
            this.currentDate = new Date();
            if (!RedisRDBImpl.redisRBDMap.isEmpty()){
                for (Map.Entry<String, RedisRDBEntryRecord> pair : RedisRDBImpl.redisRBDMap.entrySet()){
                    if (pair.getValue().expireDuration() != 0L){
                        long time_difference = this.currentDate.getTime() - pair.getValue().currentTime();
                        if (time_difference> pair.getValue().expireDuration()){
                            RedisRDBImpl.redisRBDMap.remove(pair.getKey());
                        }
                    }
                }
            }
        }
    }
}