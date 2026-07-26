package com.example.portfoliomanagement;

import com.example.portfoliomanagement.persistence.Instrument;
import com.example.portfoliomanagement.persistence.InstrumentPrice;
import com.example.portfoliomanagement.persistence.InstrumentPriceRepository;
import com.example.portfoliomanagement.persistence.InstrumentRepository;
import com.example.portfoliomanagement.persistence.InstrumentList;
import com.example.portfoliomanagement.persistence.InstrumentListRepository;
import com.example.portfoliomanagement.marketdata.HistoricalPriceProvider;
import com.example.portfoliomanagement.marketdata.YahooFinanceHistoricalPriceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainWindowController {
    private static final DataFormat INSTRUMENT_ID_DATA_FORMAT =
            new DataFormat("com.example.portfoliomanagement.instrument-id");
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class.getName());

    private final InstrumentListRepository instrumentListRepository = new InstrumentListRepository();
    private final InstrumentRepository instrumentRepository = new InstrumentRepository();
    private final InstrumentPriceRepository instrumentPriceRepository = new InstrumentPriceRepository();
    private final HistoricalPriceProvider historicalPriceProvider = new YahooFinanceHistoricalPriceProvider();
    private final ObservableList<InstrumentList> securitiesLists = FXCollections.observableArrayList();
    private final ToggleGroup securitiesMenuGroup = new ToggleGroup();
    private InstrumentList activeInstrumentList;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private ToggleButton allSecuritiesMenuItem;

    @FXML
    private VBox instrumentListsMenuItems;

    @FXML
    private Button addInstrumentButton;

    @FXML
    private SplitPane tableChartSplitPane;

    @FXML
    private Label tableTitleLabel;

    @FXML
    private TableView<SecurityRow> securitiesTable;

    @FXML
    private Label priceChartTitleLabel;

    @FXML
    private LineChart<String, Number> priceChart;

    @FXML
    private TableColumn<SecurityRow, String> nameColumn;

    @FXML
    private TableColumn<SecurityRow, String> symbolColumn;

    @FXML
    private TableColumn<SecurityRow, String> isinColumn;

    @FXML
    private TableColumn<SecurityRow, String> latestColumn;

    @FXML
    private TableColumn<SecurityRow, String> currencyColumn;

    @FXML
    private void initialize() {
        allSecuritiesMenuItem.setToggleGroup(securitiesMenuGroup);

        securitiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        symbolColumn.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        isinColumn.setCellValueFactory(cellData -> cellData.getValue().isinProperty());
        latestColumn.setCellValueFactory(cellData -> cellData.getValue().latestProperty());
        currencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());

        securitiesTable.setItems(FXCollections.observableArrayList());
        securitiesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> updatePriceChart(newSelection));
        configureInstrumentRows();
        styleTableChartDivider();
        loadInstrumentLists();
        showAllSecurities();
    }

    @FXML
    private void showAllSecurities() {
        activeInstrumentList = null;
        activateMenuItem(allSecuritiesMenuItem);
        showContent(securitiesTable);
        tableTitleLabel.setText("All Securities");
        addInstrumentButton.setDisable(false);
        loadAllInstruments();
        selectFirstSecurity();
    }

    @FXML
    private void promptForSecuritiesListName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Securities List");
        dialog.setHeaderText("New securities list");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.map(String::trim)
                .filter(name -> !name.isEmpty())
                .ifPresent(this::createSecuritiesList);
    }

    private void createSecuritiesList(String name) {
        try {
            InstrumentList instrumentList = instrumentListRepository.save(name);
            securitiesLists.add(instrumentList);
            instrumentListsMenuItems.getChildren().add(createInstrumentListMenuItem(instrumentList));
        } catch (RuntimeException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could Not Add List");
            alert.setHeaderText("The securities list was not saved.");
            alert.setContentText("Check that the name is unique and try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void promptForInstrument() {
        createInstrumentDialog()
                .showAndWait()
                .ifPresent(this::createInstrument);
    }

    private void loadInstrumentLists() {
        securitiesLists.setAll(instrumentListRepository.findAll());
        instrumentListsMenuItems.getChildren().setAll(
                securitiesLists.stream()
                        .map(this::createInstrumentListMenuItem)
                        .toList());
    }

    private ToggleButton createInstrumentListMenuItem(InstrumentList instrumentList) {
        ToggleButton menuItem = new ToggleButton(instrumentList.getName());
        menuItem.setMaxWidth(Double.MAX_VALUE);
        menuItem.setMnemonicParsing(false);
        menuItem.getStyleClass().addAll("menu-entry", "instrument-list-entry");
        menuItem.setToggleGroup(securitiesMenuGroup);
        menuItem.setOnAction(event -> showInstrumentList(instrumentList, menuItem));
        configureInstrumentDropTarget(menuItem, instrumentList);
        return menuItem;
    }

    private void showInstrumentList(InstrumentList instrumentList, ToggleButton menuItem) {
        activeInstrumentList = instrumentList;
        activateMenuItem(menuItem);
        showContent(securitiesTable);
        tableTitleLabel.setText(instrumentList.getName());
        addInstrumentButton.setDisable(false);
        loadInstrumentsForList(instrumentList);
        selectFirstSecurity();
    }

    private void activateMenuItem(ToggleButton menuItem) {
        menuItem.setSelected(true);
    }

    private void showContent(Node content) {
        securitiesTable.setVisible(securitiesTable == content);
        securitiesTable.setManaged(securitiesTable == content);
    }

    private void loadAllInstruments() {
        securitiesTable.setItems(toSecurityRows(instrumentRepository.findAll()));
        updatePriceChart(securitiesTable.getSelectionModel().getSelectedItem());
    }

    private void loadInstrumentsForList(InstrumentList instrumentList) {
        securitiesTable.setItems(toSecurityRows(instrumentRepository.findByListId(instrumentList.getId())));
        updatePriceChart(securitiesTable.getSelectionModel().getSelectedItem());
    }

    private void selectFirstSecurity() {
        if (securitiesTable.getItems().isEmpty()) {
            securitiesTable.getSelectionModel().clearSelection();
            updatePriceChart(null);
            return;
        }

        securitiesTable.getSelectionModel().selectFirst();
    }

    private ObservableList<SecurityRow> toSecurityRows(List<Instrument> instruments) {
        return FXCollections.observableArrayList(
                instruments.stream()
                        .map(instrument -> new SecurityRow(
                                instrument.getId(),
                                instrument.getName(),
                                instrument.getSymbol(),
                                instrument.getIsin(),
                                instrument.getLatest() == null ? "" : instrument.getLatest().toPlainString(),
                                instrument.getCurrency()))
                        .toList());
    }

    private Dialog<InstrumentInput> createInstrumentDialog() {
        Dialog<InstrumentInput> dialog = new Dialog<>();
        dialog.setTitle("Add Instrument");
        dialog.setHeaderText(activeInstrumentList == null
                ? "New instrument"
                : "New instrument for " + activeInstrumentList.getName());

        TextField nameField = new TextField();
        TextField symbolField = new TextField();
        TextField isinField = new TextField();
        TextField latestField = new TextField();
        TextField currencyField = new TextField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(0, new Label("Name:"), nameField);
        form.addRow(1, new Label("Symbol:"), symbolField);
        form.addRow(2, new Label("ISIN:"), isinField);
        form.addRow(3, new Label("Latest:"), latestField);
        form.addRow(4, new Label("Currency:"), currencyField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            return new InstrumentInput(
                    nameField.getText().trim(),
                    symbolField.getText().trim(),
                    isinField.getText().trim(),
                    latestField.getText().trim(),
                    currencyField.getText().trim());
        });
        return dialog;
    }

    private void createInstrument(InstrumentInput input) {
        if (input.name().isEmpty()) {
            showInstrumentSaveError("The instrument name is required.");
            return;
        }

        try {
            Instrument instrument = saveInstrument(input);
            SecurityRow securityRow = new SecurityRow(
                    instrument.getId(),
                    instrument.getName(),
                    instrument.getSymbol(),
                    instrument.getIsin(),
                    instrument.getLatest() == null ? "" : instrument.getLatest().toPlainString(),
                    instrument.getCurrency());
            securitiesTable.getItems().add(securityRow);
            securitiesTable.getSelectionModel().select(securityRow);
            importHistoricalPrices(instrument);
        } catch (NumberFormatException exception) {
            showInstrumentSaveError("Latest must be a valid number.");
        } catch (RuntimeException exception) {
            showInstrumentSaveError("Check that the ISIN is unique and try again.");
        }
    }

    private Instrument saveInstrument(InstrumentInput input) {
        if (activeInstrumentList == null) {
            return instrumentRepository.save(
                    input.name(),
                    emptyToNull(input.symbol()),
                    emptyToNull(input.isin()),
                    parseLatest(input.latest()),
                    emptyToNull(input.currency()));
        }

        return instrumentRepository.saveToList(
                activeInstrumentList.getId(),
                input.name(),
                emptyToNull(input.symbol()),
                emptyToNull(input.isin()),
                parseLatest(input.latest()),
                emptyToNull(input.currency()));
    }

    private BigDecimal parseLatest(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private void showInstrumentSaveError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add Instrument");
        alert.setHeaderText("The instrument was not saved.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void configureInstrumentRows() {
        securitiesTable.setRowFactory(tableView -> {
            TableRow<SecurityRow> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (row.isEmpty()) {
                    return;
                }

                SecurityRow securityRow = row.getItem();
                ClipboardContent content = new ClipboardContent();
                content.put(INSTRUMENT_ID_DATA_FORMAT, securityRow.id().toString());
                row.startDragAndDrop(TransferMode.COPY).setContent(content);
                event.consume();
            });
            row.emptyProperty().addListener((observable, wasEmpty, isEmpty) ->
                    row.setContextMenu(isEmpty ? null : createSecurityRowContextMenu(row)));
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    row.setContextMenu(createSecurityRowContextMenu(row));
                }
            });
            return row;
        });
    }

    private ContextMenu createSecurityRowContextMenu(TableRow<SecurityRow> row) {
        MenuItem removeFromListItem = new MenuItem(removeFromListText());
        removeFromListItem.setDisable(activeInstrumentList == null);
        removeFromListItem.setOnAction(event -> removeInstrumentFromCurrentList(row.getItem()));

        MenuItem deleteSecurityItem = new MenuItem("delete security");
        deleteSecurityItem.setOnAction(event -> deleteInstrument(row.getItem()));

        return new ContextMenu(removeFromListItem, deleteSecurityItem);
    }

    private String removeFromListText() {
        if (activeInstrumentList == null) {
            return "Remove from All Securities";
        }
        return "Remove from " + activeInstrumentList.getName();
    }

    private void removeInstrumentFromCurrentList(SecurityRow securityRow) {
        if (securityRow == null || activeInstrumentList == null) {
            return;
        }

        try {
            instrumentRepository.removeFromList(securityRow.id(), activeInstrumentList.getId());
            securitiesTable.getItems().remove(securityRow);
        } catch (RuntimeException exception) {
            showInstrumentActionError("The instrument was not removed from the list.");
        }
    }

    private void deleteInstrument(SecurityRow securityRow) {
        if (securityRow == null) {
            return;
        }

        try {
            instrumentRepository.delete(securityRow.id());
            securitiesTable.getItems().remove(securityRow);
        } catch (RuntimeException exception) {
            showInstrumentActionError("The security was not deleted.");
        }
    }

    private void showInstrumentActionError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Update Security");
        alert.setHeaderText(message);
        alert.setContentText("Please try again.");
        alert.showAndWait();
    }

    private void updatePriceChart(SecurityRow securityRow) {
        priceChart.getData().clear();
        priceChartTitleLabel.setText(securityRow == null
                ? "Price Chart"
                : "Price Chart - " + securityRow.nameProperty().get());

        if (securityRow == null) {
            return;
        }

        List<InstrumentPrice> prices = instrumentPriceRepository.findByInstrumentId(securityRow.id());
        int fromIndex = Math.max(0, prices.size() - 250);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        prices.subList(fromIndex, prices.size()).forEach(price ->
                series.getData().add(new XYChart.Data<>(
                        price.getPriceDate().toString(),
                        price.getClosePrice())));
        priceChart.getData().add(series);
    }

    private void importHistoricalPrices(Instrument instrument) {
        if (instrument.getSymbol() == null || instrument.getSymbol().isBlank()) {
            return;
        }

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusYears(1);
        CompletableFuture.runAsync(() -> {
            try {
                instrumentPriceRepository.saveAll(
                        instrument.getId(),
                        historicalPriceProvider.loadDailyPrices(instrument.getSymbol(), from, to));
                Platform.runLater(() -> {
                    SecurityRow selectedSecurity = securitiesTable.getSelectionModel().getSelectedItem();
                    if (selectedSecurity != null && selectedSecurity.id().equals(instrument.getId())) {
                        updatePriceChart(selectedSecurity);
                    }
                });
            } catch (Exception exception) {
                LOGGER.log(
                        Level.WARNING,
                        "Failed to load historical prices for instrument id "
                                + instrument.getId()
                                + " with symbol "
                                + instrument.getSymbol(),
                        exception);
                Platform.runLater(() -> showInstrumentActionError(
                        "The security was saved, but historical prices could not be loaded."));
            }
        });
    }

    private void configureInstrumentDropTarget(ToggleButton menuItem, InstrumentList instrumentList) {
        menuItem.setOnDragOver(event -> {
            if (event.getDragboard().hasContent(INSTRUMENT_ID_DATA_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
                menuItem.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drag-over"), true);
            }
            event.consume();
        });
        menuItem.setOnDragExited(event -> {
            menuItem.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drag-over"), false);
            event.consume();
        });
        menuItem.setOnDragDropped(event -> {
            boolean success = false;
            Object instrumentId = event.getDragboard().getContent(INSTRUMENT_ID_DATA_FORMAT);
            if (instrumentId instanceof String instrumentIdValue) {
                success = addInstrumentToList(Long.parseLong(instrumentIdValue), instrumentList);
            }

            menuItem.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drag-over"), false);
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean addInstrumentToList(Long instrumentId, InstrumentList instrumentList) {
        try {
            instrumentRepository.addToList(instrumentId, instrumentList.getId());
            if (activeInstrumentList != null && activeInstrumentList.getId().equals(instrumentList.getId())) {
                loadInstrumentsForList(instrumentList);
            }
            return true;
        } catch (RuntimeException exception) {
            showInstrumentListLinkError();
            return false;
        }
    }

    private void showInstrumentListLinkError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add Instrument");
        alert.setHeaderText("The instrument was not added to the list.");
        alert.setContentText("It may already be in that list.");
        alert.showAndWait();
    }

    private void styleTableChartDivider() {
        Platform.runLater(() -> tableChartSplitPane.lookupAll(".split-pane-divider").forEach(divider -> {
            divider.setStyle("-fx-background-color: #4b5563; -fx-padding: 8 0 8 0;");
            if (divider instanceof StackPane stackPane && stackPane.getChildren().stream()
                    .noneMatch(child -> child.getStyleClass().contains("chart-divider-handle"))) {
                stackPane.setMinHeight(16);
                stackPane.setPrefHeight(16);

                Region handle = new Region();
                handle.getStyleClass().add("chart-divider-handle");
                handle.setMouseTransparent(true);
                stackPane.getChildren().add(handle);
            }
        }));
    }

    private record InstrumentInput(String name, String symbol, String isin, String latest, String currency) {
    }

    public static class SecurityRow {
        private final Long id;
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty symbol = new SimpleStringProperty();
        private final StringProperty isin = new SimpleStringProperty();
        private final StringProperty latest = new SimpleStringProperty();
        private final StringProperty currency = new SimpleStringProperty();

        public SecurityRow(Long id, String name, String symbol, String isin, String latest, String currency) {
            this.id = id;
            this.name.set(name);
            this.symbol.set(symbol);
            this.isin.set(isin);
            this.latest.set(latest);
            this.currency.set(currency);
        }

        public Long id() {
            return id;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty symbolProperty() {
            return symbol;
        }

        public StringProperty isinProperty() {
            return isin;
        }

        public StringProperty latestProperty() {
            return latest;
        }

        public StringProperty currencyProperty() {
            return currency;
        }
    }
}
