package gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Utils {

	public static ArrayList<String> getStatus() {
		String lang = Locale.getDefault().getLanguage();
		switch (lang) {
			case "es":
				return new ArrayList<String>(Arrays.asList("Nuevo", "Muy Bueno", "Aceptable", "Lo ha dado todo"));
			case "eu":
			case "eus":
				return new ArrayList<String>(Arrays.asList("Berria", "Oso Ona", "Egokia", "Oso zaharra"));
			default: // "en" y cualquier otro idioma
				return new ArrayList<String>(Arrays.asList("New", "Very Good", "Acceptable", "Very Used"));
		}
	}

	public static String getStatus(int t) {
		return getStatus().get(t);
	}
}
