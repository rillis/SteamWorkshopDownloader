package com.github.rillis;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.github.rillis.download.Download;
import com.rillis.config.Config;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = -8816096921269476110L;
	
	private JPanel contentPane;
	private static JProgressBar progressBar;
	private static JLabel currentAction;
	private static JLabel lblDownloadDir;
	private static JTextField textField;
	private static JButton search;
	
	private static Config config = new Config("SteamDL.cfg");
	private static String dirName = "";
	private static File dir = null;
	private static JTextField txtAppID;
	private static JTextField txtUrl;
	private static JButton btnDownload;
	private static JLabel lblLog;
	private static JTextArea logtxt;
	private static JScrollPane scroll;
	private static JCheckBox chkAuto;
	
	public static void log(String msg) {
		logtxt.setText(logtxt.getText()+"\n"+msg);
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
		
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if(!Download.isInstalled()) {
						JOptionPane.showMessageDialog(null, "Steamcmd not found \n");
						System.exit(0);
					}
					
					if(config.has("dirName")) {
						dirName=(String) config.get("dirName");
						dir = new File(dirName);
					}
					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
					
					if(Download.isFirstRun()) {
						new Thread() {
							public void run() {
								lockCommands(true);
								currentAction.setText("Updating SteamCMD...");
								progressBar.setIndeterminate(true);
								Download.update();
								progressBar.setIndeterminate(false);
								currentAction.setText("");
								lockCommands(false);
							}
						}.start();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public MainFrame() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 910, 437);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(0, 387, 616, 21);
		contentPane.add(progressBar);
		
		currentAction = new JLabel("");
		currentAction.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		currentAction.setHorizontalAlignment(SwingConstants.CENTER);
		currentAction.setBounds(0, 329, 616, 57);
		contentPane.add(currentAction);
		
		lblDownloadDir = new JLabel("Download dir");
		lblDownloadDir.setBounds(10, 11, 210, 14);
		contentPane.add(lblDownloadDir);
		
		textField = new JTextField(dirName);
		textField.setEnabled(false);
		textField.setEditable(false);
		textField.setBounds(10, 30, 475, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		search = new JButton("Search");
		search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});
		search.setBounds(495, 29, 111, 23);
		contentPane.add(search);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 73, 596, 2);
		contentPane.add(separator);
		
		JLabel lblAppid = new JLabel("AppID");
		lblAppid.setBounds(10, 98, 46, 14);
		contentPane.add(lblAppid);
		
		txtAppID = new JTextField("");
		txtAppID.setColumns(10);
		txtAppID.setBounds(10, 123, 596, 32);
		txtAppID.setEnabled(false);
		contentPane.add(txtAppID);
		
		txtUrl = new JTextField("");
		txtUrl.setColumns(10);
		txtUrl.setBounds(11, 212, 596, 32);
		contentPane.add(txtUrl);
		
		JLabel lblUrl = new JLabel("Url");
		lblUrl.setBounds(11, 187, 46, 14);
		contentPane.add(lblUrl);
		
		btnDownload = new JButton("Download .zip");
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(txtUrl.getText().equals("")) {
					JOptionPane.showMessageDialog(contentPane, "Url blank.");
				}else if(txtAppID.getText().equals("") && !chkAuto.isSelected()){
					JOptionPane.showMessageDialog(contentPane, "AppID blank.");
				}
				else {
					String html = getHTML(txtUrl.getText());
					
					String filename = "";
					filename = html.split("breadcrumbs\">")[1].split("\">")[1].split("</a>")[0]+"-";
					filename+= html.split("workshopItemTitle\">")[1].split("</div>")[0]+".zip";
					
					filename = sanitizeFilename(filename);
					
					log("File name: "+filename);
					final String finalFileName = filename;
					String appidtemp = txtAppID.getText();
					//auto search
					if(chkAuto.isSelected()) {
						try {
							
							appidtemp = html.split("steamcommunity.com/app/")[1]; 
							appidtemp = appidtemp.split("\"")[0];
							log("APPID encontrado: "+appidtemp);
						}catch(Exception e) {
							JOptionPane.showMessageDialog(contentPane, "N�o consegui identificar o AppID.");
							System.exit(0);
						}
					}
					
					final String appid = appidtemp;
					if(!txtUrl.getText().contains("https://steamcommunity.com/sharedfiles/filedetails/?id=")) {
						JOptionPane.showMessageDialog(contentPane, "Invalid url.");
					}else {
						new Thread() {
							public void run() {								
								lockCommands(true);
								currentAction.setText("Downloading...");
								progressBar.setIndeterminate(true);
								Download.download(dir, appid, txtUrl.getText(), finalFileName);
								progressBar.setIndeterminate(false);
								currentAction.setText("Download complete.");
								lockCommands(false);
							}
						}.start();
					}
				}				
			}
		});
		btnDownload.setBounds(10, 266, 596, 52);
		contentPane.add(btnDownload);
		
		lblLog = new JLabel("LOG");
		lblLog.setBounds(644, 11, 46, 14);
		contentPane.add(lblLog);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(626, 11, 10, 386);
		contentPane.add(separator_1);
		
		logtxt = new JTextArea();
		logtxt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		logtxt.setWrapStyleWord(true);
		logtxt.setLineWrap(true);
		logtxt.setEnabled(false);
		logtxt.setEditable(false);
		logtxt.setBounds(632, 28, 262, 369);
		
		
		scroll = new JScrollPane(logtxt);
		scroll.setBounds(632, 28, 262, 369);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(scroll);
		
		chkAuto = new JCheckBox("Auto search AppID");
		chkAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chkAuto.isSelected()) {
					txtAppID.setEnabled(false);
				}else {
					txtAppID.setEnabled(true);
				}
			}
		});
		chkAuto.setSelected(true);
		chkAuto.setBounds(11, 156, 595, 23);
		contentPane.add(chkAuto);
		
	}
	
	private static void openFileChooser() {
		boolean satisfied = false;
		while(!satisfied) {
			JFileChooser file = new JFileChooser(); 
	         file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	         int i= file.showOpenDialog(null);
	       if (i==1){
	          
	       } else {
	           dir = file.getSelectedFile();
	           dirName = dir.getPath();
	           config.set("dirName", dir.getPath());
	           textField.setText(dirName);
	           satisfied=true;
	       }
		}
	}
	
	private static String getHTML(String url) {
		String content = null;
		URLConnection connection = null;
		try {
		  connection =  new URL(url).openConnection();
		  Scanner scanner = new Scanner(connection.getInputStream());
		  scanner.useDelimiter("\\Z");
		  content = scanner.next();
		  scanner.close();
		}catch ( Exception ex ) {
		    ex.printStackTrace();
		}
		return content;
	}
	
	public static String sanitizeFilename(String name) {
		name = name.replaceAll(" ", "_");
	    return name.replaceAll("[:\\\\/*?|<>]", "_");
	  }
	
	private static void lockCommands(boolean b) {
		if(b) {
			chkAuto.setEnabled(false);
			search.setEnabled(false);
			txtAppID.setEnabled(false);
			txtUrl.setEnabled(false);
			btnDownload.setEnabled(false);
		}else {
			if(chkAuto.isSelected()) {
				txtAppID.setEnabled(false);
			}else {
				txtAppID.setEnabled(true);
			}
			search.setEnabled(true);
			txtAppID.setEnabled(true);
			txtUrl.setEnabled(true);
			btnDownload.setEnabled(true);
		}
	}
}
