package com.athome.client.users;

import com.athome.client.ChatClient;

public class UserAmy {

    public static void main(String[] args) {
        try {
            new ChatClient().startClient("Amy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
