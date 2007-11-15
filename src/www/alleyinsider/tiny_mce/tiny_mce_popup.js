// Some global instances
var tinymce = null, tinyMCEPopup, tinyMCE;

tinyMCEPopup = {
	init : function() {
		var t = this, w = t.getWin(), ti;

		// Find API
		tinymce = w.tinymce;
		tinyMCE = w.tinyMCE;
		t.editor = tinymce.EditorManager.activeEditor;
		t.params = t.editor.windowManager.params;

		// Setup local DOM
		t.dom = t.editor.windowManager.createDOM(document, window);
		t.dom.loadCSS(t.editor.settings.popup_css);

		// Setup on init listeners
		t.listeners = [];
		t.onInit = {
			add : function(f, s) {
				t.listeners.push({func : f, scope : s});
			}
		};

		// Wait for body
		ti = window.setInterval(function() {
			if (document.body) {
				window.clearInterval(ti);
				t._onDOMLoaded();
			}
		}, 10);

		t.isWindow = !t.getWindowArg('mce_inline');
		t.id = t.getWindowArg('mce_window_id');
		t.editor.windowManager.onOpen.dispatch(t.editor.windowManager, window);
	},

	getWin : function() {
		return window.dialogArguments || opener || parent || top;
	},

	getWindowArg : function(n, dv) {
		var v = this.params[n];

		return tinymce.is(v) ? v : dv;
	},

	getParam : function(n, dv) {
		return this.editor.getParam(n, dv);
	},

	getLang : function(n, dv) {
		return this.editor.getLang(n, dv);
	},

	execCommand : function(cmd, ui, val) {
		return this.editor.execCommand(cmd, ui, val);
	},

	resizeToInnerSize : function() {
		var t = this, n, b = document.body, vp = t.dom.getViewPort(window), dw, dh;

		dw = t.getWindowArg('mce_width') - vp.w;
		dh = t.getWindowArg('mce_height') - vp.h;

		if (t.isWindow)
			window.resizeBy(dw, dh);
		else
			t.editor.windowManager.resizeBy(dw, dh, t.id);
	},

	executeOnLoad : function(s) {
		this.onInit.add(function() {
			eval(s);
		});
	},

	storeSelection : function() {
		this.editor.windowManager.bookmark = tinyMCEPopup.editor.selection.getBookmark(1);
	},

	restoreSelection : function() {
		var t = tinyMCEPopup;

		if (!t.isWindow)
			t.editor.selection.moveToBookmark(t.editor.windowManager.bookmark);
	},

	requireLangPack : function() {
		var u = this.getWindowArg('plugin_url') || this.getWindowArg('theme_url');

		if (u)
			document.write('<script type="text/javascript" src="' + u + '/langs/' + this.editor.settings.language + '_dlg.js' + '"></script>');
	},

	pickColor : function(e, element_id) {
		this.editor.execCommand('mceColorPicker', true, {
			color : document.getElementById(element_id).value,
			func : function(c) {
				document.getElementById(element_id).value = c;

				if (tinymce.is(document.getElementById(element_id).onchange, 'function'))
					document.getElementById(element_id).onchange();
			}
		});
	},

	openBrowser : function(element_id, type, option) {
		this.editor.execCallback('file_browser_callback', element_id, document.getElementById(element_id).value, type, window);
	},

	close : function() {
		var t = this;

		t.dom = t.dom.doc = null; // Cleanup
		t.editor.windowManager.close(window, t.id);
	},

	// Internal functions	

	_restoreSelection : function() {
		var e = window.event.srcElement;

		// If user focus a non text input or textarea
		if ((e.nodeName != 'INPUT' && e.nodeName != 'TEXTAREA') || e.type != 'text')
			tinyMCEPopup.restoreSelection();
	},

	_onDOMLoaded : function() {
		var t = this, ti = document.title, bm, h;

		// Translate page
		h = document.body.innerHTML;

		// Replace a=x with a="x" in IE
		if (tinymce.isIE)
			h = h.replace(/ (value|title|alt)=([^\s>]+)/gi, ' $1="$2"');

		document.body.innerHTML = t.editor.translate(h);
		document.title = ti = t.editor.translate(ti);
		document.body.style.display = '';

		// Restore selection in IE when focus is placed on a non textarea or input element of the type text
		if (tinymce.isIE)
			document.attachEvent('onbeforeactivate', tinyMCEPopup._restoreSelection);

		// Call onInit
		tinymce.each(t.listeners, function(o) {
			o.func.call(o.scope, t.editor);
		});

		t.resizeToInnerSize();

		if (t.isWindow)
			window.focus();
		else
			t.editor.windowManager.setTitle(ti, t.id);

		if (!tinymce.isIE && !t.isWindow) {
			tinymce.dom.Event._add(document, 'focus', function() {
				t.editor.windowManager.focus(t.id)
			});
		}

		}
};

tinyMCEPopup.init();
