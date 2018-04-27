package ru.puzpuzpuz.http.fs;

/**
 * A POJO for file metadata.
 */
public final class FileMetadata {

    private final long size;
    private final String filePath;

    public FileMetadata(long size, String filePath) {
        this.size = size;
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
            "size=" + size +
            ", filePath='" + filePath + '\'' +
            '}';
    }
}
