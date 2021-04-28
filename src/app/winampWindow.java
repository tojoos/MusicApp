package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;

public class winampWindow extends Application {
    private final static int WIDTH = 900;
    private final static int HEIGHT = 600;
    private static double DEFAULT_VOLUME = 0.3F;
    private double rememberVolume;
    private boolean isMuted = false;

    private final File directory = new File("C:\\Users\\joos\\IdeaProjects\\myWinampApp\\src\\app\\songs");

    private final ArrayList<String> songs = new ArrayList<>();
    private ListIterator<String> songIterator;
    private ListView<String> songListView;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage window;
    private BorderPane mainLayout;
    private Duration duration;
    private Duration helpDuration = Duration.ZERO;

    private Button playPauseButton;
    private Button skipButton;
    private Button prevButton;
    private Button muteButton;

    private Menu playlistOptions;
    private Menu sortOptions;
    private Menu helpOptions;
    private Menu viewOptions;
    private MenuBar menuBar;

    private Slider progressionBar;
    private Slider volumeSlider;

    private Label timeLabel;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("MusicApp v1.1");
        //icon setup
        window.getIcons().add(new Image(winampWindow.class.getResourceAsStream("icon\\icon.png")));

        //load playlists
        loadSongs();

        //create buttons
        createButtons();

        //create menu
        createMenu();

        //create sliders (currently only volume slider)
        createSliders();

