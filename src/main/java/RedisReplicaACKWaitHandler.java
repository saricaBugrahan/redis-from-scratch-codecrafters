import java.util.Date;

public class RedisReplicaACKWaitHandler{

    private final int expectedSlaveNumber;
    private final long expectedDuration;

    private final long startTime;
    public RedisReplicaACKWaitHandler(int expected, long duration){
        this.expectedDuration = duration;
        this.expectedSlaveNumber = expected;
        this.startTime = new Date().getTime();
    }

    public void waitForACK(){
        while (true){
            Date date = new Date();
            if(date.getTime()- this.startTime > this.expectedDuration){
                break;
            }
            if(expectedSlaveNumber == 0){
                break;
            }
            if(RedisCommandHandler.commandReplicaCounter == this.expectedSlaveNumber){
                break;
            }
        }
    }


}