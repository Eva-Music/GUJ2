package ru.mashkova.server;

import org.omg.IOP.IOR;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler {

    private String nickname;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Date currentTime;
    private DateFormat df;


    public String getNickname(){
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.df = new SimpleDateFormat("HH:mm:ss");

            new Thread(()->{
                try {
                    while (true){
                        String msg = in.readUTF();
                        if (msg.startsWith("/auth ")){
                            String[] tokens = msg.split("\\s");
                            String nick = server.getAuthService().getNicknameByLoginAndPassword(tokens[1],tokens[2]);
                            if (nick != null && !server.isNickBusy(nick))
                                sendMsg("/authok " + nick);
                                nickname = nick;
                                server.subscribe(this);
                                break;
                        }
                    }

                    while (true){
                        String msg = in.readUTF();
                        if(msg.startsWith("/")) {
                            if (msg.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (msg.startsWith("/w ")) {
                                String[] tokens = msg.split("\\s", 3);
                                if(tokens.length != 3) {
                                    continue;
                                }
                                server.privateMsg(this, tokens[1], tokens[2]);
                            }
                        }
                        currentTime = new Date();
                        server.broadcast("[" + df.format(currentTime) + "] " + nickname + ": " + msg);
                    }

                }catch (IOException e){
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            }).start();


        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
     server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}