package de.ovgu.wdok.guinan;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * represents a pair of Semantic Fingerprints which are to be compared
 * @author kkrieger
 *
 */
@XmlRootElement
public class SFPPair {

	 @XmlElement public String sfp1;
	 @XmlElement public String sfp2;
}
