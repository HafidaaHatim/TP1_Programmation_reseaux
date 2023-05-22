package org.example.blocking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MultiThreadChatServer extends Thread {
    private List<Conversation> conversation = new ArrayList<>();

    int ClientsCount=0;
    public static void main(String[] args) {
        new MultiThreadChatServer().start();

    }
    public void run(){
        try {
            ServerSocket ServerSocket = new ServerSocket(1234);
            while (true){
                Socket socket = ServerSocket.accept();
                ++ClientsCount;
                Conversation conversations =new Conversation(socket,ClientsCount);
                conversation.add(conversations);
                conversations.start();


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    class Conversation extends  Thread {

        private Socket socket;
        private int clientID;

        Conversation(Socket socket, int clientId) {
            this.socket = socket;
            this.clientID = clientId;
        }

        public void run() {

            try {

                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os, true);
                String ip = socket.getRemoteSocketAddress().toString();
                System.out.println("New client connection =>" + clientID + "IP=" + ip);
                pw.println("welcome your ID is =>" + clientID);
                String requester;
                while ((requester = br.readLine()) != null) {
                    System.out.println("New Request : " + requester);
                    List<Integer> clientsTo = new ArrayList<>();
                    String message;
                    if (requester.contains("=>")) {
                        String[] items = requester.split("=>");
                        String clients = items[0];
                        message = items[1];
                        if (clients.contains(",")) {
                            String[] clientIds = clients.split(",");
                            for (String id : clientIds) {
                                clientsTo.add(Integer.parseInt(id));

                            }
                        } else {
                            clientsTo.add(Integer.parseInt(clients));

                        }


                    } else {
                        clientsTo = conversation.stream().map(conversation1 -> conversation1.clientID).collect(Collectors.toList());
                        message = requester;
                    }
                    brodcastMessage(message, this, clientsTo);


                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    public void brodcastMessage(String message,Conversation from,List<Integer>clients){


            try {
                for (Conversation conversation: conversation) {
                    if(conversation!=from && clients.contains(conversation.clientID)){
                    Socket socket = conversation.socket;
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter printWriter=new PrintWriter(outputStream,true);
                    printWriter.println(message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}


