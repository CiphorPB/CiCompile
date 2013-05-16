package ciphor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Ciphor
 *
 */
public class ZipExtractor {

	public void unzip(File zipFolder, String outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			ZipInputStream zipInputStream = new ZipInputStream(
					new FileInputStream(zipFolder));

			ZipEntry zipEntry = zipInputStream.getNextEntry();

			while (zipEntry != null) {

				String fileName = zipEntry.getName();

				File newFile = new File(outputFolder + File.separator
						+ fileName);

				if (zipEntry.isDirectory()) {

					new File(newFile.getParent()).mkdirs();

				} else {

					FileOutputStream fileOutputStream = null;

					new File(newFile.getParent()).mkdirs();

					fileOutputStream = new FileOutputStream(newFile);

					int len;

					while ((len = zipInputStream.read(buffer)) > 0) {

						fileOutputStream.write(buffer, 0, len);

					}

					fileOutputStream.close();
				}

				zipEntry = zipInputStream.getNextEntry();
			}

			zipInputStream.closeEntry();

			zipInputStream.close();

			System.out.println("Added: " + zipFolder.getName());

		} catch (IOException ex) {

			ex.printStackTrace();

		}
	}

}