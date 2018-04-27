package ru.puzpuzpuz.http.request;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import static ru.puzpuzpuz.http.util.Constants.SOCKET_READ_BUFFER_SIZE_BYTES;
import static ru.puzpuzpuz.http.util.Constants.SOCKET_READ_DATA_LIMIT_BYTES;

/**
 * A reader for data incoming over the socket. Does bufferized reading of the data and converts it to text.
 */
public final class RawRequestReader {

    private final ByteBuffer readBuffer = ByteBuffer.allocate(SOCKET_READ_BUFFER_SIZE_BYTES);

    public String readRaw(ReadableByteChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        readBuffer.clear();
        int read;
        int totalRead = 0;
        while ((read = channel.read(readBuffer)) > 0) {
            totalRead += read;
            if (totalRead > SOCKET_READ_DATA_LIMIT_BYTES) {
                throw new IOException("Request data limit exceeded");
            }

            readBuffer.flip();
            byte[] bytes = new byte[readBuffer.limit()];
            readBuffer.get(bytes);
            sb.append(new String(bytes));
            readBuffer.clear();
        }

        if (read < 0) {
            throw new IOException("End of input stream. Connection is closed by the client");
        }

        return sb.toString();
    }

}
