package de.ovgu.wdok.guinan.connector.WTPSemanticFingerprint;

import de.ovgu.wdok.guinan.GuinanResult;

public class WTPSemanticFingerprintGuinanResult extends GuinanResult {
	String keyWord;
	String relatedKeyword;
	

	public WTPSemanticFingerprintGuinanResult (String keyword, String relatedKeyword)
	{
		super();
		this.keyWord = keyword;
		this.relatedKeyword = relatedKeyword;
	}


	public String getKeyWord() {
		return keyWord;
	}


	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}


	public String getRelatedKeyword() {
		return relatedKeyword;
	}


	public void setRelatedKeyword(String relatedKeyword) {
		this.relatedKeyword = relatedKeyword;
	}
}
