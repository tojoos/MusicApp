package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class winampWindow extends Application {
    private final static int WIDTH = 900;
    private final static int HEIGHT = 600;
    private static double DEFAULT_VOLUME = 0.3F;
    private double rememberVolume;
    private boolean isMuted = false;
    private boolean isLooped = false;
    private boolean isShuffled = false;

    private final File directory = new File("C:\\Users\\joos\\IdeaProjects\\myWinampApp\\src\\app\\songs");

    private final ArrayList<String> songs = new ArrayList<>();
    private ListIterator<String> songIterator;
    private ListView<String> songListView;
    private ObservableList<String> songList;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage window;
    private BorderPane mainLayout;
    private Duration duration;

    private Button playPauseButton;
    private Button skipButton;
    private Button prevButton;
    private Button muteButton;
    private Button loopButton;
    private Button shuffleButton;
    private Button addNewSongButton;
    private Button refreshSongListButton;


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

        createListViewOfSongs();

        Label playlistLabel = new Label("Current playlist: Playlist1");
        playlistLabel.setBackground(new Background(new BackgroundFill
                (Color.rgb(177,177,177,0.9),
                        new CornerRadii(2.0),
                        new Insets(0))));


        HBox listButtons = new HBox();
        listButtons.getChildren().addAll(refreshSongListButton,addNewSongButton);

        GridPane songListPane = new GridPane();
        songListPane.setHgap(20);
        songListPane.add(playlistLabel,0,0);
        songListPane.add(listButtons, 1, 0);
        songListPane.add(songListView,0,1,2,1);
        songListPane.setPadding(new Insets(5,5,5,5));


        //progressionBar - working
        progressionBar = new Slider();
        progressionBar.setPrefWidth(WIDTH-166);
        progressionBar.setId("progression-slider");
        progressionBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                //case where mediaView haven't started yet
                if(mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY)
                    mediaView.getMediaPlayer().play();
                //assurance value will be changed manually not by listener
                if(Math.abs(newValue.floatValue()-oldValue.floatValue())>1.5) {
                    mediaPlayer.seek(duration.multiply((Double) newValue / 100.0));
                    mediaView.getMediaPlayer().seek(duration.multiply((Double) newValue / 100.0));
                }
        });

        //test
        timeLabel = new Label("0:00/0:00");
        timeLabel.setPrefWidth(65);


        HBox volumeButtons = new HBox(5);
        volumeButtons.getChildren().addAll(loopButton, prevButton, playPauseButton, skipButton, shuffleButton);

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
        volumePane2.add(volumeControls, 26, 0);
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
            //to get default time for first track
            updateTimeLabel();
        });

        mediaPlayer.currentTimeProperty().addListener((O, oldValue, newValue) -> {
            if(mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN) {
                progressionBar.setValue(newValue.toMillis() / duration.toMillis() * 100);
                updateTimeLabel();
            }
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
            if(isLooped) {
                //looped mode on
                songIterator.previous();
            } else if(isShuffled){
                //shuffle mode on - avoids doubling songs
               nextShuffleSongName();
            }
            if(songIterator.hasNext()) {
                mediaPlayer.stop();
                mediaView.getMediaPlayer().pause();
            } else {
                for(int i=0;i<songs.size();i++)
                    songIterator.previous();
            }
            playMusic(songIterator.next());

        });
    }


    private void loadSongs() {
        try {
            File[] songFiles = directory.listFiles((dir, name) -> name.endsWith("mp3") || name.endsWith("mp4"));
            songs.clear();
            assert songFiles != null : "No files";
            for (File songFile : songFiles) {
                songs.add(songFile.toString());
            }
            songIterator = songs.listIterator();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createListViewOfSongs() {
        songList = FXCollections.observableArrayList();
        songList.addAll(getSongNames());
        songListView = new ListView<>();
        songListView.setItems(songList);
        songListView.setPrefWidth(200);
        songListView.setPrefHeight(HEIGHT-40);
        songListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        songListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    String help = songListView.getSelectionModel().getSelectedItem();
                    if(help != null) {
                        songIterator = songs.listIterator();
                        while (true) {
                            if ((songIterator.next().equals(directory + "\\" + help))) {
                                break;
                            }
                        }
                        mediaView.getMediaPlayer().stop();
                        mediaPlayer.stop();
                        playMusic(directory + "\\" + help);
                        playPauseButton.setText("⏸");
                    }
                });
    }

    private void nextShuffleSongName() {
        int idx;
        int randomIdx = (int)(Math.random()*(songs.size()));
        if(songIterator.hasPrevious()) {
            idx = songIterator.previousIndex();
            idx++;
        } else {
            idx = songIterator.nextIndex();
            idx--;
        }
        for(int i=0;i<idx;i++) {
            songIterator.previous();
        }
        while(randomIdx == idx) {
            randomIdx = (int) (Math.random() * (songs.size()));
        }

        for (int i = 0; i < randomIdx; i++) {
            songIterator.next();
        }
    }

    private void updateTimeLabel() {
        double totalTime = duration.toSeconds();
        double currentTime = Math.round(mediaPlayer.getCurrentTime().toSeconds());
        int minutes=0, seconds;
        int totalMinutes=0, totalSeconds;
        String timeString="";
        if(currentTime >= 60)
            minutes = (int) Math.floor(currentTime / 60);
        seconds = (int) Math.round(currentTime % 60);

        if(totalTime >= 60)
            totalMinutes = (int) Math.floor(totalTime / 60);
        totalSeconds = (int) Math.round(totalTime % 60);

        timeString += minutes + ":";

        if(seconds<10) {
            timeString += "0" + seconds;
        } else {
            timeString += seconds;
        }

        timeString += "/" + totalMinutes + ":";

        if(totalSeconds<10) {
            timeString += "0" + totalSeconds;
        } else {
            timeString += totalSeconds;
        }
        timeLabel.setText(timeString);
    }

    private void createButtons() {
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
            if(isShuffled) {
                nextShuffleSongName();
            } else {
                if (!songIterator.hasNext()) {
                    for (int i = 0; i < songs.size(); i++)
                        songIterator.previous();
                }
            }
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

        muteButton = new Button("\uD83D\uDD0A");
        muteButton.getStyleClass().clear();
        muteButton.setStyle("-fx-font-size: 16;");
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

        loopButton = new Button("\uD83D\uDD01");
        loopButton.getStyleClass().clear();
        loopButton.setPrefHeight(15);
        loopButton.setPrefWidth(20);
        loopButton.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-weight: bold;");
        loopButton.setPadding(new Insets(10,0,0,0));
        loopButton.setAlignment(Pos.CENTER);
        loopButton.setOnAction(e -> {
            if(isLooped) {
                isLooped = false;
                loopButton.setStyle("-fx-text-fill: #FFFFFF;");
            } else {
                isLooped = true;
                loopButton.setStyle("-fx-text-fill: linear-gradient(#DC9656, #AB4642);");
            }
        });

        shuffleButton = new Button("\uD83D\uDD00");
        shuffleButton.getStyleClass().clear();
        shuffleButton.setPrefHeight(20);
        shuffleButton.setPrefWidth(20);
        shuffleButton.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-weight: bold;");
        shuffleButton.setPadding(new Insets(10,0,0,0));
        shuffleButton.setAlignment(Pos.CENTER);
        shuffleButton.setOnAction(e -> {
            if(isShuffled) {
                isShuffled = false;
                shuffleButton.setStyle("-fx-text-fill: #FFFFFF;");
            } else {
                isShuffled = true;
                shuffleButton.setStyle("-fx-text-fill: linear-gradient(#DC9656, #AB4642);");
            }
        });

        addNewSongButton = new Button("+");
        addNewSongButton.getStyleClass().clear();
        addNewSongButton.setStyle("-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgb(177, 177, 177, 0.9);" +
                "-fx-background-radius: 2px;");
        addNewSongButton.setAlignment(Pos.CENTER);
        addNewSongButton.setPrefHeight(10);
        addNewSongButton.setPrefWidth(20);
        addNewSongButton.setOnAction(e -> browseAndAddSong());

        refreshSongListButton = new Button("\uD83D\uDD04");
        refreshSongListButton.getStyleClass().clear();
        refreshSongListButton.setStyle("-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgb(177, 177, 177, 0.9);" +
                "-fx-background-radius: 2px;");
        refreshSongListButton.setAlignment(Pos.CENTER);
        refreshSongListButton.setPrefHeight(10);
        refreshSongListButton.setPrefWidth(20);
        refreshSongListButton.setOnAction(e -> refreshSongList());

    }

    private void refreshSongList() {
        loadSongs();
        songListView.getItems().clear();
        songListView.getItems().addAll(getSongNames());
        songIterator = songs.listIterator();
    }

    private void browseAndAddSong() {
        FileChooser songBrowser = new FileChooser();
        songBrowser.setTitle("Add new song");
        List<File> songFilesToAdd = songBrowser.showOpenMultipleDialog(window);
        File destination = new File("C:\\Users\\joos\\IdeaProjects\\myWinampApp\\src\\app\\songs");
        if(songFilesToAdd != null) {
            for(File f : songFilesToAdd) {
                try {
                    Files.copy(f.toPath(),new File(destination +"\\" + f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
