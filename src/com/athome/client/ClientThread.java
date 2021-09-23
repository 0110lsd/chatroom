package com.athome.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ClientThread implements Runnable {

    private Selector selector;

    public ClientThread(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
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
                    } else if(channel.isReadable()) {           // 可读状态
                        readOperator(channel, selector);
                    }
                    channelIters.remove();                      // 移除已处理过的通道
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
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
        int readLength = channel.read(byteBuffer);
        String message = "";
        if(readLength > 0) {
            byteBuffer.flip();
            message += Charset.forName("UTF-8").decode(byteBuffer);
        }
        channel.register(selector, SelectionKey.OP_READ);
        if(message.length() > 0) {
            System.out.println(message);
        }
    }
}
