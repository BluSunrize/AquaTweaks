package blusunrize.aquatweaks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.aquatweaks.api.IAquaConnectable;

public class FluidUtils
{
	public static HashMap<Block,Integer> validConnectables = new HashMap<Block,Integer>();

	public static void addBlockToValidConnectables(Block block, int meta)
	{
		validConnectables.put(block, meta);
	}
	public static void addDefaultConnectables()
	{
		addBlockToValidConnectables(Blocks.ladder,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.iron_bars,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.skull,OreDictionary.WILDCARD_VALUE);
		//Buttons and levers get washed away, but at least it looks good
		addBlockToValidConnectables(Blocks.stone_button,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.wooden_button,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.lever,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.hopper,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.stone_pressure_plate,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.wooden_pressure_plate,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.light_weighted_pressure_plate,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.heavy_weighted_pressure_plate,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.anvil,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.fence,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.fence_gate,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.nether_brick_fence,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.wall_sign,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.standing_sign,OreDictionary.WILDCARD_VALUE);

		//These would usually get flushed away, but Vazkii's Waterproof fixes that
		addBlockToValidConnectables(Blocks.redstone_wire,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.redstone_torch,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.unlit_redstone_torch,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.unpowered_repeater,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.powered_repeater,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.unpowered_comparator,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.powered_comparator,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.rail,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.golden_rail,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.detector_rail,OreDictionary.WILDCARD_VALUE);
		addBlockToValidConnectables(Blocks.activator_rail,OreDictionary.WILDCARD_VALUE);
	}

	public static Vec3 getFlowVector(IBlockAccess world, int x, int y, int z, Material mat)
	{
		Vec3 vec3 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

		int flowDelay = 0;
		for(int dir=0; dir<4; ++dir)
		{
			int xx = x;
			int zz = z;

			if(dir==0)
				--xx;
			if(dir==1)
				--zz;
			if(dir==2)
				++xx;
			if(dir==3)
				++zz;

			int delay2 = getEffectiveFlowDelay(world, xx, y, zz, mat);
			int i2;

			if(delay2 < 0)
			{
				if(!world.getBlock(xx, y, zz).getMaterial().blocksMovement())
				{
					delay2 = getEffectiveFlowDelay(world, xx, y - 1, zz, mat);
					if(delay2 >= 0)
					{
						i2 = delay2 - (flowDelay - 8);
						vec3 = vec3.addVector(((xx - x) * i2), ((y - y) * i2), ((zz - z) * i2));
					}
				}
			}
			else if(delay2 >= 0)
			{
				i2 = delay2 - flowDelay;
				vec3 = vec3.addVector(((xx - x) * i2), ((y - y) * i2), ((zz - z) * i2));
			}
		}

		if(world.getBlockMetadata(x, y, z) >= 8)
			vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);

