package se233.chapter3reverseindexcreation.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import se233.chapter3reverseindexcreation.Launcher;
import se233.chapter3reverseindexcreation.model.FileFreq;
import se233.chapter3reverseindexcreation.model.PDFdocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainViewController {
    LinkedHashMap<String, ArrayList<FileFreq>> uniqueSets;
    @FXML
    private ListView<String> inputListView;
    ArrayList<String> listViewPath = new ArrayList<>(); // Exercise 3
    @FXML
    private Button startButton;
    @FXML
    public MenuItem fileClose; // Exercise 4
    @FXML
    private ListView listView;
private Scene scene;
    @FXML
    public void initialize() {
        // Exercise 4 START
        fileClose.setOnAction(event -> {
            Launcher.stage.close();
        });
        // Exercise 4 END
        inputListView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".pdf");
            if (db.hasFiles() && isAccepted) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });
        inputListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                String filePath;
                int total_files = db.getFiles().size();
                for (int i = 0; i < total_files; i++) {
                    File file = db.getFiles().get(i);
                    filePath = file.getAbsolutePath();
//                    inputListView.getItems().add(filePath);
                    listViewPath.add(filePath); // Exercise 3
                    inputListView.getItems().add(file.getName());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        startButton.setOnAction(event -> {
            Parent bgRoot = Launcher.stage.getScene().getRoot();
            Task<Void> processTask = new Task<Void>() {
                @Override
                public Void call() throws IOException {
                    ProgressIndicator pi = new ProgressIndicator();
                    VBox box = new VBox(pi);
                    box.setAlignment(Pos.CENTER);
                    Launcher.stage.getScene().setRoot(box);

                    ExecutorService executor = Executors.newFixedThreadPool(4);
                    final ExecutorCompletionService<Map<String, FileFreq>> completionService = new
                            ExecutorCompletionService<>(executor);
//                    List<String> inputListViewItems = inputListView.getItems();
                    List<String> inputListViewItems = listViewPath; // Exercise 3
                    int total_files = inputListViewItems.size();
                    Map<String, FileFreq>[] wordMap = new Map[total_files];

                    for (String inputListViewItem : inputListViewItems) {
                        try {
                            PDFdocument p = new PDFdocument(inputListViewItem);
                            completionService.submit(new WordMapPageTask(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < total_files; i++) {
                        try {
                            Future<Map<String, FileFreq>> future = completionService.take();
                            wordMap[i] = future.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        WordMapMergeTask merger = new WordMapMergeTask(wordMap);
                        Future<LinkedHashMap<String, ArrayList<FileFreq>>> future = executor.submit(merger);
                        uniqueSets = future.get();
                        // Exercise 2 For the word appearing on many PDF files, show the frequency counts separately by displaying the frequency count behind each word. For example, if the word about is presented
                        // in three files with the frequency of 1, 2, and 3, then we have to show about (3,2,1) on the
                        // ListView on the right hands side.
                        for(String word : uniqueSets.keySet()){ // Words
//                            String[] temp = {};
                            ArrayList<Integer> wordFreq = new ArrayList<>();
                            for(FileFreq f : uniqueSets.get(word)){ // File name and word's frequency
//                                temp += j.getFreq() + "";
                                wordFreq.add(f.getFreq());
                            }
                            listView.getItems().add(word + ": \n" + wordFreq.toString());
                        }
//                        listView.getItems().addAll(uniqueSets.keySet());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdown();
                    }
                    return null;
                }
            };
            processTask.setOnSucceeded(e -> {
                Launcher.stage.getScene().setRoot(bgRoot);
            });
            Thread thread = new Thread(processTask);
            thread.setDaemon(true);
            thread.start();
        });
        listView.setOnMouseClicked(event -> {
            ArrayList<FileFreq> listOfLinks = uniqueSets.get(listView.getSelectionModel().getSelectedItem());
            ListView<FileFreq> popupListView = new ListView<>();
            LinkedHashMap<FileFreq,String> lookupTable = new LinkedHashMap<>();

            for (FileFreq listOfLink : listOfLinks) {
                lookupTable.put(listOfLink, listOfLink.getPath());
                popupListView.getItems().add(listOfLink);
            }
            popupListView.setPrefHeight(popupListView.getItems().size() * 28);
            popupListView.setOnMouseClicked(innerEven -> {
                Launcher.hs.showDocument("file://" + lookupTable.get(popupListView.getSelectionModel().getSelectedItem()));
                popupListView.getScene().getWindow().hide();
            });
            Popup popup = new Popup();
            popup.getContent().add(popupListView);
            popup.show(Launcher.stage);

            // Exercise 5
            // Once the user clicks a term presented on the right-handed ListView, she or he cannot cancel
            // the selection. Our task here is to create another event handler that accepts a keystroke esc to
            // close the pop-up window.
            popupListView.setOnKeyPressed(e -> {
                if(e.getCode() == KeyCode.ESCAPE){
//                    Launcher.stage.close();
                    popupListView.setVisible(false);

                }
            });
        });

    }
}
