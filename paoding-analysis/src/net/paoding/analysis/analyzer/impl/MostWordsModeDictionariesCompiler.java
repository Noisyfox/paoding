package net.paoding.analysis.analyzer.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Properties;

import net.paoding.analysis.Constants;
import net.paoding.analysis.dictionary.Dictionary;
import net.paoding.analysis.dictionary.Hit;
import net.paoding.analysis.knife.Beef;
import net.paoding.analysis.knife.Collector;
import net.paoding.analysis.knife.Dictionaries;
import net.paoding.analysis.knife.DictionariesCompiler;
import net.paoding.analysis.knife.Knife;

public class MostWordsModeDictionariesCompiler implements DictionariesCompiler {
	public static final String VERSION = "1";
	
	public boolean shouldCompile(Properties p) throws Exception {
		String lastModifieds = p.getProperty("paoding.analysis.properties.lastModifieds");
		String files = p.getProperty("paoding.analysis.properties.files");
		String dicHome = p.getProperty("paoding.dic.home.absolute.path");
		File dicHomeFile = new File(dicHome);
		File compliedMetadataFile = new File(dicHomeFile, ".compiled/most-words-mode/.metadata");
		if (compliedMetadataFile.exists() && compliedMetadataFile.isFile()) {
			Properties compiledProperties = new Properties();
			InputStream compiledPropertiesInput = new FileInputStream(compliedMetadataFile);
			compiledProperties.load(compiledPropertiesInput);
			compiledPropertiesInput.close();
			String compiledLastModifieds = compiledProperties.getProperty("paoding.analysis.properties.lastModifieds");
			String compiledFiles = compiledProperties.getProperty("paoding.analysis.properties.files");
			String clazz = compiledProperties.getProperty("paoding.analysis.compiler.class");
			String version = compiledProperties.getProperty("paoding.analysis.compiler.version");
			if (lastModifieds.equals(compiledLastModifieds) && files.equals(compiledFiles)
					&& this.getClass().getName().equalsIgnoreCase(clazz)
					&& VERSION.equalsIgnoreCase(version)) {
				return false;
			}
		}
		return true;
	}
	
	public void compile(Dictionaries dictionaries, Knife knife, Properties p) throws Exception {
		String dicHome = p.getProperty("paoding.dic.home.absolute.path");
		String noiseCharactor = getProperty(p, Constants.DIC_NOISE_CHARACTOR);
		String noiseWord = getProperty(p, Constants.DIC_NOISE_WORD);
		String unit = getProperty(p, Constants.DIC_UNIT);
		String confucianFamilyName = getProperty(p, Constants.DIC_CONFUCIAN_FAMILY_NAME);
		String combinatorics = getProperty(p, Constants.DIC_FOR_COMBINATORICS);
		String charsetName = getProperty(p, Constants.DIC_CHARSET);
		
		File dicHomeFile = new File(dicHome);
		File compiledDicHomeFile = new File(dicHomeFile, ".compiled/most-words-mode");
		compiledDicHomeFile.mkdirs();
		//
		Dictionary vocabularyDictionary = dictionaries.getVocabularyDictionary();
		File vocabularyFile = new File(compiledDicHomeFile, "vocabulary.dic.compiled");
		compileVocabulary(vocabularyDictionary, knife, vocabularyFile, charsetName);

		//
		Dictionary noiseCharactorsDictionary = dictionaries.getNoiseCharactorsDictionary();
		File noiseCharactorsDictionaryFile = new File(compiledDicHomeFile, noiseCharactor + ".dic.compiled");
		sortCompile(noiseCharactorsDictionary, noiseCharactorsDictionaryFile, charsetName);
		//
		Dictionary noiseWordsDictionary = dictionaries.getNoiseWordsDictionary();
		File noiseWordsDictionaryFile = new File(compiledDicHomeFile, noiseWord + ".dic.compiled");
		sortCompile(noiseWordsDictionary, noiseWordsDictionaryFile, charsetName);
		//
		Dictionary unitsDictionary = dictionaries.getUnitsDictionary();
		File unitsDictionaryFile = new File(compiledDicHomeFile, unit + ".dic.compiled");
		sortCompile(unitsDictionary, unitsDictionaryFile, charsetName);
		//
		Dictionary confucianFamilyDictionary = dictionaries.getConfucianFamilyNamesDictionary();
		File confucianFamilyDictionaryFile = new File(compiledDicHomeFile, confucianFamilyName + ".dic.compiled");
		sortCompile(confucianFamilyDictionary, confucianFamilyDictionaryFile, charsetName);
		//
		Dictionary combinatoricsDictionary = dictionaries.getCombinatoricsDictionary();
		File combinatoricsDictionaryFile = new File(compiledDicHomeFile, combinatorics + ".dic.compiled");
		sortCompile(combinatoricsDictionary, combinatoricsDictionaryFile, charsetName);
		
		//
		File compliedMetadataFile = new File(dicHomeFile, ".compiled/most-words-mode/.metadata");
		if (compliedMetadataFile.exists()) {
			compliedMetadataFile.setWritable(true);
			compliedMetadataFile.delete();
		}
		else {
			compliedMetadataFile.getParentFile().mkdirs();
		}
		OutputStream compiledPropertiesOutput = new FileOutputStream(compliedMetadataFile);
		p.setProperty("paoding.analysis.compiler.class", this.getClass().getName());
		p.setProperty("paoding.analysis.compiler.version", VERSION);
		p.store(compiledPropertiesOutput, "dont edit it! this file was auto generated by paoding.");
		compiledPropertiesOutput.close();
		compliedMetadataFile.setReadOnly();
	}


