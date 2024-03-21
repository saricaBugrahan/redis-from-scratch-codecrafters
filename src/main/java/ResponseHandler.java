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
            boolean isEcho = false;
            while (response != null)
            {
                if(isEcho){
                    dos.write((response+"\r\n").getBytes(StandardCharsets.UTF_8));
                }
                if(response.equalsIgnoreCase("ping")){
                    dos.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                }
                else if (response.equalsIgnoreCase("echo")){
                    isEcho = true;
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
