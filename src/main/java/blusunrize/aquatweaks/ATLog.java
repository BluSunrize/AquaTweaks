package blusunrize.aquatweaks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ATLog
{
	public static final Logger logger = LogManager.getLogger("AquaTweaks");

	public static void info(String s)
	{
		logger.info(s);
	}
}
