package com.kamildanak.minecraft.enderpay.events;

import com.kamildanak.minecraft.enderpay.Utils;
import com.kamildanak.minecraft.enderpay.economy.Account;
import com.kamildanak.minecraft.enderpay.network.PacketDispatcher;
import com.kamildanak.minecraft.enderpay.network.client.MessageBalance;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;

public class EventHandler {
    private static long lastTickEvent = 0;

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().worldObj.isRemote) {
            Account account = Account.get((EntityPlayer) event.getEntity());
            account.update();
            long balance = account.getBalance();
            PacketDispatcher.sendTo(new MessageBalance(balance), (EntityPlayerMP) event.getEntity());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerSaveToFile(PlayerEvent.SaveToFile event) {
        Account account = Account.get(event.getEntityPlayer());
        try {
            account.writeIfChanged();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onServerTickEvent(TickEvent.ServerTickEvent event) {
        long now = Utils.getCurrentDay();
        if (lastTickEvent == now) return;
        lastTickEvent = now;
        IntegratedServer integratedServer = Minecraft.getMinecraft().getIntegratedServer();
        if (integratedServer == null) return;
        for (EntityPlayerMP entityPlayer : integratedServer.getPlayerList().getPlayerList()) {
            Account account = Account.get(entityPlayer);
            if (account.update())
                PacketDispatcher.sendTo(new MessageBalance(account.getBalance()), entityPlayer);
        }
    }
}
