package org.halvors.nuclearphysics.common.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.halvors.nuclearphysics.common.network.PacketHandler;
import org.halvors.nuclearphysics.common.tile.ITileNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * This packet i used by tile tile entities to send custom information from server to client.
 */
public class PacketTileEntity extends PacketLocation implements IMessage {
	private List<Object> objects;
	private ByteBuf storedBuffer = null;

	public PacketTileEntity() {

	}

	public PacketTileEntity(int x, int y, int z, List<Object> objects) {
		super(x, y, z);

		this.objects = objects;
	}

	public <T extends TileEntity & ITileNetwork> PacketTileEntity(T tile) {
		this(tile.xCoord, tile.yCoord, tile.zCoord, tile.getPacketData(new ArrayList<>()));
	}

	@Override
	public void fromBytes(ByteBuf dataStream) {
		super.fromBytes(dataStream);

		storedBuffer = dataStream.copy();
	}

	@Override
	public void toBytes(ByteBuf dataStream) {
		super.toBytes(dataStream);

		PacketHandler.writeObjects(objects, dataStream);
	}

	public static class PacketTileEntityMessage implements IMessageHandler<PacketTileEntity, IMessage> {
		@Override
		public IMessage onMessage(PacketTileEntity message, MessageContext messageContext) {
			EntityPlayer player = PacketHandler.getPlayer(messageContext);

			if (player != null) {
				World world = PacketHandler.getWorld(messageContext);
				TileEntity tile = world.getTileEntity(message.getX(), message.getY(), message.getZ());

				if (tile instanceof ITileNetwork) {
					ITileNetwork tileNetwork = (ITileNetwork) tile;

					try {
						tileNetwork.handlePacketData(message.storedBuffer);
					} catch (Exception e) {
						e.printStackTrace();
					}

					message.storedBuffer.release();
				}
			}

			return null;
		}
	}
}