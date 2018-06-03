package net.bplaced.clayn.yagol.fx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.bplaced.clayn.yagol.Field;
import net.bplaced.clayn.yagol.io.FieldInputStream;
import net.bplaced.clayn.yagol.io.FieldOutputStream;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class FieldController implements Initializable
{

    private static final FontAwesome FONT = new FontAwesome();
    private static final Glyph PLAY = FONT.create(FontAwesome.Glyph.PLAY);
    private static final Glyph PAUSE = FONT.create(FontAwesome.Glyph.PAUSE);
    private final DoubleProperty waitTime = new SimpleDoubleProperty();
    @FXML
    private BorderPane root;
    @FXML
    private Pane pane;

    @FXML
    private ColorPicker deadPicker;
    @FXML
    private ColorPicker alivePicker;

    @FXML
    private ProgressBar aliveBar;

    @FXML
    private ProgressBar deadBar;

    @FXML
    private Button playButton;
    @FXML
    private Slider timeSlider;

    private Rectangle[][] rectangle;

    private final BooleanProperty auto = new SimpleBooleanProperty(false);

    private final ObjectProperty<Field> currentField = new SimpleObjectProperty<>();
    private final IntegerProperty generation = new SimpleIntegerProperty(0);
    private final ObjectProperty<Color> aliveColor = new SimpleObjectProperty<>(
            Color.GREEN);
    private final ObjectProperty<Color> deadColor = new SimpleObjectProperty<>(
            Color.RED);
    private final Timer timer = new Timer("YAGOL-Timer");
    private final IntegerProperty cellCount = new SimpleIntegerProperty(0);
    private final IntegerProperty aliveCount = new SimpleIntegerProperty(0);
    private final DoubleProperty deadCount = new SimpleDoubleProperty(0);

    @FXML
    private void onSave() throws FileNotFoundException, IOException
    {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File load = chooser.showSaveDialog(pane.getScene().getWindow());
        auto.set(false);
        if (load == null)
        {
            return;
        }
        try(FieldOutputStream out=new FieldOutputStream(load)) {
            out.writeField(currentField.get());
        }
    }

    @FXML
    private void onLoad() throws FileNotFoundException, IOException
    {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File load = chooser.showOpenDialog(pane.getScene().getWindow());
        if (load == null || !load.exists() || load.isDirectory())
        {
            return;
        }
        auto.set(false);
        try (FieldInputStream in = new FieldInputStream(load))
        {
            currentField.set(in.readField());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        currentField.addListener(new ChangeListener<Field>()
        {
            @Override
            public void changed(
                    ObservableValue<? extends Field> observable, Field oldValue,
                    Field newValue)
            {
                if (oldValue != null)
                {
                    oldValue.stop();
                }
                generation.set(0);
                updateGrid();
            }
        });
        waitTime.bind(timeSlider.valueProperty());
        playButton.graphicProperty().bind(Bindings.createObjectBinding(
                () -> auto.get() ? PAUSE : PLAY,
                auto));
        cellCount.bind(Bindings.createIntegerBinding(() -> Optional.ofNullable(
                currentField.get()).map(Field::getSize).map((i) -> i * i).orElse(
                -1), currentField));
        aliveCount.bind(Bindings.createIntegerBinding(() -> Optional.ofNullable(
                currentField.get()).map(Field::getAliveCells).orElse(
                -1), currentField, generation));
        deadCount.bind(Bindings.createIntegerBinding(() -> Optional.ofNullable(
                currentField.get()).map(Field::getDeadCells).orElse(
                -1), currentField, generation));
        currentField.set(new Field(60));
        alivePicker.valueProperty().bindBidirectional(aliveColor);
        deadPicker.valueProperty().bindBidirectional(deadColor);
        aliveBar.progressProperty().bind(new SimpleDoubleProperty(1).subtract(
                deadBar.progressProperty()));
        deadBar.progressProperty().bind(deadCount.divide(cellCount));
        aliveBar.styleProperty().bind(Bindings.createStringBinding(
                () -> String.format("-fx-accent: %s;",
                        "#" + Integer.toHexString(aliveColor.get().hashCode())),
                aliveColor));
        deadBar.styleProperty().bind(Bindings.createStringBinding(
                () -> String.format("-fx-accent: %s;",
                        "#" + Integer.toHexString(deadColor.get().hashCode())),
                deadColor));
        alivePicker.valueProperty().addListener(new ChangeListener<Color>()
        {
            @Override
            public void changed(
                    ObservableValue<? extends Color> observable, Color oldValue,
                    Color newValue)
            {
                if (newValue != null)
                {
                    updateValues();
                }
            }
        });
        deadPicker.valueProperty().addListener(new ChangeListener<Color>()
        {
            @Override
            public void changed(
                    ObservableValue<? extends Color> observable, Color oldValue,
                    Color newValue)
            {
                if (newValue != null)
                {
                    updateValues();
                }
            }
        });
    }

    @FXML
    private void onPlay() throws InterruptedException, ExecutionException
    {
        if (auto.get())
        {
            auto.set(false);
            return;
        }
        auto.set(true);
        onNext();
    }

    public void addCloseListener()
    {
        pane.getScene().getWindow().setOnCloseRequest(
                new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent event)
            {
                Field field = currentField.get();
                if (field == null)
                {
                    return;
                }
                field.stop();
                timer.cancel();
            }
        });
        ((Stage) pane.getScene().getWindow()).titleProperty().bind(
                Bindings.createStringBinding(() -> String.format(
                "Generation: %d", generation.get()), generation));
    }

    private void updateValues()
    {
        Field field = currentField.get();
        if (field == null)
        {
            return;
        }
        for (int x = 0; x < field.getSize(); x++)
        {
            for (int y = 0; y < field.getSize(); y++)
            {
                boolean alive = field.isAlive(x, y);
                Rectangle r = rectangle[x][y];
                r.setFill(alive ? aliveColor.get() : deadColor.get());
                r.setStroke(!alive ? aliveColor.get() : deadColor.get());
            }
        }
        if (auto.get())
        {
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Platform.runLater(() ->
                    {
                        try
                        {
                            onNext();
                        } catch (InterruptedException | ExecutionException ex)
                        {
                            Logger.getLogger(FieldController.class.getName()).log(
                                    Level.SEVERE,
                                    null, ex);
                        }
                    });
                }
            }, waitTime.longValue());
        }
    }

    @FXML
    private void onStop()
    {
        auto.set(false);
        currentField.set(new Field(currentField.get().getSize()));
    }

    private void onNext() throws InterruptedException, ExecutionException
    {
        Field field = currentField.get();
        if (field == null)
        {
            return;
        }
        field.tick(() -> Platform.runLater(this::updateValues));
        generation.set(generation.get() + 1);
        System.out.println(
                "Percent Alive: " + aliveBar.getProgress());
    }

    private void updateGrid()
    {
        pane.getChildren().clear();
        if (currentField.get() == null)
        {
            return;
        }
        Field field = currentField.get();
        rectangle = new Rectangle[field.getSize()][field.getSize()];
        for (int x = 0; x < field.getSize(); x++)
        {
            for (int y = 0; y < field.getSize(); y++)
            {
                boolean alive = field.isAlive(x, y);
                Rectangle rect = new Rectangle();
                DoubleBinding width = pane.widthProperty().divide(
                        field.getSize());
                DoubleBinding height = pane.heightProperty().divide(
                        field.getSize());
                rect.widthProperty().bind(width);
                rect.heightProperty().bind(height);
                rect.xProperty().bind(rect.widthProperty().multiply(x));
                rect.yProperty().bind(rect.heightProperty().multiply(y));
                rect.setFill(alive ? aliveColor.get() : deadColor.get());
                rect.setStrokeWidth(1);
                rect.setStroke(!alive ? aliveColor.get() : deadColor.get());
                int rx = x;
                int ry = y;
                rect.setOnMousePressed(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        Field f = currentField.get();
                        if (f == null)
                        {
                            return;
                        }
                        f.setCell(rx, ry, !f.isAlive(rx, ry));
                        boolean localAlive = f.isAlive(rx, ry);
                        rect.setFill(
                                localAlive ? aliveColor.get() : deadColor.get());
                        rect.setStroke(
                                !localAlive ? aliveColor.get() : deadColor.get());
                    }
                });
                rect.setOnDragDetected(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent event)
                    {
                        Dragboard db = rect.startDragAndDrop(TransferMode.ANY);

                        /* Put a string on a dragboard */
                        ClipboardContent content = new ClipboardContent();
                        content.putString("DRAG");
                        db.setContent(content);

                        event.consume();
                    }
                });
                rect.setOnDragEntered(new EventHandler<DragEvent>()
                {
                    @Override
                    public void handle(DragEvent event)
                    {
                        if (event.getGestureSource() != rect
                                && event.getDragboard().hasString())
                        {
                            Field f = currentField.get();
                            if (f == null)
                            {
                                return;
                            }
                            f.setCell(rx, ry, !f.isAlive(rx, ry));
                            boolean localAlive = f.isAlive(rx, ry);
                            rect.setFill(
                                    localAlive ? aliveColor.get() : deadColor.get());
                            rect.setStroke(
                                    !localAlive ? aliveColor.get() : deadColor.get());
                            event.acceptTransferModes(TransferMode.ANY);
                        }

                        event.consume();
                    }
                });
                rectangle[x][y] = rect;
                pane.getChildren().add(rect);

            }
        }
    }
}
