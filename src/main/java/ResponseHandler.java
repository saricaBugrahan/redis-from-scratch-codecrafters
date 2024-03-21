import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ResponseHandler implements Runnable{

    private Socket clientSocket;

    public ResponseHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(this.clientSocket.getOutputStream());
            String response = bufferedReader.readLine();
            System.out.println(response);
            while (response != null)
            {
                if(response.contains("ping")){
                    dos.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                }
                response =  bufferedReader.readLine();
            }
            dos.close();
        }
        catch (IOException e){
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (clientSocket != null){
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
