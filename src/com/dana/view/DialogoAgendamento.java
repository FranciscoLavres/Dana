package com.dana.view;

import com.dana.model.Resgate;
import com.dana.util.Formato;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Janela para escolher dia e horário de uso de um voucher (só datas dentro da validade). */
public class DialogoAgendamento extends Dialog<LocalDateTime> {

    private static final LocalTime PRIMEIRO_HORARIO = LocalTime.of(8, 0);
    private static final LocalTime ULTIMO_HORARIO = LocalTime.of(20, 0);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public DialogoAgendamento(Resgate resgate) {
        setTitle("Agendar uso do voucher");
        setHeaderText("Quando você vai usar \"" + resgate.getServico().getNome() + "\" em "
                + resgate.getServico().getParceiro().getNome() + "?\nO voucher vale até "
                + Formato.data(resgate.getValidade()) + ".");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        LocalDate hoje = LocalDate.now();
        DatePicker data = new DatePicker(hoje.plusDays(1).isAfter(resgate.getValidade()) ? hoje : hoje.plusDays(1));
        data.setEditable(false);
        data.setDayCellFactory(seletor -> new DateCell() {
            @Override
            public void updateItem(LocalDate dia, boolean vazio) {
                super.updateItem(dia, vazio);
                setDisable(vazio || dia.isBefore(hoje) || dia.isAfter(resgate.getValidade()));
            }
        });

        List<LocalTime> horarios = new ArrayList<>();
        for (LocalTime h = PRIMEIRO_HORARIO; !h.isAfter(ULTIMO_HORARIO); h = h.plusMinutes(30)) {
            horarios.add(h);
        }
        ChoiceBox<LocalTime> hora = new ChoiceBox<>(FXCollections.observableArrayList(horarios));
        hora.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime h) {
                return h == null ? "" : h.format(HORA);
            }

            @Override
            public LocalTime fromString(String texto) {
                return null;
            }
        });
        LocalDateTime atual = resgate.getAgendadoPara();
        hora.setValue(atual != null && horarios.contains(atual.toLocalTime()) ? atual.toLocalTime() : LocalTime.of(14, 0));
        if (atual != null && !atual.toLocalDate().isBefore(hoje)) {
            data.setValue(atual.toLocalDate());
        }

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Dia"), 0, 0);
        grade.add(data, 1, 0);
        grade.add(new Label("Horário"), 0, 1);
        grade.add(hora, 1, 1);
        getDialogPane().setContent(grade);

        setResultConverter(botao -> botao == ButtonType.OK && data.getValue() != null && hora.getValue() != null
                ? LocalDateTime.of(data.getValue(), hora.getValue()) : null);
    }
}
