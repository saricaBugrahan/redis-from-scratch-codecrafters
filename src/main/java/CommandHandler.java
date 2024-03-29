import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;


public interface CommandHandler {
    void outputHandler(DataOutputStream dataOutputStream, List <String> list, boolean isReplica) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,String response) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,List<String> response) throws IOException;

    void sendResponse(DataOutputStream dataOutputStream, int number) throws IOException;
    void sendResponsePing(DataOutputStream dataOutputStream,List<String> response) throws IOException;
    void sendRDB(DataOutputStream dataOutputStream,String prefix,byte[] rdb) throws  IOException;

    void sendResponseSimple(DataOutputStream dataOutputStream,String response) throws IOException;
    int getCommandLength(String input);


}