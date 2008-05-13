package ed.appserver.templates.djang10.filters;

import java.util.Collections;
import java.util.Comparator;

import ed.js.JSArray;
import ed.js.JSObject;

public class DictSortFilter implements Filter {
	private final boolean isReversed;
	
	
	
	public DictSortFilter(boolean isReversed) {
		super();
		this.isReversed = isReversed;
	}

	public Object apply(boolean wasFound, Object value, String param) {
		JSArray sortedList = new JSArray((JSArray)value);
		Collections.sort(sortedList, new PropertyComparer(param, isReversed));
		return sortedList;
	}
	
	private static class PropertyComparer implements Comparator<JSObject> {
		private final String propName;
		private final boolean isReversed;
		
		public PropertyComparer(String propName, boolean isReversed) {
			super();
			this.propName = propName;
			this.isReversed = isReversed;
		}


		public int compare(JSObject o1, JSObject o2) {
			Object propValue1 = o1.get(propName);
			Object propValue2 = o2.get(propName);
			
			int result;
			
			if(propValue1 == null && propValue2 == null)
				result = 0;
			else if(propValue1 == null)
				result = -1;
			else if(propValue2 == null)
				result = 1;
			else if(propValue1 instanceof Comparable && propValue1.getClass().isAssignableFrom(propValue2.getClass())) 
				result = ((Comparable)propValue1).compareTo(propValue2);
			else
				result = propValue1.toString().compareTo(propValue2.toString());
			
			return result * (isReversed? -1 : 1);
		}
	}

}
