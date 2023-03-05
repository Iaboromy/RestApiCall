import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RestAPICall {

    static HttpClient httpClient = HttpClient.newHttpClient();
    static Gson gson = new Gson();
    static Logger logger = Logger.getLogger(RestAPICall.class.getName());
    public static void main(String[] args) throws Exception{

        TransciptObject transciptObject = new TransciptObject();

        HttpResponse<String>  transcription ;

        transciptObject.setAudio_url("https://www.youtube.com/watch?v=JhU0yO43b6o");

        String jsonRequest = GsonObjectToJson(transciptObject);

        HttpRequest httpRequest = requestBuilder(jsonRequest);

        transcription = assemblyAISend(httpRequest);

        transciptObject = getTranscriptionResult(gson.fromJson(transcription.body(),TransciptObject.class));

        if (transciptObject.getText() == null || transciptObject.getStatus().equals("error")) {
            logger.log(Level.SEVERE,"TRANSCRIPTION ERROR :  " + transciptObject.getError());
        }
        else {
            logger.log(Level.INFO,"TRANSCRIPTION RESULT   : " + transciptObject.getText());
        }

    }

     static HttpRequest requestBuilder(String jsonRequest) throws Exception{
        return HttpRequest.newBuilder()
                .uri(new URI(Constants.ASSEMBLY_AI_URI))
                .header("Authorization",Constants.API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

    }

     static TransciptObject getTranscriptionResult (TransciptObject transciptObject) throws Exception {

        logger.log(Level.INFO, "Waiting result for transcriptionid: {"+transciptObject.getId()+"}");
        HttpRequest getRequestResult = HttpRequest.newBuilder()
                .uri(new URI(Constants.ASSEMBLY_AI_URI + "/" + transciptObject.getId() ))
                .header("Authorization",Constants.API_KEY)
                .build();

        while (true) {

            HttpResponse<String> getResponse = httpClient.send(getRequestResult, HttpResponse.BodyHandlers.ofString());
            transciptObject = gson.fromJson(getResponse.body(),TransciptObject.class);


            logger.log(Level.INFO,"CURRENT STATUS : " + transciptObject.getStatus());

            if ("completed".equals(transciptObject.getStatus()) || "error".equals(transciptObject.getStatus())) {
                break;
            }

            Thread.sleep(1000);
        }


        logger.log(Level.INFO,"TRANSCRIPTION COMPLETE ! ");

        return transciptObject;
    }

     static String GsonObjectToJson(TransciptObject transciptObject) {
        Gson gson = new Gson();
        String result = gson.toJson(transciptObject);
        logger.log(Level.INFO,"Object to JSON : " + result);
        return result;
    }

     static HttpResponse<String> assemblyAISend(HttpRequest httpRequest) throws Exception{

        logger.log(Level.INFO, "Sending request to {"+ Constants.ASSEMBLY_AI_URI +"}");

        HttpResponse<String> response =   httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        logger.log(Level.INFO, "Assembly AI JSON Response : " + response.body());


        return response;
    }

}
