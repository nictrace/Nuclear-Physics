package org.halvors.quantum.common.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.HashSet;
import java.util.Set;

public class TileQuantum extends TileEntity {
    /** The players currently using this block. */
    private final Set<EntityPlayer> playersUsing = new HashSet<>();

    public void open(EntityPlayer player) {
        playersUsing.add(player);
    }

    public void close(EntityPlayer player) {
        playersUsing.remove(player);
    }

    public Set<EntityPlayer> getPlayersUsing() {
        return playersUsing;
    }
}