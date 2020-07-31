package de.keksuccino.fancymenu.mainwindow;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;

import de.keksuccino.fancymenu.FancyMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class MainWindowHandler {

	private static File icondir = new File("config/fancymenu/minecraftwindow/icons");
	
	public static void init() {
		if (!icondir.exists()) {
			icondir.mkdirs();
		}
	}
	
	public static void updateWindowIcon() {
		if (FancyMenu.config.getOrDefault("customwindowicon", false)) {
			try {
				File i16 = new File(icondir.getPath() + "/icon16x16.png");
				File i32 = new File(icondir.getPath() + "/icon32x32.png");
				if (!i16.exists() || !i32.exists()) {
					System.out.println("## ERROR ## [FANCYMENU] Unable to set custom icons: 'icon16x16.png' or 'icon32x32.png' missing!");
					return;
				}
				
				BufferedImage i16buff = ImageIO.read(i16);
				if ((i16buff.getHeight() != 16) || (i16buff.getWidth() != 16)) {
					System.out.println("'## ERROR ## [FANCYMENU] Unable to set custom icons: 'icon16x16.png' not 16x16!");
					return;
				}
				BufferedImage i32buff = ImageIO.read(i32);
				if ((i32buff.getHeight() != 32) || (i32buff.getWidth() != 32)) {
					System.out.println("'## ERROR ## [FANCYMENU] Unable to set custom icons: 'icon32x32.png' not 32x32!");
					return;
				}

		        int[] i1 = i16buff.getRGB(0, 0, i16buff.getWidth(), i16buff.getHeight(), (int[])null, 0, i16buff.getWidth());
		        ByteBuffer i16bytebuffer = ByteBuffer.allocate(4 * i1.length);
		        for (int i : i1) {
		        	i16bytebuffer.putInt(i << 8 | i >> 24 & 255);
		        }
		        i16bytebuffer.flip();
		        
		        int[] i2 = i32buff.getRGB(0, 0, i32buff.getWidth(), i32buff.getHeight(), (int[])null, 0, i32buff.getWidth());
		        ByteBuffer i32bytebuffer = ByteBuffer.allocate(4 * i2.length);
		        for (int i : i2) {
		        	i32bytebuffer.putInt(i << 8 | i >> 24 & 255);
		        }
		        i32bytebuffer.flip();
		        
		        Display.setIcon(new ByteBuffer[] {i16bytebuffer, i32bytebuffer});
		        
				System.out.println("[FANCYMENU] Custom minecraft icon successfully loaded!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void updateWindowTitle() {
		String s = FancyMenu.config.getOrDefault("customwindowtitle", "");
		if ((s != null) && (!s.equals(""))) {
			Display.setTitle(s);
		}
	}
	
	public static int getScaledWidth() {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		return res.getScaledWidth();
	}
	
	public static int getScaledHeight() {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		return res.getScaledHeight();
	}

}
