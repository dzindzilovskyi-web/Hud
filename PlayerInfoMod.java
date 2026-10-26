package com.example.playerinfo;

import net.fabricmc.api.ClientModInitializer;

public class PlayerInfoMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("PlayerInfo loaded");
    }
}
