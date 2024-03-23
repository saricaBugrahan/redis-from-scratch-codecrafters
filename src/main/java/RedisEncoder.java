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
        for(int i = 0;i<list.size();i++){
            response.append(parseResponseIntoRESPBulk(list.get(i)));
        }
        return response.toString();
    }
    public String parseResponseRDB(String rdb){
        return "$"+rdb.length()+"\r\n"+rdb;
    }


}


