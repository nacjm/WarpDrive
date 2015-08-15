package cr0s.warpdrive.block.energy;

import cpw.mods.fml.common.Optional;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockUpdateDetector;
import cr0s.warpdrive.block.TileEntityAbstractLaser;
import cr0s.warpdrive.block.TileEntityLaserMedium;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityEnanReactorLaser extends TileEntityAbstractLaser implements IBlockUpdateDetector {
	Vector3 myVec;
	Vector3 reactorVec;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	TileEntityLaserMedium booster;
	TileEntityEnanReactorCore reactor;

	boolean useLaser = false;
	boolean doOnce = false;

	public TileEntityEnanReactorLaser() {
		methodsArray = new String[] { "energy", "hasReactor", "side", "sendLaser", "help" };
		peripheralName = "warpdriveEnanReactorLaser";
	}

	public TileEntityEnanReactorCore scanForReactor() {
		reactor = null;
		TileEntity te;
		// I AM ON THE NORTH SIDE
		side = ForgeDirection.UNKNOWN;
		te = worldObj.getTileEntity(xCoord, yCoord, zCoord + 2);
		if (te instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord, yCoord, zCoord + 1)) {
			side = ForgeDirection.NORTH;
			reactor = (TileEntityEnanReactorCore) te;
		}

		// I AM ON THE SOUTH SIDE
		te = worldObj.getTileEntity(xCoord, yCoord, zCoord - 2);
		if (te instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord, yCoord, zCoord - 1)) {
			side = ForgeDirection.SOUTH;
			reactor = (TileEntityEnanReactorCore) te;
		}

		// I AM ON THE WEST SIDE
		te = worldObj.getTileEntity(xCoord + 2, yCoord, zCoord);
		if (te instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord + 1, yCoord, zCoord)) {
			side = ForgeDirection.WEST;
			reactor = (TileEntityEnanReactorCore) te;
		}

		// I AM ON THE EAST SIDE
		te = worldObj.getTileEntity(xCoord - 2, yCoord, zCoord);
		if (te instanceof TileEntityEnanReactorCore && worldObj.isAirBlock(xCoord - 1, yCoord, zCoord)) {
			side = ForgeDirection.EAST;
			reactor = (TileEntityEnanReactorCore) te;
		}

		setMetadata();

		if (reactor != null) {
			reactorVec = new Vector3(reactor).translate(0.5);
		}
		return reactor;
	}

	private void setMetadata() {
		int metadata = 0;
		if (side != ForgeDirection.UNKNOWN) {
			metadata = side.ordinal() - 1;
		}
		if (getBlockMetadata() != metadata) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
		}
	}

	public TileEntityLaserMedium scanForBooster() {
		booster = null;
		TileEntity te;
		te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
		if (te != null && te instanceof TileEntityLaserMedium) {
			booster = (TileEntityLaserMedium) te;
		}

		te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if (te != null && te instanceof TileEntityLaserMedium) {
			booster = (TileEntityLaserMedium) te;
		}

		return booster;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (doOnce == false) {
			scanForReactor();
			scanForBooster();
			myVec = new Vector3(this).translate(0.5);
			doOnce = true;
		}

		if (useLaser == true) {
			PacketHandler.sendBeamPacket(worldObj, myVec, reactorVec, 0.1F, 0.2F, 1.0F, 25, 50, 100);
			useLaser = false;
		}
	}

	public void unlink() {
		side = ForgeDirection.UNKNOWN;
		setMetadata();
	}

	@Override
	public void updatedNeighbours() {
		scanForBooster();
		scanForReactor();
	}

	private void laserReactor(int energy) {
		if (energy <= 0) {
			return;
		}

		scanForBooster();
		scanForReactor();
		if (booster == null)
			return;
		if (reactor == null)
			return;
		if (booster.consumeEnergy(energy, false)) {
			// WarpDrive.debugPrint("ReactorLaser on " + side.toString()
			// +" side sending " + amount);
			useLaser = true;
			reactor.decreaseInstability(side, energy);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	private static String helpStr(Object[] args) {
		if (args.length > 0) {
			String arg = args[0].toString().toLowerCase();
			if (arg.equals("energy")) {
				return WarpDrive.defEnergyStr;
			} else if (arg.equals("hasReactor")) {
				return "hasReactor(): returns true if the laser can see a reactor and false otherwise";
			} else if (arg.equals("sendlaser")) {
				return "sendLaser(int): sends a laser of energy int to the reactor";
			} else if (arg.equals("side")) {
				return "side(): returns 0-3 depending on which side of the reactor its on";
			}
		}
		return WarpDrive.defHelpStr;
	}

	// ComputerCraft methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = methodsArray[method];
		if (methodName.equals("energy")) {
			scanForBooster();
			if (booster == null) {
				return new Object[] { 0, 0 };
			} else {
				return new Object[] { booster.getEnergyStored(), booster.getMaxEnergyStored() };
			}
		} else if (methodName.equals("hasReactor")) {
			return new Object[] { scanForReactor() != null };
		} else if (methodName.equals("sendLaser")) {
			if (arguments.length >= 1) {
				laserReactor(toInt(arguments[0]));
			}
		} else if (methodName.equals("help")) {
			return new Object[] { helpStr(arguments) };
		} else if (methodName.equals("side")) {
			return new Object[] { side.ordinal() - 2 };
		}
		return null;
	}
}