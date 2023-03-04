import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class RestAPICall {

    static HttpClient httpClient = HttpClient.newHttpClient();
    static Gson gson = new Gson();
    public static void main(String[] args) throws Exception{


        TransciptObject transciptObject = new TransciptObject();
        HttpResponse<String>  transcription ;

        transciptObject.setAudio_url("https://www.youtube.com/watch?v=JhU0yO43b6o");

        String jsonRequest = gson.toJson(transciptObject);

        System.out.println(jsonRequest);
        HttpRequest httpRequest = requestBuilder(jsonRequest);

        transcription =  httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        transciptObject = gson.fromJson(transcription.body(),TransciptObject.class);

        System.out.println(transciptObject.getId());
        transciptObject = getTranscriptionResult(transciptObject);

        System.out.println("Transcription complete ! ");
        System.out.println(transciptObject.getText());



    }

    public static HttpRequest requestBuilder(String jsonRequest) throws Exception{
        return HttpRequest.newBuilder()
                .uri(new URI(Constants.ASSEMBLY_AI_URI))
                .header("Authorization",Constants.API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

    }

    public static TransciptObject getTranscriptionResult (TransciptObject transciptObject) throws Exception {

        HttpRequest getRequestResult = HttpRequest.newBuilder()
                .uri(new URI(Constants.ASSEMBLY_AI_URI + "/" + transciptObject.getId() ))
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

        return transciptObject;
    }

}
