/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.example.project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import com.backblaze.b2.client.contentHandlers.B2ContentFileWriter;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2Bucket;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2ListBucketsResponse;

/**
 * Class to hold the character count info
 * 
 * @author Michael Lam
 *
 */
class CharacterCount {
	String fileName;
	long count;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}

/**
 * ASSIGNMENT: Write a command-line program in Java to perform simple data
 * analysis on some files stored in the B2 cloud storage system. The program
 * should accept two command-line arguments: an application key ID, and an
 * application key to use when connecting to B2.
 * 
 * Using the ​b2-sdk-java​ library and the application key id and key from the
 * command line, find all the buckets that belong to the account, find all the
 * files in those buckets, and count how many times the lower-case letter "a"
 * appears in each file. (You can assume the files contain ASCII.) Then print a
 * report listing the count and filename for each file, showing how many times
 * the letter "a" appears in the file.
 * 
 * The output should be sorted, with primary sorting on the count, and secondary
 * sorting on the file name in the cases where the count is the same. The output
 * should look like this, with a space between the count and the file name: 9
 * file7.txt 23 file3.txt 23 file4.txt 62 file2.txt If any exceptions happen
 * during execution, report the error to stderr and exit with non-zero status.
 * 
 * @author Michael Lam
 *
 */
public class App {
	private static final String USER_AGENT = "B2Sample";

	private final B2StorageClient client;

	public App(String applicationKey, String applicationKeyId) {
		client = B2StorageClientFactory.createDefaultFactory().create(applicationKeyId, applicationKey, USER_AGENT);
	}

	public App() {
		client = B2StorageClientFactory.createDefaultFactory().create(USER_AGENT);
	}

	/**
	 * Download individual file by id using sdk
	 * 
	 * @param args
	 * @throws B2Exception
	 */
	private void downloadFileById(String[] args) throws B2Exception {
		final int iLastArg = args.length - 1;
		final String fileId = args[iLastArg - 1];
		final String localFileName = args[iLastArg];

		final B2ContentFileWriter sink = B2ContentFileWriter.builder(new File(localFileName))
				.setVerifySha1ByRereadingFromDestination(true).build();

		int tryCount = 0;

		try {
			client.downloadById(fileId, sink);
		} catch (B2Exception e) {
			if (++tryCount < 5) {
				client.downloadById(fileId, sink);
			}
		}
	}

