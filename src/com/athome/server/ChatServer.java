package com.athome.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 服务端
 */
public class ChatServer {

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();       // 获取ServerSocketChannel
            serverSocketChannel.bind(new InetSocketAddress(8081));      // 绑定端口
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();                                     // 获取Selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);     // 将通道注册到选择器
            System.out.println("服务器已经启动了");

            for(;;) {
                int events = selector.select();                 // 获取selector中就绪状态
                if(events == 0) {
                    continue;
                }
                Set<SelectionKey> readyChannels = selector.selectedKeys();      // 获取就绪的通道
                Iterator<SelectionKey> channelIters = readyChannels.iterator();
                while(channelIters.hasNext()) {                             // 轮循遍历
                    SelectionKey channel = channelIters.next();
                    if(channel.isAcceptable()) {                // 可接收状态
                        acceptOperator(serverSocketChannel, selector);
                    } else if(channel.isReadable()) {           // 可读状态
                        readOperator(channel, selector);
                    }
                    channelIters.remove();                      // 移除已处理过的通道
                }
            }
        } catch (Exception e) {

        }

    }

    /**
     * 接入时的操作
     * @param serverSocketChannel
     * @param selector
     * @throws IOException
     */
    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);         // 通道注册到选择器
        ByteBuffer msgBuffer = Charset.forName("UTF-8").encode("欢迎进入聊天室，请注意隐私安全");
        socketChannel.write(msgBuffer);                     // 客户端回复消息
    }

    /**
     * 读状态时的操作
     * @param selectionKey
     * @param selector
     * @throws IOException
     */
    private void readOperator(SelectionKey selectionKey, Selector selector) throws IOException, ClassCastException {
        SocketChannel channel = (SocketChannel)selectionKey.channel();
        channel.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int readLength = channel.read(byteBuffer);          // 读入内存
        String message = "";
        if(readLength > 0) {
            byteBuffer.flip();                          // 转换为读模式
            message += Charset.forName("UTF-8").decode(byteBuffer);
        }
        channel.register(selector, SelectionKey.OP_READ);
        if(message.length() > 0) {
            System.out.println(message);
            castOtherClient(selector, selectionKey, message);
        }
    }

    /**
     * 消息广播到其它客户端
     * @param selector
     * @param selectionKey
     * @param message
     * @throws IOException
     */
    private void castOtherClient(Selector selector, SelectionKey selectionKey, String message) throws IOException, ClassCastException {
        SocketChannel channel = (SocketChannel)selectionKey.channel();
        Set<SelectionKey> channels = selector.keys();
        for (SelectionKey key : channels) {
            Channel curChannel = key.channel();
            if(curChannel instanceof SocketChannel && curChannel != channel)  {
                ((SocketChannel)curChannel).write(Charset.forName("UTF-8").encode(message));
            }

            // Error: 先判断是否为SocketChannel的实例，否则全部强转为SocketChannel会抛异常，导致服务器停止
            /*SocketChannel socketChannel = (SocketChannel) key.channel();
            if(socketChannel instanceof SocketChannel && socketChannel != channel) {
                socketChannel.write(Charset.forName("UTF-8").encode(message));
            }*/
        }
    }
}
