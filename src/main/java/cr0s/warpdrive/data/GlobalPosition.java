package cr0s.warpdrive.data;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import net.minecraftforge.common.DimensionManager;

public class GlobalPosition {
	public final int dimensionId;
	public final int x, y, z;
	
	public GlobalPosition(final int dimensionId, final int x, final int y, final int z) {
		this.dimensionId = dimensionId;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public GlobalPosition(TileEntity tileEntity) {
		this(tileEntity.getWorldObj().provider.dimensionId, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}
	
	public WorldServer getWorldServerIfLoaded() {
		WorldServer world = DimensionManager.getWorld(dimensionId);
		// skip unloaded worlds
		if (world == null) {
			return null;
		}
		
		boolean isLoaded;
		if (world.getChunkProvider() instanceof ChunkProviderServer) {
			ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
			try {
				isLoaded = chunkProviderServer.loadedChunkHashMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4));
			} catch (NoSuchFieldError exception) {
				isLoaded = chunkProviderServer.chunkExists(x >> 4, z >> 4);
			}
		} else {
			isLoaded = world.getChunkProvider().chunkExists(x >> 4, z >> 4);
		}
		// skip unloaded chunks
		if (!isLoaded) {
			return null;
		}
		return world;
	}
	
	public boolean isLoaded() {
		return getWorldServerIfLoaded() != null;
	}
	
	public VectorI getSpaceCoordinates() {
		CelestialObject celestialObject = StarMapRegistry.getCelestialObject(dimensionId, x, z);
		if (celestialObject == null) {
			// not a registered area
			return null;
		}
		if (celestialObject.isHyperspace()) {
			return new VectorI(x, y + 512, z);
		}
		if (celestialObject.isSpace()) {
			return new VectorI(x, y + 256, z);
		}
		VectorI vEntry = celestialObject.getEntryOffset();
		return new VectorI(x - vEntry.x, y, z - vEntry.z);
	}
	
	public boolean equals(final TileEntity tileEntity) {
		return dimensionId == tileEntity.getWorldObj().provider.dimensionId
			&& x == tileEntity.xCoord && y == tileEntity.yCoord && z == tileEntity.zCoord;
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 24 + (x >> 10) << 12 + y << 10 + (z >> 10);
	}
}