package com.aol.mineusb.bootstrap;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import com.aol.w67clement.mineapi.frame.dialogs.DownloadDialog;
import com.aol.w67clement.mineapi.logger.MineLogger;
import com.aol.w67clement.mineapi.logger.MineLoggerManager;

public class Bootstrap {

	// Data
	private static File data = new File("data/MineUSB/");
	// Logger
	private static MineLogger logger;
	// Latest version
	private static String latestVersion;

	public static void main(String[] args) {
		MineLoggerManager.registerLogger("MineUSB Bootstrap");
		logger = MineLoggerManager.getLogger("MineUSB Bootstrap");
		logger.info("Starting MineUSB's bootstrap...");
		logger.info("Initializing of look and feel...");
		// Initialize look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable ignored) {
			try {
				logger.error("Your java failed to provide normal look and feel, trying the old fallback now");
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} catch (Throwable t) {
				logger.error("Unexpected exception setting look and feel");
			}
		}
		if (!data.exists())
			data.mkdirs();
		String webSite = "https://67clement.github.io/downloads/MineUSB/Latest.txt";
		if (checkConnectivity(webSite)) {
			if (checkUpdate(webSite)) {
				retrieveUpdate(latestVersion);
			} else {
				logger.info("No update found.");
			}
		} else {
			logger.info("Cannot check update. Starting MineUSB...");
		}
		File file = new File("MineUSB.jar");
		if (!file.exists()) {
			checkUpdate(webSite);
			retrieveUpdate(latestVersion);
		}
		startMineUSB(file);
	}

	public static boolean checkConnectivity(String webSite) {
		logger.printInfo("Check connectivity to " + webSite + "... ");
		try {
			URL url = new URL(webSite);
			url.openConnection().getContent();
			logger.println("Done.");
			return true;
		} catch (IOException e) {
			// e.printStackTrace();
		}
		logger.println("Failed.");
		return false;
	}

	public static boolean checkUpdate(String webSite) {
		URLConnection connection;
		try {
			// Open connection
			connection = new URL(webSite).openConnection();
			connection.addRequestProperty("User-Agent", "MineUSB");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			List<String> lines = new ArrayList<String>();
			String line;
			// Read lines
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			// Version found
			latestVersion = lines.get(0);
			// File of local version
			File file = new File("data/MineUSB/Version.txt");
			// Check file
			if (!file.exists()) {
				PrintWriter writer = new PrintWriter(file);
				writer.print(latestVersion);
				writer.close();
				logger.info("Update found! Version: " + latestVersion);
				return true;
			}
			// Read the file
			BufferedReader br = new BufferedReader(new FileReader(file));
			List<String> localLines = new ArrayList<String>();
			String localline;
			while ((localline = br.readLine()) != null) {
				localLines.add(localline);
			}
			br.close();
			// Local version
			String localVersion = localLines.get(0);
			if (!latestVersion.equals(localVersion)) {
				logger.info("Update found! Version: " + latestVersion);
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void retrieveUpdate(final String version) {

		File dest = new File("MineUSB.jar");
		String webSite = "https://67clement.github.io/downloads/MineUSB/MineUSB_"
				+ version + ".jar";
		DownloadDialog dialog = new DownloadDialog(
				"Downloading MineUSB...",
				"Downloading MineUSB " + version + " (0%)",
				Toolkit.getDefaultToolkit()
						.getImage(
								Bootstrap.class
										.getResource("/com/aol/mineusb/bootstrap/res/favicon.png")));
		dialog.setVisible(true);
		try {
			if (dest.exists()) {
				dest.delete();
			}
			dialog.getProgressBar().setIndeterminate(true);
			if (!checkConnectivity(webSite)) {
				logger.error("MineUSB's Bootstrap can't retrieve MineUSB.");
				return;
			}
			final URLConnection connection = new URL(webSite).openConnection();
			connection.addRequestProperty("User-Agent", "MineUSB");
			final long size = connection.getContentLengthLong();
			final BufferedInputStream in = new BufferedInputStream(
					connection.getInputStream());
			final FileOutputStream out = new FileOutputStream(dest);
			final byte data[] = new byte[1024];
			int count;
			double sumCount = 0.0;
			dialog.getProgressBar().setIndeterminate(false);
			int count2 = 0;
			int count3 = 0;
			while ((count = in.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
				sumCount += count;
				if (size > 0) {
					final int percent = (int) (sumCount / size * 100.0);
					if (dialog != null) {
						dialog.setPercent(percent);
						dialog.setText("Downloading MineUSB " + version + " ("
								+ percent + "%)");
						if (count3 == 75) {
							if (count2 == 4) {
								count2 = 0;
							}
							switch (count2) {
							default:
								dialog.setTitle("Downloading MineUSB");
								break;
							case 0:
								dialog.setTitle("Downloading MineUSB");
								break;
							case 1:
								dialog.setTitle("Downloading MineUSB.");
								break;
							case 2:
								dialog.setTitle("Downloading MineUSB..");
								break;
							case 3:
								dialog.setTitle("Downloading MineUSB...");
								break;
							}
							count2++;
							count3 = 0;
						} else {
							count3++;
						}
					}
				}
			}
			out.close();
			in.close();
			dialog.dispose();
			// File of local version
			File file = new File("data/MineUSB/Version.txt");
			// Check file
			if (!file.exists()) {
				PrintWriter writer = new PrintWriter(file);
				writer.print(latestVersion);
				writer.close();
			} else {
				file.delete();
				PrintWriter writer = new PrintWriter(file);
				writer.print(latestVersion);
				writer.close();
			}
		} catch (final Exception ex) {
			if (dest.exists()) {
				dest.delete();
			}
			ex.printStackTrace();
		}

	}

	public static void startMineUSB(File mineUSBJar) {
		logger.info("Launching MineUSB...");
		try {
			@SuppressWarnings("resource")
			Class<?> mineUSBClass = new URLClassLoader(new URL[] { mineUSBJar
					.toURI().toURL() })
					.loadClass("com.aol.mineusb.MineUSB");
			Constructor<?> constructor = mineUSBClass
					.getConstructor(new Class<?>[] {});
			mineUSBClass.getMethod("main", new Class<?>[] { String[].class })
					.invoke(constructor.newInstance(new Object[] {}),
							(Object) new String[] {});
		} catch (ClassNotFoundException | MalformedURLException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException | InstantiationException e) {
			logger.error("MineUSB's Bootstrap can't launch MineUSB.");
			e.printStackTrace();
		}
	}
}
