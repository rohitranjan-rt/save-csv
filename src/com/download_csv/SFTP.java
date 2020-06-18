package com.download_csv;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.jcraft.jsch.*;
import com.save_csv.Save;

public class SFTP {
	private String host;
	private Integer port;
	private String user;
	private String password;
	private JSch jsch;
	private Session session;
	private Channel channel;
	private static ChannelSftp sftpChannel;
	
	public SFTP(String host, Integer port, String user, String password) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
	}

	public void connect() {
		System.out.println("connecting..."+host);
		try {
			jsch = new JSch();
			session = jsch.getSession(user, host,port);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp) channel;
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
	public void disconnect() {
		System.out.println("disconnecting...");
		sftpChannel.disconnect();
		channel.disconnect();
		session.disconnect();
	}
	public void download(String fileName, String localDir) {
		byte[] buffer = new byte[1024];
		BufferedInputStream bis;
		connect();
		try {
			// Change to output directory
			String cdDir = fileName.substring(0, fileName.lastIndexOf("/") + 1);
			sftpChannel.cd(cdDir);

			File file = new File(fileName);
			bis = new BufferedInputStream(sftpChannel.get(file.getName()));

			File newFile = new File(localDir + "/" + file.getName());
			
			// Download file
			OutputStream os = new FileOutputStream(newFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			int readCount;
			while ((readCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, readCount);
			}
			Save save = new Save();
			save.saveCsv(newFile);
			bis.close();
			bos.close();
			System.out.println("File downloaded successfully - "+ file.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}
		disconnect();
	}

	public static void main(String[] args) {
		
		
		String localPath = "/home/Downloads/";
		String remotePath = "/home/csv/";
		
		SFTP ftp = new SFTP("10.100.12.14", 2222, "madan", "password");
		try {
		Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("*.cvs");
		for(ChannelSftp.LsEntry entry : list) {
				sftpChannel.get(entry.getFilename(), remotePath + entry.getFilename());
				Set set = new HashSet((Collection) entry);
				ftp.download(remotePath + set, localPath);
			} }catch (SftpException e) {
				e.printStackTrace();
			}
		
		

	}

}
