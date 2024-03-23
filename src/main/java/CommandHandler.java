import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface CommandHandler {
    List<String> inputHandler(BufferedReader bufferedReader);
    void outputHandler(DataOutputStream dataOutputStream,List <String> list) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,String response) throws IOException;
    void sendResponse(DataOutputStream dataOutputStream,List<String> response) throws IOException;
    void sendResponsePing(DataOutputStream dataOutputStream,List<String> response) throws IOException;
    int getCommandLength(String input);


}
