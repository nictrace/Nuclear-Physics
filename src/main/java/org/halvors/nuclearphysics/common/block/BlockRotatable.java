package org.halvors.nuclearphysics.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.halvors.nuclearphysics.common.tile.ITileRotatable;

public abstract class BlockRotatable extends BlockContainerBase {
    protected byte rotationMask = Byte.parseByte("111100", 2);
    protected boolean isFlipPlacement = false;

    protected BlockRotatable(String name, Material material) {
        super(name, material);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemIn) {
        final TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof ITileRotatable) {
            ITileRotatable tileRotatable = (ITileRotatable) tile;

            tileRotatable.setFacing(ForgeDirection.VALID_DIRECTIONS[determineOrientation(world, x, y, z, entity)]);
        }
    }

    @Override
    public ForgeDirection[] getValidRotations(World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        ForgeDirection[] valid = new ForgeDirection[6];

        if (tile instanceof ITileRotatable) {
            ITileRotatable tileRotatable = (ITileRotatable) tile;

            for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
                if (tileRotatable.canSetFacing(facing)) {
                    valid[facing.ordinal()] = facing;
                }
            }
        }

        return valid;
    }

    @Override
    public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection side) {
        final TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof ITileRotatable) {
            ITileRotatable tileRotatable = (ITileRotatable) tile;

            if (tileRotatable.canSetFacing(side)) {
                tileRotatable.setFacing(side);
            }

            return true;
        }

        return false;
    }

    public boolean canRotate(int ordinal) {
        return (rotationMask & 1 << ordinal) != 0;
    }

    public int determineOrientation(World world, int x, int y, int z, EntityLivingBase entity) {
        if (MathHelper.abs((float) entity.posX - x) < 2 && MathHelper.abs((float) entity.posZ - z) < 2) {
            double d0 = entity.posY + 1.82D - entity.yOffset;

            if (canRotate(1) && (d0 - y > 2)) {
                return 1;
            }

            if (canRotate(0) && (y - d0 > 0)) {
                return 0;
            }
        }

        int playerSide = MathHelper.floor_double(entity.rotationYaw * 4 / 360 + 0.5) & 0x3;
        int returnSide = playerSide == 3 && canRotate(4) ? 4 : playerSide == 2 && canRotate(3) ? 3 : playerSide == 1 && canRotate(5) ? 5 : playerSide == 0 && canRotate(2) ? 2 : 0;

        if (isFlipPlacement) {
            return ForgeDirection.getOrientation(returnSide).getOpposite().ordinal();
        }

        return returnSide;
    }
}