        ObservableList<String> songList = FXCollections.observableArrayList();
        songList.addAll(getSongNames());
        songListView = new ListView<>();
        songListView.setItems(songList);
        songListView.setPrefWidth(200);
        songListView.setPrefHeight(HEIGHT-40);
        songListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        songListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    String help = songListView.getSelectionModel().getSelectedItem();
                        songIterator = songs.listIterator();
                        while(true) {
                            if((songIterator.next().equals(directory+"\\"+help))) {
                                break;
                            }
                        }
                        mediaView.getMediaPlayer().stop();
                        mediaPlayer.stop();
                        playMusic(directory+"\\"+help);
                        playPauseButton.setText("⏸");
                } );

        Label playlistLabel = new Label("Current playlist: Playlist1");
        playlistLabel.setBackground(new Background(new BackgroundFill
                (Color.rgb(177,177,177,0.9),
                        new CornerRadii(2.0),
                        new Insets(0))));

        GridPane songListPane = new GridPane();
        songListPane.add(playlistLabel,0,0);
        songListPane.add(songListView,0,1);
        songListPane.setPadding(new Insets(5,5,5,5));

        //progressionBar - working
        progressionBar = new Slider();
        progressionBar.setPrefWidth(WIDTH-166);
        progressionBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                //case where mediaView haven't started yet
                if(mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY)
                    mediaView.getMediaPlayer().play();
                if(Math.abs(newValue.floatValue()-oldValue.floatValue())>1.5) {
                    mediaPlayer.seek(duration.multiply((Double) newValue / 100.0));
                    mediaView.getMediaPlayer().seek(duration.multiply((Double) newValue / 100.0));
                }
        });

        //test
        timeLabel = new Label("TE:ST/TE:ST");
        timeLabel.setBackground(new Background(new BackgroundFill
                (Color.rgb(177,177,177,0.6),
                        new CornerRadii(2.0),
                        new Insets(-1))));
        timeLabel.setPrefWidth(65);


        HBox volumeButtons = new HBox(5);
        volumeButtons.getChildren().addAll(prevButton, playPauseButton, skipButton);

        HBox volumeControls = new HBox(5);
        volumeControls.getChildren().addAll(muteButton, volumeSlider);

        GridPane volumePane = new GridPane();

        //volumePane.setGridLinesVisible(true);
        volumePane.setHgap(20);
        volumePane.setVgap(5);
        volumePane.setPadding(new Insets(5,5,2,5));
        volumePane.add(progressionBar,2,0);
        volumePane.add(timeLabel,1,0,1,1);


        GridPane volumePane2 = new GridPane();

        //volumePane2.setGridLinesVisible(true);
        volumePane2.setHgap(20);
        volumePane2.setVgap(5);
        volumePane2.setPadding(new Insets(2,5,5,5));
        volumePane2.add(volumeControls, 29, 0);
        volumePane2.add(volumeButtons, 20, 0);


        VBox volumeLabel = new VBox(0);
        volumeLabel.setPadding(new Insets(5,5,5,5));
        volumeLabel.getChildren().addAll(volumePane, volumePane2);
        volumeLabel.setBackground(new Background(new BackgroundFill
                (Color.rgb(177,177,177,0.7),
                        new CornerRadii(5.0),
                        new Insets(-1.0))));
        volumeLabel.setPrefWidth(WIDTH);
        volumeLabel.setPrefHeight(HEIGHT/7);


        mainLayout = new BorderPane();

        mainLayout.setBottom(volumeLabel);
        mainLayout.setTop(menuBar);
        mainLayout.setLeft(songListPane);


        if(songIterator.hasNext()) {
            playMusic(songIterator.next());
            mediaPlayer.pause();
            mediaView.getMediaPlayer().pause();
            playPauseButton.setText("▶");
        }



        Scene scene = new Scene(mainLayout,WIDTH,HEIGHT);
        //to do - stylesheets
        scene.getStylesheets().add(getClass().getResource("myStyle.css").toString());
        window.setScene(scene);
        window.setMinWidth(WIDTH+25);
        window.setMinHeight(HEIGHT+25);
        window.setMaxWidth(WIDTH+25);
        window.setMaxHeight(HEIGHT+25);
        primaryStage.show();

    }

    private ArrayList<String> getSongNames() {
        ArrayList<String> names = new ArrayList<>();
        for(String s : songs) {
            names.add(s.substring(s.lastIndexOf("\\") + 1));
        }
        return names;
    }


    private void playMusic(String musicFile) { //double opening -> to be fixed
        mediaPlayer = new MediaPlayer(new Media(Paths.get(musicFile).toUri().toString()));
        //duration listener (total track length)
        mediaPlayer.setOnReady(() -> {
            if(mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN)
            duration = mediaPlayer.getMedia().getDuration();
        });
        mediaPlayer.currentTimeProperty().addListener((O, oldValue, newValue) -> {
            if(mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN && mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED);
            progressionBar.setValue(newValue.toMillis()/duration.toMillis()*100);
        });

        if(musicFile.contains(".mp4")) {
            mediaView = new MediaView(mediaPlayer);
        } else {
            mediaView = new MediaView(new MediaPlayer(new Media(Paths.get("C:\\Users\\joos\\IdeaProjects\\myWinampApp\\src\\app\\backgroundvideo\\mp3replacement.mp4").toUri().toString())));
            mediaView.getMediaPlayer().setOnEndOfMedia(() -> mediaView.getMediaPlayer().seek(Duration.ZERO));
            mediaView.getMediaPlayer().play();
        }
        mediaView.setFitHeight(500);
        mediaView.setFitWidth(600);
        mainLayout.setCenter(mediaView);

        songListView.getSelectionModel().select(musicFile.substring(musicFile.lastIndexOf("\\")+1));
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

    public void createButtons() {

        playPauseButton = new Button("▶");
        playPauseButton.setPrefWidth(40);
        playPauseButton.setPrefHeight(40);
        playPauseButton.setOnAction(e -> {
            if(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                mediaView.getMediaPlayer().pause();
                playPauseButton.setText("▶");
            } else {
                mediaPlayer.play();
                mediaView.getMediaPlayer().play();
                playPauseButton.setText("⏸");
            }
        });

        skipButton = new Button("⏭");
        skipButton.setPrefHeight(30);
        skipButton.setPrefWidth(30);
        skipButton.setOnAction(e -> {
            mediaPlayer.stop();
            mediaView.getMediaPlayer().stop();
            if(songIterator.hasNext())
                playMusic(songIterator.next());
        });

        prevButton = new Button("⏮");
        prevButton.setPrefHeight(30);
        prevButton.setPrefWidth(30);
        prevButton.setOnAction(e -> {
            mediaPlayer.stop();
            mediaView.getMediaPlayer().stop();
            if(songIterator.hasPrevious())
                playMusic(songIterator.previous());
        });

        //mute button - done!
        muteButton = new Button("\uD83D\uDD0A");
        muteButton.getStyleClass().clear();
        muteButton.setStyle("-fx-font-size: 18;");
        muteButton.setPrefHeight(20);
        muteButton.setPrefWidth(20);
        muteButton.setOnAction(e -> {
            if(isMuted) {
                muteButton.setText("\uD83D\uDD0A");
                isMuted = true;
                volumeSlider.setValue(rememberVolume);
            } else {
                muteButton.setText("\uD83D\uDD07");
                isMuted = false;
                rememberVolume = DEFAULT_VOLUME*100;
                volumeSlider.setValue(0.0);
            }
        });
    }

    private void createSliders() {
        volumeSlider = new Slider(0.0,100.0,30.0);
        volumeSlider.setPrefWidth(120);
        volumeSlider.setPrefHeight(10);
        volumeSlider.setPadding(new Insets(5,0,0,0));
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            DEFAULT_VOLUME = volumeSlider.getValue()/100.0;
            mediaPlayer.setVolume(DEFAULT_VOLUME);
            if(volumeSlider.getValue()==0.0) {
                isMuted = true;
                muteButton.setText("\uD83D\uDD07");
            } else {
                isMuted = false;
                muteButton.setText("\uD83D\uDD0A");
            }
        });
    }

    private void createMenu() {
        playlistOptions = new Menu("_Playlist");
        sortOptions = new Menu("_Sort");
        helpOptions = new Menu("_Help");
        viewOptions = new Menu("_View");
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(playlistOptions, sortOptions, viewOptions, helpOptions);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
