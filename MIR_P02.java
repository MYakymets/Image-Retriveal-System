package net.semanticmetadata;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("unused")
public class MIR_P02 {
	public static void main(String[] args) throws IOException {
		// Delete all previous indexes
		deleteAllFilesFolder(args[1]);
		// Checking if arg[0] is there and if it is a directory.
		boolean passed = false;
		if (args.length > 0) {
			File f = new File(args[0]);
			System.out.println("Indexing images in " + args[0]);
			if (f.exists() && f.isDirectory())
				passed = true;
		} // if
		if (!passed) {
			System.out.println("No directory given as first argument.");
			System.exit(1);
		} // if

		// Checking if arg[1] is there and if it is a index path
		passed = false;
		if (args.length > 0) {
			File f = new File(args[1]);
			if (f.exists() && f.isDirectory())
				passed = true;
		} // if
		if (!passed) {
			System.out.println("No index directory given as second argument.");
			System.exit(1);
		} // if

		// Getting all images from a directory and its sub directories.
		ArrayList<String> images = FileUtils.getAllImages(new File(args[0]), true);

		// Creating a CEDD document builder and indexing all files.
		DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
		// Creating an Lucene IndexWriter
	//	IndexWriterConfig conf = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
		//		new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
	//	IndexWriter iw = new IndexWriter(FSDirectory.open(new File(args[1])), conf);
		 IndexWriter iw = LuceneUtils.createIndexWriter(args[1], true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
		// Iterating through images building the low level features
		for (Iterator<String> it = images.iterator(); it.hasNext();) {
			String imageFilePath = it.next();
			try {
				BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
				Document document = builder.createDocument(img, imageFilePath);
				iw.addDocument(document);
			} catch (Exception e) {
				System.err.println("Error reading image or indexing it.");
				e.printStackTrace();
			}
		} // for
			// closing the IndexWriter
		iw.close();
		System.out.println("Finished indexing.");

		// search
		// Checking if arg[2] is there and if it is an image.
		BufferedImage img = null;
		passed = false;
		if (args.length > 0) {
			File f = new File(args[2]);
			if (f.exists()) {
				try {
					img = ImageIO.read(f);
					passed = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} // if
		} // if
		if (!passed) {
			System.out.println("No image given as third argument.");
			System.exit(1);
		} // if

		IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(args[1])));
		ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(10);

		ImageSearchHits hits = searcher.search(img, ir);
		System.out.println();
		System.out.format("%-20s%s\n", "Distance value:", "Path:");

		for (int i = 0; i < hits.length(); i++) {
			String fileName = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
			System.out.format("%-20s%s\n", hits.score(i) + ":", fileName);
		} // for i

	}// main

	public static void deleteAllFilesFolder(String path) {
		for (File myFile : new File(path).listFiles())
			if (myFile.isFile())
				myFile.delete();
	}// deleteAllFilesolder
}// MIR_P02
