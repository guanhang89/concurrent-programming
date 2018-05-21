package chapter5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class NIOClient {

    private Selector selector;

    public void init(String ip, int port) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            this.selector = SelectorProvider.provider().openSelector();
            channel.connect(new InetSocketAddress(ip, port));
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void working() throws IOException {
        while (true) {
            if (!selector.isOpen()) {
                break;
            }
            selector.select();
            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isConnectable()) {
                    connect(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        channel.read(buffer);
        byte[] array = buffer.array();
        String msg = new String(array).trim();
        System.out.println("客户端收到：" + msg);
        channel.close();
        key.selector().close();
    }


    public void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.write(ByteBuffer.wrap(new String("hello server").getBytes()));
        channel.register(this.selector, SelectionKey.OP_READ);
    }
}
