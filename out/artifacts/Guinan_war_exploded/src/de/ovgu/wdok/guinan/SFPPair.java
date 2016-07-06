package de.ovgu.wdok.guinan;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * represents a pair of Semantic Fingerprints which are to be compared, the pair will be send as POST payload
 * @author kkrieger
 *
 */
@XmlRootElement
public class SFPPair {

	 @XmlElement public String sfp1;
	 @XmlElement public String sfp2;
	 
	public String getSfp1() {
		return sfp1;
	}
	public void setSfp1(String sfp1) {
		this.sfp1 = sfp1;
	}
	public String getSfp2() {
		return sfp2;
	}
	public void setSfp2(String sfp2) {
		this.sfp2 = sfp2;
	}
	 
	 
}
