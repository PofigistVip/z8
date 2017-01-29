package org.zenframework.z8.server.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.zenframework.z8.server.base.file.Folders;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;

public class PdfUtils {

	private static List<String> TIFF_EXTENSIONS = Arrays.asList("tif", "tiff");
	private static List<Integer> TIFF_MAGIC_NUMBERS = Arrays.asList(0x49492A00, 0x4D4D002A);

	public static int getPageCount(File file) throws IOException {
		return getPageCount(file.getCanonicalPath());
	}

	public static int getPageCount(String file) throws IOException {
		PdfReader reader = new PdfReader(new RandomAccessFileOrArray(file, false, true), new byte[0]);
		try {
			return reader.getNumberOfPages();
		} finally {
			reader.close();
		}
	}

	public static void textToPdf(File sourceFile, File convertedFile) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFile);
			textToPdf(IOUtils.readText(in, Charset.forName(IOUtils.determineEncoding(sourceFile, "UTF-8"))), convertedFile);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static void textToPdf(String text, File file) throws IOException {
		// If text is empty, itext generates incorrect pdf
		if (text == null || text.isEmpty())
			text = " ";
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, new FileOutputStream(file));
			document.open();
			document.add(new Paragraph(text, getFont()));
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	public static void imageToPdf(File sourceFile, File convertedFile) throws IOException {
		Document document = new Document();
		try {
			PdfWriter.getInstance(document, new FileOutputStream(convertedFile));
			document.open();
			if (isTiff(sourceFile)) {
				RandomAccessFileOrArray raf = new RandomAccessFileOrArray(sourceFile.getCanonicalPath());
				int numberOfPages = TiffImage.getNumberOfPages(raf);
				for (int i = 1; i <= numberOfPages; i++) {
					addImage(document, TiffImage.getTiffImage(raf, i));
				}
			} else {
				addImage(document, Image.getInstance(sourceFile.getCanonicalPath()));
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	private static void addImage(Document document, Image image) throws DocumentException {
		int indentation = 0;
		float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - indentation) / image
				.getWidth()) * 100;
		image.scalePercent(scaler);
		document.add(image);
	}

	public static boolean isTiff(File file) throws IOException {
		if (TIFF_EXTENSIONS.contains(FilenameUtils.getExtension(file.getName()).toLowerCase()))
			return true;
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		try {
			return TIFF_MAGIC_NUMBERS.contains(in.readInt());
		} finally {
			in.close();
		}
	}

	public static void merge(List<File> sourceFiles, File mergedFile, String comment) throws IOException {
		Document document = new Document();
		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mergedFile));
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			for (File file : sourceFiles) {
				InputStream in = new FileInputStream(file);
				try {
					PdfReader reader = new PdfReader(in);
					for (int i = 1; i <= reader.getNumberOfPages(); i++) {
						document.newPage();
						cb.addTemplate(writer.getImportedPage(reader, i), 0, 0);
					}
				} finally {
					in.close();
				}
			}
			if (comment != null && !comment.isEmpty()) {
				document.newPage();
				document.add(new Paragraph(comment, getFont()));
			}
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			document.close();
		}
	}

	private static Font getFont() throws DocumentException, IOException {
		return new Font(BaseFont.createFont(new File(Folders.Base, "fonts/times.ttf").getCanonicalPath(),
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 14);
	}
}
