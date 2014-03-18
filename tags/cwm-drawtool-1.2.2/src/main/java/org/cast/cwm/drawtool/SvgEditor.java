/**
 * Copyright 2011-2013 CAST, Inc.
 *
 * This file is part of the CAST extension of SVG Edit;
 * see http://code.google.com/p/cwm-drawtool for more information.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.cast.cwm.drawtool;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.cast.cwm.drawtool.extension.Extension;

/**
 * Use {@link #getEditor(String, String)} to generate an InlineFrame
 * that you can use to edit a string of SVG.  
 * 
 * @author jbrookover
 *
 */

public class SvgEditor extends WebPage implements IHeaderContributor {
	
	/*
	 * These values are set in the javascript.  Perhaps in the future, 
	 * they will be flexible.  For now, they are not.
	 */
	public static final int EDITOR_WIDTH = 550;
	public static final int EDITOR_HEIGHT = 400;
	public static final int CANVAS_WIDTH = 535;
	public static final int CANVAS_HEIGHT = 325;
	
	public static final String EXTENSION_MARKUP_ID = "extensionMarkup";
	
	private IModel<String> mSvg;
	private List<String> drawingStarters;
	
	private List<Extension> extensions = new ArrayList<Extension>();
	
	public SvgEditor() {
		super(PageMap.forName("SvgFrame"));
		
		// Drawing Starters
		add(new Label("drawingStarters", new AbstractReadOnlyModel<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				
				StringBuffer starters = new StringBuffer("starterImageUrls = [");
				if (drawingStarters != null) {
					for (String url : drawingStarters) {
						starters.append("\"" + url + "\",\n");
					}
				}
				starters.append("];");
				return starters.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		// Server Image Path
		// TODO: Don't make this hard-coded
		add(new Label("serverImagePath", "\nserverImagePath = 'resources/org.cast.cwm.drawtool.SvgEditor/';\n").setEscapeModelStrings(false));
		
		add(new ListView<Extension>("extensionList", new PropertyModel<List<Extension>>(this, "extensions")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Extension> item) {
				if (item.getModelObject() instanceof WebMarkupContainerWithAssociatedMarkup)
					item.add((WebMarkupContainerWithAssociatedMarkup) item.getModelObject());
				else
					item.setVisible(false);
				item.setRenderBodyOnly(true);
			}
		});
	}
	
	
	public void renderHead(IHeaderResponse response) {
		
		StringBuffer canvasInit = new StringBuffer();
		
		// Load initial SVG data
		if (mSvg != null && mSvg.getObject() != null)
			canvasInit.append("svgEditor.loadFromString('" + mSvg.getObject().replace('\n', ' ') + "');\n");
		
		response.renderJavascript(canvasInit, "Existing SVG Load");

		// Store canvas in "document" so it is accessible from parent page via W3C standards		
		response.renderOnLoadJavascript("document.svgCanvas = window.svgCanvas;"); 
		for (Extension ext : extensions)
			response.renderJavascriptReference(ext.getJavascriptResource());
	}
	
	/**
	 * Returns an {@link InlineFrame} that displays this SVG Editor.  This is the
	 * preferred component to add to a page.
	 * 
	 * @param id wicket:id
	 * @return
	 */
	public InlineFrame getEditor(String id) {
		InlineFrame frame = new InlineFrame(id, this);
		frame.add(new SimpleAttributeModifier("style", "background:#FFFFFF;border:2px solid #AAAAAA"));
		frame.add(new SimpleAttributeModifier("width", String.valueOf(EDITOR_WIDTH)));
		frame.add(new SimpleAttributeModifier("height", String.valueOf(EDITOR_HEIGHT)));
		frame.add(new SimpleAttributeModifier("scrolling", "no"));
		frame.setOutputMarkupId(true);
		return frame;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public List<String> getDrawingStarters() {
		return drawingStarters;
	}

	public void setDrawingStarters(List<String> drawingStarters) {
		this.drawingStarters = drawingStarters;
	}

	public IModel<String> getMSvg() {
		return mSvg;
	}

	public void setMSvg(IModel<String> mSvg) {
		this.mSvg = mSvg;
	}
	
	@Deprecated
	// TODO remove after next stable release
	public String getSvg() {
		return mSvg.getObject();
	}

	@Deprecated
	// TODO remove after next stable release
	public void setSvg(String svg) {
		this.mSvg = Model.of(svg);
	}
	
	public void addExtension(Extension ext) {
		extensions.add(ext);
	}
	
	public List<Extension> getExtensions() {
		return extensions;
	}

	@Override
	protected void onDetach() {
		if (mSvg != null)
			mSvg.detach();
		super.onDetach();
	}
}