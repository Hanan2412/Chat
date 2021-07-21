package Model;

import android.content.Context;
import android.graphics.Bitmap;
//version 3 of the server class
public class Server3 extends Uploads implements IServer3{

    private static Server3 server3 = null;

    private Server3()
    {

    }

    public static Server3 getInstance()
    {
        if (server3 == null)
            server3 = new Server3();
        return server3;
    }

    @Override
    public void uploadImageBitmap(Bitmap imageBitmap) {
        super.uploadImageBitmap(imageBitmap);
    }

    @Override
    public void uploadImage(String photoPath) {
        super.uploadImage(photoPath);
    }

    @Override
    public void uploadFile(String filePath) {
        super.uploadFile(filePath);
    }

    @Override
    public void SetListener(onResult result) {
        super.SetListener(result);
    }
}
