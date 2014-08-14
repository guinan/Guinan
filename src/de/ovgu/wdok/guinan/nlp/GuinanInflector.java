package de.ovgu.wdok.guinan.nlp;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASubpod;

public class GuinanInflector {

	private static String appid = "V2PUTH-49E2R2VU9E";
	WAEngine engine;

	public GuinanInflector() {
		engine = new WAEngine();
		engine.setAppID(appid);
		engine.addFormat("plaintext");
	}

	public void query(String input) {
		WAQuery query = engine.createQuery();
		query.setInput("word " + input + " inflected form");
		try {
			WAQueryResult queryResult = engine.performQuery(query);

			if (queryResult.isError()) {
				System.out.println("Query error");
				System.out.println("  error code: "
						+ queryResult.getErrorCode());
				System.out.println("  error message: "
						+ queryResult.getErrorMessage());
			} else if (!queryResult.isSuccess()) {
				System.out
						.println("Query was not understood; no results available.");
			} else {
				// Got a result.
				System.out.println("Successful query. Pods follow:\n");
				for (WAPod pod : queryResult.getPods()) {
					if (!pod.isError()) {
						System.out.println(pod.getTitle());
						System.out.println("------------");
						for (WASubpod subpod : pod.getSubpods()) {
							for (Object element : subpod.getContents()) {
								if (element instanceof WAPlainText) {
									System.out.println(((WAPlainText) element)
											.getText());
									System.out.println("");
								}
							}
						}
						System.out.println("");
					}
				}
				// We ignored many other types of Wolfram|Alpha output, such as
				// warnings, assumptions, etc.
				// These can be obtained by methods of WAQueryResult or objects
				// deeper in the hierarchy.
			}
		} catch (WAException e) {
			e.printStackTrace();
		}
	}
	
}
