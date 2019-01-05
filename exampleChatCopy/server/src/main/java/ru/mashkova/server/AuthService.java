package ru.mashkova.server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
}