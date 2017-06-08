package org.halvors.quantum.lib.thermal;


import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import org.halvors.quantum.common.reactor.IReactor;
import org.halvors.quantum.common.transform.vector.VectorWorld;
import org.halvors.quantum.lib.event.ThermalEvent;
import universalelectricity.api.net.IUpdate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ThermalGrid implements IUpdate {
    private final float spread = 0.14285715F;
    private final float loss = 0.1F;
    private static final HashMap<VectorWorld, Float> thermalSource = new HashMap<>();
    private int tick = 0;
    private final float deltaTime = 0.05F;

    public static float getDefaultTemperature(VectorWorld position) {
        return ThermalPhysics.getTemperatureForCoordinate(position.world, position.intX(), position.intZ());
    }

    public static void addTemperature(VectorWorld position, float deltaTemperature) {
        float defaultTemperature = getDefaultTemperature(position);
        float original;

        if (thermalSource.containsKey(position)) {
            original = thermalSource.get(position);
        } else {
            original = defaultTemperature;
        }

        float newTemperature = original + deltaTemperature;

        if (Math.abs(newTemperature - defaultTemperature) > 0.4D) {
            thermalSource.put(position, original + deltaTemperature);
        } else {
            thermalSource.remove(position);
        }
    }

    public static float getTemperature(VectorWorld position) {
        if (thermalSource.containsKey(position)) {
            return thermalSource.get(position);
        }

        return ThermalPhysics.getTemperatureForCoordinate(position.world, position.intX(), position.intZ());
    }

    public void update() {
        Iterator<Map.Entry<VectorWorld, Float>> it = new HashMap<>(thermalSource).entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<VectorWorld, Float> entry = (Map.Entry)it.next();
            VectorWorld pos = entry.getKey();
            float currentTemperature = getTemperature(pos);

            if (currentTemperature < 0.0F) {
                thermalSource.remove(pos);
            } else {
                float deltaFromEquilibrium = getDefaultTemperature(pos) - currentTemperature;

                TileEntity possibleReactor = pos.getTileEntity();
                boolean isReactor = false;

                if (possibleReactor != null && possibleReactor instanceof IReactor) {
                    isReactor = true;
                } else {
                    isReactor = false;
                }

                ThermalEvent.ThermalEventUpdate event = new ThermalEvent.ThermalEventUpdate(pos, currentTemperature, deltaFromEquilibrium, 0.05F, isReactor);
                MinecraftForge.EVENT_BUS.post(event);

                float loss = event.heatLoss;
                addTemperature(pos, (deltaFromEquilibrium > 0.0F ? 1 : -1) * Math.min(Math.abs(deltaFromEquilibrium), Math.abs(loss)));

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    VectorWorld adjacent = (VectorWorld) pos.clone().translate(dir);
                    float deltaTemperature = getTemperature(pos) - getTemperature(adjacent);

                    Material adjacentMat = adjacent.world.getBlock(adjacent.intX(), adjacent.intY(), adjacent.intZ()).getMaterial();

                    float spread = (adjacentMat.isSolid() ? 0.14285715F : 0.14285715F / 2.0F) * 0.05F;

                    if (deltaTemperature > 0.0F) {
                        addTemperature(adjacent, deltaTemperature * spread);
                        addTemperature(pos, -deltaTemperature * spread);
                    }
                }
            }
        }
    }

    public boolean canUpdate() {
        return true;
    }

    public boolean continueUpdate() {
        return true;
    }
}