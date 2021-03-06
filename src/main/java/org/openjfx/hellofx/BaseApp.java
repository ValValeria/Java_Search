package org.openjfx.hellofx;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javafx.util.Duration;
import store.Store;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.openjfx.hellofx.interfaces.IDrawUI;

public class BaseApp extends Application implements IDrawUI {
    public final String TITLE = "App";
    public final int HEIGHT = 700;
    public final int WIDTH = 1000;
    private VBox box;
    final String URL_SEARCH = "https://unsplash.com/napi/search?xp=feedback-loop-v2%3Acontrol&per_page=20&query=";
    private FlowPane pane;
    private FlowPane spinner;
    private FlowPane pagination;
    private int page = 1;
    private Button prev_btn;
    private Button next_btn;
    private Stage stage;
    final DirectoryChooser directoryChooser = new DirectoryChooser();
    private VBox boxRight;
    private Path path;
    protected Map<List<String>, byte[]> history = new HashMap<>();
    private ObjectInputStream historyStream;
    private Accordion accordion;
    private FlowPane choicesView;

    @Override
    public void init() {
        try {
            path = Paths.get("store");
            boolean isExists = Files.exists(path);
            historyStream = null;

            if (!isExists) {
                Files.createFile(path);
            } else {
                historyStream = new ObjectInputStream(new FileInputStream(path.toString()));
                this.history.putAll((Map<List<String>, byte[]>) historyStream.readObject());
            }

        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("exports")
    @Override
    public void start(Stage stage) throws IOException {
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color:white;");
        borderPane.setStyle("-fx-background-color:white;-fx-height:100%;-fx-width:100%;");

        this.box = new VBox(30);
        this.box.setStyle("-fx-background-color:" + BACKGROUND_COLOR + ";-fx-padding:30px;");
        this.box.setAlignment(Pos.CENTER);
        this.stage = stage;
        

        this.boxRight = new VBox(30);
        this.boxRight.setStyle("-fx-height:100%;-fx-width:100%;-fx-padding:40px;");
        this.boxRight.setPrefHeight(this.stage.getHeight());
        this.box.setAlignment(Pos.CENTER);
        this.addHistory();

        ScrollPane scrollBoxRight = new ScrollPane();
        this.boxRight.setStyle("-fx-height:100%;-fx-width:100%;-fx-padding:40px;");
        scrollBoxRight.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollBoxRight.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollBoxRight.setContent(this.boxRight);
        scrollBoxRight.setPrefViewportHeight(this.stage.getHeight());

        borderPane.setLeft(this.box);
        borderPane.setCenter(scrollBoxRight);

        Scene scene = new Scene(borderPane);

        this.addLabel(box);

        this.addTextField(box, new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sendHttpRequest();
            }
        });
        
        choicesView = new FlowPane(10,10);
        setStylesView(choicesView);
        choicesView.setAlignment(Pos.CENTER);
        
        RadioButton btn1 = new RadioButton("Video");
        RadioButton btn2 = new RadioButton("Image");
        btn2.setSelected(true);
        setWhiteFont(btn2);
        setWhiteFont(btn1);
        
        choicesView.getChildren().addAll(btn2,btn1);
        this.box.getChildren().add(choicesView);

        this.pane = new FlowPane();
        this.addResultBar(box, this.pane);
        this.pane.setAlignment(Pos.CENTER);
        this.setVisible(this.pane, false);
        this.box.getChildren().add(this.pane);

        this.spinner = this.addSpinner();
        this.box.getChildren().add(spinner);
        this.setVisible(spinner, false);

        stage.setTitle(this.TITLE);
        stage.setMaxWidth(this.WIDTH + 100);
        stage.setMinWidth(this.WIDTH);
        stage.setMinHeight(this.HEIGHT);
        stage.setMaxHeight(this.HEIGHT + 200);
        stage.setScene(scene);
        stage.show();
    }

    private void addHistory() {
        Label label = new Label("History".toUpperCase());
        label.setStyle("-fx-font-size:25px;-fx-width:100%;");
        label.setAlignment(Pos.CENTER);
        this.boxRight.getChildren().add(label);

        this.accordion = new Accordion();

        if (this.history.size() > 0) {
            this.history.forEach((s, p) -> {
                this.addtilepane(s, p);
            });
        }

        this.boxRight.getChildren().add(this.accordion);
    }

    private void addtilepane(List<String> s, byte[] p) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(s.get(0));
        titledPane.setStyle("-fx-font-size:16px;");
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color:white;");
        Label filePath = new Label("Path : " + s.get(1));
        filePath.setStyle("-fx-font-size:16px;");

        Image image = new Image(new ByteArrayInputStream(p), 200, 200, false, true);
        ImageView imageView = new ImageView(image);

        Button button = new Button("Delete");
        button.setStyle("-fx-font-size:16px;");

