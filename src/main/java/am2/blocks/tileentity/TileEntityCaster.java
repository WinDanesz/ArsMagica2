package am2.blocks.tileentity;

import am2.power.PowerTypes;

public class TileEntityCaster extends TileEntityAMPower{

	public TileEntityCaster(int capacity) {
		super(1000);
	}

	@Override
	public boolean canRelayPower(PowerTypes type) {
		return false;
	}

	@Override
	public int getChargeRate() {
		return 200;
	}

}
