/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.quikj.ace.web.client.presenter.EmoticonPresenter;
import com.quikj.ace.web.client.view.EmoticonPallette;
import com.quikj.ace.web.client.view.EmoticonUtils;
import com.quikj.ace.web.client.view.EmoticonUtils.EmoticonInfo;

/**
 * @author amit
 * 
 */
public class DesktopEmoticonPallette extends PopupPanel implements
		EmoticonPallette {

	// TODO we are not accounting whether the emoticon in the emoticons list
	// have a display attribute set to false. The display attribute = false
	// indicates that the emoticon should not be displayed (but it can be
	// typed in).

	private static final int NUM_COLUMNS = 10;
	private Grid grid;
	private EmoticonPresenter presenter;

	public DesktopEmoticonPallette(EmoticonPresenter presenter) {
		this.presenter = presenter;
		setAutoHideEnabled(true);
		setSize("100%", "100%");

		int size = EmoticonUtils.getEmoticons().size();
		int numRows = (size / NUM_COLUMNS);
		if (size % NUM_COLUMNS > 0) {
			numRows++;
		}

		grid = new Grid(numRows, NUM_COLUMNS);
		add(grid);

		int row = 0;
		int col = 0;
		for (int i = 0; i < size; i++) {
			EmoticonInfo emoticon = EmoticonUtils.getEmoticons().get(i);
			grid.setWidget(row, col++, new Image(emoticon.url));
			if (col >= NUM_COLUMNS) {
				col = 0;
				row++;
			}
		}

		grid.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Cell cell = grid.getCellForEvent(event);
				if (cell != null) {
					int element = (cell.getRowIndex() * NUM_COLUMNS)
							+ cell.getCellIndex();
					DesktopEmoticonPallette.this.presenter
							.emoticonSelected(EmoticonUtils.getEmoticons().get(
									element).url);
					hide();
				}
			}
		});
	}
}
