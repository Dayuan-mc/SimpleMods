package me.dayuan.fontsfix;

/*
 https://www.mcmod.cn/class/152.html
*/

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = "fontsfix", name = "Fonts fix")
public class fontsfix {
	@Mod.Instance("Fontsfix")
	public static fontsfix instance;

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Minecraft mc = FMLClientHandler.instance().getClient();
		mc.fontRendererObj.setUnicodeFlag(false);
		mc.fontRendererObj.setBidiFlag(false);
	}
	
	
}
