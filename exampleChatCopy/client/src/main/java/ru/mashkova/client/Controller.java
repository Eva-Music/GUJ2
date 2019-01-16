package ru.mashkova.client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

@FXML HBox authPanel, msgPanel;
@FXML TextField loginField, msgField;
@FXML PasswordField passField;
@FXML ListView<String> clientsList;
@FXML TextArea textArea;



private Socket socket;
private DataOutputStream out;
private DataInputStream in;
private boolean authentificated;


public void setAuthentificated(boolean authentificated){
    this.authentificated = authentificated;

    authPanel.setManaged(!authentificated);
    authPanel.setVisible(!authentificated);

    msgPanel.setVisible(authentificated);
    msgPanel.setManaged(authentificated);
    clientsList.setVisible(authentificated);
    clientsList.setManaged(authentificated);
}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
     setAuthentificated(false);

     clientsList.setOnMouseClicked(event -> {
         if(event.getClickCount() == 2){
             String nickname = clientsList.getSelectionModel().getSelectedItem();
             msgField.setText("/w " + nickname + " ");
             msgField.requestFocus();
             msgField.selectEnd();
         }
     });
    }

    public void sendAuth(){
        try {
            if (socket==null || socket.isClosed()){
                connect();
              }
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        try {
            setAuthentificated(false);
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t = new Thread(()->{
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if( msg.startsWith("/authok")){
                            setAuthentificated(true);
                            String[] tokens = msg.split("\\s");
                            String nickname = tokens[1];
                            break;
                        }
                    }

                    while (true){
                        String msg = in.readUTF();
                        if(msg.startsWith("/")){
                            if(msg.startsWith("/clients")){
                                String[] tokens = msg.split("\\s");
                                Platform.runLater(()->{
                                    clientsList.getItems().clear();
                                    for (int i = 1; i < tokens.length; i++) {
                                        clientsList.getItems().add(tokens[i]);
                                    }
                                });
                            }
                            if(msg.equals("/end")){
                                break;
                            }
                            continue;
                        }
                        textArea.appendText(msg + "\n");
                    }
                } catch (IOException e){
                    showAlert("Connection failed");
                } finally {
                    closeConnection();
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(){
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAlert(String msg){
        Platform.runLater(()->{
            Alert al = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            al.showAndWait();
        });
    }

    public void closeConnection(){
    setAuthentificated(false);
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

    public void changeFocus(){
    passField.requestFocus();
    }


}
