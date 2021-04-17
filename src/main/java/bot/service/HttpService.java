package bot.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;


final public class HttpService {
    public static Instant counterLimit;
    public static HttpResponse<String> GetResponse(String uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        String[] LimitCountArray = response.headers().allValues("X-App-Rate-Limit-Count").get(0).split("(:|,)");
        int counterSecond = Integer.parseInt(LimitCountArray[0]);
        int counter2Minutes = Integer.parseInt(LimitCountArray[2]);
        if (counter2Minutes == 1) {
           counterLimit = Instant.now();
        }
        if (counterSecond >= 19) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (counter2Minutes >= 99) {
            try {
                Thread.sleep(2*60*1000 + 1000 - Duration.between(counterLimit, Instant.now()).toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return response;
    }
}
