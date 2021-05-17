package NormalObjects;



import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadWeb extends Thread{


    public interface finishedThread{
        void onThreadFinish();
    }
    finishedThread callback;
    private String link;

    public void setUp(String link){
        this.link = link;


        Thread downloadWeb = new Thread(this);
        downloadWeb.setName("download web thread");
        Thread waitToFinish = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    downloadWeb.join();
                    System.out.println("its finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        waitToFinish.setName("wait to finish thread");
        waitToFinish.start();
        downloadWeb.start();

    }

    @Override
    public void run() {
        super.run();
        StringBuilder stringBuilder = new StringBuilder();
        try{
            Document doc = Jsoup.connect(link).userAgent("Mozilla").get();
            String title = doc.title();
            Elements icons = doc.select("link[href]");
            stringBuilder.append(title).append("\n");
           // Elements meta = doc.select("meta[property=og:url]");
            Elements webImage = doc.select("meta[property=og:image]");
            String imageLink = webImage.attr("content");
            Elements webDescription = doc.select("meta[property=og:description]");
            String description = webDescription.attr("content");

            /*for (Element icon : icons)
            {
                stringBuilder.append("\n").append("Link: ").append(icon.attr("href")).append("\n").append("Text: ").append(icon.text());
                String favicon = icon.attr("href");
                if(favicon.contains("favicon"))
                {
                    if(favicon.startsWith("/"))
                    {
                    // String siteName = meta.attr("content");
                   //  favicon = siteName + favicon;
                    // System.out.println("this is favicon: " + favicon);
                     break;
                    }
                }
            }*/
            System.out.println("this is imageLink: " + imageLink);

        }catch (IOException e){
            stringBuilder.append("Error : ").append(e.getMessage()).append("\n");
        }

        System.out.println("this is string builder: " + stringBuilder.toString());
    }
}
