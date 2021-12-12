package NormalObjects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("Convert2Lambda")
public class Web {

    public interface onWebDownload{
        void onMetaDataDownload(String description,String title);
        void onWebImageSuccess(Bitmap bitmap);
        void onFailed();
    }

    private onWebDownload listener;

    public void setListener(onWebDownload listener){this.listener = listener;}

    public void downloadWebPreview(String link)
    {
        if (listener!=null) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
                @Override
                public Bitmap call() throws Exception {
                    Document doc = Jsoup.connect(link).userAgent("Mozilla").get();
                    String title = doc.title();
                    Elements webImage = doc.select("meta[property=og:image]");
                    String imageLink = webImage.attr("content");
                    Elements webDescription = doc.select("meta[property=og:description]");
                    String description = webDescription.attr("content");
                    URL url = new URL(imageLink);
                    Bitmap webImageBitmap = null;
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.connect();
                    int responseCode = httpsURLConnection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputStream = httpsURLConnection.getInputStream();
                        webImageBitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        httpsURLConnection.disconnect();
                    }
                    httpsURLConnection.disconnect();
                    listener.onMetaDataDownload(description, title);
                    return webImageBitmap;
                }
            };
            Future<Bitmap> bitmapFuture = executorService.submit(bitmapCallable);
            Thread downloadWeb = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (!bitmapFuture.isDone()) {
                        /*
                         * waiting for the picture to load
                         * since get method blocks and makes the app freeze until the image is loaded
                         * noticeable with multiple images
                         * */
                    }
                    try {
                        Bitmap bitmap = bitmapFuture.get();
                        listener.onWebImageSuccess(bitmap);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        listener.onFailed();
                    }
                }
            };
            downloadWeb.setName("web image");
            downloadWeb.start();
        }else Log.e("Web", "web listener is null" );
    }
}
