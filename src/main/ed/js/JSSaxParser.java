package ed.js;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ed.js.func.JSFunctionCalls2;
import ed.js.engine.Scope;

/** XML parsing functions
 * @expose
 */
public class JSSaxParser extends DefaultHandler {
	private Scope scope;
	private JSObject handler;
	private StringBuilder textBuffer = new StringBuilder();


	private JSSaxParser(Scope scope, JSObject handler) {
		this.scope = scope;
		this.handler = handler;
	}

    /** Get this parser.
     * @return This parser
     */
	public static JSFunction getParser(){
		return _realParseFunction;
	}

	private static JSFunction _realParseFunction = new JSFunctionCalls2() {
		public Object call(Scope parentScope, Object handler, Object xml, Object extra[] ){

			Scope handlerScope = parentScope.child();
			handlerScope.setThis( handler );

			JSSaxParser delegateHandler = new JSSaxParser(handlerScope, (JSObject)handler);

			try {
				XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(delegateHandler);
				reader.setErrorHandler(delegateHandler);

				InputStream s = new ByteArrayInputStream(((JSString)xml).getBytes() );
				reader.parse(new InputSource(s));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}

			return null;
		}

	};

	@Override
	public void startDocument() throws SAXException {
		Object jsHandlerObj = handler.get("startDocument");

		if(jsHandlerObj instanceof JSFunction) {
			JSFunction jsHandlerFn = (JSFunction)jsHandlerObj;

			jsHandlerFn.call(scope);
		}
	}
	@Override
	public void endDocument() throws SAXException {
		Object jsHandlerObj = handler.get("endDocument");

		if(jsHandlerObj instanceof JSFunction) {
			JSFunction jsHandlerFn = (JSFunction)jsHandlerObj;

			jsHandlerFn.call(scope);
		}
	}
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if(textBuffer.length() > 0)
			notifyText();

		Object jsHandlerObj = handler.get("startElement");

		if(! (jsHandlerObj instanceof JSFunction))
			return;

		//gather attributes
		JSArray jsAttributes = new JSArray();

		for(int i=0; i<attributes.getLength(); i++) {

			JSObjectBase jsAttr = new JSObjectBase();
			jsAttr.set("localName", attributes.getLocalName(i));
			jsAttr.set("qName", attributes.getQName(i));
			jsAttr.set("uri", attributes.getURI(i));
			jsAttr.set("value", attributes.getValue(i));

			jsAttributes.add(jsAttr);
		}

		((JSFunction)jsHandlerObj).call(scope, uri, localName, name, jsAttributes);
	}
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if(textBuffer.length() > 0)
			notifyText();

		Object jsHandlerObj = handler.get("endElement");

		if(! (jsHandlerObj instanceof JSFunction))
			return;

		((JSFunction)jsHandlerObj).call(scope, uri, localName, name);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		textBuffer.append(ch, start, length);
	}

	private void notifyText() {
		Object jsHandlerObj = handler.get("text");

		if(! (jsHandlerObj instanceof JSFunction))
			return;

		((JSFunction)jsHandlerObj).call(scope, textBuffer.toString());
		textBuffer.setLength(0);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		Object jsHandlerObj = handler.get("warning");

		if(! (jsHandlerObj instanceof JSFunction) )
			return;

		((JSFunction)jsHandlerObj).call(scope, e.getMessage(), e.getLineNumber(), e.getColumnNumber());
	}
	@Override
	public void error(SAXParseException e) throws SAXException {
		Object jsHandlerObj = handler.get("error");

		if(! (jsHandlerObj instanceof JSFunction) )
			return;

		((JSFunction)jsHandlerObj).call(scope, e.getMessage(), e.getLineNumber(), e.getColumnNumber());
	}
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		Object jsHandlerObj = handler.get("fatalError");

		if(! (jsHandlerObj instanceof JSFunction) )
			return;

		((JSFunction)jsHandlerObj).call(scope, e.getMessage(), e.getLineNumber(), e.getColumnNumber());
	}
}
