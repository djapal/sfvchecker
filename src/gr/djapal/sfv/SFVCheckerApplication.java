/*
 *  Main SFV Checker window
 *  Copyright (c) 2014 Apostolos Alexiadis <djapal@gmail.com>
 *                All Rights Reserved
 *
 *  This program is free software. It comes without any warranty, to
 *  the extent permitted by applicable law. You can redistribute it
 *  and/or modify it under the terms of the Do What the Fuck You Want
 *  to Public License, Version 2, as published by Sam Hocevar. See
 *  http://www.wtfpl.net/ for more details.
 */
package gr.djapal.sfv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import gr.djapal.sfv.Finder;
import gr.djapal.sfv.bean.SFVInfo;
import gr.djapal.sfv.custom.SFVCellFactory;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Main JavaFX Window for SFV file checking
 * 
 * @author djapal
 *
 */
public class SFVCheckerApplication extends Application {

	private final ObservableList<SFVInfo> data = FXCollections.observableArrayList();

	private TableView<SFVInfo> table 	= new TableView<SFVInfo>();
	private ProgressBar pb 				= new ProgressBar();
	final VBox foldersVB 				= new VBox();
	final TextField sourceFolder 		= new TextField();
	final TextField destinationFolder 	= new TextField();
	final TextField incompleteFolder	= new TextField();
	Button sourceFolderButton 			= new Button();
	Button destinationFolderButton 		= new Button();
	Button incompleteFolderButton 		= new Button();
	final HBox hb 						= new HBox();
	final Text statusText 				= new Text("");

	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage stage) {

		stage.setTitle("SFV Checker v1.0 by Dj Apal®");
		stage.setWidth(700);
		stage.setHeight(650);

		initializeComponents();
		setActions();

		final Button runButton = new Button("Start");
		runButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				Task copyWorker = new Task() {
		            @Override
		            protected Object call() throws Exception {
						
						data.clear();

						Path startingDir = Paths.get(sourceFolder.getText());
						String pattern = "*.sfv";

						Finder finder = new Finder(pattern);
						try {
							Files.walkFileTree(startingDir, finder);
						} catch (IOException e2) {
							e2.printStackTrace();
						}

						finder.done();
						int totalCounter = 0;
						for (Path path : finder.matchingFilesList) {
							totalCounter++;
							statusText.setText("Checking " + totalCounter + " of " + finder.getTotals() + "...");
							try {
								List<String> lines = Files.readAllLines(path, StandardCharsets.ISO_8859_1);
								
								int totalFiles = 0;
								int goodFiles = 0;
								int badFiles = 0;
								int missingFiles = 0;
								
								for (String line : lines) {
									if (!line.startsWith(";")) {
										totalFiles++;
										try {
											String[] split = line.split(" ");
											if (split.length > 2) {
												String fileName = "";
												for (int i = 0; i < split.length - 2; i++) {
													fileName += split[i] + " ";
												}
												fileName += split[split.length - 2];
												long crc = CRC32Check.checksumBufferedInputStream(path.getParent() + "\\" + fileName);
												String storedCrc = split[split.length - 1];
												String calculatedCrc = Long.toHexString(crc);
												if (calculatedCrc.length() < storedCrc.length()) {
													for (int toPrepend = storedCrc.length() - calculatedCrc.length(); toPrepend > 0; toPrepend--) {
														calculatedCrc = "0" + calculatedCrc;
													}
												}

												if (calculatedCrc.equalsIgnoreCase(storedCrc)) {
													goodFiles++;
												} else {
													badFiles++;
												}
											} else {
												long crc = CRC32Check.checksumBufferedInputStream(path.getParent() + "\\" + split[0]);
												String storedCrc = split[1];
												String calculatedCrc = Long.toHexString(crc);
												if (calculatedCrc.length() < storedCrc.length()) {
													for (int toPrepend = storedCrc.length()	- calculatedCrc.length(); toPrepend > 0; toPrepend--) {
														calculatedCrc = "0" + calculatedCrc;
													}
												}
												if (calculatedCrc.equalsIgnoreCase(storedCrc)) {
													goodFiles++;
												} else {
													badFiles++;
												}
											}
										} catch (FileNotFoundException e1) {
											missingFiles++;
										} catch (Exception ge) {
											//
										}
									}
								}
								if (goodFiles == totalFiles) {
									Path target = Paths.get(destinationFolder.getText(), path.getParent().getFileName().toString());
								    Files.move(path.getParent(), target, StandardCopyOption.REPLACE_EXISTING);
									data.add(new SFVInfo(path.getParent().toString(), 
											Integer.toString(totalFiles), 
											Integer.toString(goodFiles), 
											Integer.toString(badFiles), 
											Integer.toString(missingFiles)));
									updateProgress(totalCounter, finder.getTotals());
								} else {
									data.add(new SFVInfo(path.getParent().toString(), 
											Integer.toString(totalFiles), 
											Integer.toString(goodFiles), 
											Integer.toString(badFiles), 
											Integer.toString(missingFiles)));
									if (incompleteFolder.getText().trim().length() > 0) {
										  Path target = Paths.get(incompleteFolder.getText(), path.getParent().getFileName().toString());
										  try { 
											  Files.move(path.getParent(), target, StandardCopyOption.REPLACE_EXISTING); 
										  }	catch (IOException e) { e.printStackTrace(); }										
									}
								}
							} catch (Exception e3) {
								e3.printStackTrace();
								System.err.println("PROBLEM WITH " + path);
							}
						}
						statusText.setText("Finished!");
				return true;
            }
        };
				pb.progressProperty().unbind();
				pb.progressProperty().bind(copyWorker.progressProperty());
                
				new Thread(copyWorker).start();
			}
		});
		
		pb.setStyle("-fx-accent: green;");
		pb.setPrefHeight(20);
		hb.getChildren().addAll(runButton);
		hb.getChildren().addAll(pb);
		hb.setSpacing(3);
		pb.prefWidthProperty().bind(hb.widthProperty().subtract(53));
		
		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(foldersVB, table, hb);

		HBox statusbar = new HBox();
		statusbar.setStyle("-fx-background-color: gainsboro");
		statusbar.getChildren().add(statusText);

		AnchorPane anchorpane = new AnchorPane();
		
		AnchorPane.setTopAnchor(vbox, 10.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 10.0);
		AnchorPane.setBottomAnchor(vbox, 10.0);

		AnchorPane.setBottomAnchor(statusbar, 0.0);
		AnchorPane.setRightAnchor(statusbar, 0.0);
		
		anchorpane.getChildren().addAll(vbox, statusbar);

		Scene scene = new Scene(anchorpane);
		scene.getStylesheets().add("gr/djapal/sfv/default.css");

		stage.setScene(scene);
		stage.show();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initializeComponents() {

		/**
		 * FOLDERS SECTION
		 */
		sourceFolder.setPromptText("Source Folder");
		destinationFolder.setPromptText("Destination Folder");
		incompleteFolder.setPromptText("Incomplete Folder");

		sourceFolderButton.setText("Browse Directory");
		destinationFolderButton.setText("Browse Directory");
		incompleteFolderButton.setText("Browse Directory");

		HBox sourceFolderBox = new HBox();
		sourceFolderBox.getChildren().addAll(sourceFolder, sourceFolderButton);
		HBox.setHgrow(sourceFolder, Priority.ALWAYS);
		sourceFolderBox.setSpacing(3);

		HBox destinationFolderBox = new HBox();
		destinationFolderBox.getChildren().addAll(destinationFolder, destinationFolderButton);
		HBox.setHgrow(destinationFolder, Priority.ALWAYS);
		destinationFolderBox.setSpacing(3);

		HBox incompleteFolderBox = new HBox();
		incompleteFolderBox.getChildren().addAll(incompleteFolder, incompleteFolderButton);
		HBox.setHgrow(incompleteFolder, Priority.ALWAYS);
		sourceFolderBox.setSpacing(3);

		foldersVB.getChildren().addAll(sourceFolderBox, destinationFolderBox, incompleteFolderBox);
		foldersVB.setSpacing(3);

		/**
		 * TABLE SECTION
		 */
		TableColumn filenameColumn = new TableColumn("SFV");
		filenameColumn.setPrefWidth(350);
		filenameColumn.setCellValueFactory(new PropertyValueFactory<SFVInfo, String>("filename"));

		TableColumn totalFilesColumn = new TableColumn("Total Files");
		totalFilesColumn.setPrefWidth(100);
		totalFilesColumn.setCellValueFactory(new PropertyValueFactory<SFVInfo, String>("all"));

		TableColumn goodFilesColumn = new TableColumn("Good");
		goodFilesColumn.setPrefWidth(50);
		goodFilesColumn.setCellValueFactory(new PropertyValueFactory<SFVInfo, String>("good"));

		TableColumn badFilesColumn = new TableColumn("Bad");
		badFilesColumn.setPrefWidth(50);
		badFilesColumn.setCellValueFactory(new PropertyValueFactory<SFVInfo, String>("bad"));

		TableColumn missingFilesColumn = new TableColumn("Missing");
		missingFilesColumn.setPrefWidth(50);
		missingFilesColumn.setCellValueFactory(new PropertyValueFactory<SFVInfo, String>("missing"));

		filenameColumn.setCellFactory(new SFVCellFactory());
		totalFilesColumn.setCellFactory(new SFVCellFactory());
		goodFilesColumn.setCellFactory(new SFVCellFactory());
		badFilesColumn.setCellFactory(new SFVCellFactory());
		missingFilesColumn.setCellFactory(new SFVCellFactory());

		table.setItems(data);
		table.getColumns().addAll(filenameColumn, totalFilesColumn, goodFilesColumn, badFilesColumn, missingFilesColumn);
		table.setEditable(false);
	}

	private void setActions() {
		sourceFolderButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser
						.setTitle("Select the source folder to search for .sfv files");
				// Show open file dialog
				File file = directoryChooser.showDialog(null);
				if (file != null) {
					sourceFolder.setText(file.getPath());
				}
			}
		});

		destinationFolderButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Select the destination folder where all the sfv-pass folders will be moved");
				// Show open file dialog
				File file = directoryChooser.showDialog(null);
				if (file != null) {
					destinationFolder.setText(file.getPath());
				}
			}
		});

		incompleteFolderButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Select the destination folder where all the incomplete sfv folders will be moved");
				// Show open file dialog
				File file = directoryChooser.showDialog(null);
				if (file != null) {
					incompleteFolder.setText(file.getPath());
				}
			}
		});
	}

}
