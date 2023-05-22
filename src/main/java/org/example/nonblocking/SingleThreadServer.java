package org.example.nonblocking;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SingleThreadServer {
    private Map<SocketChannel,Integer> socketChannels=new HashMap();
    private int clientsCount;
    public static void main(String[] args){
        new SingleThreadServer();
    }

    public SingleThreadServer(){
        this.startServer();
    }

    public void startServer(){
        try {
            Selector selector=Selector.open();
            ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost",1234));
            serverSocketChannel.configureBlocking(false);
            int validOps = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true){
                int readyChannels = selector.select();
                if (readyChannels==0) continue;
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        handleForAccept(selector,selectionKey);
                    }
                    else if(selectionKey.isConnectable()){
                        System.out.println("A new Connection has been established");
                    }
                    else if(selectionKey.isReadable()){
                        handleForRead(selector,selectionKey);
                    }
                    else if(selectionKey.isWritable()){
                        //System.out.println("a channel is ready for writing");
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleForAccept(Selector selector, SelectionKey selectionKey) throws IOException {
        System.out.println("Asking for new Connection");
        ServerSocketChannel serverSocketChannel= (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        ++clientsCount;
        socketChannels.put(socketChannel,clientsCount);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        sendMessage(String.format("Welcome you are the client number %s \n ",clientsCount),socketChannel);

    }

    private void handleForRead (Selector selector, SelectionKey selectionKey) throws IOException{

        System.out.println("A Channel is ready for Read");

        SocketChannel socketChannel= (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);

        int read = socketChannel.read(byteBuffer);

        if(read==-1){System.out.println("Client disconnected ....");

            socketChannels.remove(socketChannel);
            socketChannel.close();

            socketChannel.keyFor(selector).channel();

        } else{
            String request = new String(byteBuffer.array(), 0, read).trim();

            if(request.length()>0){
                int fromId=socketChannels.get(socketChannel);

                System.out.println("\tclient number "+fromId+" send : "+request);
                String message=request;

                List<Integer> destinationList=new ArrayList<>();

                String[] requestItems=request.split(">");

                // System.out.println(requestItems.length);

                if(requestItems.length==2){
                    String destination=requestItems[0];
                    message=requestItems[1];

                    if(destination.trim().contains(" ")){
                        String[] destinations=destination.trim().split(" ");
                        for(String d : destinations){
                            destinationList.add(Integer.parseInt(d));
                        }
                    } else{ destinationList.add(Integer.parseInt(destination)); }

                }
                broadCastMessage(message,socketChannel,destinationList);
            }

        }
    }

    private void broadCastMessage(String message, SocketChannel from, List<Integer> destinations) throws IOException {
        for (SocketChannel socketChannel:socketChannels.keySet()){
            int clientId=socketChannels.get(socketChannel);
            boolean all=destinations.size()==0;
            if(!socketChannel.equals(from) && (destinations.contains(clientId) || all)){
                ByteBuffer byteBufferResponse=ByteBuffer.allocate(2048);
                int fromId=socketChannels.get(from);
                String formattedMessage=String.format("[Client %s]: %s",fromId,message);
                byteBufferResponse.put(formattedMessage.getBytes());
                byteBufferResponse.flip();
                socketChannel.write(byteBufferResponse);
            }
        }
    }
    private void sendMessage(String message, SocketChannel socketChannel) throws IOException {
        System.out.println("Send new message => "+message);
        ByteBuffer byteBufferResponse=ByteBuffer.allocate(2048);
        byteBufferResponse.put(message.getBytes());
        byteBufferResponse.flip();
        socketChannel.write(byteBufferResponse);
    }

}