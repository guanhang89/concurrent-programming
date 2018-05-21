package chapter5;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOEchoServer {
    private Selector selector;
    private ExecutorService tp = Executors.newCachedThreadPool();
    public static Map<Socket, Long> time_start = new HashMap<>();

    private void startServer() throws IOException {
        selector = SelectorProvider.provider().openSelector();
        //获取服务端的SocketChannel实例
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //设置为费阻塞模式
        ssc.configureBlocking(false);

        //绑定端口
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), 8000);
        ssc.socket().bind(isa);
        //绑定到Selector上，注册感兴趣的事件为Accept
        //注册后Selector就能为这个Channel服务了
        //SelectionKey表示一对Selector和Channel的关系
        SelectionKey acceptKey = ssc.register(selector, SelectionKey.OP_ACCEPT);

        for(;;) {
            //是一个阻塞方法，如果当前数据没有准备好就一直等待。
            selector.select();
            Set readKeys = selector.selectedKeys();
            Iterator i = readKeys.iterator();
            long e = 0;
            while (i.hasNext()) {
                SelectionKey sk = (SelectionKey) i.next();
                //当处理一个SelectionKey之后务必要删除，以免重复处理
                i.remove();
                if (sk.isAcceptable()) {
                    //进行客户端的接收
                    doAccept(sk);
                } else if (sk.isValid() && sk.isReadable()) {
                    //判断Channel是否已经可读
                    if (!time_start.containsKey(((SocketChannel) sk.channel()).socket())) {
                        time_start.put(((SocketChannel) sk.channel()).socket(), System.currentTimeMillis());
                    }
                    doRead(sk);
                } else if (sk.isValid() && sk.isWritable()) {
                    //是否准备好进行写
                    doWrite(sk);
                    e = System.currentTimeMillis();
                    long b = time_start.remove(((SocketChannel) sk.channel()).socket());
                    System.out.println("spend:" + (e - b) + "ms");
                }
            }
        }
    }

    private void doWrite(SelectionKey sk) {
        SocketChannel channel = (SocketChannel) sk.channel();
        NIOEchClient echClient = (NIOEchClient) sk.attachment();
        LinkedList<ByteBuffer> outq = echClient.getOutq();

        ByteBuffer bb = outq.getLast();

        try {
            int len = channel.write(bb);
            if (len == -1) {
                disconnet(sk);
                return;
            }
            if (bb.remaining() == 0) {
                outq.removeLast();
            }
        } catch (IOException e) {
            System.out.println("Failed to write to client.");
            e.printStackTrace();
            disconnet(sk);
        }

        if (outq.size() == 0) {
            sk.interestOps(SelectionKey.OP_READ);
        }
    }

    private void doRead(SelectionKey sk) {
        SocketChannel channel = (SocketChannel) sk.channel();
        ByteBuffer bb = ByteBuffer.allocate(8192);
        int len;
        try {
            len = channel.read(bb);
            if (len < 0) {
                disconnet(sk);
            }
        } catch (IOException e) {
            System.out.println("Failed to read from clent.");
            e.printStackTrace();
            disconnet(sk);
        }
        bb.flip();
        tp.execute(new HandleMsg(sk,bb));

    }

    class HandleMsg implements Runnable {
        SelectionKey sk;
        ByteBuffer bb;

        public HandleMsg(SelectionKey sk, ByteBuffer bb) {
            this.sk = sk;
            this.bb = bb;
        }

        @Override
        public void run() {
            NIOEchClient echoClient = (NIOEchClient) sk.attachment();
            echoClient.enqueue(bb);
            sk.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }



    private void doAccept(SelectionKey sk) {
        //与客户端建立连接
        ServerSocketChannel server = (ServerSocketChannel) sk.channel();
        SocketChannel clientChannel;

        try {
            clientChannel = server.accept();
            //配置为非阻塞模式
            clientChannel.configureBlocking(false);
            //表示对读操作感兴趣
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            //共享client变量
            NIOEchClient echoClient = new NIOEchClient();
            clientKey.attach(echoClient);

            InetAddress clientAddress = clientChannel.socket().getInetAddress();
            System.out.println("Accepted connection from:" + clientAddress.getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class NIOEchClient {
        private LinkedList<ByteBuffer> outq;

        public NIOEchClient() {
            outq = new LinkedList<>();
        }

        public LinkedList<ByteBuffer> getOutq() {
            return outq;
        }

        public void enqueue(ByteBuffer byteBuffer) {
            outq.addFirst(byteBuffer);
        }
    }

    private void disconnet(SelectionKey sk) {
        sk.channel();
    }

}
