package de.ovgu.wdok.guinan.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class KeywordExtractor {

	//	final String opennlp_home = "programming/java/Guinan/WebContent/WEB-INF/lib/apache-opennlp-1.5.3/";
	// if this doesn't work set your tomcat working directory to the project root dir
	// In Eclipse: Run -> Run Configurations... -> Tomcat vX.0 at localhost -> Arguments Tab -> In the working directory section choose other and enter ${workspace_loc:Guinan}
	final String opennlp_home = "WebContent/WEB-INF/lib/apache-opennlp-1.5.3/";
	final String opennlp_models_dir = opennlp_home + "/models/";
	final String POSTagger_model = "en-pos-maxent.bin";
	final String sentdect_model_file = "en-sent.bin";
	final String tokenizer_model_file = "en-token.bin";

	private POSModel pos_model;
	private POSTaggerME pos_tagger;
	private SentenceModel sentence_model;
	private TokenizerModel tokenizer_model;
	private SentenceDetectorME sentenceDetector;
	private Tokenizer tokenizer;

	String originaltext;

	public KeywordExtractor() {
		
		InputStream pos_modelIn = null;
		InputStream sentence_modelIn = null;
		InputStream tokenizer_modelIn = null;

		try {

			// getting sentence detector ready
			System.out.print("Initializing sentence detector \t\t\t");
			sentence_modelIn = new FileInputStream(opennlp_models_dir
					+ sentdect_model_file);
			System.out.print(".");
			this.sentence_model = new SentenceModel(sentence_modelIn);
			System.out.print(".");
			this.sentenceDetector = new SentenceDetectorME(sentence_model);
			System.out.println(". OK");
		} catch (IOException e) {
			// Model loading failed, handle the error
			System.out.println("... fail");
			System.err
					.println("Could not get the sentence detector ready. Exiting ...");
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (sentence_modelIn != null) {
				try {
					sentence_modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		try {

			// getting tokenizer ready
			System.out.print("Initializing tokenizer \t\t\t\t");
			tokenizer_modelIn = new FileInputStream(opennlp_models_dir
					+ tokenizer_model_file);
			System.out.print(".");
			this.tokenizer_model = new TokenizerModel(tokenizer_modelIn);
			System.out.print(".");
			this.tokenizer = new TokenizerME(tokenizer_model);
			System.out.println(". OK");
		} catch (IOException e) {
			// Model loading failed, handle the error
			System.out.println("... fail");
			System.err
					.println("Could not get the tokenizer ready. Exiting ...");
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (tokenizer_modelIn != null) {
				try {
					tokenizer_modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		try {

			// getting POS tagger ready
			System.out.print("Initializing POS Tagger \t\t\t");
			pos_modelIn = new FileInputStream(opennlp_models_dir
					+ POSTagger_model);
			System.out.print(".");
			this.pos_model = new POSModel(pos_modelIn);
			System.out.print(".");
			this.pos_tagger = new POSTaggerME(pos_model);
			System.out.println(". OK");

		} catch (IOException e) {
			// Model loading failed, handle the error
			System.out.println("... fail");
			System.err
					.println("Could not get POS tagger tools ready. Exiting ...");
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (pos_modelIn != null) {
				try {
					pos_modelIn.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public ArrayList<String> extractKeywords(String text) {
		ArrayList<String> keywords = new ArrayList<String>();

		SortedMap<String, Integer> tmp = new TreeMap<String, Integer>();
		ArrayList<String> simple_keywords = new ArrayList<String>(); // stores
																		// keywords
																		// without
																		// weight
		// read file content into string

		// clean text
		System.out
				.print("Removing non-alphanumeric characters from input\t\t...");
		text = removeNonAlphanumChars(text);
		System.out.println(" OK");
		// System.out.println(content);
		String sentences[] = this.sentenceDetector.sentDetect(text);

		for (int i = 0; i < sentences.length; i++) {
			// tokenize sentences
			String tokens[] = this.tokenizer.tokenize(sentences[i]);
			// POS tag tokens
			String tags[] = this.pos_tagger.tag(tokens);
			for (int j = 0; j < tags.length; j++) {
				if (tags[j].startsWith("N") && tokens[j].length() > 2) {
					if (tmp.containsKey(tokens[j])) {// wert auslesen, aus liste
														// löschen, neu
														// schreiben
						int weight = tmp.get(tokens[j]);
						tmp.remove(tokens[j]);
						tmp.put(tokens[j], weight + 1);
						// simple_keywords.add(tokens[j]);
					} else
						tmp.put(tokens[j], new Integer(1));
					simple_keywords.add(tokens[j]);
				}
			}
		}
		keywords.addAll(tmp.keySet());

		return keywords;
	}

	/** convert ArrayList to HashSet, thus eliminating duplicate entries **/
	public HashSet<String> getSetofKeywords(ArrayList<String> tagsfromcomments) {
		HashSet<String> setofkeywords = new HashSet<String>();

		setofkeywords.addAll(tagsfromcomments);

		return setofkeywords;
	}

	/** compute a common tagset **/
	public HashSet<String> getAggregatedTags(
			ArrayList<String> tags_from_resource,
			ArrayList<String> additional_tags_from_resource) {
		
		HashSet<String> common_tags = new HashSet<String>();
		
		return common_tags;

	}

	/**
	 * Helper method to eliminate non alphanumerical characters from the source
	 * string
	 * 
	 * @param s
	 *            source string
	 * @return cleaned string containing only alphanumerical characters
	 */
	private static String removeNonAlphanumChars(String a) {
		return a.replaceAll("[^a-zA-Z0-9äöüß\\s]", " ");
	}
}
