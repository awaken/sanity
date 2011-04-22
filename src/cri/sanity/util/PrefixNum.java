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
		if(prefixMap == null) buildPrefixMap();
		final String prefix = prefixMap.get(country.toLowerCase());
		return prefix==null ? "+" : '+'+prefix;
	}

	//public static final String fromLocale(Locale locale) { return fromCountry(locale.getCountry()); }

	private static Map<String,String> prefixMap;

	private static void buildPrefixMap()
	{
		prefixMap = new HashMap<String,String>();
		prefixMap.put("au", "61");
		prefixMap.put("be", "32");
		prefixMap.put("br", "55");
		prefixMap.put("ca", "1");
		prefixMap.put("ch", "41");
		prefixMap.put("cl", "56");
		prefixMap.put("cn", "86");
		prefixMap.put("cz", "420");
		prefixMap.put("de", "49");
		prefixMap.put("dk", "45");
		prefixMap.put("fi", "358");
		prefixMap.put("fr", "58");
		prefixMap.put("hk", "852");
		prefixMap.put("hu", "36");
		prefixMap.put("in", "91");
		prefixMap.put("ir", "353");
		prefixMap.put("it", "39");
		prefixMap.put("jp", "81");
		prefixMap.put("ko", "82");
		prefixMap.put("lk", "94");
		prefixMap.put("mx", "52");
		prefixMap.put("nl", "31");
		prefixMap.put("no", "47");
		prefixMap.put("pl", "48");
		prefixMap.put("pt", "351");
		prefixMap.put("ru", "7");
		prefixMap.put("sp", "34");
		prefixMap.put("sw", "46");
		prefixMap.put("th", "66");
		prefixMap.put("tr", "90");
		prefixMap.put("tw", "886");
		prefixMap.put("uk", "44");
		prefixMap.put("us", "1");
	}
}
