package Retrofit;

import androidx.annotation.NonNull;

public class Response {

    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private String size;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDownloadUri() {
        return fileDownloadUri;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @NonNull
    @Override
    public String toString() {
        return "fileName: " + fileName + " fileDownloadUri: " + fileDownloadUri + " fileType: " + fileType + " size: " + size;
    }
}
