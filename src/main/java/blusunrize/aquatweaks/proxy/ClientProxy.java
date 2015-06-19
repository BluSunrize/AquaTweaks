package blusunrize.aquatweaks.proxy;

import net.minecraftforge.common.MinecraftForge;
import blusunrize.aquatweaks.AquaEventHandler;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new AquaEventHandler());;
	}
}
