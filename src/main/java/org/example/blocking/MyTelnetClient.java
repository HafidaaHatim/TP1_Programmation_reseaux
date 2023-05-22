package org.example.blocking;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class MyTelnetClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost",1234);
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os,true);
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         String reponse;

                     while ((reponse=br.readLine())!= null) {

                         System.out.println(reponse);
                     }
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }


                     }


             }).start();
            Scanner scanner=new Scanner(System.in);

            while (true){
            String request =scanner.nextLine();
            pw.println(request);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
