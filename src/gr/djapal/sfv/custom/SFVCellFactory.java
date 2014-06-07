/*
 *  Table cell factory
 *  Copyright (c) 2009 Apostolos Alexiadis <djapal@gmail.com>
 *                All Rights Reserved
 *
 *  This program is free software. It comes without any warranty, to
 *  the extent permitted by applicable law. You can redistribute it
 *  and/or modify it under the terms of the Do What the Fuck You Want
 *  to Public License, Version 2, as published by Sam Hocevar. See
 *  http://www.wtfpl.net/ for more details.
 */
package gr.djapal.sfv.custom;

import gr.djapal.sfv.bean.SFVInfo;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.util.Callback;

public class SFVCellFactory implements Callback<TableColumn, TableCell> {

	@Override
	public TableCell<SFVInfo, Object> call(TableColumn p) {

   TableCell<SFVInfo, Object> cell = new TableCell<SFVInfo, Object>() {

        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : getString());
            setGraphic(null);
            TableRow currentRow = getTableRow();
            SFVInfo currentSFVInfo = currentRow == null ? null : (SFVInfo)currentRow.getItem();

            if (currentSFVInfo != null) {   
                String missingFiles = currentSFVInfo.getMissing();
                clearPriorityStyle();
                if (!isHover() && !isSelected() && !isFocused()) {
                    setRowCSS(Integer.parseInt(currentSFVInfo.getMissing()) + Integer.parseInt(currentSFVInfo.getBad()));
                }
            }
        }

        @Override
        public void updateSelected(boolean upd) {
            super.updateSelected(upd);
            System.out.println("is update");
        }

        private void clearPriorityStyle() {
            ObservableList<String> styleClasses = getStyleClass();
            styleClasses.remove("priorityLow");
            styleClasses.remove("priorityMedium");
            styleClasses.remove("priorityHigh");
        }

        private void setRowCSS(int erroneousFiles) {
        	if (erroneousFiles > 0) {
        		getStyleClass().add("sfvProblem");
        	} else {
        		getStyleClass().add("sfvOK");
        	}
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    };


    return cell;
} }