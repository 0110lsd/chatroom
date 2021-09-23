package com.athome.client.users;

import com.athome.client.ChatClient;

public class UserJimmy {

    public static void main(String[] args) {
        try {
            new ChatClient().startClient("Jimmy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
