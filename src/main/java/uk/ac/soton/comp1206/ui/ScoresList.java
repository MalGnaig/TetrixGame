package uk.ac.soton.comp1206.ui;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Pair;

public class ScoresList {
    private SimpleListProperty<Pair<String, Integer>> scores;

    public ScoresList() {
        scores = new SimpleListProperty<>();
    }

    public  ScoresList(SimpleListProperty<Pair<String, Integer>> scores) {
        this.scores = scores;
    }

    /**
     * @return the first ten elements as a ListView
     */
    public ListView<Pair<String, Integer>> reveal(){
        return new ListView<>(
                FXCollections.observableArrayList(scores.stream().limit(10).toList()));

    }

    public ObservableList<Pair<String, Integer>> getScores() {
        return scores.get();
    }

    public SimpleListProperty<Pair<String, Integer>> scoresProperty() {
        return scores;
    }

    public void setScores(ObservableList<Pair<String, Integer>> scores) {
        this.scores.set(scores);
    }
}
