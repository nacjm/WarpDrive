package cr0s.warpdrive.block.hull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IHullBlock;
import cr0s.warpdrive.config.WarpDriveConfig;

public class BlockHullGlass extends BlockColored implements IHullBlock {
	private int tier;
	
	public BlockHullGlass(final int tier) {
		super(Material.glass);
		this.tier = tier;
		setHardness(WarpDriveConfig.HULL_HARDNESS[tier - 1]);
		setResistance(WarpDriveConfig.HULL_BLAST_RESISTANCE[tier - 1] * 5 / 3);
		setStepSound(Block.soundTypeGlass);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.hull" + tier + ".glass.");
		setBlockTextureName("warpdrive:hull/glass");
	}
	
	@Override
	public int getMobilityFlag() {
		return 2;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (world.isAirBlock(x, y, z)) {
			return true;
		}
		ForgeDirection direction = ForgeDirection.getOrientation(side).getOpposite();
		Block sideBlock = world.getBlock(x, y, z);
		if (sideBlock instanceof BlockGlass || sideBlock instanceof BlockHullGlass) {
			return world.getBlockMetadata(x, y, z)
				!= world.getBlockMetadata(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		}
		return !world.isSideSolid(x, y, z, direction, false);
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public void downgrade(World world, int x, int y, int z) {
		int metadata = world.getBlockMetadata(x, y, z);
		if (tier == 1) {
			world.setBlockToAir(x, y, z);
		} else {
			world.setBlock(x, y, z, WarpDrive.blockHulls_plain[tier - 2], metadata, 2);
		}
	}
}
