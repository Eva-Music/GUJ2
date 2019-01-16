package ru.mashkova.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService(){
        return authService;
    }

    public Server(){
        clients = new Vector<>();
        authService = new SimpleAuthService();

        try(ServerSocket serverSocket = new ServerSocket(8189)){
            while(true){
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void broadcast(String msg){
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    public void privateMsg(ClientHandler sender, String reciever, String msg){
        if(sender.getNickname().equals(reciever)){
            sender.sendMsg("Note for me: " + msg);
            return;
        }
        for (ClientHandler client : clients) {
            if(client.getNickname().equals(reciever)){
                client.sendMsg("private message from "+sender.getNickname()+": "+msg);
                sender.sendMsg("private message to " + reciever +": "+ msg);
                return;
            }
        }
        sender.sendMsg("Client "+ reciever + " not found");

    }

    public void broadcastClientsList(){
        StringBuilder sb = new StringBuilder(10*clients.size());
        sb.append("/clients ");
        for (ClientHandler client : clients) {
            sb.append(client.getNickname()+ " ");
        }
        sb.setLength(sb.length()-1);
        String out = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMsg(out);
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientsList();
    }


    public boolean isNickBusy(String nick){
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nick)){
                return true;
            }
        }
        return false;
    }

}