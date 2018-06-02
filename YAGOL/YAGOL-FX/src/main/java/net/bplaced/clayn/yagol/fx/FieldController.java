package net.bplaced.clayn.yagol.fx;

import java.net.URL;
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
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.bplaced.clayn.yagol.Field;

public class FieldController implements Initializable
{

    private final DoubleProperty waitTime = new SimpleDoubleProperty();
    private final DoubleProperty size = new SimpleDoubleProperty();
    @FXML
    private BorderPane root;
    @FXML
    private Pane pane;
    @FXML
    private ToggleButton autoButton;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider sizeSlider;

    private Rectangle[][] rectangle;

    private final BooleanProperty auto = new SimpleBooleanProperty(false);

    private final ObjectProperty<Field> currentField = new SimpleObjectProperty<>();
    private final IntegerProperty generation = new SimpleIntegerProperty(0);
    private final Timer timer = new Timer("YAGOL-Timer");

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        auto.bind(autoButton.selectedProperty());
        size.bind(sizeSlider.valueProperty());
        waitTime.bind(timeSlider.valueProperty());
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
                updateGrid();
            }
        });

        currentField.set(new Field(90));
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
                r.setFill(alive ? Color.GREEN : Color.RED);
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
    private void onNext() throws InterruptedException, ExecutionException
    {
        Field field = currentField.get();
        if (field == null)
        {
            return;
        }
        field.tick(() -> Platform.runLater(this::updateValues));
        generation.set(generation.get() + 1);
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
                rect.setFill(alive ? Color.GREEN : Color.RED);
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
                        rect.setFill(localAlive ? Color.GREEN : Color.RED);
                    }
                });
                rectangle[x][y] = rect;
                pane.getChildren().add(rect);

            }
        }
    }
}
