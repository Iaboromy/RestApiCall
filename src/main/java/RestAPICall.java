import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class RestAPICall {

    public static void main(String[] args) throws Exception{

    //
        TransciptObject transciptObject = new TransciptObject();
        transciptObject.setAudio_url("https://www.youtube.com/watch?v=JhU0yO43b6o");
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transciptObject);
        System.out.println(jsonRequest);


        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization",Constants.API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String>  transcription ;

       transcription =  httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        transciptObject =   gson.fromJson(transcription.body(),TransciptObject.class);

        System.out.println(transciptObject.getId());


        HttpRequest getRequestResult = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transciptObject.getId() ))
                .header("Authorization",Constants.API_KEY)
                .build();

        while (true) {

            HttpResponse<String> getResponse = httpClient.send(getRequestResult, HttpResponse.BodyHandlers.ofString());
            transciptObject = gson.fromJson(getResponse.body(),TransciptObject.class);

            System.out.println(transciptObject.getStatus());

            if ("completed".equals(transciptObject.getStatus()) || "error".equals(transciptObject.getStatus())) {
                break;

            }

            Thread.sleep(1000);
        }

        System.out.println("Transcription complete ! ");
        System.out.println(transciptObject.getText());

    }
}