		vec3 = vec3.normalize();
		return vec3;
	}

	public static int getEffectiveFlowDelay(IBlockAccess world, int x, int y, int z, Material mat)
	{
		if(world.getBlock(x, y, z).getMaterial()!=mat)
			return -1;
		else
		{
			int l = world.getBlockMetadata(x, y, z);
			if (l >= 8)
				l = 0;
			return l;
		}
	}
	public static double getFlowDirection(IBlockAccess world, int x, int y, int z, Material mat)
	{
		Vec3 vec3 = getFlowVector(world, x, y, z, mat);
		return vec3.xCoord == 0.0D && vec3.zCoord == 0.0D ? -1000.0D : Math.atan2(vec3.zCoord, vec3.xCoord) - (Math.PI / 2D);
	}
	public static float getFluidHeight(IBlockAccess world, int x, int y, int z, Material mat)
	{
		int l = 0;
		float f = 0.0F;

		if(canConnectAquaConnectable(world, x,y+1,z, 0) && (getFluidHeight(world,x,y+1,z,mat)>0||getFluidHeight(world,x+1,y+1,z,mat)>0||getFluidHeight(world,x+1,y+1,z+1,mat)>0||getFluidHeight(world,x,y+1,z+1,mat)>0))
			return 1f;
		for(int i1 = 0; i1 < 4; ++i1)
		{
			int j1 = x - (i1 & 1);
			int k1 = z - (i1 >> 1 & 1);

			if (world.getBlock(j1, y + 1, k1).getMaterial()==mat)
				return 1F;
			if(canConnectAquaConnectable(world, j1,y+1,k1, 0) && (getFluidHeight(world,j1,y+1,k1,mat)>0||getFluidHeight(world,j1+1,y+1,k1,mat)>0||getFluidHeight(world,j1+1,y+1,k1+1,mat)>0||getFluidHeight(world,j1,y+1,k1+1,mat)>0))
				return 1f;

			Material material1 = world.getBlock(j1, y, k1).getMaterial();
			if(material1==mat)
			{
				int l1 = world.getBlockMetadata(j1, y, k1);
				if (l1 >= 8 || l1 == 0)
				{
					f += BlockLiquid.getLiquidHeightPercent(l1) * 10f;
					l += 10;
				}
				f += BlockLiquid.getLiquidHeightPercent(l1);
				++l;
			}
			else if (!material1.isSolid())
			{
				++f;
				++l;
			}
		}
		return 1f-f/(float)l;
	}

	public static void tessellateFluidBlock(IBlockAccess world, int x, int y, int z, RenderBlocks renderer, Tessellator tes)
	{
		Block block = world.getBlock(x, y, z);

		Material material = null;
		Block fluidBlock = null;
		for(int yy : new int[]{0,1})
			for(int xx : new int[]{-1,0,1})
				for(int zz : new int[]{-1,0,1})
					if(world.getBlock(x+xx,y+yy,z+zz).getMaterial()==Material.water)
					{
						material = Material.water;
						fluidBlock = world.getBlock(x+xx,y+yy,z+zz);
					}
					else if(material==null && world.getBlock(x+xx,y+yy,z+zz).getMaterial()==Material.lava)
					{
						material = Material.lava;
						fluidBlock = world.getBlock(x+xx,y+yy,z+zz);
					}
		if(material==null||fluidBlock==null)
			return;


		int i1 = 0;
		double d6 = 0.0010000000474974513D;
		double height00 = getFluidHeight(world, x, y, z, material)-d6;
		double height01 = getFluidHeight(world, x, y, z + 1, material)-d6;
		double height11 = getFluidHeight (world, x + 1, y, z + 1, material)-d6;
		double height10 = getFluidHeight(world, x + 1, y, z, material)-d6;

		if(canConnectAquaConnectable(world, x,y+1,z, 0) && (getFluidHeight(world,x,y+1,z,material)>0||getFluidHeight(world,x+1,y+1,z,material)>0||getFluidHeight(world,x+1,y+1,z+1,material)>0||getFluidHeight(world,x,y+1,z+1,material)>0))
		{
			height00 = 1;
			height01 = 1;
			height11 = 1;
			height10 = 1;
		}
		else
		{
			if(Double.isNaN(height00)&&Double.isNaN(height01)&&Double.isNaN(height11)&&Double.isNaN(height10))
				return;
			if(height00<=0&&height01<=0&&height11<=0&&height10<=0)
				return;
			//			height00 -= d6;
			//			height01 -= d6;
			//			height11 -= d6;
			//			height10 -= d6;
			if(Double.isNaN(height00)||height00<=0)
				height00=.0625f;
			if(Double.isNaN(height01)||height01<=0)
				height01=.0625f;
			if(Double.isNaN(height11)||height11<=0)
				height11=.0625f;
			if(Double.isNaN(height10)||height10<=0)
				height10=.0625f;
			if(height00<.0625&&height01<.0625&&height11<.0625&&height10<.0625)
				return;
		}

		double depth00=0;
		double depth01=0;
		double depth11=0;
		double depth10=0;
		if(world.getBlock(x,y-1,z).getMaterial()==material)
		{
			depth00 = getFluidHeight(world, x+0, y-1, z+0, material);
			depth01 = getFluidHeight(world, x+0, y-1, z+1, material);
			depth11 = getFluidHeight(world, x+1, y-1, z+1, material);
			depth10 = getFluidHeight(world, x+1, y-1, z+0, material);
			if(Double.isNaN(height00)||height00<=0)
				height00=.0625f;
			if(Double.isNaN(height01)||height01<=0)
				height01=.0625f;
			if(Double.isNaN(height11)||height11<=0)
				height11=.0625f;
			if(Double.isNaN(height10)||height10<=0)
				height10=.0625f;
			depth00 -= 1;
			depth01 -= 1;
			depth11 -= 1;
			depth10 -= 1;
		}
		depth00 += d6;
		depth01 += d6;
		depth11 += d6;
		depth10 += d6;

		int colour00 = world.getBlock(x,y,z).getMaterial()==material?world.getBlock(x,y,z).colorMultiplier(renderer.blockAccess,x+1,y,z): 0xffffff;
		int colour01 = world.getBlock(x,y,z+1).getMaterial()==material?world.getBlock(x,y,z+1).colorMultiplier(renderer.blockAccess,x+1,y,z): 0xffffff;
		int colour11 = world.getBlock(x+1,y,z+1).getMaterial()==material?world.getBlock(x+1,y,z+1).colorMultiplier(renderer.blockAccess,x+1,y,z): 0xffffff;
		int colour10 = world.getBlock(x+1,y,z).getMaterial()==material?world.getBlock(x+1,y,z).colorMultiplier(renderer.blockAccess,x+1,y,z): 0xffffff;
		float[] col00={(colour00>>16&255)/255f, (colour00>>8&255)/255f, (colour00&255)/255f};
		float[] col01={(colour01>>16&255)/255f, (colour01>>8&255)/255f, (colour01&255)/255f};
		float[] col11={(colour11>>16&255)/255f, (colour11>>8&255)/255f, (colour11&255)/255f};
		float[] col10={(colour10>>16&255)/255f, (colour10>>8&255)/255f, (colour10&255)/255f};

		IIcon iicon = renderer.getBlockIconFromSideAndMetadata(fluidBlock, 1, i1);

		float f7 = (float)getFlowDirection(renderer.blockAccess, x, y, z, material);
		if (f7 > -999.0F)
		{
			iicon = renderer.getBlockIconFromSideAndMetadata(fluidBlock, 2, i1);
		}

		double d7;
		double d8;
		double d10;
		double d12;
		double d14;
		double d16;
		double d18;
		double d20;

		if (f7 < -999.0F)
		{
			d7 = iicon.getInterpolatedU(0.0D);
			d14 = iicon.getInterpolatedV(0.0D);
			d8 = d7;
			d16 = iicon.getInterpolatedV(16.0D);
			d10 = iicon.getInterpolatedU(16.0D);
			d18 = d16;
			d12 = d10;
			d20 = d14;
		}
		else
		{
			float f9 = MathHelper.sin(f7) * 0.25F;
			float f10 = MathHelper.cos(f7) * 0.25F;
			d7 = iicon.getInterpolatedU((8.0F + (-f10 - f9) * 16.0F));
			d14 = iicon.getInterpolatedV((8.0F + (-f10 + f9) * 16.0F));
			d8 = iicon.getInterpolatedU((8.0F + (-f10 + f9) * 16.0F));
			d16 = iicon.getInterpolatedV((8.0F + (f10 + f9) * 16.0F));
			d10 = iicon.getInterpolatedU((8.0F + (f10 + f9) * 16.0F));
			d18 = iicon.getInterpolatedV((8.0F + (f10 - f9) * 16.0F));
			d12 = iicon.getInterpolatedU((8.0F + (f10 - f9) * 16.0F));
			d20 = iicon.getInterpolatedV((8.0F + (-f10 - f9) * 16.0F));
		}

		//TOP
		if(!canFakeFluidConnectToBlock(world,x,y+1,z,1,material))
		{
			tes.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z));
			tes.setColorOpaque_F(col00[0],col00[1],col00[2]);
			tes.addVertexWithUV(x+0, y+height00, z+0, d7, d14);
			tes.setColorOpaque_F(col01[0],col01[1],col01[2]);
			tes.addVertexWithUV(x+0, y+height01, z+1, d8, d16);
			tes.setColorOpaque_F(col11[0],col11[1],col11[2]);
			tes.addVertexWithUV(x+1, y+height11, z+1, d10, d18);
			tes.setColorOpaque_F(col10[0],col10[1],col10[2]);
			tes.addVertexWithUV(x+1, y+height10, z+0, d12, d20);

			tes.setColorOpaque_F(col00[0],col00[1],col00[2]);
			tes.addVertexWithUV(x+0, y+height00, z+0, d7, d14);
			tes.setColorOpaque_F(col10[0],col10[1],col10[2]);
			tes.addVertexWithUV(x+1, y+height10, z+0, d12, d20);
			tes.setColorOpaque_F(col11[0],col11[1],col11[2]);
			tes.addVertexWithUV(x+1, y+height11, z+1, d10, d18);
			tes.setColorOpaque_F(col01[0],col01[1],col01[2]);
			tes.addVertexWithUV(x+0, y+height01, z+1, d8, d16);
		}
		//BOTTOM
		if(!canFakeFluidConnectToBlock(world,x,y-1,z,0,material))
		{
			float mod = .5f;
			tes.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y-1, z));
			tes.setColorOpaque_F(col01[0]*mod,col01[1]*mod,col01[2]*mod);
			tes.addVertexWithUV(x+0, y+depth01, z+1, d8, d16);
			tes.setColorOpaque_F(col00[0]*mod,col00[1]*mod,col00[2]*mod);
			tes.addVertexWithUV(x+0, y+depth00, z+0, d7, d14);
			tes.setColorOpaque_F(col10[0]*mod,col10[1]*mod,col10[2]*mod);
			tes.addVertexWithUV(x+1, y+depth10, z+0, d12, d20);
			tes.setColorOpaque_F(col11[0]*mod,col11[1]*mod,col11[2]*mod);
			tes.addVertexWithUV(x+1, y+depth11, z+1, d10, d18);

			tes.setColorOpaque_F(col10[0]*mod,col10[1]*mod,col10[2]*mod);
			tes.addVertexWithUV(x+1, y+depth10, z+0, d12, d20);
			tes.setColorOpaque_F(col00[0]*mod,col00[1]*mod,col00[2]*mod);
			tes.addVertexWithUV(x+0, y+depth00, z+0, d7, d14);
			tes.setColorOpaque_F(col01[0]*mod,col01[1]*mod,col01[2]*mod);
			tes.addVertexWithUV(x+0, y+depth01, z+1, d8, d16);
			tes.setColorOpaque_F(col11[0]*mod,col11[1]*mod,col11[2]*mod);
			tes.addVertexWithUV(x+1, y+depth11, z+1, d10, d18);
		}

		//SIDES
		for(int side=0; side<4; ++side)
		{
			int xx = x+(side==2?-1:side==3?1:0);
			int zz = z+(side==0?-1:side==1?1:0);

			iicon = renderer.getBlockIconFromSideAndMetadata(fluidBlock, side + 2, i1);

			if(!canFakeFluidConnectToBlock(world,xx,y,zz,side+2,material))
			{
				double h0 = side==0?height00: side==1?height11: side==2?height01: height10;
				double h1 = side==0?height10: side==1?height01: side==2?height00: height11;
				double d0 = side==0?depth00: side==1?depth11: side==2?depth01: depth10;
				double d1 = side==0?depth10: side==1?depth01: side==2?depth00: depth11;
				double xMin = side==0?x: side==1?x+1: side==2?x+d6: x+1-d6;
				double xMax = side==0?x+1: side==1?x: side==2?x+d6: x+1-d6;
				double zMin = side==0?z+d6: side==1?z+1-d6: side==2?z+1: z;
				double zMax = side==0?z+d6: side==1?z+1-d6: side==2?z: z+1;
				float[] col0 = side==0?col00: side==1?col11: side==2?col01: col10;
				float[] col1 = side==0?col10: side==1?col01: side==2?col00: col11;

				float f8 = iicon.getInterpolatedU(0.0D);
				float f9 = iicon.getInterpolatedU(8.0D);
				float f10 = iicon.getInterpolatedV((1.0D - h0) * 16.0D * 0.5D);
				float f11 = iicon.getInterpolatedV((1.0D - h1) * 16.0D * 0.5D);
				float f12 = iicon.getInterpolatedV(8.0D);

				tes.setBrightness(block.getMixedBrightnessForBlock(world,xx,y,zz));
				float mod = 1.0F;
				mod *= side<2?.8f:.6f;
				tes.setColorOpaque_F(col0[0]*mod,col0[1]*mod,col0[2]*mod);
				tes.addVertexWithUV(xMin, y+d0, zMin, f8, f12);
				tes.addVertexWithUV(xMin, y+h0, zMin, f8, f10);
				tes.setColorOpaque_F(col1[0]*mod,col1[1]*mod,col1[2]*mod);
				tes.addVertexWithUV(xMax, y+h1, zMax, f9, f11);
				tes.addVertexWithUV(xMax, y+d1, zMax, f9, f12);

				tes.addVertexWithUV(xMax, y+d1, zMax, f9, f12);
				tes.addVertexWithUV(xMax, y+h1, zMax, f9, f11);
				tes.setColorOpaque_F(col0[0]*mod,col0[1]*mod,col0[2]*mod);
				tes.addVertexWithUV(xMin, y+h0, zMin, f8, f10);
				tes.addVertexWithUV(xMin, y+d0, zMin, f8, f12);
			}
		}


	}


	public static boolean canFluidConnectToBlock(IBlockAccess world, int x, int y, int z, int side, Material material)
	{
		return world.getBlock(x,y,z).getMaterial()==material || (side!=1&& (world.getBlock(x,y,z).isOpaqueCube() || (AquaTweaks.tweakGlass&&world.getBlock(x,y,z).getMaterial()==Material.glass))) || (canConnectAquaConnectable(world, x,y,z, side)&&isBlockSubmerged(world,x,y,z,material, true)&&getFakeFillMaterial(world,x,y,z)==material);
	}
	public static boolean canFakeFluidConnectToBlock(IBlockAccess world, int x, int y, int z, int side, Material material)
	{
		return world.getBlock(x,y,z).getMaterial()==material || (side!=1&& (world.getBlock(x,y,z).isOpaqueCube() || (AquaTweaks.tweakGlass&&world.getBlock(x,y,z).getMaterial()==Material.glass))) || (canConnectAquaConnectable(world, x,y,z, side)&&isBlockSubmerged(world,x,y,z,material, true)&&getFakeFillMaterial(world,x,y,z)==material);
	}

	public static boolean canConnectAquaConnectable(IBlockAccess world, int x, int y, int z, int side)
	{
		Block b = world.getBlock(x, y, z);
		if(b!=Blocks.air)
		{
			if(b instanceof IAquaConnectable && ((IAquaConnectable)b).canConnectTo(world,x,y,z,side))
				return true;
			for(Map.Entry<Block, Integer> e:  validConnectables.entrySet())
				if(e.getKey()==b && (e.getValue()==OreDictionary.WILDCARD_VALUE || e.getValue()==world.getBlockMetadata(x, y, z)))
					return true;
		}
		return false;
	}

	public static boolean shouldRenderAquaConnectable(IBlockAccess world, int x, int y, int z)
	{
		Block b = world.getBlock(x, y, z);
		if(b!=Blocks.air)
		{
			if(b instanceof IAquaConnectable && ((IAquaConnectable)b).shouldRenderFluid(world,x,y,z))
				return true;
			for(Map.Entry<Block, Integer> e:  validConnectables.entrySet())
				if(e.getKey()==b && (e.getValue()==OreDictionary.WILDCARD_VALUE || e.getValue()==world.getBlockMetadata(x, y, z)))
					return true;
		}
		return false;
	}

	public static boolean isBlockSubmerged(IBlockAccess world, int x, int y, int z, Material material, boolean fakeFluid)
	{
		for(int f=2; f<6; ++f)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(f);
			if(world.getBlock(x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ) instanceof BlockLiquid && getEffectiveFlowDelay(world, x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ, material)<8)
				return true;
			if(fakeFluid && canConnectAquaConnectable(world, x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ, ForgeDirection.OPPOSITES[f])
					)
				//						&& getFluidHeight(world,x+fd.offsetX,y+fd.offsetY,z+fd.offsetZ,material)<.0625)
				return true;
		}	
		return false;
	}

	public static Material getFakeFillMaterial(IBlockAccess world, int x, int y, int z)
	{
		Material material = null;
		for(int yy : new int[]{0,1})
			for(int xx : new int[]{-1,0,1})
				for(int zz : new int[]{-1,0,1})
					if(world.getBlock(x+xx,y+yy,z+zz).getMaterial()==Material.water)
						material = Material.water;
					else if(material==null && world.getBlock(x+xx,y+yy,z+zz).getMaterial()==Material.lava)
						material = Material.lava;
		return material;
	}

	public static void renderTowardsGlass(IBlockAccess world, int x, int y, int z, RenderBlocks renderBlocks)
	{
		boolean ao = renderBlocks.enableAO;
		renderBlocks.enableAO = false;
		renderBlocks.setRenderBounds(0,0,0,1,1,1);

		IIcon icon = null;

		float alpha = .625f;
		Tessellator.instance.setBrightness(0xF000F0);
		if(world.getBlock(x,y-1,z).getMaterial()==Material.water)
		{
			int col = world.getBlock(x,y-1,z).colorMultiplier(world, x,y-1,z);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x,y-1,z).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceYPos(world.getBlock(x,y-1,z), x,y-1,z, icon);
		}
		if(world.getBlock(x,y+1,z).getMaterial()==Material.water)
		{
			int col = world.getBlock(x,y+1,z).colorMultiplier(world, x,y+1,z);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x,y+1,z).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceYNeg(world.getBlock(x,y+1,z), x,y+1,z, icon);
		}
		if(world.getBlock(x,y,z-1).getMaterial()==Material.water)
		{
			int col = world.getBlock(x,y,z-1).colorMultiplier(world, x,y,z-1);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x,y,z-1).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceZPos(world.getBlock(x,y,z-1), x,y,z-1, icon);
		}
		if(world.getBlock(x,y,z+1).getMaterial()==Material.water)
		{
			int col = world.getBlock(x,y,z+1).colorMultiplier(world, x,y,z+1);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x,y,z+1).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceZNeg(world.getBlock(x,y,z+1), x,y,z+1, icon);
		}
		if(world.getBlock(x-1,y,z).getMaterial()==Material.water)
		{
			int col = world.getBlock(x-1,y,z).colorMultiplier(world, x-1,y,z);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x-1,y,z).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceXPos(world.getBlock(x-1,y,z), x-1,y,z, icon);
		}
		if(world.getBlock(x+1,y,z).getMaterial()==Material.water)
		{
			int col = world.getBlock(x+1,y,z).colorMultiplier(world, x+1,y,z);
			Tessellator.instance.setColorRGBA_F((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f, alpha);
			icon = world.getBlock(x+1,y,z).getIcon(0,0);
			if(icon!=null)
				renderBlocks.renderFaceXNeg(world.getBlock(x+1,y,z), x+1,y,z, icon);
		}
		renderBlocks.enableAO = ao;
	}

}