package org.halvors.nuclearphysics.common.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.*;
import org.halvors.nuclearphysics.common.NuclearPhysics;
import org.halvors.nuclearphysics.common.Reference;
import org.halvors.nuclearphysics.common.block.fluid.BlockFluidPlasma;
import org.halvors.nuclearphysics.common.block.fluid.BlockFluidRadioactive;
import org.halvors.nuclearphysics.common.event.handler.ItemEventHandler;
import org.halvors.nuclearphysics.common.utility.FluidUtility;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModFluids {
    /**
     * The fluids registered by this mod. Includes fluids that were already registered by another mod.
     */
    public static final Set<Fluid> fluids = new HashSet<>();

    /**
     * The fluid blocks from this mod only. Doesn't include blocks for fluids that were already registered by another mod.
     */
    public static final Set<IFluidBlock> fluidBlocks = new HashSet<>();

    public static final Fluid deuterium = createFluid("deuterium",
            fluid -> fluid.setDensity(1110), // Density: 1.11 g/cm3
            fluid -> new BlockFluidClassic(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final Fluid uraniumHexaflouride = createFluid("uranium_hexafluoride",
            fluid -> fluid.setDensity(5090).setGaseous(true), // Density: 5.09 g/cm3
            fluid -> new BlockFluidClassic(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final Fluid plasma = createFluid("plasma",
            fluid -> fluid.setLuminosity(7).setGaseous(true), // Density: ? g/cm3
            fluid -> new BlockFluidPlasma(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final Fluid steam = createFluid("steam",
            fluid -> fluid.setDensity(1).setGaseous(true), // Density: 0.0006 g/cm3
            fluid -> new BlockFluidClassic(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final Fluid tritium = createFluid("tritium",
            fluid -> fluid.setDensity(1215).setLuminosity(15), // Density: 1.215 g/cm3
            fluid -> new BlockFluidRadioactive(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final Fluid toxicWaste = createFluid("toxic_waste",
            fluid -> fluid.setDensity(8900), // Density: 8.9 g/cm3
            fluid -> new BlockFluidRadioactive(fluid, new MaterialLiquid(MapColor.adobeColor)));

    public static final FluidStack fluidStackDeuterium = new FluidStack(FluidRegistry.getFluid("deuterium"), 0);
    public static final FluidStack fluidStackUraniumHexaflouride = new FluidStack(FluidRegistry.getFluid("uranium_hexafluoride"), 0);
    public static final FluidStack fluidStackPlasma = new FluidStack(FluidRegistry.getFluid("plasma"), 0);
    public static final FluidStack fluidStackSteam = new FluidStack(FluidRegistry.getFluid("steam"), 0);
    public static final FluidStack fluidStackTritium = new FluidStack(FluidRegistry.getFluid("tritium"), 0);
    public static final FluidStack fluidStackToxicWaste = new FluidStack(FluidRegistry.getFluid("toxic_waste"), 0);
    public static final FluidStack fluidStackWater = new FluidStack(FluidRegistry.WATER, 0);

    /**
     * Create a {@link Fluid} and its {@link IFluidBlock}, or use the existing ones if a fluid has already been registered with the same name.
     *
     * @param name                 The name of the fluid
     * @param fluidPropertyApplier A function that sets the properties of the {@link Fluid}
     * @param blockFactory         A function that creates the {@link IFluidBlock}
     * @return The fluid and block
     */
    private static <T extends Block & IFluidBlock> Fluid createFluid(String name, Consumer<Fluid> fluidPropertyApplier, Function<Fluid, T> blockFactory) {
        Fluid fluid = new Fluid(name);
        final boolean useOwnFluid = FluidRegistry.registerFluid(fluid);

        if (useOwnFluid) {
            fluidPropertyApplier.accept(fluid);
            fluidBlocks.add(blockFactory.apply(fluid));
        } else {
            fluid = FluidRegistry.getFluid(name);
        }

        fluids.add(fluid);

        return fluid;
    }

    public static void registerFluids() {
        for (final Fluid fluid : fluids) {
            FluidRegistry.registerFluid(fluid);
        }

        for (final IFluidBlock fluidBlock : fluidBlocks) {
            final Block block = (Block) fluidBlock;
            final Fluid fluid = fluidBlock.getFluid();

            block.setUnlocalizedName(fluid.getUnlocalizedName());
            block.setTextureName(Reference.PREFIX + "fluids/" + fluid.getName() + "_still");
            block.setCreativeTab(NuclearPhysics.getCreativeTab());
            block.setCreativeTab(NuclearPhysics.getCreativeTab());

            if (fluid == plasma) {
                block.setCreativeTab(NuclearPhysics.getCreativeTab());
            }

            GameRegistry.registerBlock(block, fluid.getName());
        }

        registerFluidContainers();
    }

    private static void registerFluidContainers() {
        FluidContainerRegistry.registerFluidContainer(FluidRegistry.WATER, FluidUtility.getFilledCell(FluidRegistry.WATER), new ItemStack(ModItems.itemCell));

        for (final Fluid fluid : fluids) {
            if (fluid == deuterium || fluid == tritium) {
                FluidContainerRegistry.registerFluidContainer(fluid, FluidUtility.getFilledCell(fluid), new ItemStack(ModItems.itemCell));
            } else if (fluid == toxicWaste) {
                ItemBucket itemBucket = (ItemBucket) ModItems.itemToxicWasteBucket;

                ItemEventHandler.registerBucket(fluid, itemBucket);
                FluidContainerRegistry.registerFluidContainer(fluid, new ItemStack(itemBucket), FluidContainerRegistry.EMPTY_BUCKET);
            }
        }
    }

    /*
    @EventBusSubscriber
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            final IForgeRegistry<Block> registry = event.getRegistry();

            for (final IFluidBlock fluidBlock : fluidBlocks) {
                final Block block = (Block) fluidBlock;
                final Fluid fluid = fluidBlock.getFluid();

                block.setUnlocalizedName(fluid.getUnlocalizedName());
                block.setRegistryName(fluid.getName());

                if (fluid == plasma) {
                    block.setCreativeTab(NuclearPhysics.getCreativeTab());
                }

                registry.register(block);
            }
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            final IForgeRegistry<Item> registry = event.getRegistry();

            for (final IFluidBlock fluidBlock : fluidBlocks) {
                registry.register(new ItemBlockTooltip((Block) fluidBlock));
            }

            registerFluidContainers();
        }
    }
    */
}