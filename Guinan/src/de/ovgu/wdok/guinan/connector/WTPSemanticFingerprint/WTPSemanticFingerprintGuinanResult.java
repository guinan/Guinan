package de.ovgu.wdok.guinan.connector.WTPSemanticFingerprint;

import java.util.LinkedList;

import de.ovgu.wdok.guinan.GuinanResult;

public class WTPSemanticFingerprintGuinanResult extends GuinanResult {
	LinkedList<String>keyWord;
	LinkedList<LinkedList<String>> relatedKeyword;
	

	public WTPSemanticFingerprintGuinanResult (LinkedList<String> keyword, LinkedList<LinkedList<String>> relatedKeyword)
	{
		super();
		this.keyWord = keyword;
		this.relatedKeyword = relatedKeyword;
	}


	public LinkedList<String> getKeyWord() {
		return keyWord;
	}

	
	public void setKeyWord(LinkedList<String> keyWord) {
		this.keyWord = keyWord;
	}


	public LinkedList<LinkedList<String>> getRelatedKeyword() {
		return relatedKeyword;
	}


	public void setRelatedKeyword(LinkedList<LinkedList<String>> relatedKeyword) {
		this.relatedKeyword = relatedKeyword;
	}
	
	
	public void addRelatedWords(int i, LinkedList<String> relatedWords){
		relatedKeyword.add(relatedWords);
	}
}
