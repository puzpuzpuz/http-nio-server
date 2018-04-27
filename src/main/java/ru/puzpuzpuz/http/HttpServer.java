package ru.puzpuzpuz.http;

import ru.puzpuzpuz.http.handler.HttpRequestHandler;
import ru.puzpuzpuz.http.util.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static ru.puzpuzpuz.http.util.Constants.SHUTDOWN_TIMEOUT_MILLIS;

/**
 * The main class of the HTTP server. Uses Java NIO for non-blocking HTTP request handling.
 * Runs an event loop and orchestrates all other modules internally.
 * <p>
 * Warning: this and all corresponding classes are not thread-safe. The server is supposed to be run in
 * a single thread as a {@link Runnable}. Stopping the server with {@link Thread#interrupt()} is not guaranteed to
 * be properly working.
 * <p>
 * Also supports a simple graceful shutdown mode for the server with a fixed timeout. During the timeout
 * in this mode the server won't be accepting new connections.
 */
public final class HttpServer implements Runnable {

    private static final Logger LOGGER = new Logger(HttpServer.class.getName());

    private final ServerSettings settings;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private int connectionsNum;
    private volatile long shutdownSignalTime = -1L;

    public HttpServer(ServerSettings settings) {
        this.settings = settings;
    }

    @Override
    public void run() {
        try {
            init();
            startLoop();
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred. Stopping server", e);
        } finally {
            stop();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress((InetAddress) null, settings.getPort()));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // register a simple graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void start() {
                try {
                    LOGGER.info("Got shutdown signal. Switching to shutdown mode");
                    // can't use Thread#interrupt here because this affects Channel#write
                    HttpServer.this.shutdownSignalTime = System.currentTimeMillis();
                    Thread.sleep(SHUTDOWN_TIMEOUT_MILLIS);
                } catch (Exception e) {
                    LOGGER.warn("Error during shutdown. Forced shutdown", e);
                } finally {
                    LOGGER.info("Stopped");
                    Logger.resetFinally();
                }
            }
        });

        LOGGER.info("Server is now listening on port: " + settings.getPort());
    }

    private void stop() {
        try {
            LOGGER.info("Stopping server");
            selector.close();
            serverChannel.close();
        } catch (IOException e) {
            LOGGER.warn("Error during stopping server. Ignoring", e);
        }
    }

    private void startLoop() throws IOException {
        boolean needToStop = false;
        while (!needToStop) {
            boolean shutdownMode = shutdownSignalTime > 0;
            if (shutdownMode) {
                needToStop = System.currentTimeMillis() - shutdownSignalTime >= SHUTDOWN_TIMEOUT_MILLIS;
            }
            handleLoopTick(shutdownMode);
        }
    }

    private void handleLoopTick(boolean shutdownMode) throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();

        Iterator<SelectionKey> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            try {
                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    if (shutdownMode) {
                        continue;
                    }
                    accept();
                } else if (key.isReadable()) {
                    if (shutdownMode) {
                        continue;
                    }
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            } catch (Exception e) {
                LOGGER.error("Closing channel: error while handling selection key. Channel: " + key.channel(), e);
                closeChannelSilently(key);
            }
        }
    }

    private void accept() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel == null) {
            LOGGER.warn("No connection is available. Skipping selection key");
            return;
        }

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        HttpRequestHandler handler = (HttpRequestHandler) key.attachment();
        if (handler == null) {
            connectionsNum++;
            LOGGER.info("Got new connection handler for channel: " + clientChannel
                + ", connection #: " + connectionsNum);
            handler = HttpRequestHandler.build(settings, connectionsNum);
            key.attach(handler);
            return;
        }

        handler.read(clientChannel);

        // switch to write mode
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException {
        HttpRequestHandler handler = (HttpRequestHandler) key.attachment();
        if (handler == null) {
            throw new IOException("Handler is missing for the channel: " + key.channel());
        }

        SocketChannel clientChannel = (SocketChannel) key.channel();
        handler.write(clientChannel);

        if (handler.hasNothingToWrite()) {
            closeChannelSilently(key);
        } else {
            // keep writing
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void closeChannelSilently(SelectionKey key) {
        connectionsNum--;

        SocketChannel channel = (SocketChannel) key.channel();
        key.cancel();
        LOGGER.info("Closing connection for channel: " + channel + ", active connections: " + connectionsNum);

        HttpRequestHandler handler = (HttpRequestHandler) key.attachment();
        if (handler != null) {
            handler.releaseSilently();
        }

        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.warn("Error during closing channel: " + channel, e);
        }
    }
}
