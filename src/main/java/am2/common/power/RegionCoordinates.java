package am2.common.power;

import net.minecraft.util.math.ChunkPos;

public class RegionCoordinates{
	public final int x;
	public final int z;
	public final int dimension;

	public RegionCoordinates(int x, int z, int dimension){
		this.x = x;
		this.z = z;
		this.dimension = dimension;
	}

	public RegionCoordinates(ChunkPos pair, int dimension){
		this.x = (int)Math.floor(pair.x / 32);
		this.z = (int)Math.floor(pair.z / 32);
		this.dimension = dimension;
	}

	@Override
	public boolean equals(Object obj){
		RegionCoordinates coord = (RegionCoordinates)obj;
		return coord.x == this.x & coord.z == this.z && coord.dimension == this.dimension;
	}

	@Override
	public int hashCode(){
		return x + z + dimension;
	}
}
