package org.halvors.nuclearphysics.common.tile;

import net.minecraftforge.common.util.ForgeDirection;

public interface ITileRotatable {
    boolean canSetFacing(ForgeDirection facing);

    ForgeDirection getFacing();

    void setFacing(ForgeDirection facing);
}