	/**
	 * Download files into directory given
	 * 
	 * @param directoryName
	 */
	public void downloadFiles(String directoryName) {
		try {
			B2ListBucketsResponse response = client.listBuckets();

			List<B2Bucket> listBucket = response.getBuckets();
			listBucket.parallelStream().forEach((b) -> {
				String bucketId = b.getBucketId();
				String[] args = null;
				try {
					for (B2FileVersion file : client.fileNames(bucketId)) {
						args = new String[2];
						args[0] = file.getFileId();
						args[1] = directoryName + File.separator + file.getFileName();

						downloadFileById(args);
					}
				} catch (B2Exception e) {
					e.printStackTrace();
				}

			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get number of occurrences of a char in a file
	 * 
	 * @param filePath
	 * @param c1
	 * @return
	 */
	public long getNumberOfOccurences(String filePath, char c1) {
		long count = 0;

		String s = readAllLines(filePath);

		// Turn the String into chars.
		Character[] cA = s.chars().mapToObj(c -> (char) c).toArray(Character[]::new);
		// Stream it.
		Stream<Character> stream = Arrays.asList(cA).stream();
		// Get count of the sought character.
		count = stream.filter(ch -> ch == c1).count();

		return count;
	}

	/**
	 * Read all lines from a file
	 * 
	 * @param filePath
	 * @return
	 */
	private String readAllLines(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	/**
	 * Clear folder of old contents
	 * 
	 * @param directoryName
	 */
	public void clearFolder(String directoryName) {
		File directory = new File(directoryName);

		// Get all files in directory
		File[] files = directory.listFiles();
		for (File file : files) {
			// Delete each file
			if (!file.delete()) {
				System.out.println("Failed to delete " + file);
			}
		}
	}

	/**
	 * Sort list by count and then file name, in ascending order
	 * 
	 * @param unsortedListCharCount
	 * @return
	 */
	public List<CharacterCount> sortListCharCountAscending(List<CharacterCount> unsortedListCharCount) {
		Comparator<CharacterCount> compare1 = Comparator.comparing(CharacterCount::getCount)
				.thenComparing(CharacterCount::getFileName);

		List<CharacterCount> sortedList = unsortedListCharCount.stream().sorted(compare1).collect(Collectors.toList());

		return sortedList;
	}

	/**
	 * Sort list by count and then file name, in descending order
	 * 
	 * @param unsortedListCharCount
	 * @return
	 */
	public List<CharacterCount> sortListCharCountDescending(List<CharacterCount> unsortedListCharCount) {
		Comparator<CharacterCount> compare1 = Comparator
				.comparing(CharacterCount::getCount, (c1, c2) -> c2.compareTo(c1))
				.thenComparing(CharacterCount::getFileName, (c1, c2) -> c2.compareTo(c1));

		List<CharacterCount> sortedList = unsortedListCharCount.stream().sorted(compare1).collect(Collectors.toList());

		return sortedList;
	}

	/**
	 * Write to output file
	 * 
	 * @param fileName
	 * @param listCharCount
	 */
	private void writeOutputToFile(String fileName, List<CharacterCount> listCharCount) {
		FileWriter myWriter = null;

		try {
			myWriter = new FileWriter(fileName);
			for (CharacterCount cc : listCharCount) {
				myWriter.write(String.format("%d %s \n", cc.getCount(), cc.getFileName()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (myWriter != null)
					myWriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get options based on input args and return map of these options
	 * 
	 * @param args
	 * @return
	 */
	private static Map<String, String> getOptionsMap(String[] args) {
		Map<String, String> mapOptions = new HashMap<>();

		if (args == null || args.length < 4) {
			System.out.println("Proper Usage is: java program -B2_APPLICATION_KEY XXXXX -B2_APPLICATION_KEY_ID XXXXX");
			System.exit(0);
		}

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				if (args[i].length() < 2 || args[i + 1].length() == 0)
					throw new IllegalArgumentException("Not a valid argument: " + args[i]);

				mapOptions.put(args[i].substring(1), args[i + 1]);
				break;
			default:
				break;
			}
		}

		return mapOptions;
	}

	public static void main(String[] args) {

		Map<String, String> mapOptions = getOptionsMap(args);

		App app = new App(mapOptions.get("B2_APPLICATION_KEY"), mapOptions.get("B2_APPLICATION_KEY_ID"));
		String directoryName = "files";
		String outputFileName = "output.txt";

		try {
			File directory = new File(directoryName);
			if (!directory.exists()) {
				directory.mkdir();
			}

			app.clearFolder(directoryName);
			app.downloadFiles(directoryName);

			final List<CharacterCount> listCharCount = new ArrayList<>();

			Path dir = Paths.get(directoryName);
			List<Path> files = Files.list(dir).filter(Files::isRegularFile).collect(Collectors.toList());

			for (Path f : files) {
				CharacterCount charCount = null;

				f = f.normalize();
				long count = app.getNumberOfOccurences(f.toString(), 'a');

				charCount = new CharacterCount();
				charCount.setCount(count);
				charCount.setFileName(f.getFileName().toString());

				listCharCount.add(charCount);
			}

			System.out.println("In Ascending Order: ");
			List<CharacterCount> listSortedCharCountAscending = app.sortListCharCountAscending(listCharCount);

			for (CharacterCount cc : listSortedCharCountAscending) {
				System.out.println(String.format("%d %s", cc.getCount(), cc.getFileName()));
			}

			System.out.println("In Descending Order: ");
			List<CharacterCount> listSortedCharCountDescending = app.sortListCharCountDescending(listCharCount);

			for (CharacterCount cc : listSortedCharCountDescending) {
				System.out.println(String.format("%d %s", cc.getCount(), cc.getFileName()));
			}

			app.writeOutputToFile(directoryName + File.separator + outputFileName, listSortedCharCountAscending);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}

		System.exit(0);
	}

}