        button.setOnAction((e) -> {
            try {
                Files.delete(Paths.get(s.get(1)));
                this.fade(titledPane, 1, 0);

                Runnable runnable = () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    this.accordion.getPanes().remove(titledPane);
                    this.history.remove(s, p);
                };

                Platform.runLater(runnable);

            } catch (Throwable error) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setContentText("Your file has been deleted by someone. Sorry");
                alert.setHeaderText(null);
                alert.showAndWait();

                this.accordion.getPanes().remove(titledPane);
                this.history.remove(s, p);
            }
        });
        
        vbox.getChildren().addAll(filePath,imageView,button);

        titledPane.setContent(vbox);
        this.accordion.getPanes().add(titledPane);
    }

    protected void sendHttpRequest()
    {
        String text = Store.text;
        this.page = 1;

        if (!text.isEmpty()) {
            this.runTask(text, false);
        }
    }

    private void runTask(String text, boolean isPagination)
    {
        this.setVisible(spinner, true);
        this.setVisible(this.pane, false);
        this.pane.getChildren().clear();

        if (!isPagination) {
            this.removePagination();
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                List<Result> data = new ArrayList<>();
                int start = PER_PAGE * (page - 1);
                int end = start + PER_PAGE;

                if (!isPagination) {
                    data.addAll(processResponse(getBody(URL_SEARCH, text)));
                } else {
                    data.addAll(Store.list);
                }
                return data.subList(start, end);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }).thenAccept(v -> {
            List<byte[]> list = this.showImages(box, spinner, v);

            Platform.runLater(() -> {
                list.forEach(item -> {
                    StackPane stack = new StackPane();
                    ImageView image = processImage(item);
                    FlowPane content = new FlowPane();
                    stack.setMaxWidth(image.getFitWidth());
                    stack.setAlignment(Pos.CENTER);
                    this.addDownloadContent(content, item, stack.getWidth(), stack.getHeight());
                    content.setAlignment(Pos.CENTER);
                    setVisible(content, false);

                    stack.setOnMouseEntered((e) -> {
                        fade(content, 0, 0.9);
                        setVisible(content, true);
                    });

                    stack.setOnMouseExited((e) -> {
                        fade(content, 0.9, 0);
                    });

                    stack.getChildren().addAll(image, content);
                    this.pane.getChildren().add(stack);
                });
                setVisible(this.pane, true);
                setVisible(spinner, false);
                int math = (int) Math.ceil(Store.list.size() / PER_PAGE);

                if (math >= page) {
                    this.removePagination();
                    this.addPagination(math);
                }
            });
        });
    }

    private void addDownloadContent(FlowPane content, byte bytes[], double d, double f) 
    {
        Button btnDownLoad = new Button("download");

        String width = String.valueOf(d) + "px";
        String height = String.valueOf(f) + "px";
        content.setStyle("-fx-background-color:#231F20;" + String.format("-fx-width:%s;-fx-height:%s", width, height));
       
        btnDownLoad.setStyle("-fx-text-fill:white;" + "-fx-background-color:#f7b731;" + "-fx-font-size:20px;"
                + "-fx-padding:5 25px;");

        btnDownLoad.setOnAction((e) -> {
            configuringDirectoryChooser(directoryChooser);

            File directory = directoryChooser.showDialog(this.stage);
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-HH-mm-ss");

            String filename = myFormatObj.format(myDateObj) + "image.jpg";
            System.out.println(filename);
            String path = Paths.get(directory.getAbsolutePath(),filename).toAbsolutePath().toString();
           
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

            addToHistory(filename, path,bytes);

            try {
                Files.copy(stream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        content.getChildren().add(btnDownLoad);
    }

    private void addToHistory(String file, String path2,byte[] bytes_data) 
    {
        List<String> file_data = List.of(file,path2);
        this.history.put(file_data, bytes_data);
        this.addtilepane(file_data,bytes_data);
    }

    private void removePagination() 
    {
        if (this.box.getChildren().contains(this.pagination)) {
            this.box.getChildren().remove(this.pagination);
        }
    }

    private void addPagination(int math) 
    {
        this.pagination = new FlowPane();
        this.pagination.setAlignment(Pos.CENTER);
        this.pagination.setHgap(10);
        this.pagination.setVgap(10);
        String style = "-fx-background-color:#F1EBE5;" + "-fx-font-size:14px;" + "-fx-padding:5 25px;"
                + "-fx-width:150px;";

        Consumer<String> consumer = (v) -> {
            if (v == "prev" && this.page > 1) {
                this.page -= 1;
            } else if (v == "next" && Store.list.size() > (PER_PAGE * page)) {
                this.page += 1;
                this.prev_btn.setDisable(false);
            }
            runTask(Store.text, true);
        };

        this.prev_btn = new Button("previous");
        prev_btn.setOnAction((e) -> {
            consumer.accept("prev");
        });
        prev_btn.setStyle(style);
        if (this.page == 1) {
            prev_btn.setDisable(true);
        }

        this.next_btn = new Button("next");
        next_btn.setOnAction((e) -> {
            consumer.accept("next");
        });
        next_btn.setStyle(style);
        if (math == page) {
            next_btn.setDisable(true);
        }

        this.pagination.getChildren().addAll(prev_btn, next_btn);

        this.box.getChildren().add(this.pagination);
    }

    public void fade(Node content, double from, double to)
    {
        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(1000));
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setNode(content);
        fade.play();
    }

    private void configuringDirectoryChooser(DirectoryChooser directoryChooser) 
    {
        directoryChooser.setTitle("Select Some Directories");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    @Override
    public void stop() 
    {
        try(  FileOutputStream file_stream = new FileOutputStream(this.path.toFile());
              ObjectOutputStream stream = new ObjectOutputStream(file_stream);) {
            
            stream.flush();
            stream.writeObject(this.history);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
