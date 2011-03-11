package cri.sanity.util;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;


public final class PrefixNum
{
	public static final String get() { return fromCountry(Locale.getDefault().getCountry()); }
	
	public static final String fromCountry(String country)
	{
		if(country == null) return "+";
		final String prefix = map.get(country.toLowerCase());
		return prefix==null ? "+" : '+'+prefix;
	}

	//public static final String fromLocale(Locale locale) { return fromCountry(locale.getCountry()); }

	private static final Map<String,String> map = new HashMap<String,String>();

	static {
		map.put("au", "61");
		map.put("be", "32");
		map.put("br", "55");
		map.put("ca", "1");
		map.put("ch", "41");
		map.put("cl", "56");
		map.put("cn", "86");
		map.put("cz", "420");
		map.put("de", "49");
		map.put("dk", "45");
		map.put("fi", "358");
		map.put("fr", "58");
		map.put("hk", "852");
		map.put("hu", "36");
		map.put("in", "91");
		map.put("ir", "353");
		map.put("it", "39");
		map.put("jp", "81");
		map.put("ko", "82");
		map.put("lk", "94");
		map.put("mx", "52");
		map.put("nl", "31");
		map.put("no", "47");
		map.put("pl", "48");
		map.put("pt", "351");
		map.put("ru", "7");
		map.put("sp", "34");
		map.put("sw", "46");
		map.put("th", "66");
		map.put("tr", "90");
		map.put("tw", "886");
		map.put("uk", "44");
		map.put("us", "1");
	}
}
