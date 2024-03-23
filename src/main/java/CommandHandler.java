import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface CommandHandler {
    void outputHandler(DataOutputStream dataOutputStream, List <String> list, boolean isReplica, ConcurrentHashMap<String,String> keyValuePair) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,String response) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,List<String> response) throws IOException;
    void sendResponsePing(DataOutputStream dataOutputStream,List<String> response) throws IOException;
    void sendRDB(DataOutputStream dataOutputStream,String prefix,byte[] rdb) throws  IOException;
    int getCommandLength(String input);


}
