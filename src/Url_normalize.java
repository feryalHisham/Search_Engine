import java.net.MalformedURLException;
import java.net.URL;

public class Url_normalize {

	public Url_normalize() {
		// TODO Auto-generated constructor stub
	}
	
    
	public static String url_normalization(String url)
	{
		URL url_kamel = null;
		try {
			url_kamel = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String base_to_be_replaced = url_kamel.getProtocol() + "://" + url_kamel.getHost();
		String base_sa7 = "http" + "://" + url_kamel.getHost();
		base_sa7=base_sa7.toLowerCase();
		url=url.replaceAll(base_to_be_replaced, base_sa7);
		
		replace_encoded_chars(url);
		
		//Removing the default port
		url=url.replaceAll(".com:80", ".com");
		if(url.charAt(url.length()-1)!='/')
		{
			url=url.concat("/");
		}

		return url;
	}

	public static String replace_encoded_chars(String url)
	{
		url=url.replaceAll("%21","!");
		url=url.replaceAll("%23","#");
		url=url.replaceAll("%24","$");
		url=url.replaceAll("%26","&");
		url=url.replaceAll("%27","'");
		url=url.replaceAll("%28","(");
		url=url.replaceAll("%29",")");
		url=url.replaceAll("%2A","*");
		url=url.replaceAll("%2a","*");
		url=url.replaceAll("%2B","+");
		url=url.replaceAll("%2b","+");
		url=url.replaceAll("%2C",",");
		url=url.replaceAll("%2c",",");
		url=url.replaceAll("%2F","/");
		url=url.replaceAll("%2f","/");
		url=url.replaceAll("%3A",":");
		url=url.replaceAll("%3a",":");
		url=url.replaceAll("%3B",";");
		url=url.replaceAll("%3b",";");
		url=url.replaceAll("%3D","=");
		url=url.replaceAll("%3d","=");
		url=url.replaceAll("%3F","?");
		url=url.replaceAll("%3f","?");
		url=url.replaceAll("%40","@");
		url=url.replaceAll("%5B","[");
		url=url.replaceAll("%5b","[");
		url=url.replaceAll("%5D","]");
		url=url.replaceAll("%5d","]");
		url= url.replaceAll("%20","").replaceAll("%25","%").replaceAll("%2D","-").replaceAll("%2d","-").replaceAll("%2E",".").replaceAll("%2e",".");
		url = url.replaceAll("%3C","<").replaceAll("%3c","<").replaceAll("%3E",">").replaceAll("%3e",">").replaceAll("%5C","\\\\");
		url= url.replaceAll("%5c","\\\\").replaceAll("%5E","^").replaceAll("%5e","^").replaceAll("%5F","").replaceAll("%5f","").replaceAll("%60","`");
		url= url.replaceAll("%7B","{").replaceAll("%7b","{").replaceAll("%7C","|").replaceAll("%7c","|").replaceAll("%7D","}").replaceAll("%7d","}").replaceAll("%7E","~").replaceAll("%7e","~");
		
		return url;
	}
}
