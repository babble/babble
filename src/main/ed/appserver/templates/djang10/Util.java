package ed.appserver.templates.djang10;

public class Util {
	public static int countOccurance(String str, char character) { 
		int count = 0;
		
		for(int pos = str.indexOf(character, 0); pos > 0; pos = str.indexOf(character, pos + 1))
			count++;
		
		return count;
	}
}
