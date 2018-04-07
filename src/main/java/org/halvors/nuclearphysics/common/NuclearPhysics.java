package org.halvors.nuclearphysics.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.halvors.nuclearphysics.common.entity.EntityParticle;
import org.halvors.nuclearphysics.common.event.handler.FulminationEventHandler;
import org.halvors.nuclearphysics.common.event.handler.ItemEventHandler;
import org.halvors.nuclearphysics.common.event.handler.PlayerEventHandler;
import org.halvors.nuclearphysics.common.event.handler.ThermalEventHandler;
import org.halvors.nuclearphysics.common.grid.UpdateTicker;
import org.halvors.nuclearphysics.common.grid.thermal.ThermalGrid;
import org.halvors.nuclearphysics.common.init.*;
import org.halvors.nuclearphysics.common.network.PacketHandler;

@Mod(modid = Reference.ID,
     name = Reference.NAME,
     version = Reference.VERSION,
	 dependencies = "after:mekanism",
	 guiFactory = "org.halvors." + Reference.ID + ".client.gui.configuration.GuiConfiguationFactory")
public class NuclearPhysics {
	// The instance of your mod that Forge uses.
	@Instance(Reference.ID)
	private static NuclearPhysics instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "org.halvors." + Reference.ID + ".client.ClientProxy", serverSide = "org.halvors." + Reference.ID + ".common.CommonProxy")
	private static CommonProxy proxy;

	// Configuration
	private static Configuration configuration;

	// Network
	private static final PacketHandler packetHandler = new PacketHandler();

	// Logger
	private static final Logger logger = LogManager.getLogger(Reference.ID);

	// Creative Tab
	private static final CreativeTab creativeTab = new CreativeTab();

	// Grids
	private static final ThermalGrid thermalGrid = new ThermalGrid();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Initialize configuration.
        configuration = new Configuration(event.getSuggestedConfigurationFile());

		// Load the configuration.
		ConfigurationManager.loadConfiguration(configuration);

		// Call functions for adding blocks, items, etc.
		ModFluids.registerFluids();
		ModBlocks.registerBlocks();
		ModItems.registerItems();

		ModEntities.registerEntities();
		ModMessages.registerMessages();
		ModRecipes.registerRecipes();
		ModWorldGenerators.registerWorldGenerators();

		// Calling proxy handler.
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Register event handlers.
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
		MinecraftForge.EVENT_BUS.register(new ThermalEventHandler());
		MinecraftForge.EVENT_BUS.register(new ItemEventHandler());
		MinecraftForge.EVENT_BUS.register(new FulminationEventHandler());

		// Register the proxy as our GuiHandler to NetworkRegistry.
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

		ForgeChunkManager.setForcedChunkLoadingCallback(this, (tickets, world) -> {
            for (Ticket ticket : tickets) {
                if (ticket.getType() == ForgeChunkManager.Type.ENTITY) {
					Entity entity = ticket.getEntity();

					if (entity instanceof EntityParticle) {
						((EntityParticle) entity).updateTicket = ticket;
					}
                }
            }
        });

		// Calling proxy handler.
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (!UpdateTicker.getInstance().isAlive()) {
			UpdateTicker.getInstance().start();
		}

		// Register our grids.
		UpdateTicker.addNetwork(thermalGrid);

		// Initialize mod integration.
		Integration.initialize();

		// Calling proxy handler.
		proxy.postInit();
	}

	public static NuclearPhysics getInstance() {
		return instance;
	}

	public static CommonProxy getProxy() {
		return proxy;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	public static PacketHandler getPacketHandler() {
		return packetHandler;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static CreativeTabs getCreativeTab() {
		return creativeTab;
	}
}