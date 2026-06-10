package com.coloryr.allmusic.server;

import com.coloryr.allmusic.codec.MusicPack;
import com.coloryr.allmusic.comm.MusicCodec;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.command.PermissionList;
import com.coloryr.allmusic.server.core.objs.music.PlayerAddMusicObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.side.BaseSide;
import com.coloryr.allmusic.server.event.MusicAddEvent;
import com.coloryr.allmusic.server.event.MusicPlayEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.util.Collection;

public class SideNeoForge extends BaseSide {
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
        return source.isPlayer();
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
        if (player instanceof ServerPlayer player1) {
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
        if (player instanceof ServerPlayer player1) {
            send(player1, pack);
        }
    }

    @Override
    public Object getPlayer(String player) {
        return AllMusicServer.server.getPlayerList().getPlayerByName(player);
    }

    @Override
    public void sendBar(Object player, net.kyori.adventure.text.Component data) {
        if (player instanceof ServerPlayer player1) {
            AllMusicServer.audiences.audience(player1).sendActionBar(data);
        }
    }

    @Override
    public File getFolder() {
        return new File(AllMusic.SERVER_DIR);
    }

    @Override
    public void broadcast(net.kyori.adventure.text.Component message) {
        for (ServerPlayer player : AllMusicServer.server.getPlayerList().getPlayers()) {
            if (!AllMusic.isSkip(player.getName().getString(), null, false)) {
                AllMusicServer.audiences.audience(player).sendMessage(message);
            }
        }
    }

    @Override
    public void sendMessage(Object obj, net.kyori.adventure.text.Component message) {
        if (obj instanceof CommandSourceStack source) {
            AllMusicServer.audiences.audience(source).sendMessage(message);
        }
    }

    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        MusicPlayEvent event = new MusicPlayEvent(obj);
        NeoForge.EVENT_BUS.post(event);
        return event.isCancel();
    }

    @Override
    public boolean onMusicAdd(Object obj, PlayerAddMusicObj music) {
        CommandSourceStack source = (CommandSourceStack) obj;
        MusicAddEvent event = new MusicAddEvent(music, source.getPlayer());
        NeoForge.EVENT_BUS.post(event);
        return event.isCancel();
    }

    private void send(ServerPlayer players, MusicPack data) {
        if (players == null)
            return;
        try {
            runTask(() -> PacketDistributor.sendToPlayer(players, new MusicCodec(data)));
        } catch (Exception e) {
            AllMusic.log.data("§c数据发送发生错误");
            e.printStackTrace();
        }
    }
}
