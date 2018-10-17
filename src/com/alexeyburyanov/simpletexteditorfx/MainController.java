package com.alexeyburyanov.simpletexteditorfx;

import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class MainController {

    public VBox VBoxMain;
    public TabPane TabPaneMain;
    public Tab TabDefault;
    public AnchorPane AnchorPaneDefault;
    public TextArea TextAriaDefault;
    public Label LabelStatus;
    public Menu MenuLastFiles;

    // Вкладки
    private List<Tab> _tabList = new LinkedList<>();
    // Панели внутри вкладок
    private List<AnchorPane> _anchorPaneList = new LinkedList<>();
    // Контент панели внутри вкладок
    private List<TextArea> _textAreaList = new LinkedList<>();
    // Пути для сохранения
    private List<String> _pathsSave = new LinkedList<>();

    public void initialize() {
        _tabList.add(TabDefault);
        _anchorPaneList.add(AnchorPaneDefault);
        _textAreaList.add(TextAriaDefault);
        _pathsSave.add("");
    }

    /** Новый документ */
    public void createNewDoc() {
        // Создаём новую вкладку содержащую в себе AnchorPane с
        // текстовой зоной на всё доступное пространство панели
        var newTab = new Tab("Новый");
        var newTextArea = new TextArea();
        AnchorPane.setTopAnchor(newTextArea, 0.0);
        AnchorPane.setRightAnchor(newTextArea, 0.0);
        AnchorPane.setLeftAnchor(newTextArea, 0.0);
        AnchorPane.setBottomAnchor(newTextArea, 0.0);
        var newPane = new AnchorPane(newTextArea);
        newTab.setContent(newPane);
        // Добавляем вкладку в панель вкладок
        TabPaneMain.getTabs().add(newTab);
        // Заполняем списки всех созданных компонентов
        // для дальнейшего обращения к ним по индексу
        _tabList.add(newTab);
        _anchorPaneList.add(newPane);
        _textAreaList.add(newTextArea);
        _pathsSave.add("");
        // Выделяем последний таб-элемент
        TabPaneMain.getSelectionModel().selectLast();
    }

    /** Открытие файла */
    public void openFile() {
        // Настройка диалога
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл, который хотите прочесть, как текстовый");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        // Показываем диалог
        File selectedFile = fileChooser.showOpenDialog(VBoxMain.getScene().getWindow());
        // Если диалог подтверждён
        if (selectedFile != null)  {
            // Создаём новый документ
            createNewDoc();
            // Получаем путь
            String path = selectedFile.getAbsolutePath();
            // Сохраняем путь по выбранному индексу
            _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), path);
            // Читаем файл
            readFile(path);
            // Меняем тайтл вкладки
            TabPaneMain.getSelectionModel().getSelectedItem().setText(selectedFile.getName());
            // Меняем статус
            LabelStatus.setText(String.format("Открыт файл %s", path));
            // Заполняем меню последних файлов
            _pathsSave.forEach(p -> {
                if (!p.equals("")) {
                    var menuItem = new MenuItem(p);
                    menuItem.setOnAction(event -> {
                        var file = new File(p);
                        // Создаём новый документ
                        createNewDoc();
                        // Грузим файл
                        _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), file.getAbsolutePath());
                        readFile(file.getAbsolutePath());
                        TabPaneMain.getSelectionModel().getSelectedItem().setText(file.getName());
                    });
                    MenuLastFiles.getItems().add(menuItem);
                } // if
            });
        } // if
    }

    /** Чтение файла построчно */
    private void readFile(String path) {
        try (var reader = new FileReader(path)) {
            var bufReader = new BufferedReader(reader);
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                text.append(line).append("\n");
            }
            _textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).setText(text.toString());
            LabelStatus.setText(String.format("Открыт файл %s", path));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Ошибка чтения");
            alert.setHeaderText("Невозможно прочитать данный файл");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            LabelStatus.setText("Ошибка чтения");
        }
    }

    /** Сохранить */
    public void save() {
        if (_pathsSave.get(TabPaneMain.getSelectionModel().getSelectedIndex()).equals("")) {
            openSaveDialog();
        } else {
            writeFile();
        }
    }

    /** Запись файла */
    private void writeFile() {
        try (var writer = new FileWriter(_pathsSave.get(TabPaneMain.getSelectionModel().getSelectedIndex()), false)) {
            writer.write(_textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).getText());
            writer.flush();
            LabelStatus.setText("Сохранено");
        } catch (IOException ex) {
            ShowWriteError(ex);
        }
    }

    /** Запись файла по указанному пути */
    private void writeFile(String fileName) {
        try (var writer = new FileWriter(fileName, false)) {
            writer.write(_textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).getText());
            writer.flush();
            LabelStatus.setText(String.format("Сохранён файл %s", fileName));
        } catch (IOException ex) {
            ShowWriteError(ex);
        }
    }

    /** Показать ошибку записи */
    private void ShowWriteError(IOException ex) {
        System.out.println(ex.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Ошибка записи");
        alert.setHeaderText("Невозможно записать данный файл");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
        LabelStatus.setText("Ошибка записи");
    }

    /** Сохранить как */
    public void openSaveDialog() {
        // Настройка диалога
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*.*"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        // Показываем диалог
        File selectedFile = fileChooser.showSaveDialog(VBoxMain.getScene().getWindow());
        // Если диалог подтверждён
        if (selectedFile != null) {
            // Сохраняем путь по выбранному индексу
            _pathsSave.set(TabPaneMain.getSelectionModel().getSelectedIndex(), selectedFile.getAbsolutePath());
            // Сохраняем
            writeFile(selectedFile.getAbsolutePath());
            // Меняем тайтл выбранной вкладки
            TabPaneMain.getSelectionModel().getSelectedItem().setText(selectedFile.getName());
            // Меняем статус
            LabelStatus.setText(String.format("Сохранено как %s", selectedFile.getAbsolutePath()));
        }
    }

    /** Закрыть документ */
    public void closeDoc() {
        var index = TabPaneMain.getSelectionModel().getSelectedIndex();
        if (_anchorPaneList.size() != 1) {
            TabPaneMain.getTabs().remove(index);
            _anchorPaneList.remove(index);
            _pathsSave.remove(index);
            _tabList.remove(index);
            _textAreaList.remove(index);
            TabPaneMain.getSelectionModel().selectLast();
            LabelStatus.setText("Документ закрыт");
        } else {
            TabPaneMain.getSelectionModel().getSelectedItem().setText("Новый");
            _textAreaList.get(TabPaneMain.getSelectionModel().getSelectedIndex()).setText("");
            LabelStatus.setText("Документ закрыт");
        }
    }

    /** Выход */
    public void exit() {
        System.exit(0);
    }

    /** О программе */
    public void about() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("О программе");
        alert.setHeaderText("Простой текстовый редактор с возможностью чтения и сохранения файлов,\n" +
                "а также удобной навигацией в виде вкладок.");
        alert.setContentText("Написанный с помощью технологии Java FX.\n\nCopyright (c) Алексей Юрьевич Бурьянов, 2018.");
        alert.showAndWait();
    }
} // MainController