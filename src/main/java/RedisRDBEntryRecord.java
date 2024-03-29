public record RedisRDBEntryRecord(Long expireDuration, long currentTime,int valueFlag, String Key, String Value) {
}