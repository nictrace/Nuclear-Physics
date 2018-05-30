package org.halvors.nuclearphysics.common.tile.machine;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom.Item;

import org.halvors.nuclearphysics.api.recipe.QuantumAssemblerRecipes;
import org.halvors.nuclearphysics.common.NuclearPhysics;
import org.halvors.nuclearphysics.common.ConfigurationManager.Assembler;
import org.halvors.nuclearphysics.common.block.machine.BlockMachine.EnumMachine;
import org.halvors.nuclearphysics.common.capabilities.energy.EnergyStorage;
import org.halvors.nuclearphysics.common.init.ModSounds;
import org.halvors.nuclearphysics.common.network.packet.PacketTileEntity;
import org.halvors.nuclearphysics.common.tile.TileInventoryMachine;
import org.halvors.nuclearphysics.common.utility.OreDictionaryHelper;

import cpw.mods.fml.common.registry.GameRegistry;

public class TileQuantumAssembler extends TileInventoryMachine {
    private static final int ENERGY_PER_TICK = 2048000;
    public static final int TICKS_REQUIRED = 120 * 20;

    // Used for rendering.
    private EntityItem entityItem = null;
    private float rotationYaw1, rotationYaw2, rotationYaw3;

    public TileQuantumAssembler() {
        this(EnumMachine.QUANTUM_ASSEMBLER);
    }

    public TileQuantumAssembler(final EnumMachine type) {
        super(type, 7);

        energyStorage = new EnergyStorage(ENERGY_PER_TICK * 2);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!worldObj.isRemote) {
            if (canFunction() && canProcess() && energyStorage.extractEnergy(ENERGY_PER_TICK, true) >= ENERGY_PER_TICK) {
                if (operatingTicks < TICKS_REQUIRED) {
                    operatingTicks++;
                } else {
                    process();
                    reset();
                }

                energyUsed = energyStorage.extractEnergy(ENERGY_PER_TICK, false);
            } else if (getStackInSlot(6) == null) {
                reset();
            }

            if (worldObj.getWorldTime() % 10 == 0) {
                NuclearPhysics.getPacketHandler().sendToReceivers(new PacketTileEntity(this), this);
            }
        } else  {
            if (operatingTicks > 0) {
                if (worldObj.getWorldTime() % 600 == 0) {
                    worldObj.playSoundEffect(xCoord, yCoord, zCoord, ModSounds.ASSEMBLER, 0.7F, 1);
                }

                rotationYaw1 += 3;
                rotationYaw2 += 2;
                rotationYaw3 += 1;
            }

            final ItemStack itemStack = getStackInSlot(6);

            if (itemStack != null) {
                if (entityItem == null || !itemStack.isItemEqual(entityItem.getEntityItem())) {
                    entityItem = getEntityForItem(itemStack);
                }
            } else {
                entityItem = null;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean isItemValidForSlot(final int slot, final ItemStack itemStack) {
        switch (slot) {
            case 6:
                return QuantumAssemblerRecipes.hasRecipe(itemStack);
        }

        return OreDictionaryHelper.isDarkmatterCell(itemStack);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean canProcess() {
        final ItemStack itemStack = getStackInSlot(6);	// template and result

        if (itemStack != null) {
        	if(Assembler.whiteList.length == 0 && Assembler.blackList.length == 0)
        		if (QuantumAssemblerRecipes.hasRecipe(itemStack)) {	// if cloning enabled
        			for (int i = 0; i <= 5; i++) {
        				final ItemStack itemStackInSlot = getStackInSlot(i);

        				if (!OreDictionaryHelper.isDarkmatterCell(itemStackInSlot)) {
        					return false;							// slots 0-5 must contains dark matter cells
        				}
        			}
        		}
        		else return false;	// item not in recipes
        	else if(Assembler.whiteList.length > 0) {	// whitelist present
        		checkForLists(itemStack);
        	}
            return itemStack.stackSize < 64;	// and target slot must have free space (if template have an NBT, cannot copy it)
        }

        return false;
    }

    // Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack.
    private void process() {
        if (canProcess()) {
            for (int slot = 0; slot <= 5; slot++) {
                ItemStack itemStack = getStackInSlot(slot);

                if (itemStack != null) {
                	NuclearPhysics.getLogger().warn("*** TileQuantum: slot " + slot + " decreasing...");
                    decrStackSize(slot, 1);
                }
            }

            final ItemStack resultStack = getStackInSlot(6);

            if (resultStack != null) {
                resultStack.stackSize++;
                markDirty();					// repaint
            }
        }
    }

    private EntityItem getEntityForItem(final ItemStack itemStack) {
        final EntityItem entityItem = new EntityItem(worldObj, 0, 0, 0, itemStack.copy());
        entityItem.setAgeToCreativeDespawnTime();

        return entityItem;
    }

    public EntityItem getEntityItem() {
        return entityItem;
    }

    public float getRotationYaw1() {
        return rotationYaw1;
    }

    public float getRotationYaw2() {
        return rotationYaw2;
    }

    public float getRotationYaw3() {
        return rotationYaw3;
    }
    /**
     * Check white/black lists and RecipeRegistry...
     * @return
     */
    protected boolean checkForLists(final ItemStack his) {
    	if(Assembler.whiteList.length == 0 && Assembler.blackList.length == 0 && QuantumAssemblerRecipes.hasRecipe(his)) {
    		return true;
    	}
    	else if(Assembler.whiteList.length > 0) {
    		if(inList(Assembler.whiteList, his)) return true;
    	}
    	else if(Assembler.blackList.length > 0) {
    		if(!inList(Assembler.blackList, his)) return true;
    	}
    	return false;
    }
    
    /**
     * Better solution is to save all elements as IDs & metas, and compare with this
     * @param list
     * @param itemStack
     * @return
     */
    protected boolean inList(String[] list, ItemStack itemStack) {
    	String[] parts;
    	String meta = "0";
    	net.minecraft.item.Item found;
    	
    	for(String itm_name : list) {
			parts = itm_name.split(":");
    		found = GameRegistry.findItem(parts[0], parts[1]);
    		if(parts.length == 3) meta = parts[2];
    		if(found != null) if(found.equals(itemStack.getItem()) && itemStack.getMetadata() == Integer.valueOf(meta)) return true;
    	}
    	return false;
    }
}