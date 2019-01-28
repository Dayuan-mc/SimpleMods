package me.dayuan.guildautokick;

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
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = "guildautokick", name = "Guild Auto Kick", version = "1.0")
public class GuildAutoKick {
	private Minecraft mc;
	private static File saveFile;
	public static boolean enabled = true;
	public static int timer = 300;
	public static int timeLeft = -1;
	public static int kickCD = 500;
	public static boolean started = false;
	public static boolean timerStarted = false;
	public static boolean isSpecialMsgReceived = false;
	public static String specialGroup = "TrialMember";
	public static String kickReason = "YouLose!";
	public static final Logger logger = LogManager.getLogger("GuildAutoKick");
	
	
	
	@Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        this.mc = Minecraft.getMinecraft();
        GuildAutoKick.saveFile = new File(this.mc.mcDataDir, "guildautokick.cfg");
        this.loadSettings();
        ClientCommandHandler.instance.registerCommand(new Command());
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
	

	
	   public static void kicktimer() {
		   timerStarted = true;
	        try {
	        	for(int i = 0;i<timer;i++){
	        		Thread.sleep(1000L);
	        		timeLeft = timer - i;
	        	}
	        	
				Thread.yield();
				return;
	            }

	        catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	}

    /*
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (enabled) {

        }
    }	
    */
	

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onJoin(ClientConnectedToServerEvent event){
		if(enabled){
			if(!timerStarted){
				new Thread(()->kickStart()).start();
			}
		}
	}

	   
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event){
        if (event.isCanceled()) {
            return;
        }
        if (event.message.getUnformattedText().isEmpty()) {
            return;
        }
        if(!enabled){
        	return;
        }
        if(!started){
        	return;
        }
        String message = event.message.getFormattedText();
  //      logger.info(event.message.getFormattedText());
   //     logger.info(event.message.getUnformattedText());
        if(message.contains("-- "+ specialGroup + " --")){
        	isSpecialMsgReceived = true;
        	logger.info("Step 0");
        }
        if(isSpecialMsgReceived){
        	logger.info("Step 1");
        	if(message.contains(" ●  ")){
        		logger.info("Step 2");
        		isSpecialMsgReceived = false;
        		started = false;
        		String playerlist[] = event.message.getUnformattedText().replace("[VIP] ", "").replace("[VIP+] ", "").replace("[MVP] ", "").replace("[MVP+] ", "").split(" ●  ");
        		String formattedlist[] = event.message.getFormattedText().replace("[VIP] ", "").replace("[VIP+] ", "").replace("[MVP] ", "").replace("[MVP+] ", "").split(" ●  ");
      //  		String messagecut = message;
        		Boolean[] playerinfo = new Boolean[playerlist.length];
        	//	int specialCharAt = 0;
        		for(int i = 0;i<playerlist.length;i++){
        			String result = formattedlist[i].substring(formattedlist[i].length()-1);
        			//String a = (message.substring(message.indexOf("●", specialCharAt)-2, message.indexOf("●", specialCharAt)-2));
        			//logger.info(a);
        			playerinfo[i] = result.equals("a");
        			//specialCharAt = message.indexOf("●", specialCharAt) +1;
        		}
        		timerStarted = false;
        		new Thread(()->kickPlayer(playerlist,playerinfo)).start();;

        	}
        }
    }
	
    public static void kickPlayer(String[] player,Boolean[] online){
    	if(!enabled){
    		return;
    	}
    	for(int i = 0;i<player.length;i++){
    		if(!online[i]){
    			Minecraft.getMinecraft().thePlayer.sendChatMessage("/g kick "+player[i]+" "+kickReason);
    			logger.info(player[i]+","+online[i]);
    			try {
    				Thread.sleep(kickCD);
    			} catch (InterruptedException e) {
    				// TODO 自动生成的 catch 块
    				e.printStackTrace();
    			}
    		}
    	}
    	if(enabled&&!timerStarted)new Thread(()->kickStart()).start();;
    	Thread.yield();
    }
    
    public static void kickStart(){
    	//new Thread(()->kicktimer()).start();
    	kicktimer();
    	if(enabled)started = true;
    	if(enabled)Minecraft.getMinecraft().thePlayer.sendChatMessage("/g members");
    	return;
    }
	
    public static void saveSettings() {
        final Configuration config = new Configuration(saveFile);
        updateSettings(config, true);
        config.save();
    }
    
    public void loadSettings() {
        final Configuration config = new Configuration(GuildAutoKick.saveFile);
        config.load();
        GuildAutoKick.updateSettings(config, false);
    }
    
    private static void updateSettings(final Configuration config, final boolean save) {
        Property prop = config.get("global", "GroupName", "TrialMember");
        if (save) {
            prop.set(specialGroup);
        }
        else {
           specialGroup = prop.getString();
        }
        
        prop = config.get("global", "KickReason", "YouLose!");
        if (save) {
            prop.set(kickReason);
        }
        else {
           kickReason = prop.getString();
        }
        
        prop = config.get("global", "KickTimer", 300);
        if(save){
        	prop.set(timer);
        }else{
        	timer = prop.getInt();
        }
        
        prop = config.get("global", "CommandCD", 500);
        if(save){
        	prop.set(kickCD);
        }else{
        	kickCD = prop.getInt();
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
		list.add("gautokick");
		list.add("gakick");
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
		return "guildautokick";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		
		return "/guildautokick";
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
					GuildAutoKick.timer = Integer.valueOf(args[1]);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]移除公会会员的间隔已更改为"+GuildAutoKick.timer+"秒."));
				}
			}
			else if(args[0].equals("cmdcd")){
				if(isInteger(args[1])){
					GuildAutoKick.kickCD = Integer.valueOf(args[1]);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]执行命令的间隔已更改为"+GuildAutoKick.kickCD+"毫秒."));
				}
			}
			else if(args[0].equals("reason")){
					GuildAutoKick.kickReason = args[1];
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]移除原因已更改为"+GuildAutoKick.kickReason+"."));
				
			}
			else if(args[0].equals("name")){
					GuildAutoKick.specialGroup = args[1];
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]清理组名已更改为"+GuildAutoKick.specialGroup+"."));			
			}
		}else if(args.length==1){
			if(args[0].equals("toggle")){
				GuildAutoKick.enabled = !GuildAutoKick.enabled;
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]Mod已"+ (GuildAutoKick.enabled ? "开启":"关闭")));
			}else if(args[0].equals("kick")){
				GuildAutoKick.enabled = !GuildAutoKick.enabled;
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[GuildAutoKick]执行单次启动."));
				GuildAutoKick.started = true;
		    	Minecraft.getMinecraft().thePlayer.sendChatMessage("/g members");
			}
		}else{
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("==== GuildAutoKick Mod by Daaayuan ===="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick timer <秒数> 更改踢人间隔.   ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick cmdcd <毫秒数> 更改命令间隔. ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick reason <原因> 更改踢人原因.  ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick name <组名> 更改清理组名.    ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick toggle      开关Mod.        ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("= /gakick kick      单次启动.         ="));
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("当前状态:"+(GuildAutoKick.enabled?"开启":"关闭")+",距离下一次清理还有"+GuildAutoKick.timeLeft+"秒."));
		}
		GuildAutoKick.saveSettings();
	}
	
}