package NormalObjects;

import org.json.JSONArray;

public class Gif {

    private String url;
    private JSONArray dimensions;
    private int size;
    private String preview;
    private double duration;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONArray getDimensions() {
        return dimensions;
    }

    public void setDimensions(JSONArray dimensions) {
        this.dimensions = dimensions;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
