
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class ZipBruteforcer {

	boolean RUN = true;

	static File file;
	String archive, folder;

	static String possibleChars;
	String word = "";
	char[] word1;

	static boolean displayFailed = false;

	public ZipBruteforcer(String[] args) {

		// System.out.println(Arrays.toString(args));
		try {
			boolean canrun = true;
			boolean output = false;

			try {
				for (int i = 0; i < args.length; i++) {

					if (args[i].equals("-i") && i < args.length + 1 && !args[i + 1].equals("-i")
							&& !args[i + 1].equals("-o") && !args[i + 1].equals("-f")) {
						archive = args[i + 1];
						file = new File(archive);
						if (file.exists()) {
							canrun = true;
						} else {
							canrun = false;
							System.out.println("Input archive does not exist. Aborting");
							return;
						}
					} else if (args[i].equals("-o") && i < args.length + 1 && !args[i + 1].equals("-i")
							&& !args[i + 1].equals("-o") && !args[i + 1].equals("-f")) {
						folder = args[i + 1];
						if (new File(folder).exists()) {
							output = true;
						} else {
							System.out.println("Destination directory does not exist. Aborting");
							return;
						}
					} else if (args[i].equals("-f")) {
						displayFailed = true;
					} else if (args[i].equals("-c") && i < args.length + 1 && !args[i + 1].equals("-i")
							&& !args[i + 1].equals("-o") && !args[i + 1].equals("-f")) {
						possibleChars = args[i + 1];
					} else if (args[i].equals("-h")) {
						displayHelp();
						canrun = false;
						return;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("There was some error with the arguments, check them again");

				canrun = false;
				return;

			}

			if (!output && canrun) {
				folder = file.getParent();
				System.out.println(folder);
			}

			if (file == null || archive == null) {
				canrun = false;
			}

			if (possibleChars == null) {
				System.out.println("You have not defined the character to compose the password with");
				displayHelp();
				return;
			}

			System.out.println("Archive to bruteforce: " + archive);
			System.out.println("Destination folder is " + folder);
			System.out.println("Displaying failed passwords:  " + displayFailed);

			if (canrun) {

				/** ACTUAL BRUTEFORCE **/
				while (RUN) {
					word1 = word.toCharArray();
					if (word.length() > 0) {
						incrementCharacterAt(0);
					}
					word += "a";
					
					System.out.println("Now trying with " + word.length() + " passwords");
				}

			} else {
				return;
			}
		} catch (NullPointerException e) {
			displayHelp();
			return;
		}
	}

	public static void displayHelp() {
		System.out.print("Help menu for Zip BruteForcer. \n"
				+ "This programm is used to bruteforce the password of encrypted zip archives. Only works with .zip files\n"
				+ "Arguments:\n" + "\t-c\tAfter this type the character that should compose you password. (Required)\n"
				+ "\t-i\tDefines the input file		(Required)\n"
				+ "\t-o\tDefines the output folder. Default is the folder where the archive is located\n"
				+ "\t-h\tDisplays this help menu without executing anything\n"
				+ "\t-f\tDisplays also the failed passwords, not just the working one\n");
	}

	void incrementCharacterAt(int offset) {
		if (offset < word.length() && RUN) {

			for (int i = 0; i < possibleChars.length(); i++) {
				word1[offset] = possibleChars.charAt(i);
				// System.out.println("Trying with password: " + String.valueOf(word1));
				unzipWithPass(String.valueOf(word1));
				incrementCharacterAt(offset + 1);
			}
		}
	}

	void unzipWithPass(String pass) {
		boolean fail = false;
		if (RUN) {

			final FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("N/A", "zip");
			// Folder where zip file is present
			try {
				ZipFile zipFile = new ZipFile(file);
				if (extensionFilter.accept(file)) {
					if (zipFile.isEncrypted()) {
						// Your ZIP password

						zipFile.setPassword(pass);
					}
					List fileHeaderList = zipFile.getFileHeaders();

					for (int i = 0; i < fileHeaderList.size(); i++) {
						FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
						// Path where you want to Extract
						try {
							zipFile.extractFile(fileHeader, folder);
							fail = false;
						} catch (net.lingala.zip4j.exception.ZipException e) {
							// System.out.println(e);
							fail = true;
						}
					}
				}
			} catch (Exception e) {
				fail = true;
			}

			if (!fail) {
				RUN = false;
				System.out.println("Done! Archive password is: " + pass);

				try {
					String verify, putData;
					File file = new File(folder + "/" + "password.txt");
					file.createNewFile();
					FileWriter fw = new FileWriter(file);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write("Password for archive " + archive + " is " + pass);
					bw.flush();
					bw.close();

					System.out.println("Password has been saved to:" + file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (displayFailed) {
					System.out.println("Password: " + pass + " failed");
				}
			}
		}
	}

	public static void main(String[] args) {
		new ZipBruteforcer(args);
	}
}
