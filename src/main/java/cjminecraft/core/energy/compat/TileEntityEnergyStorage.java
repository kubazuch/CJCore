package cjminecraft.core.energy.compat;

import cjminecraft.core.energy.EnergyUnits;
import cjminecraft.core.energy.EnergyUtils;
import cofh.redstoneflux.api.IEnergyStorage;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IMultiEnergySource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;

/**
 * A {@link TileEntity} which acts as an energy storage. The {@link TileEntity}
 * uses forge energy and to access the storage use <code>this.storage</code>.
 * This is a storage which should handle its own transferring of energy using
 * {@link EnergyUtils} preferably
 * 
 * @author CJMinecraft
 *
 */
@Optional.InterfaceList(value = { @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "ic2"),
		@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "ic2"),
		@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyStorage", modid = "redstoneflux") })
public class TileEntityEnergyStorage extends TileEntityEnergy implements IEnergyStorage, IEnergySink, IEnergySource {

	private Object teslaWrapper;

	/**
	 * Create an energy storage
	 * 
	 * @param capacity
	 *            The capacity of the energy storage
	 */
	public TileEntityEnergyStorage(long capacity) {
		super(capacity, capacity, capacity, 0);
	}

	/**
	 * Create an energy storage
	 * 
	 * @param capacity
	 *            The capacity of the energy storage
	 * @param maxTransfer
	 *            The max receive and max extract of the energy storage
	 */
	public TileEntityEnergyStorage(long capacity, long maxTransfer) {
		super(capacity, maxTransfer, maxTransfer, 0);
	}

	/**
	 * Create an energy storage
	 * 
	 * @param capacity
	 *            The capacity of the energy storage
	 * @param maxReceive
	 *            The maximum amount of energy which can be received
	 * @param maxExtract
	 *            The maximum amount of energy which can be extracted
	 */
	public TileEntityEnergyStorage(long capacity, long maxReceive, long maxExtract) {
		super(capacity, maxReceive, maxExtract, 0);
	}

