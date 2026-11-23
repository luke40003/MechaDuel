package com.mechaduel.view;

import com.mechaduel.MechaApplication;
import com.mechaduel.controller.ControlEnsamble;
import com.mechaduel.model.Robot;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnsambleView {

    @FXML private ComboBox<Robot> cmbRobots;
    @FXML private TextField txtNombre;
    @FXML private TextField txtAtk;
    @FXML private TextField txtDef;
    @FXML private TextField txtSpd;
    @FXML private TextField txtNrg;
    @FXML private CheckBox chkQuemadura;
    @FXML private CheckBox chkRegeneracion;
    @FXML private CheckBox chkNucleo;
    @FXML private Button btnConfirmar;
    @FXML private Label lblMensaje;

    private ControlEnsamble control;

    @FXML
    public void initialize() {
        this.control = new ControlEnsamble(this);
        recargarComboRobots();
    }

    public void recargarComboRobots() {
        cmbRobots.getItems().clear();
        cmbRobots.getItems().addAll(control.obtenerRobotsGuardados());
    }

    @FXML
    protected void onSeleccionarRobot() {
        Robot seleccionado = cmbRobots.getValue();
        if (seleccionado != null) {
            txtNombre.setText(seleccionado.getNombre());
            txtAtk.setText(String.valueOf(seleccionado.getAtk()));
            txtDef.setText(String.valueOf(seleccionado.getDef()));
            txtSpd.setText(String.valueOf(seleccionado.getSpd()));
            txtNrg.setText(String.valueOf(seleccionado.getNrg()));

            chkQuemadura.setSelected(seleccionado.getEfectos().contains("Q"));
            chkRegeneracion.setSelected(seleccionado.getEfectos().contains("R"));
            chkNucleo.setSelected(seleccionado.getEfectos().contains("N"));

            lblMensaje.setText("Cargado: " + seleccionado.getNombre());
        }
    }

    @FXML
    protected void onGuardarRobotPrevia() {
        Robot guardado = control.guardarRobot(
                txtNombre.getText(),
                txtAtk.getText(),
                txtDef.getText(),
                txtSpd.getText(),
                txtNrg.getText(),
                obtenerEfectosMarcados()
        );
        if (guardado != null) {
            recargarComboRobots();
            cmbRobots.setValue(guardado);
        }
    }

    @FXML
    protected void onConfirmarMecha() {
        control.confirmarYEnviarMecha(
                txtNombre.getText(),
                txtAtk.getText(),
                txtDef.getText(),
                txtSpd.getText(),
                txtNrg.getText(),
                obtenerEfectosMarcados()
        );
    }

    private List<String> obtenerEfectosMarcados() {
        List<String> efectos = new ArrayList<>();
        if (chkQuemadura.isSelected()) efectos.add("Q");
        if (chkRegeneracion.isSelected()) efectos.add("R");
        if (chkNucleo.isSelected()) efectos.add("N");
        return efectos;
    }

    // Métodos invocados por el Controlador para actualizar la Vista
    public void mostrarMensaje(String texto) {
        Platform.runLater(() -> lblMensaje.setText(texto));
    }

    public void bloquearConfirmacion(boolean bloquear) {
        Platform.runLater(() -> btnConfirmar.setDisable(bloquear));
    }

    public void mostrarAlerta(String titulo, String contenido) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(contenido);
            alert.showAndWait();
        });
    }

    public void irAEstrategia() {
        Platform.runLater(() -> {
            try {
                MechaApplication.cambiarVista("estrategia-view.fxml", 520, 460);
            } catch (IOException e) {
                lblMensaje.setText("Error al abrir pantalla de estrategia.");
            }
        });
    }
}
