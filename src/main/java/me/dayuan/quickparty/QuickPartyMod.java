package me.dayuan.quickparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "quickparty", name = "Quick Party Mod", version = "1.0.0")
public class QuickPartyMod {
	private Minecraft mc;
	private static File saveFile;
	public static int commandCD = 200;
	public static List<String> IDList = new ArrayList<String>();
	public static final Logger logger = LogManager.getLogger("QuickPartyMod");
	
	
	
	@Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        this.mc = Minecraft.getMinecraft();
        QuickPartyMod.saveFile = new File(this.mc.mcDataDir, "quickparty.cfg");
        QuickPartyMod.loadSettings();
        QuickPartyMod.saveSettings();
        ClientCommandHandler.instance.registerCommand(new Command());
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
	
	



    private static void invitePlayer(List<String> PlayerList){
    	for(int i = 0;i<PlayerList.size();i++){
    			Minecraft.getMinecraft().thePlayer.sendChatMessage("/party invite "+PlayerList.get(i));
    			logger.info("Invited "+PlayerList.get(i));
    			try {
    				Thread.sleep(commandCD);
    			} catch (InterruptedException e) {
    				// TODO 自动生成的 catch 块
    				e.printStackTrace();
    			}
    		}

    }
    
    public static void invitePlayer(){
    	new Thread(()->invitePlayer(IDList)).start();
    }
    
    public static void EditPlayerList(String playerName,Boolean isAddingPlayer){
    	if(isAddingPlayer){
    		IDList.add(playerName);
    	}else{
    		IDList.remove(playerName);
    	}
    }

    public static void saveSettings() {
        final Configuration config = new Configuration(saveFile);
        updateSettings(config, true);
        config.save();
    }
    
    public static void loadSettings() {
        final Configuration config = new Configuration(saveFile);
        config.load();
        updateSettings(config, false);
    }
    
    private static void updateSettings(final Configuration config, final boolean save) {
        Property prop;
        
        prop = config.get("global", "IDList", new String[] {"testkey4", "testkey5", "testkey6"});
        if (save) {
        	String[] tmpSaveIDArray = new String[IDList.size()];
        	for(int i=0;i<IDList.size();i++){
        		tmpSaveIDArray[i] = (String) IDList.get(i);
        	}
            prop.set(tmpSaveIDArray);
            
        }else {
           String[] tmploadIDArray = prop.getStringList();
           List<String> tmpIDList = new ArrayList<String>();
           for(int i = 0;i<tmploadIDArray.length;i++){
        	  tmpIDList.add(tmploadIDArray[i]);
           }
           IDList = tmpIDList;
        }
        
        prop = config.get("global", "CommandCD", 200);
        if(save){
        	prop.set(commandCD);
        }else{
        	commandCD = prop.getInt();
        }

    }
}

class Command extends CommandBase{

	@Override
	public List<String> getCommandAliases() {
		List<String> list = new ArrayList<String>();
		list.add("qp");
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
		return "quickparty";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		
		return "/quickparty";
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
			if(args[0].equals("cmdcd")){
				if(isInteger(args[1])){
					QuickPartyMod.commandCD = Integer.valueOf(args[1]);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]执行命令的间隔已更改为"+QuickPartyMod.commandCD+"毫秒."));
					QuickPartyMod.saveSettings();
				}
			}
			else if(args[0].equals("remove")){
					QuickPartyMod.EditPlayerList(args[1], false);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]已从组队列表中移除玩家 "+args[1]+" ."));
					QuickPartyMod.saveSettings();
				
			}
			else if(args[0].equals("add")){
					QuickPartyMod.EditPlayerList(args[1], true);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]已向组队列表中添加玩家 "+args[1]+" ."));
					QuickPartyMod.saveSettings();
			}
		}else if(args.length==1){
			if(args[0].equals("party")){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]拉人,冲冲冲!"));
				QuickPartyMod.invitePlayer();
			}else if(args[0].equals("reload")){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]正在重载配置文件."));
				QuickPartyMod.loadSettings();
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]重载完成."));
			}else if(args[0].equals("show")){
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[QuickParty]组队列表中有玩家如下: "+QuickPartyMod.IDList.toString()));
			}
		}else{
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("====== QuickParty Mod by Daaayuan ======"));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty remove <玩家ID> 将玩家从列表中移除    "));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty add  <玩家ID> 添加玩家至组队列表 "));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty cmdcd  <毫秒数> 更改命令间隔."));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty reload 重新从配置文件中读取组队列表 "));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty show 列出当前组队列表中的玩家 "));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /quickparty party 组人,开冲!!!!!!!!!!!!!"));
			
		}
		
	}
	
}
	