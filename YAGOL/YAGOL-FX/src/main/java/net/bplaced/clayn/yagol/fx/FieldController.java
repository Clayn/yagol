package net.bplaced.clayn.yagol.fx;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import net.bplaced.clayn.yagol.Field;

public class FieldController implements Initializable
{

    private final DoubleProperty waitTime=new SimpleDoubleProperty();
    private final DoubleProperty size=new SimpleDoubleProperty();
    @FXML
    private BorderPane pane;
    @FXML
    private GridPane grid;
    @FXML
    private ToggleButton autoButton;
    
    @FXML
    private Slider timeSlider;
    
    @FXML
    private Slider sizeSlider;

    private Button buttons[][];

    private final BooleanProperty auto = new SimpleBooleanProperty(false);

    private final ObjectProperty<Field> currentField = new SimpleObjectProperty<>();

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
        
        currentField.set(new Field(40));
    }

    
    public void addCloseListener() {
        grid.getScene().getWindow().setOnCloseRequest(
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
            }
        });
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
                Button b = buttons[x][y];
                b.setTextFill(alive ? Color.RED : Color.GREEN);
                b.setStyle("-fx-background-color:" + (alive ? "green" : "red"));
            }
        }
        if (auto.get())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(waitTime.longValue());
                        Platform.runLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    onNext();
                                } catch (InterruptedException ex)
                                {
                                    Logger.getLogger(
                                            FieldController.class.getName()).log(
                                            Level.SEVERE,
                                            null, ex);
                                } catch (ExecutionException ex)
                                {
                                    Logger.getLogger(
                                            FieldController.class.getName()).log(
                                            Level.SEVERE,
                                            null, ex);
                                }
                            }
                        });
                    } catch (Exception ex)
                    {
                        Logger.getLogger(FieldController.class.getName()).log(
                                Level.SEVERE,
                                null, ex);
                    }
                }
            }).start();
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
    }

    private void updateGrid()
    {
        grid.getChildren().clear();
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();
        if (currentField.get() == null)
        {
            return;
        }
        Field field = currentField.get();
        buttons = new Button[field.getSize()][field.getSize()];
        for (int x = 0; x < field.getSize(); x++)
        {
            ColumnConstraints column = new ColumnConstraints();
            RowConstraints row = new RowConstraints();
            column.maxWidthProperty().bind(size);
            column.minWidthProperty().bind(size);
            row.maxHeightProperty().bind(size);
            row.minHeightProperty().bind(size);
            grid.getRowConstraints().add(row);
            grid.getColumnConstraints().add(column);
            for (int y = 0; y < field.getSize(); y++)
            {
                boolean alive = field.isAlive(x, y);
                Button b = new Button();
                b.setTextFill(alive ? Color.RED : Color.GREEN);
                b.setStyle("-fx-background-color:" + (alive ? "green" : "red"));
                grid.add(b, x, y);
                b.maxWidthProperty().bind(size);
                b.minWidthProperty().bind(size);
                buttons[x][y] = b;
                int bX = x;
                int bY = y;
                b.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        boolean alive = field.isAlive(bX, bY);
                        field.setCell(bX, bY, !alive);
                        Platform.runLater(FieldController.this::updateValues);
                    }
                });
            }
        }
    }
}
