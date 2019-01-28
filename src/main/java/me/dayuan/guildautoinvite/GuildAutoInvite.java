package me.dayuan.guildautoinvite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "guildautoinvite", name = "Guild Auto Invite", version = "1.0.0")
public class GuildAutoInvite {
	
	private static File saveFile;
	private Minecraft mc;
	public static boolean enabled = true;
	public static int delay = 50;
	public static int timer = 30;
	public static final Logger logger = LogManager.getLogger("GuildAutoInvite");
	
	
	@EventHandler
	public void Init(FMLInitializationEvent event){
        this.mc = Minecraft.getMinecraft();
        GuildAutoInvite.saveFile = new File(this.mc.mcDataDir, "guildautoinvite.cfg");
        System.out.println("Guild Auto Invite Mod is Loading!");
        this.loadSettings();
        GuildAutoInvite.saveSettings();
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new Command());
	}
	
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event){
		if(event.message.getUnformattedText().contains("You are AFK.")){
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_four_four");
		}
		
		
		if(event.message.getUnformattedText().contains("保护你的床并摧毁敌人的床。收集铁锭，金锭，绿宝石和钻石")){
			new Thread(()->inviteAll()).start();
			if(!enabled)return;
			new Thread(()->nextGame()).start();
		}
	}
	
	public void nextGame(){
		if(!enabled)return;
		try {
			Thread.sleep(timer*1000L);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		Minecraft.getMinecraft().thePlayer.sendChatMessage("/play bedwars_four_four");
	}
	
	public void inviteAll(){
	/*	try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		Collection<NetworkPlayerInfo> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
		for(final NetworkPlayerInfo player:players){
			 try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			 Minecraft.getMinecraft().thePlayer.sendChatMessage("/guild invite "+player.getGameProfile().getName());
		}	*/
        final Collection<NetworkPlayerInfo> playersC = (Collection<NetworkPlayerInfo>)Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        
        playersC.forEach(loadedPlayer -> {
            String loadedPlayerName = loadedPlayer.getGameProfile().getName();
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/guild invite "+loadedPlayerName);
            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
	}
	
    public static void saveSettings() {
        final Configuration config = new Configuration(saveFile);
        updateSettings(config, true);
        config.save();
    }
    
    public void loadSettings() {
        final Configuration config = new Configuration(GuildAutoInvite.saveFile);
        config.load();
        GuildAutoInvite.updateSettings(config, false);
    }
    
    private static void updateSettings(final Configuration config, final boolean save) {
        Property prop;
        
        
        prop = config.get("global", "Timer", 30);
        if(save){
        	prop.set(timer);
        }else{
        	timer = prop.getInt();
        }
        
        prop = config.get("global", "CommandCD", 200);
        if(save){
        	prop.set(delay);
        }else{
        	delay = prop.getInt();
        }
        
        prop = config.get("global", "Enabled", true);
        if(save){
        	prop.set(enabled);
        }else{
        	enabled = prop.getBoolean();
        }
    }
	
}

class Command extends CommandBase{

	@Override
	public List<String> getCommandAliases() {
		List<String> list = new ArrayList<String>();
		list.add("gautoinvite");
		list.add("gainvite");
		return list;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		// TODO 自动生成的方法存根
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		// TODO 自动生成的方法存根
		return super.getRequiredPermissionLevel();
	}

	@Override
	public String getCommandName() {
		// TODO 自动生成的方法存根
		return "guildautoinvite";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		
		return "/guildautoinvite";
	}
	public static boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    if (str.isEmpty()) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (str.length() == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < str.length(); i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length>1){
			if(args[0].equals("timer")){
				if(isInteger(args[1])){
					GuildAutoInvite.timer = Integer.valueOf(args[1]);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoInvite]邀请公会会员的间隔已更改为"+GuildAutoInvite.timer+"秒."));
				}
			}
			else if(args[0].equals("cmdcd")){
				if(isInteger(args[1])){
					GuildAutoInvite.delay = Integer.valueOf(args[1]);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoInvite]执行命令的间隔已更改为"+GuildAutoInvite.delay+"毫秒."));
				}
			}
		}else if(args.length==1){
			if(args[0].equals("toggle")){
				GuildAutoInvite.enabled = !GuildAutoInvite.enabled;
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoInvite]Mod已"+ (GuildAutoInvite.enabled ? "开启":"关闭")));
			}
		}else{
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("==== GuildAutoInvite Mod by Daaayuan ===="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gainvite timer <秒数> 更改邀请间隔.   ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gainvite cmdcd <毫秒数> 更改命令间隔. ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gainvite toggle      开关Mod.        ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("当前状态:"+(GuildAutoInvite.enabled?"开启":"关闭")));
		}
		GuildAutoInvite.saveSettings();
	}
	

}