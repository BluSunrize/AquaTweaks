package blusunrize.aquatweaks;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AquaEventHandler
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldRenderMid(RenderWorldEventMid event)
	{
		handleWaterRendering(event);
	}

	@SideOnly(Side.CLIENT)
	public static void handleWaterRendering(RenderWorldEventMid event)
	{
		if(event.pass==1)
		{
			for(int yy=0; yy<event.chunkCache.getHeight(); yy++)
				for(int xx=0; xx<16; xx++)
					for(int zz=0; zz<16; zz++)
					{
						int x = event.renderer.posX+xx;
						int y = event.renderer.posY+yy;
						int z = event.renderer.posZ+zz;
						if(FluidUtils.shouldRenderAquaConnectable(event.chunkCache, x,y,z))
							FluidUtils.tessellateFluidBlock(event.renderBlocks.blockAccess, x,y,z, event.renderBlocks, Tessellator.instance);
						if(AquaTweaks.tweakGlass && event.chunkCache.getBlock(x,y,z).getMaterial()==Material.glass)
							FluidUtils.renderTowardsGlass(event.chunkCache, x,y,z, event.renderBlocks);
					}
		}
	}

	private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("textures/misc/underwater.png");
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Pre event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		if(player!=null && !player.isInsideOfMaterial(Material.water) && isInFakeWater(player))
		{

			if(!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderBlockOverlayEvent(player, event.partialTicks, net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType.WATER, Blocks.water, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ))))
			{
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				mc.getTextureManager().bindTexture(WATER_TEXTURE);
				Tessellator tessellator = Tessellator.instance;
				float f1 = mc.thePlayer.getBrightness(event.partialTicks)*.75f;
				GL11.glColor4f(f1, f1, f1, .5F);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glPushMatrix();
				float f2 = 4.0F;
				float f3 = -1.0F;
				float f4 = 1.0F;
				float f5 = -1.0F;
				float f6 = 1.0F;
				float f7 = -0.5F;
				float f8 = -mc.thePlayer.rotationYaw / 64.0F;
				float f9 = mc.thePlayer.rotationPitch / 64.0F;
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV((double)f3, (double)f5, (double)f7, (double)(f2 + f8), (double)(f2 + f9));
				tessellator.addVertexWithUV((double)f4, (double)f5, (double)f7, (double)(0.0F + f8), (double)(f2 + f9));
				tessellator.addVertexWithUV((double)f4, (double)f6, (double)f7, (double)(0.0F + f8), (double)(0.0F + f9));
				tessellator.addVertexWithUV((double)f3, (double)f6, (double)f7, (double)(f2 + f8), (double)(0.0F + f9));
				tessellator.draw();
				GL11.glPopMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
	}

	private static boolean isInFakeWater(EntityLivingBase living)
	{
		for (int i = 0; i < 8; ++i)
		{
			float f = ((float)((i >> 0) % 2) - 0.5F) * living.width * 0.8F;
			float f1 = ((float)((i >> 1) % 2) - 0.5F) * 0.1F;
			float f2 = ((float)((i >> 2) % 2) - 0.5F) * living.width * 0.8F;
			int x = MathHelper.floor_double(living.posX + f);
			int y = MathHelper.floor_double(living.posY + living.getEyeHeight()+f1);
			int z = MathHelper.floor_double(living.posZ + f2);

			if(FluidUtils.shouldRenderAquaConnectable(living.worldObj, x,y,z) && FluidUtils.getFakeFillMaterial(living.worldObj, x, y, z)==Material.water)
				return true;
		}
		return false;
	}
}