	public Dictionaries readCompliedDictionaries(Properties p) {
		String dicHomeAbsolutePath = p.getProperty("paoding.dic.home.absolute.path");
		String noiseCharactor = getProperty(p, Constants.DIC_NOISE_CHARACTOR);
		String noiseWord = getProperty(p, Constants.DIC_NOISE_WORD);
		String unit = getProperty(p, Constants.DIC_UNIT);
		String confucianFamilyName = getProperty(p, Constants.DIC_CONFUCIAN_FAMILY_NAME);
		String combinatorics = getProperty(p, Constants.DIC_FOR_COMBINATORICS);
		String charsetName = getProperty(p, Constants.DIC_CHARSET);
		return new CompiledFileDictionaries(
				dicHomeAbsolutePath + "/.compiled/most-words-mode",
				noiseCharactor, noiseWord, unit,
				confucianFamilyName, combinatorics, charsetName);
	}
	
	private static String getProperty(Properties p, String name) {
		return Constants.getProperty(p, name);
	}
	

	private void sortCompile(final Dictionary dictionary, 
			File dicFile, String charsetName) throws FileNotFoundException,
			IOException, UnsupportedEncodingException {
		int wordsSize = dictionary.size();
		if (dicFile.exists()) {
			dicFile.setWritable(true);
			dicFile.delete();
		}
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(dicFile), 1024 * 16);
		
		for (int i = 0; i < wordsSize; i++) {
			out.write(dictionary.get(i).getBytes(charsetName));
			out.write('\r');
			out.write('\n');
		}
		out.flush();
		out.close();
		dicFile.setReadOnly();
	}
	
	private void compileVocabulary(final Dictionary vocabularyDictionary, Knife knife,
			File vocabularyFile, String charsetName) throws FileNotFoundException,
			IOException, UnsupportedEncodingException {
		int vocabularySize = vocabularyDictionary.size();
		String[] vocabularyWords = new String[vocabularySize];
		char[] chs = new char[128];
		for (int i = 0; i < vocabularySize; i ++) {
			final String curWord = vocabularyDictionary.get(i);
			curWord.getChars(0, curWord.length(), chs, 0);
			chs[curWord.length()] = (char) -1;
			Beef beef = new Beef(chs, 0, curWord.length() + 1);
			final BitSet bs = new BitSet(curWord.length());
			knife.dissect(new Collector(){
				public void collect(String word, int offset, int end) {
					Hit hit = vocabularyDictionary.search(word, 0, word.length());
					if (hit.isHit() && hit.getWord().length() != curWord.length()) {
						for (int j = offset; j < end; j++) {
							bs.set(j, true);
						}
					}
				}
				
			}, beef, 0);
			
			for (int j = 0; j < curWord.length();j++) {
				if (!bs.get(j)) {
					vocabularyWords[i] = curWord;
					break;
				}
			}
		}
		if (vocabularyFile.exists()) {
			vocabularyFile.setWritable(true);
			vocabularyFile.delete();
		}
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(vocabularyFile), 1024 * 16);
		
		for (int i = 0; i < vocabularySize; i++) {
			if (vocabularyWords[i] != null) {
				out.write(vocabularyWords[i].getBytes(charsetName));
				out.write('\r');
				out.write('\n');
			}
		}
		out.flush();
		out.close();
		vocabularyFile.setReadOnly();
	}
}
