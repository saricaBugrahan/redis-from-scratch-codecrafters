import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisRDBImpl {

    public String RDBDirectory = "";
    public String RDBFilename = "";
    public String magicString;
    public String RDBVersion;
    public String RDBNumber;

    //static public LinkedList<RedisRDBEntryRecord> redisRDBEntryRecords = new LinkedList<>();

    static public ConcurrentHashMap<String,RedisRDBEntryRecord> redisRBDMap = new ConcurrentHashMap<>();

    public RedisRDBImpl(){

    }

    public String getRDBDirectory() {
        return RDBDirectory;
    }

    public void setRDBDirectory(String RDBDirectory) {
        this.RDBDirectory = RDBDirectory;
    }

    public String getRDBFilename() {
        return RDBFilename;
    }

    public void setRDBFilename(String RDBFilename) {
        this.RDBFilename = RDBFilename;
    }

    public void readRDB(){
        if (!RDBDirectory.equalsIgnoreCase("")){
            try (FileInputStream fileInputStream = new FileInputStream(this.RDBDirectory+"/"+this.RDBFilename);
                 DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {
                magicString = new String(dataInputStream.readNBytes(5));
                RDBVersion = new String(dataInputStream.readNBytes(4));
                byte command;
                while ((command = dataInputStream.readByte()) != (byte)0xFB);
                int lengthOfTable = getLength(dataInputStream);
                int expireHashTable = getLength(dataInputStream);
                readKeyValuePair(dataInputStream,lengthOfTable);

            } catch (IOException e) {
                System.out.println("RDB cannot be found in the location");
            }
        }
    }
    private int getLength(DataInputStream dataInputStream) throws IOException {
        byte command = dataInputStream.readByte();
        if((command & 0xc0) == 0x00){
            //System.out.println("Next 6 bit represent length");
            return command & 0x3F;
        } else if((command &  0xc0) == 0x40){
            //System.out.println("Read one additional byte. The combined 14 bits represent the length");
            return ((command & 0x3F) <<8) | (dataInputStream.readByte() & 0xff);

        } else if ((command &  0xc0) == 0x80){
            //System.out.println("Discard the remaining 6 bits. The next 4 bytes from the stream represent the length");
            return dataInputStream.readInt();
        } else if ((command & 0xc0) == 0xC0){
            //System.out.println("The next object is encoded in a special format. The remaining 6 bits indicate the format. May be used to store numbers or Strings");
        } else{
            System.out.println("Wrong number of input");
        }
        return -1;
    }

    private void readKeyValuePair(DataInputStream dataInputStream, int recordSize) throws IOException {
        while (true){
            byte command = dataInputStream.readByte();
            long expireTime = 0L;
            if (command == (byte) 0xFC || command == (byte) 0xFD){
                expireTime = getExpireTime(dataInputStream,command);
                command = dataInputStream.readByte();
            }
            if(command ==0){
                int lengthOfKey = getLength(dataInputStream);
                String key = new String(dataInputStream.readNBytes(lengthOfKey));
                int lengthOfValue = getLength(dataInputStream);
                String value = new String(dataInputStream.readNBytes(lengthOfValue));
                System.out.println("Key ->"+key);
                System.out.println("Value ->"+value);
                System.out.println("Expire Time -> "+ expireTime);
                if (expireTime == 0L)
                    redisRBDMap.put(key,new RedisRDBEntryRecord(expireTime,new Date().getTime(),0,key,value));
                else
                    redisRBDMap.put(key,new RedisRDBEntryRecord(expireTime-new Date().getTime(),new Date().getTime(),0,key,value));
            }
            if (command == -1)
                break;
        }

    }
    public Enumeration<String> getKeys(){
        return redisRBDMap.keys();
    }

    public String getValue(String key){
        System.out.println("Keys");
        Collections.list(getKeys()).forEach(System.out::println);
        if (redisRBDMap.containsKey(key)){
            System.out.println("Contains key");

            return redisRBDMap.get(key).Value();

        }

        return "null";
    }

    /*
        FC-> Read 8 bytes, expire time in milliseconds
        FD-> Read 4 bytes, expire time in seconds
     */
    public long getExpireTime(DataInputStream dataInputStream, byte command) throws IOException {
        byte[] expireTimeData;
        ByteBuffer byteBuffer;
        if (command == (byte) 0xFC) {
            expireTimeData = dataInputStream.readNBytes(8);
            byteBuffer = ByteBuffer.wrap(expireTimeData).order(ByteOrder.LITTLE_ENDIAN);
            return byteBuffer.getLong();
        } else if (command == (byte) 0xFD) {
            expireTimeData = dataInputStream.readNBytes(4);
            byteBuffer = ByteBuffer.wrap(expireTimeData).order(ByteOrder.LITTLE_ENDIAN);
            return Integer.toUnsignedLong(byteBuffer.getInt()) * 1000L;
        } else {
            System.out.println("Unknown command " + command);
        }
        return 0L;
    }
}