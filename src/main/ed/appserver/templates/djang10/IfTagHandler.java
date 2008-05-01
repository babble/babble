/**
 * 
 */
package ed.appserver.templates.djang10;

import ed.appserver.templates.Djang10Converter.MyGenerator;

public class IfTagHandler extends TagHandler {
	
	public void Compile(MyGenerator g, String name, String... params) {
		if(params.length == 0)
			throw new RuntimeException();
		
		g.append("if(");
		
		String paramStr = join(params, " ");
		String[] args = paramStr.split(" and ");
		boolean isAnd = true;
		
		if(args.length == 1) {
			args = paramStr.split(" or ");
			isAnd = false;
		}
		
		boolean isFirst  = true;
		for (String arg : args) {
			arg = arg.trim();

			if(!isFirst)
				g.append(isAnd? " && " : " || ");
			isFirst = false;

			if(arg.contains(" ")) {
				String[] argParts = arg.split(" ");
				if(!"not".equals(argParts[0]) )
					throw new RuntimeException();
				
				g.append("!");
			}
			
			g.appendVarExpansion(arg, "\"\"");
		}
		g.append(") {\n");
	}
	
	static String join(String[] arr, String sep) {
		if(arr.length == 0)
			return "";
		
		StringBuilder buff = new StringBuilder();
		buff.append(arr[0]);
		for(int i=1;i<arr.length;i++) {
			buff.append(sep);
			buff.append(arr[0]);
		}
		return buff.toString();
	}
}