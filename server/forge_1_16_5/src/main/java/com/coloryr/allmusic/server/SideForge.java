package com.coloryr.allmusic.server;

import com.coloryr.allmusic.codec.MusicPack;
import com.coloryr.allmusic.codec.MusicPacketCodec;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.command.PermissionList;
import com.coloryr.allmusic.server.core.objs.music.PlayerAddMusicObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.side.BaseSide;
import com.coloryr.allmusic.server.event.MusicAddEvent;
import com.coloryr.allmusic.server.event.MusicPlayEvent;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

public class SideForge extends BaseSide {

    @Override
    public void runTask(Runnable run) {
        AllMusicServer.server.execute(run);
    }

    @Override
    public void runTask(Runnable run1, int delay) {
        Tasks.add(new TaskItem() {{
            tick = delay;
            run = run1;
        }});
    }

    @Override
    public boolean checkPermission(Object player, String permission) {
        CommandSourceStack source = (CommandSourceStack) player;
        // 先检查是否为管理员（控制台或allmusic.admin权限）
        if (checkPermission(player)) {
            return true;
        }
        // 再检查具体权限节点
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.hasPermissions(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkPermission(Object player) {
        CommandSourceStack source = (CommandSourceStack) player;
        // 先检查LuckPerms权限节点，支持通过allmusic.admin授予管理员权限
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.hasPermissions(PermissionList.PERMISSION_ADMIN)) {
                return true;
            }
        }
        // 回退到命令等级2检查（兼容无权限插件的场景，控制台也会通过此检查）
        return source.hasPermission(2);
    }

    @Override
    public boolean isPlayer(Object player) {
        CommandSourceStack source = (CommandSourceStack) player;
        return source.getEntity() instanceof Player;
    }

    @Override
    public boolean needPlay(boolean islist) {
        for (ServerPlayer player : AllMusicServer.server.getPlayerList().getPlayers()) {
            if (!AllMusic.isSkip(player.getName().getString(), null, false, islist)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<?> getPlayers() {
        return AllMusicServer.server.getPlayerList().getPlayers();
    }

    @Override
    public String getPlayerName(Object player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer player1 = (ServerPlayer) player;
            return player1.getName().getString();
        }

        return null;
    }

    @Override
    public String getPlayerServer(Object player) {
        return null;
    }

    @Override
    public void send(Object player, MusicPack pack) {
        if (player instanceof ServerPlayer) {
            ServerPlayer player1 = (ServerPlayer) player;
            send(player1, MusicPacketCodec.pack(pack));
        }
    }

    @Override
    public Object getPlayer(String player) {
        return AllMusicServer.server.getPlayerList().getPlayerByName(player);
    }

    @Override
    public void sendBar(Object player, Component data) {
        if (player instanceof ServerPlayer) {
            ServerPlayer player1 = (ServerPlayer) player;
            net.minecraft.network.chat.Component textComponent = AllMusicServer.parse(data);
            ClientboundSetTitlesPacket pack = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.ACTIONBAR, textComponent);
            player1.connection.send(pack);
        }
    }

    @Override
    public File getFolder() {
        return new File(AllMusic.SERVER_DIR);
    }

    @Override
    public void broadcast(Component message) {
        net.minecraft.network.chat.Component textComponent = AllMusicServer.parse(message);
        for (ServerPlayer player : AllMusicServer.server.getPlayerList().getPlayers()) {
            if (!AllMusic.isSkip(player.getName().getString(), null, false)) {
                player.sendMessage(textComponent, UUID.randomUUID());
            }
        }
    }

    @Override
    public void sendMessage(Object obj, Component message) {
        if (obj instanceof CommandSourceStack) {
            CommandSourceStack sender = (CommandSourceStack) obj;
            net.minecraft.network.chat.Component textComponent = AllMusicServer.parse(message);
            sender.sendSuccess(textComponent, false);
        }
    }


    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        MusicPlayEvent event = new MusicPlayEvent(obj);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Override
    public boolean onMusicAdd(Object obj, PlayerAddMusicObj music) {
        MusicAddEvent event = new MusicAddEvent(music, (CommandSourceStack) obj);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    private void send(ServerPlayer players, ByteBuf data) {
        if (players == null)
            return;
        runTask(() -> PacketDistributor.PLAYER.with(() -> players)
                .send(new ClientboundCustomPayloadPacket(AllMusicServer.channel, new FriendlyByteBuf(data))));
    }
}
