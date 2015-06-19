package blusunrize.aquatweaks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.aquatweaks.proxy.CommonProxy;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = AquaTweaks.MODID, name = AquaTweaks.MODNAME, version = AquaTweaks.VERSION)
public class AquaTweaks
{
	public static final String MODID = "AquaTweaks";
	public static final String MODNAME = "AquaTweaks";
	public static final String VERSION = "1.0";

	@Instance(MODID)
	public static AquaTweaks instance = new AquaTweaks();	

	@SidedProxy(clientSide="blusunrize.aquatweaks.proxy.ClientProxy", serverSide="blusunrize.aquatweaks.proxy.CommonProxy")
	public static CommonProxy proxy;

	
	public static boolean tweakGlass = true;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getModConfigurationDirectory());
		tweakGlass = config.get("tweaks", "tweakGlass", tweakGlass, "Set to false to re-enable water rendering its sides towards glass").getBoolean();
	}
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.registerHandlers();
		FluidUtils.addDefaultConnectables();
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		ImmutableList<FMLInterModComms.IMCMessage> messages = FMLInterModComms.fetchRuntimeMessages(this);
		for (FMLInterModComms.IMCMessage message : messages)
			if(message.key.equals("registerAquaConnectable"))
			{
				NBTTagCompound tag = message.getNBTValue();
				if(tag!=null && tag.hasKey("modid",8) && tag.hasKey("block",8))
				{
					Block b = GameRegistry.findBlock(tag.getString("modid"), tag.getString("block"));
					int meta = !tag.hasKey("meta")?OreDictionary.WILDCARD_VALUE: tag.getInteger("meta");
					FluidUtils.addBlockToValidConnectables(b, meta);
				}
			}
	}
}
