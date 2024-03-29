import java.util.List;

public class RedisEncoder {

    public String parseResponseIntoRESPBulk(String response){
        if(response.equalsIgnoreCase("null")){
            return "$-1\r\n";
        }
        return "$"+response.length()+"\r\n"+response+"\r\n";
    }

    public String parseResponseIntoRESPBulk(List<String> list){
        StringBuilder response = new StringBuilder("*%d\r\n".formatted(list.size()));
        for (String s : list) {
            response.append(parseResponseIntoRESPBulk(s));
        }
        return response.toString();
    }
    public String parseResponseIntoRESPBulk(int number){
        return ":"+number+"\r\n";
    }
    public String simpleString(String response,boolean isError){
        if (isError){
            return "-"+response+"\r\n";
        }
        return "+"+response+"\r\n";
    }


}