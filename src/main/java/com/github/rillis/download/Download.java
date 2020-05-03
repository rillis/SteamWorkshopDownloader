package com.github.rillis.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;

import com.github.rillis.MainFrame;

public class Download {
	private static File rootDir = null;
	private static File steamcmdDir = null;
	private static File steamcmd = null;
	private static File sevenzDir = null;
	private static File sevenz = null;
	
	public static String ERROR = "";
	
	private static void config() {
		//Setar as pastas de run
		try {
			rootDir = new File(Download.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		if(rootDir.isFile()) {
			rootDir = rootDir.getParentFile();
		}
		
		steamcmdDir = new File(rootDir.getPath()+"\\steamcmd");
		steamcmd = new File(steamcmdDir.getPath()+"\\steamcmd.exe");
		sevenzDir = new File(rootDir.getPath()+"\\7z");
		sevenz = new File(sevenzDir.getPath()+"\\7za.exe");	
	}
	
	public static boolean download(File directory, String appID, String url, String fileName) {
		//Setar o product id
		String pID = url.split("id=")[1];
		pID= pID.replace("&searchtext=", "");
		
		//Configura��es de pastas
		config();
		
		MainFrame.log("dir: "+directory.getPath());
		MainFrame.log("appid: "+appID);
		MainFrame.log("pid: "+pID);
		
		//comando para baixar
		command("\""+steamcmd.getPath()+"\" +login anonymous +workshop_download_item "+appID+" "+pID+" +quit");
		
		//comando para zipar
		command("cd \""+sevenzDir.getPath()+"\" && 7za.exe a -tzip "+fileName+" "+"\""+steamcmdDir.getPath()+"\\steamapps\\workshop\\content\\"+appID+"\\"+pID+"\\*\"");
		
		//comando para excluir o cache
		deleteDir(new File(steamcmdDir.getPath()+"\\steamapps\\workshop\\content\\"+appID+"\\"+pID));
		
		//mover zip para pasta final
		try {
			Files.move(new File(sevenzDir.getPath()+"\\"+fileName).toPath(), new File(directory.getPath()+"\\"+fileName).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	private static void command(String cmd) {
		try {
			//Process runtime = Runtime.getRuntime().exec("cmd /c "+cmd);	
			ProcessBuilder builder = new ProcessBuilder(
		            "cmd.exe", "/c", cmd);
		        builder.redirectErrorStream(true);
		        Process runtime = builder.start();
			
			//runtime.waitFor();
		        MainFrame.log("Comando: "+ cmd);
			BufferedReader r = new BufferedReader(new InputStreamReader(runtime.getInputStream()));
	        String line;
	        while (true) {
	            line = r.readLine();
	            if (line == null) { break; }
	            MainFrame.log(line);
	        }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void update() {
		config();
		command("\""+steamcmd.getPath()+"\" +quit");
	}
	
	public static boolean isInstalled() {
		config();
		if(!steamcmd.exists()||!sevenz.exists()) {
			return false;
		}
		return true;
	}
	
	public static boolean isFirstRun() {
		config();
		if(steamcmdDir.list().length>1) {
			return false;
		}
		return true;
	}
}
