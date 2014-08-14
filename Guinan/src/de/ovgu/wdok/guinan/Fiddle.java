package de.ovgu.wdok.guinan;

import java.util.ArrayList;

public class Fiddle {
	public static void main(String args[]) {
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add("Foobar");
		tmp.add("Blue");
		tmp.add("WERRTFSFGSAGSFD");
		System.out.println("********Original String*********");
		System.out.println(tmp.toString());
		for(int i = 0; i<tmp.size(); i++){
			tmp.set(i, tmp.get(i).toLowerCase());
		}
		System.out.println("********Manipulated String*********");
		System.out.println(tmp.toString());
		
	}
}