	/**
	 * Create an energy storage
	 * 
	 * @param capacity
	 *            The capacity of the energy storage
	 * @param maxReceive
	 *            The maximum amount of energy which can be received
	 * @param maxExtract
	 *            The maximum amount of energy which can be extracted
	 * @param energy
	 *            The energy inside of the energy storage
	 */
	public TileEntityEnergyStorage(long capacity, long maxReceive, long maxExtract, long energy) {
		super(capacity, maxReceive, maxExtract, energy);
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return (int) this.storage.receiveEnergy(maxReceive, simulate);
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return (int) this.storage.extractEnergy(maxExtract, simulate);
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int getEnergyStored() {
		return (int) this.storage.getEnergyStored();
	}

	@Optional.Method(modid = "redstoneflux")
	@Override
	public int getMaxEnergyStored() {
		return (int) this.storage.getMaxEnergyStored();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (EnergyUtils.TESLA_LOADED && (capability == EnergyUtils.TESLA_CONSUMER
				|| capability == EnergyUtils.TESLA_PRODUCER || capability == EnergyUtils.TESLA_HOLDER))
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (EnergyUtils.TESLA_LOADED && (capability == EnergyUtils.TESLA_CONSUMER
				|| capability == EnergyUtils.TESLA_PRODUCER || capability == EnergyUtils.TESLA_HOLDER)) {
			if (this.teslaWrapper == null)
				this.teslaWrapper = new TeslaWrapper(this.storage);
			return (T) this.teslaWrapper;
		}
		return super.getCapability(capability, facing);
	}

	/**
	 * Determine if this acceptor can accept current from an adjacent emitter in
	 * a direction.
	 *
	 * The TileEntity in the emitter parameter is what was originally added to
	 * the energy net, which may be normal in-world TileEntity, a delegate or an
	 * IMetaDelegate.
	 *
	 * @param emitter
	 *            energy emitter, may also be null or an IMetaDelegate
	 * @param side
	 *            side the energy is being received from
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing side) {
		return true;
	}

	/**
	 * Determine if this emitter can emit energy to an adjacent receiver.
	 *
	 * The TileEntity in the receiver parameter is what was originally added to
	 * the energy net, which may be normal in-world TileEntity, a delegate or an
	 * IMetaDelegate.
	 *
	 * @param receiver
	 *            receiver, may also be null or an IMetaDelegate
	 * @param side
	 *            side the energy is to be sent to
	 * @return Whether energy should be emitted
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
		return true;
	}

	/**
	 * Maximum energy output provided by the source this tick, typically the
	 * stored energy.
	 *
	 * <p>
	 * The value will be limited to the source tier's power multiplied with the
	 * packet count (see {@link IMultiEnergySource}, default 1).
	 *
	 * <i>Modifying the energy net from this method is disallowed.</i>
	 *
	 * @return Energy offered this tick
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public double getOfferedEnergy() {
		return EnergyUtils.convertEnergy(EnergyUnits.FORGE_ENERGY, EnergyUnits.ENERGY_UNIT,
				this.storage.getMaxExtract());
	}

	/**
	 * Draw energy from this source's buffer.
	 *
	 * <p>
	 * If the source doesn't have a buffer, this may be a no-op.
	 *
	 * @param amount
	 *            amount of EU to draw, may be negative
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public void drawEnergy(double amount) {
		this.storage.extractEnergy(
				(int) EnergyUtils.convertEnergy(EnergyUnits.ENERGY_UNIT, EnergyUnits.FORGE_ENERGY, amount), false);
	}

	/**
	 * Determine the tier of this energy source. 1 = LV, 2 = MV, 3 = HV, 4 = EV
	 * etc.
	 *
	 * <i>Modifying the energy net from this method is disallowed.</i>
	 *
	 * @return tier of this energy source
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public int getSourceTier() {
		return ((int) (Math.log(getOfferedEnergy()) / Math.log(2)) - 3) / 2;
	}

	/**
	 * Determine how much energy the sink accepts.
	 *
	 * Make sure that injectEnergy() does accepts energy if demandsEnergy()
	 * returns anything greater than 0.
	 * 
	 * <i>Modifying the energy net from this method is disallowed.</i>
	 *
	 * @return max accepted input in eu
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public double getDemandedEnergy() {
		return EnergyUtils.convertEnergy(EnergyUnits.FORGE_ENERGY, EnergyUnits.ENERGY_UNIT,
				this.storage.getMaxReceive());
	}

	/**
	 * Determine the tier of this energy sink. 1 = LV, 2 = MV, 3 = HV, 4 = EV
	 * etc.
	 * 
	 * <i>Modifying the energy net from this method is disallowed. Return
	 * Integer.MAX_VALUE to allow any voltage.</i>
	 *
	 * @return tier of this energy sink
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public int getSinkTier() {
		return ((int) (Math.log(getDemandedEnergy()) / Math.log(2)) - 3) / 2;
	}

	/**
	 * Transfer energy to the sink.
	 * 
	 * It's highly recommended to accept all energy by letting the internal
	 * buffer overflow to increase the performance and accuracy of the
	 * distribution simulation.
	 *
	 * @param directionFrom
	 *            direction from which the energy comes from
	 * @param amount
	 *            energy to be transferred
	 * @param voltage
	 *            The voltage of the energy
	 * @return Energy not consumed (leftover)
	 */
	@Override
	@Optional.Method(modid = "ic2")
	public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
		return Math.abs(EnergyUtils.convertEnergy(EnergyUnits.FORGE_ENERGY, EnergyUnits.ENERGY_UNIT,
				this.storage.receiveEnergy(
						(int) EnergyUtils.convertEnergy(EnergyUnits.ENERGY_UNIT, EnergyUnits.FORGE_ENERGY, amount),
						false))
				- amount);
	}
}
