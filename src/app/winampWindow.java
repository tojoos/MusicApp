package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;

public class winampWindow extends Application {
    private final ArrayList<String> songs = new ArrayList<>();
    private ListIterator<String> songIterator;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage window;
    private BorderPane mainLayout;
    private final static int WIDTH = 900;
    private final static int HEIGHT = 600;
    private static double DEFAULT_VOLUME = 0.3F;

    private Button playPause;
    private Button skipButton;
    private Button prevButton;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("MusicApp v1.1");
        loadSongs();


        Menu songOptions = new Menu("Options");
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(songOptions);



        Label musicInfo = new Label("\uD83D\uDD0A");

        Slider volumeSlider = new Slider(0.0,100.0,30.0);

        volumeSlider.setPrefWidth(111);
        volumeSlider.setPrefHeight(10);
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            DEFAULT_VOLUME = volumeSlider.getValue()/100.0;
            mediaPlayer.setVolume(DEFAULT_VOLUME);
        });


        //Volume buttons creation
        createVolumeButtons();

        //to do
        Slider progressionBar = new Slider();
        progressionBar.setPrefWidth(WIDTH-120);




        HBox volumeButtons = new HBox(5);
        volumeButtons.getChildren().addAll(prevButton, playPause, skipButton);

        HBox volumeControls = new HBox(5);
        volumeControls.getChildren().addAll(musicInfo, volumeSlider);

        GridPane volumePane = new GridPane();

        //volumePane.setGridLinesVisible(true);
        volumePane.setHgap(20);
        volumePane.setVgap(5);
        volumePane.setPadding(new Insets(5,5,2,5));
        volumePane.add(progressionBar,3,0);


        GridPane volumePane2 = new GridPane();

        //volumePane2.setGridLinesVisible(true);
        volumePane2.setHgap(20);
        volumePane2.setVgap(5);
        volumePane2.setPadding(new Insets(2,5,5,5));
        volumePane2.add(volumeControls, 31, 0);
        volumePane2.add(volumeButtons, 20, 0);


        VBox volumeLabel = new VBox(0);
        volumeLabel.setPadding(new Insets(5,5,5,5));
        volumeLabel.getChildren().addAll(volumePane, volumePane2);
        volumeLabel.setBackground(new Background(new BackgroundFill
                (Color.rgb(177,177,177,0.7),
                        new CornerRadii(5.0),
                        new Insets(-1.0))));

        mainLayout = new BorderPane();

        mainLayout.setBottom(volumeLabel);
        mainLayout.setTop(menuBar);
        //


        if(songIterator.hasNext()) {
            playMusic(songIterator.next());
            mediaPlayer.pause();
            mediaPlayer = new MediaPlayer(new Media(new File(songs.get(0)).toURI().toString()));
            mediaView.setFitHeight(500);
            mediaView.setFitWidth(600);
            mainLayout.setCenter(mediaView);
        }

        Scene scene = new Scene(mainLayout,WIDTH,HEIGHT);
        window.setScene(scene);
        window.setMinWidth(WIDTH+25);
        window.setMinHeight(HEIGHT+25);
        window.setMaxWidth(WIDTH+25);
        window.setMaxHeight(HEIGHT+25);
        primaryStage.show();
    }

    private void playMusic(String musicFile) {
        mediaPlayer = new MediaPlayer(new Media(Paths.get(musicFile).toUri().toString()));
        mediaView = new MediaView(mediaPlayer);
        mediaView.setFitHeight(500);
        mediaView.setFitWidth(600);
        mainLayout.setCenter(mediaView);
        mediaPlayer.play();
        mediaPlayer.setVolume(DEFAULT_VOLUME);
        mediaPlayer.setOnEndOfMedia(() -> {
            if(songIterator.hasNext()) {
                mediaPlayer.stop();
                mediaView.getMediaPlayer().pause();
                playMusic(songIterator.next());

            }
        });
    }

    private void loadSongs() {
        try {
            File directory = new File("C:\\Users\\joos\\IdeaProjects\\myWinampApp\\src\\app\\songs");
            File[] songFiles = directory.listFiles((dir, name) -> name.endsWith("mp3") || name.endsWith("mp4"));
            assert songFiles != null : "No files";
            for (File songFile : songFiles) {
                songs.add(songFile.toString());
            }
            songIterator = songs.listIterator();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createVolumeButtons() {
        playPause = new Button("▶");
        playPause.setMinWidth(30);
        playPause.setMinHeight(30);
        playPause.setOnAction(e -> {
            if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                mediaView.getMediaPlayer().pause();
                playPause.setText("▶");
            } else {
                mediaPlayer.play();
                mediaView.getMediaPlayer().play();
                playPause.setText("⏸");
            }
        });

        skipButton = new Button("⏭");
        skipButton.setMinHeight(25);
        skipButton.setMinWidth(25);
        skipButton.setOnAction(e -> {
            mediaPlayer.stop();
            mediaView.getMediaPlayer().stop();
            playPause.setText("⏸");
            if(songIterator.hasNext())
                playMusic(songIterator.next());
        });

        prevButton = new Button("⏮");
        prevButton.setMinHeight(25);
        prevButton.setMinWidth(25);
        prevButton.setOnAction(e -> {
            mediaPlayer.stop();
            if(songIterator.hasPrevious())
                playMusic(songIterator.previous());
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}