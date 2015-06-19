package blusunrize.aquatweaks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.ChunkCache;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * An event that is fired during block rendering, rather than before and after.
 * Because I couldn't get it to work on pre and post >_>
 */
@SideOnly(Side.CLIENT)
public class RenderWorldEventMid extends Event
{
    public final WorldRenderer renderer;
    public final ChunkCache chunkCache;
    public final RenderBlocks renderBlocks;
    public final int pass;
    
	public RenderWorldEventMid(WorldRenderer renderer, ChunkCache chunkCache, RenderBlocks renderBlocks, int pass)
	{
		this.renderer = renderer;
		this.chunkCache = chunkCache;
		this.renderBlocks = renderBlocks;
		this.pass = pass;
	}
}
