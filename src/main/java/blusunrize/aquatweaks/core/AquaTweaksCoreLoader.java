package blusunrize.aquatweaks.core;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name(AquaTweaksCoreLoader.NAME)
public class AquaTweaksCoreLoader implements IFMLLoadingPlugin
{
	public static final String NAME = "AquaTweaks Core";
	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{AquaTweaksCoreTransformer.class.getName()};
	}
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	@Override
	public String getSetupClass()
	{
		return null;
	}
	@Override
	public void injectData(Map<String, Object> data)
	{
	}
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}