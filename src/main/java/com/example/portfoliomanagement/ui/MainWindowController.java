package com.example.portfoliomanagement.ui;

import com.example.portfoliomanagement.calculation.CashBalanceCalculator;
import com.example.portfoliomanagement.persistence.Instrument;
import com.example.portfoliomanagement.persistence.InstrumentPrice;
import com.example.portfoliomanagement.persistence.InstrumentPriceRepository;
import com.example.portfoliomanagement.persistence.InstrumentRepository;
import com.example.portfoliomanagement.persistence.InstrumentList;
import com.example.portfoliomanagement.persistence.InstrumentListRepository;
import com.example.portfoliomanagement.persistence.PortfolioTransaction;
import com.example.portfoliomanagement.persistence.PortfolioTransactionRepository;
import com.example.portfoliomanagement.persistence.CashAccount;
import com.example.portfoliomanagement.persistence.CashAccountRepository;
import com.example.portfoliomanagement.persistence.SecurityAccount;
import com.example.portfoliomanagement.persistence.SecurityAccountRepository;
import com.example.portfoliomanagement.persistence.TransactionType;
import com.example.portfoliomanagement.service.MarketDataService;
import com.example.portfoliomanagement.service.TransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.StringConverter;

public class MainWindowController {
    private static final DataFormat INSTRUMENT_ID_DATA_FORMAT =
            new DataFormat("com.example.portfoliomanagement.instrument-id");
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class.getName());
    private static final DateTimeFormatter AXIS_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    private final InstrumentListRepository instrumentListRepository = new InstrumentListRepository();
    private final InstrumentRepository instrumentRepository = new InstrumentRepository();
    private final InstrumentPriceRepository instrumentPriceRepository = new InstrumentPriceRepository();
    private final PortfolioTransactionRepository portfolioTransactionRepository = new PortfolioTransactionRepository();
    private final CashAccountRepository cashAccountRepository = new CashAccountRepository();
    private final SecurityAccountRepository securityAccountRepository = new SecurityAccountRepository();
    private final TransactionService transactionService = new TransactionService();
    private final MarketDataService marketDataService = new MarketDataService();
    private final CashBalanceCalculator cashBalanceCalculator = new CashBalanceCalculator();
    private final ObservableList<InstrumentList> securitiesLists = FXCollections.observableArrayList();
    private final ToggleGroup securitiesMenuGroup = new ToggleGroup();
    private final ToggleGroup chartRangeGroup = new ToggleGroup();
    private InstrumentList activeInstrumentList;
    private MainView activeView = MainView.SECURITIES;
    private ChartRange activeChartRange = ChartRange.ONE_YEAR;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private ToggleButton allSecuritiesMenuItem;

    @FXML
    private ToggleButton securityAccountsMenuItem;

    @FXML
    private ToggleButton cashAccountsMenuItem;

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
    private TableView<SecurityAccountRow> securityAccountsTable;

    @FXML
    private TableView<CashAccountRow> cashAccountsTable;

    @FXML
    private TableView<SecurityTransactionRow> securityTransactionsTable;

    @FXML
    private TableView<AccountTransactionRow> accountTransactionsTable;

    @FXML
    private TableView<SecurityAccountTransactionRow> securityAccountTransactionsTable;

    @FXML
    private VBox chartDetailSection;

    @FXML
    private VBox accountDetailSection;

    @FXML
    private Label priceChartTitleLabel;

    @FXML
    private Label accountDetailTitleLabel;

    @FXML
    private HBox chartRangeButtons;

    @FXML
    private LineChart<Number, Number> priceChart;

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
    private TableColumn<SecurityAccountRow, String> securityAccountNameColumn;

    @FXML
    private TableColumn<SecurityAccountRow, String> securityAccountNoteColumn;

    @FXML
    private TableColumn<CashAccountRow, String> cashAccountNameColumn;

    @FXML
    private TableColumn<CashAccountRow, String> cashAccountCurrencyColumn;

    @FXML
    private TableColumn<CashAccountRow, String> cashAccountBalanceColumn;

    @FXML
    private TableColumn<CashAccountRow, String> cashAccountNoteColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionDateColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionTypeColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionSharesColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionAmountColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionCurrencyColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionFeesColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionTaxesColumn;

    @FXML
    private TableColumn<SecurityTransactionRow, String> securityTransactionNoteColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionDateColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionTypeColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionAmountColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionCurrencyColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionSourceColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionTargetColumn;

    @FXML
    private TableColumn<AccountTransactionRow, String> accountTransactionNoteColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionDateColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionTypeColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionInstrumentColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionSharesColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionAmountColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionCurrencyColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionCashAccountColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionFeesColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionTaxesColumn;

    @FXML
    private TableColumn<SecurityAccountTransactionRow, String> securityAccountTransactionNoteColumn;

    @FXML
    private void initialize() {
        allSecuritiesMenuItem.setToggleGroup(securitiesMenuGroup);
        securityAccountsMenuItem.setToggleGroup(securitiesMenuGroup);
        cashAccountsMenuItem.setToggleGroup(securitiesMenuGroup);

        securitiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        securityAccountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        cashAccountsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        securityTransactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        accountTransactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        securityAccountTransactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        symbolColumn.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        isinColumn.setCellValueFactory(cellData -> cellData.getValue().isinProperty());
        latestColumn.setCellValueFactory(cellData -> cellData.getValue().latestProperty());
        currencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());
        securityAccountNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        securityAccountNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        cashAccountNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        cashAccountCurrencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());
        cashAccountBalanceColumn.setCellValueFactory(cellData -> cellData.getValue().balanceProperty());
        cashAccountNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        securityTransactionDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        securityTransactionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        securityTransactionSharesColumn.setCellValueFactory(cellData -> cellData.getValue().sharesProperty());
        securityTransactionAmountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        securityTransactionCurrencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());
        securityTransactionFeesColumn.setCellValueFactory(cellData -> cellData.getValue().feesProperty());
        securityTransactionTaxesColumn.setCellValueFactory(cellData -> cellData.getValue().taxesProperty());
        securityTransactionNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        accountTransactionDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        accountTransactionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        accountTransactionAmountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        accountTransactionCurrencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());
        accountTransactionSourceColumn.setCellValueFactory(cellData -> cellData.getValue().sourceAccountProperty());
        accountTransactionTargetColumn.setCellValueFactory(cellData -> cellData.getValue().targetAccountProperty());
        accountTransactionNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        securityAccountTransactionDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        securityAccountTransactionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        securityAccountTransactionInstrumentColumn.setCellValueFactory(cellData -> cellData.getValue().instrumentProperty());
        securityAccountTransactionSharesColumn.setCellValueFactory(cellData -> cellData.getValue().sharesProperty());
        securityAccountTransactionAmountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        securityAccountTransactionCurrencyColumn.setCellValueFactory(cellData -> cellData.getValue().currencyProperty());
        securityAccountTransactionCashAccountColumn.setCellValueFactory(cellData -> cellData.getValue().cashAccountProperty());
        securityAccountTransactionFeesColumn.setCellValueFactory(cellData -> cellData.getValue().feesProperty());
        securityAccountTransactionTaxesColumn.setCellValueFactory(cellData -> cellData.getValue().taxesProperty());
        securityAccountTransactionNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());

        securitiesTable.setItems(FXCollections.observableArrayList());
        securityAccountsTable.setItems(FXCollections.observableArrayList());
        cashAccountsTable.setItems(FXCollections.observableArrayList());
        securityTransactionsTable.setItems(FXCollections.observableArrayList());
        accountTransactionsTable.setItems(FXCollections.observableArrayList());
        securityAccountTransactionsTable.setItems(FXCollections.observableArrayList());
        securitiesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> updateSecurityDetails(newSelection));
        securityAccountsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> updateSecurityAccountDetails(newSelection));
        cashAccountsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldSelection, newSelection) -> updateCashAccountDetails(newSelection));
        configurePriceChart();
        configureChartRangeButtons();
        configureInstrumentRows();
        configureCashAccountRows();
        styleTableChartDivider();
        loadInstrumentLists();
        showAllSecurities();
    }

    @FXML
    private void showAllSecurities() {
        activeInstrumentList = null;
        activateMenuItem(allSecuritiesMenuItem);
        showContent(securitiesTable);
        showDetailSection(chartDetailSection);
        tableTitleLabel.setText("All Securities");
        activeView = MainView.SECURITIES;
        addInstrumentButton.setDisable(false);
        loadAllInstruments();
        selectFirstSecurity();
    }

    @FXML
    private void showSecurityAccounts() {
        activeInstrumentList = null;
        activateMenuItem(securityAccountsMenuItem);
        showContent(securityAccountsTable);
        showDetailSection(accountDetailSection);
        tableTitleLabel.setText("Security Accounts");
        activeView = MainView.SECURITY_ACCOUNTS;
        addInstrumentButton.setDisable(false);
        loadSecurityAccounts();
    }

    @FXML
    private void showCashAccounts() {
        activeInstrumentList = null;
        activateMenuItem(cashAccountsMenuItem);
        showContent(cashAccountsTable);
        showDetailSection(accountDetailSection);
        tableTitleLabel.setText("Cash Accounts");
        activeView = MainView.CASH_ACCOUNTS;
        addInstrumentButton.setDisable(false);
        loadCashAccounts();
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
        switch (activeView) {
            case SECURITIES -> createInstrumentDialog()
                    .showAndWait()
                    .ifPresent(this::createInstrument);
            case SECURITY_ACCOUNTS -> createSecurityAccountDialog()
                    .showAndWait()
                    .ifPresent(this::createSecurityAccount);
            case CASH_ACCOUNTS -> createCashAccountDialog()
                    .showAndWait()
                    .ifPresent(this::createCashAccount);
        }
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
        showDetailSection(chartDetailSection);
        tableTitleLabel.setText(instrumentList.getName());
        activeView = MainView.SECURITIES;
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
        securityAccountsTable.setVisible(securityAccountsTable == content);
        securityAccountsTable.setManaged(securityAccountsTable == content);
        cashAccountsTable.setVisible(cashAccountsTable == content);
        cashAccountsTable.setManaged(cashAccountsTable == content);
    }

    private void showDetailSection(Node visibleSection) {
        chartDetailSection.setVisible(chartDetailSection == visibleSection);
        chartDetailSection.setManaged(chartDetailSection == visibleSection);
        accountDetailSection.setVisible(accountDetailSection == visibleSection);
        accountDetailSection.setManaged(accountDetailSection == visibleSection);
    }

    private void loadAllInstruments() {
        securitiesTable.setItems(toSecurityRows(instrumentRepository.findAll()));
        updateSecurityDetails(securitiesTable.getSelectionModel().getSelectedItem());
    }

    private void loadInstrumentsForList(InstrumentList instrumentList) {
        securitiesTable.setItems(toSecurityRows(instrumentRepository.findByListId(instrumentList.getId())));
        updateSecurityDetails(securitiesTable.getSelectionModel().getSelectedItem());
    }

    private void selectFirstSecurity() {
        if (securitiesTable.getItems().isEmpty()) {
            securitiesTable.getSelectionModel().clearSelection();
            updateSecurityDetails(null);
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

    private void loadSecurityAccounts() {
        securityAccountsTable.setItems(FXCollections.observableArrayList(
                securityAccountRepository.findAll().stream()
                        .map(securityAccount -> new SecurityAccountRow(
                                securityAccount.getId(),
                                securityAccount.getName(),
                                securityAccount.getNote()))
                        .toList()));
        selectFirstSecurityAccount();
    }

    private void loadCashAccounts() {
        cashAccountsTable.setItems(FXCollections.observableArrayList(
                cashAccountRepository.findAll().stream()
                        .map(cashAccount -> new CashAccountRow(
                                cashAccount.getId(),
                                cashAccount.getName(),
                                cashAccount.getCurrency(),
                                decimalToString(cashBalanceCalculator.calculate(
                                        cashAccount.getId(),
                                        portfolioTransactionRepository.findByCashAccountId(cashAccount.getId()))),
                                cashAccount.getNote()))
                        .toList()));
        selectFirstCashAccount();
    }

    private void selectFirstSecurityAccount() {
        if (securityAccountsTable.getItems().isEmpty()) {
            securityAccountsTable.getSelectionModel().clearSelection();
            updateSecurityAccountDetails(null);
            return;
        }

        securityAccountsTable.getSelectionModel().selectFirst();
    }

    private void selectFirstCashAccount() {
        if (cashAccountsTable.getItems().isEmpty()) {
            cashAccountsTable.getSelectionModel().clearSelection();
            updateCashAccountDetails(null);
            return;
        }

        cashAccountsTable.getSelectionModel().selectFirst();
    }

    private void selectCashAccount(Long cashAccountId) {
        cashAccountsTable.getItems().stream()
                .filter(row -> row.id().equals(cashAccountId))
                .findFirst()
                .ifPresentOrElse(
                        row -> cashAccountsTable.getSelectionModel().select(row),
                        this::selectFirstCashAccount);
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

    private Dialog<SecurityAccountInput> createSecurityAccountDialog() {
        Dialog<SecurityAccountInput> dialog = new Dialog<>();
        dialog.setTitle("Add Security Account");
        dialog.setHeaderText("New security account");

        TextField nameField = new TextField();
        TextArea noteField = new TextArea();
        noteField.setPrefRowCount(4);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(0, new Label("Name:"), nameField);
        form.addRow(1, new Label("Note:"), noteField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            return new SecurityAccountInput(
                    nameField.getText().trim(),
                    noteField.getText().trim());
        });
        return dialog;
    }

    private Dialog<CashAccountInput> createCashAccountDialog() {
        Dialog<CashAccountInput> dialog = new Dialog<>();
        dialog.setTitle("Add Cash Account");
        dialog.setHeaderText("New cash account");

        TextField nameField = new TextField();
        TextField currencyField = new TextField();
        TextArea noteField = new TextArea();
        noteField.setPrefRowCount(4);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(0, new Label("Name:"), nameField);
        form.addRow(1, new Label("Currency:"), currencyField);
        form.addRow(2, new Label("Note:"), noteField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            return new CashAccountInput(
                    nameField.getText().trim(),
                    currencyField.getText().trim(),
                    noteField.getText().trim());
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

    private void createSecurityAccount(SecurityAccountInput input) {
        if (input.name().isEmpty()) {
            showAccountSaveError("The security account name is required.");
            return;
        }

        try {
            SecurityAccount securityAccount = securityAccountRepository.save(
                    input.name(),
                    emptyToNull(input.note()));
            SecurityAccountRow accountRow = new SecurityAccountRow(
                    securityAccount.getId(),
                    securityAccount.getName(),
                    securityAccount.getNote());
            securityAccountsTable.getItems().add(accountRow);
            securityAccountsTable.getSelectionModel().select(accountRow);
        } catch (RuntimeException exception) {
            showAccountSaveError("Check that the security account name is unique and try again.");
        }
    }

    private void createCashAccount(CashAccountInput input) {
        if (input.name().isEmpty()) {
            showAccountSaveError("The cash account name is required.");
            return;
        }
        if (input.currency().isEmpty()) {
            showAccountSaveError("The cash account currency is required.");
            return;
        }
        if (input.currency().length() != 3) {
            showAccountSaveError("Currency must be a 3-letter currency code.");
            return;
        }

        try {
            CashAccount cashAccount = cashAccountRepository.save(
                    input.name(),
                    input.currency().toUpperCase(),
                    emptyToNull(input.note()));
            CashAccountRow accountRow = new CashAccountRow(
                    cashAccount.getId(),
                    cashAccount.getName(),
                    cashAccount.getCurrency(),
                    decimalToString(cashBalanceCalculator.calculate(
                            cashAccount.getId(),
                            portfolioTransactionRepository.findByCashAccountId(cashAccount.getId()))),
                    cashAccount.getNote());
            cashAccountsTable.getItems().add(accountRow);
            cashAccountsTable.getSelectionModel().select(accountRow);
        } catch (RuntimeException exception) {
            showAccountSaveError("Check that the cash account name is unique and try again.");
        }
    }

    private void promptForSecurityTransaction(TransactionType type, SecurityRow securityRow) {
        if (securityRow == null) {
            return;
        }

        createSecurityTransactionDialog(type, securityRow)
                .showAndWait()
                .ifPresent(input -> createSecurityTransaction(type, securityRow, input));
    }

    private Dialog<SecurityTransactionInput> createSecurityTransactionDialog(
            TransactionType type,
            SecurityRow securityRow) {
        Dialog<SecurityTransactionInput> dialog = new Dialog<>();
        dialog.setTitle(transactionTitle(type));
        dialog.setHeaderText(transactionTitle(type) + " - " + securityTitle(securityRow));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField sharesField = new TextField();
        TextField amountField = new TextField();
        TextField currencyField = new TextField(securityRow.currencyProperty().get());
        TextField feesField = new TextField("0");
        TextField taxesField = new TextField("0");
        TextArea noteField = new TextArea();
        noteField.setPrefRowCount(3);
        ComboBox<EntityChoice> securityAccountField = new ComboBox<>(toSecurityAccountChoices());
        ComboBox<EntityChoice> cashAccountField = new ComboBox<>(toCashAccountChoices());
        securityAccountField.getSelectionModel().selectFirst();
        cashAccountField.getSelectionModel().selectFirst();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(0, new Label("Date:"), datePicker);
        if (type != TransactionType.DIVIDEND) {
            form.addRow(1, new Label("Shares:"), sharesField);
        }
        form.addRow(2, new Label("Amount:"), amountField);
        form.addRow(3, new Label("Currency:"), currencyField);
        form.addRow(4, new Label("Security Account:"), securityAccountField);
        form.addRow(5, new Label("Cash Account:"), cashAccountField);
        if (type != TransactionType.DIVIDEND) {
            form.addRow(6, new Label("Fees:"), feesField);
        }
        form.addRow(7, new Label("Taxes:"), taxesField);
        form.addRow(8, new Label("Note:"), noteField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            return new SecurityTransactionInput(
                    datePicker.getValue(),
                    sharesField.getText().trim(),
                    amountField.getText().trim(),
                    currencyField.getText().trim(),
                    selectedId(securityAccountField),
                    selectedId(cashAccountField),
                    feesField.getText().trim(),
                    taxesField.getText().trim(),
                    noteField.getText().trim());
        });
        return dialog;
    }

    private void createSecurityTransaction(
            TransactionType type,
            SecurityRow securityRow,
            SecurityTransactionInput input) {
        if (input.transactionDate() == null) {
            showTransactionSaveError("The transaction date is required.");
            return;
        }
        if (input.currency().isEmpty()) {
            showTransactionSaveError("The transaction currency is required.");
            return;
        }

        try {
            BigDecimal amount = parseRequiredDecimal(input.amount());
            BigDecimal taxes = parseOptionalDecimal(input.taxes());
            if (type == TransactionType.DIVIDEND) {
                transactionService.dividend(
                        securityRow.id(),
                        input.securityAccountId(),
                        input.cashAccountId(),
                        input.transactionDate(),
                        amount,
                        input.currency().toUpperCase(),
                        taxes,
                        emptyToNull(input.note()));
                updateSecurityTransactions(securityRow);
                return;
            }

            BigDecimal shares = parseRequiredDecimal(input.shares());
            BigDecimal fees = parseOptionalDecimal(input.fees());
            if (type == TransactionType.BUY) {
                transactionService.buy(
                        securityRow.id(),
                        input.securityAccountId(),
                        input.cashAccountId(),
                        input.transactionDate(),
                        shares,
                        amount,
                        input.currency().toUpperCase(),
                        fees,
                        taxes,
                        emptyToNull(input.note()));
            } else if (type == TransactionType.SELL) {
                transactionService.sell(
                        securityRow.id(),
                        input.securityAccountId(),
                        input.cashAccountId(),
                        input.transactionDate(),
                        shares,
                        amount,
                        input.currency().toUpperCase(),
                        fees,
                        taxes,
                        emptyToNull(input.note()));
            }
            updateSecurityTransactions(securityRow);
        } catch (NumberFormatException exception) {
            showTransactionSaveError("Amount, shares, fees, and taxes must be valid numbers.");
        } catch (RuntimeException exception) {
            showTransactionSaveError("The transaction was not saved.");
        }
    }

    private void promptForCashAccountTransaction(TransactionType type, CashAccountRow cashAccountRow) {
        if (cashAccountRow == null) {
            return;
        }

        createCashAccountTransactionDialog(type, cashAccountRow)
                .showAndWait()
                .ifPresent(input -> createCashAccountTransaction(type, cashAccountRow, input));
    }

    private Dialog<CashAccountTransactionInput> createCashAccountTransactionDialog(
            TransactionType type,
            CashAccountRow cashAccountRow) {
        Dialog<CashAccountTransactionInput> dialog = new Dialog<>();
        dialog.setTitle(transactionTitle(type));
        dialog.setHeaderText(transactionTitle(type) + " - " + cashAccountRow.nameProperty().get());

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField amountField = new TextField();
        TextField currencyField = new TextField(cashAccountRow.currencyProperty().get());
        TextArea noteField = new TextArea();
        noteField.setPrefRowCount(3);
        ComboBox<EntityChoice> targetCashAccountField = new ComboBox<>(toCashAccountChoices(cashAccountRow.id()));
        targetCashAccountField.getSelectionModel().selectFirst();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 0, 0, 0));
        form.addRow(0, new Label("Date:"), datePicker);
        form.addRow(1, new Label("Amount:"), amountField);
        form.addRow(2, new Label("Currency:"), currencyField);
        if (type == TransactionType.CASH_TRANSFER) {
            form.addRow(3, new Label("Target Account:"), targetCashAccountField);
        }
        form.addRow(4, new Label("Note:"), noteField);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return null;
            }
            return new CashAccountTransactionInput(
                    datePicker.getValue(),
                    amountField.getText().trim(),
                    currencyField.getText().trim(),
                    selectedId(targetCashAccountField),
                    noteField.getText().trim());
        });
        return dialog;
    }

    private void createCashAccountTransaction(
            TransactionType type,
            CashAccountRow cashAccountRow,
            CashAccountTransactionInput input) {
        if (input.transactionDate() == null) {
            showTransactionSaveError("The transaction date is required.");
            return;
        }
        if (input.currency().isEmpty()) {
            showTransactionSaveError("The transaction currency is required.");
            return;
        }
        if (type == TransactionType.CASH_TRANSFER && input.targetCashAccountId() == null) {
            showTransactionSaveError("The target cash account is required.");
            return;
        }

        try {
            BigDecimal amount = parseRequiredDecimal(input.amount());
            if (type == TransactionType.DEPOSIT) {
                transactionService.deposit(
                        cashAccountRow.id(),
                        input.transactionDate(),
                        amount,
                        input.currency().toUpperCase(),
                        emptyToNull(input.note()));
            } else if (type == TransactionType.WITHDRAWAL) {
                transactionService.withdrawal(
                        cashAccountRow.id(),
                        input.transactionDate(),
                        amount,
                        input.currency().toUpperCase(),
                        emptyToNull(input.note()));
            } else if (type == TransactionType.CASH_TRANSFER) {
                transactionService.transfer(
                        cashAccountRow.id(),
                        input.targetCashAccountId(),
                        input.transactionDate(),
                        amount,
                        input.currency().toUpperCase(),
                        emptyToNull(input.note()));
            }
            loadCashAccounts();
            selectCashAccount(cashAccountRow.id());
        } catch (NumberFormatException exception) {
            showTransactionSaveError("Amount must be a valid number.");
        } catch (RuntimeException exception) {
            showTransactionSaveError("The transaction was not saved.");
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

    private BigDecimal parseRequiredDecimal(String value) {
        if (value.isEmpty()) {
            throw new NumberFormatException("Decimal value is required.");
        }
        return new BigDecimal(value);
    }

    private BigDecimal parseOptionalDecimal(String value) {
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private String decimalToString(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private ObservableList<EntityChoice> toCashAccountChoices() {
        return toCashAccountChoices(null);
    }

    private ObservableList<EntityChoice> toCashAccountChoices(Long excludedAccountId) {
        List<EntityChoice> choices = new java.util.ArrayList<>();
        choices.add(EntityChoice.none());
        cashAccountRepository.findAll().stream()
                .filter(account -> excludedAccountId == null || !excludedAccountId.equals(account.getId()))
                .map(account -> new EntityChoice(account.getId(), account.getName()))
                .forEach(choices::add);
        return FXCollections.observableArrayList(choices);
    }

    private ObservableList<EntityChoice> toSecurityAccountChoices() {
        List<EntityChoice> choices = new java.util.ArrayList<>();
        choices.add(EntityChoice.none());
        securityAccountRepository.findAll().stream()
                .map(account -> new EntityChoice(account.getId(), account.getName()))
                .forEach(choices::add);
        return FXCollections.observableArrayList(choices);
    }

    private Long selectedId(ComboBox<EntityChoice> comboBox) {
        EntityChoice selectedChoice = comboBox.getSelectionModel().getSelectedItem();
        return selectedChoice == null ? null : selectedChoice.id();
    }

    private String transactionTitle(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "Deposit";
            case WITHDRAWAL -> "Withdrawal";
            case CASH_TRANSFER -> "Transfer";
            case BUY -> "Buy";
            case SELL -> "Sell";
            case DIVIDEND -> "Dividend";
        };
    }

    private void showInstrumentSaveError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add Instrument");
        alert.setHeaderText("The instrument was not saved.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAccountSaveError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add Account");
        alert.setHeaderText("The account was not saved.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showTransactionSaveError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Add Transaction");
        alert.setHeaderText("The transaction was not saved.");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateSecurityAccountDetails(SecurityAccountRow accountRow) {
        if (activeView != MainView.SECURITY_ACCOUNTS) {
            return;
        }

        showAccountTransactionTable(securityAccountTransactionsTable);
        accountDetailTitleLabel.setText(accountRow == null
                ? "No account selected"
                : accountRow.nameProperty().get());
        updateSecurityAccountTransactions(accountRow);
    }

    private void updateCashAccountDetails(CashAccountRow accountRow) {
        if (activeView != MainView.CASH_ACCOUNTS) {
            return;
        }

        showAccountTransactionTable(accountTransactionsTable);
        accountDetailTitleLabel.setText(accountRow == null
                ? "No account selected"
                : accountRow.nameProperty().get());
        updateCashAccountTransactions(accountRow);
    }

    private void showAccountTransactionTable(Node visibleTable) {
        securityAccountTransactionsTable.setVisible(securityAccountTransactionsTable == visibleTable);
        securityAccountTransactionsTable.setManaged(securityAccountTransactionsTable == visibleTable);
        accountTransactionsTable.setVisible(accountTransactionsTable == visibleTable);
        accountTransactionsTable.setManaged(accountTransactionsTable == visibleTable);
    }

    private void updateSecurityAccountTransactions(SecurityAccountRow accountRow) {
        if (accountRow == null) {
            securityAccountTransactionsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        securityAccountTransactionsTable.setItems(FXCollections.observableArrayList(
                portfolioTransactionRepository.findBySecurityAccountId(accountRow.id()).stream()
                        .map(transaction -> new SecurityAccountTransactionRow(
                                transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().toString(),
                                transaction.getType() == null ? "" : transactionTitle(transaction.getType()),
                                transaction.getInstrument() == null ? "" : transaction.getInstrument().getName(),
                                decimalToString(transaction.getShares()),
                                decimalToString(transaction.getAmount()),
                                transaction.getCurrency(),
                                transaction.getCashAccount() == null ? "" : transaction.getCashAccount().getName(),
                                decimalToString(transaction.getFees()),
                                decimalToString(transaction.getTaxes()),
                                transaction.getNote()))
                        .toList()));
    }

    private void updateCashAccountTransactions(CashAccountRow accountRow) {
        if (accountRow == null) {
            accountTransactionsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        accountTransactionsTable.setItems(FXCollections.observableArrayList(
                portfolioTransactionRepository.findByCashAccountId(accountRow.id()).stream()
                        .map(transaction -> new AccountTransactionRow(
                                transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().toString(),
                                transaction.getType() == null ? "" : transactionTitle(transaction.getType()),
                                decimalToString(transaction.getAmount()),
                                transaction.getCurrency(),
                                transaction.getCashAccount() == null ? "" : transaction.getCashAccount().getName(),
                                transaction.getTargetCashAccount() == null ? "" : transaction.getTargetCashAccount().getName(),
                                transaction.getNote()))
                        .toList()));
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
        MenuItem buyItem = new MenuItem("buy");
        buyItem.setOnAction(event -> promptForSecurityTransaction(TransactionType.BUY, row.getItem()));

        MenuItem sellItem = new MenuItem("sell");
        sellItem.setOnAction(event -> promptForSecurityTransaction(TransactionType.SELL, row.getItem()));

        MenuItem dividendItem = new MenuItem("dividend");
        dividendItem.setOnAction(event -> promptForSecurityTransaction(TransactionType.DIVIDEND, row.getItem()));

        MenuItem removeFromListItem = new MenuItem(removeFromListText());
        removeFromListItem.setDisable(activeInstrumentList == null);
        removeFromListItem.setOnAction(event -> removeInstrumentFromCurrentList(row.getItem()));

        MenuItem deleteSecurityItem = new MenuItem("delete security");
        deleteSecurityItem.setOnAction(event -> deleteInstrument(row.getItem()));

        return new ContextMenu(buyItem, sellItem, dividendItem, removeFromListItem, deleteSecurityItem);
    }

    private void configureCashAccountRows() {
        cashAccountsTable.setRowFactory(tableView -> {
            TableRow<CashAccountRow> row = new TableRow<>();
            row.emptyProperty().addListener((observable, wasEmpty, isEmpty) ->
                    row.setContextMenu(isEmpty ? null : createCashAccountRowContextMenu(row)));
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    row.setContextMenu(createCashAccountRowContextMenu(row));
                }
            });
            return row;
        });
    }

    private ContextMenu createCashAccountRowContextMenu(TableRow<CashAccountRow> row) {
        MenuItem depositItem = new MenuItem("deposit");
        depositItem.setOnAction(event -> promptForCashAccountTransaction(TransactionType.DEPOSIT, row.getItem()));

        MenuItem withdrawalItem = new MenuItem("withdrawal");
        withdrawalItem.setOnAction(event -> promptForCashAccountTransaction(TransactionType.WITHDRAWAL, row.getItem()));

        MenuItem transferItem = new MenuItem("transfer");
        transferItem.setOnAction(event -> promptForCashAccountTransaction(TransactionType.CASH_TRANSFER, row.getItem()));

        return new ContextMenu(depositItem, withdrawalItem, transferItem);
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

    private void updateSecurityDetails(SecurityRow securityRow) {
        updatePriceChart(securityRow);
        updateSecurityTransactions(securityRow);
    }

    private void updatePriceChart(SecurityRow securityRow) {
        priceChart.getData().clear();
        priceChartTitleLabel.setText(securityRow == null
                ? "No security selected"
                : securityTitle(securityRow));

        if (securityRow == null) {
            updatePriceChartXAxis(List.of(), null);
            return;
        }

        List<InstrumentPrice> prices = instrumentPriceRepository.findByInstrumentId(securityRow.id());
        LocalDate firstVisibleDate = firstVisibleDate(prices);
        updatePriceChartXAxis(prices, firstVisibleDate);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        prices.stream()
                .filter(price -> firstVisibleDate == null || !price.getPriceDate().isBefore(firstVisibleDate))
                .forEach(price -> series.getData().add(new XYChart.Data<>(
                        price.getPriceDate().toEpochDay(),
                        price.getClosePrice())));
        priceChart.getData().add(series);
    }

    private void updateSecurityTransactions(SecurityRow securityRow) {
        if (securityRow == null) {
            securityTransactionsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        securityTransactionsTable.setItems(FXCollections.observableArrayList(
                portfolioTransactionRepository.findByInstrumentId(securityRow.id()).stream()
                        .map(transaction -> new SecurityTransactionRow(
                                transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().toString(),
                                transaction.getType() == null ? "" : transactionTitle(transaction.getType()),
                                decimalToString(transaction.getShares()),
                                decimalToString(transaction.getAmount()),
                                transaction.getCurrency(),
                                decimalToString(transaction.getFees()),
                                decimalToString(transaction.getTaxes()),
                                transaction.getNote()))
                        .toList()));
    }

    private void updatePriceChartXAxis(List<InstrumentPrice> prices, LocalDate firstVisibleDate) {
        if (!(priceChart.getXAxis() instanceof NumberAxis xAxis)) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate lowerDate = firstVisibleDate;
        if (lowerDate == null && !prices.isEmpty()) {
            lowerDate = prices.get(0).getPriceDate();
        }
        if (lowerDate == null) {
            lowerDate = today.minusDays(1);
        }

        long lowerBound = lowerDate.toEpochDay();
        long upperBound = today.toEpochDay();
        if (lowerBound >= upperBound) {
            lowerBound = upperBound - 1;
        }

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(lowerBound);
        xAxis.setUpperBound(upperBound);
        xAxis.setTickUnit(Math.max(1, (upperBound - lowerBound) / 6));
    }

    private String securityTitle(SecurityRow securityRow) {
        String symbol = securityRow.symbolProperty().get();
        String name = securityRow.nameProperty().get();
        if (symbol == null || symbol.isBlank()) {
            return name;
        }
        return symbol + " - " + name;
    }

    private void configurePriceChart() {
        if (priceChart.getXAxis() instanceof NumberAxis xAxis) {
            xAxis.setForceZeroInRange(false);
            xAxis.setTickLabelFormatter(new StringConverter<>() {
                @Override
                public String toString(Number value) {
                    return LocalDate.ofEpochDay(value.longValue()).format(AXIS_DATE_FORMATTER);
                }

                @Override
                public Number fromString(String value) {
                    return 0;
                }
            });
        }

        if (priceChart.getYAxis() instanceof NumberAxis yAxis) {
            yAxis.setForceZeroInRange(false);
        }
    }

    private void configureChartRangeButtons() {
        for (ChartRange range : ChartRange.values()) {
            ToggleButton rangeButton = new ToggleButton(range.label);
            rangeButton.getStyleClass().add("chart-range-button");
            rangeButton.setToggleGroup(chartRangeGroup);
            rangeButton.setMnemonicParsing(false);
            rangeButton.setOnAction(event -> {
                activeChartRange = range;
                updateSecurityDetails(securitiesTable.getSelectionModel().getSelectedItem());
            });
            chartRangeButtons.getChildren().add(rangeButton);

            if (range == activeChartRange) {
                rangeButton.setSelected(true);
            }
        }
    }

    private LocalDate firstVisibleDate(List<InstrumentPrice> prices) {
        if (prices.isEmpty() || activeChartRange == ChartRange.ALL) {
            return null;
        }

        LocalDate latestPriceDate = prices.get(prices.size() - 1).getPriceDate();
        if (activeChartRange == ChartRange.YEAR_TO_DATE) {
            return LocalDate.of(latestPriceDate.getYear(), 1, 1);
        }
        return activeChartRange.startDate(latestPriceDate);
    }

    private void importHistoricalPrices(Instrument instrument) {
        if (instrument.getSymbol() == null || instrument.getSymbol().isBlank()) {
            return;
        }

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusYears(10);
        CompletableFuture.runAsync(() -> {
            try {
                instrumentPriceRepository.saveAll(
                        instrument.getId(),
                        marketDataService.loadDailyPrices(instrument.getSymbol(), from, to));
                Platform.runLater(() -> {
                    SecurityRow selectedSecurity = securitiesTable.getSelectionModel().getSelectedItem();
                    if (selectedSecurity != null && selectedSecurity.id().equals(instrument.getId())) {
                        updateSecurityDetails(selectedSecurity);
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

    private enum ChartRange {
        ONE_MONTH("1M"),
        TWO_MONTHS("2M"),
        SIX_MONTHS("6M"),
        ONE_YEAR("1Y"),
        TWO_YEARS("2Y"),
        THREE_YEARS("3Y"),
        TEN_YEARS("10Y"),
        YEAR_TO_DATE("YTD"),
        ALL("ALL");

        private final String label;

        ChartRange(String label) {
            this.label = label;
        }

        private LocalDate startDate(LocalDate latestPriceDate) {
            return switch (this) {
                case ONE_MONTH -> latestPriceDate.minusMonths(1);
                case TWO_MONTHS -> latestPriceDate.minusMonths(2);
                case SIX_MONTHS -> latestPriceDate.minusMonths(6);
                case ONE_YEAR -> latestPriceDate.minusYears(1);
                case TWO_YEARS -> latestPriceDate.minusYears(2);
                case THREE_YEARS -> latestPriceDate.minusYears(3);
                case TEN_YEARS -> latestPriceDate.minusYears(10);
                case YEAR_TO_DATE, ALL -> latestPriceDate;
            };
        }
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

    private record SecurityAccountInput(String name, String note) {
    }

    private record CashAccountInput(String name, String currency, String note) {
    }

    private record SecurityTransactionInput(
            LocalDate transactionDate,
            String shares,
            String amount,
            String currency,
            Long securityAccountId,
            Long cashAccountId,
            String fees,
            String taxes,
            String note) {
    }

    private record CashAccountTransactionInput(
            LocalDate transactionDate,
            String amount,
            String currency,
            Long targetCashAccountId,
            String note) {
    }

    private record EntityChoice(Long id, String name) {
        private static EntityChoice none() {
            return new EntityChoice(null, "None");
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum MainView {
        SECURITIES,
        SECURITY_ACCOUNTS,
        CASH_ACCOUNTS
    }

    public static class SecurityAccountRow {
        private final Long id;
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty note = new SimpleStringProperty();

        public SecurityAccountRow(Long id, String name, String note) {
            this.id = id;
            this.name.set(name);
            this.note.set(note);
        }

        public Long id() {
            return id;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty noteProperty() {
            return note;
        }
    }

    public static class CashAccountRow {
        private final Long id;
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty currency = new SimpleStringProperty();
        private final StringProperty balance = new SimpleStringProperty();
        private final StringProperty note = new SimpleStringProperty();

        public CashAccountRow(Long id, String name, String currency, String balance, String note) {
            this.id = id;
            this.name.set(name);
            this.currency.set(currency);
            this.balance.set(balance);
            this.note.set(note);
        }

        public Long id() {
            return id;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty currencyProperty() {
            return currency;
        }

        public StringProperty balanceProperty() {
            return balance;
        }

        public StringProperty noteProperty() {
            return note;
        }
    }

    public static class SecurityTransactionRow {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty type = new SimpleStringProperty();
        private final StringProperty shares = new SimpleStringProperty();
        private final StringProperty amount = new SimpleStringProperty();
        private final StringProperty currency = new SimpleStringProperty();
        private final StringProperty fees = new SimpleStringProperty();
        private final StringProperty taxes = new SimpleStringProperty();
        private final StringProperty note = new SimpleStringProperty();

        public SecurityTransactionRow(
                String date,
                String type,
                String shares,
                String amount,
                String currency,
                String fees,
                String taxes,
                String note) {
            this.date.set(date);
            this.type.set(type);
            this.shares.set(shares);
            this.amount.set(amount);
            this.currency.set(currency);
            this.fees.set(fees);
            this.taxes.set(taxes);
            this.note.set(note);
        }

        public StringProperty dateProperty() {
            return date;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public StringProperty sharesProperty() {
            return shares;
        }

        public StringProperty amountProperty() {
            return amount;
        }

        public StringProperty currencyProperty() {
            return currency;
        }

        public StringProperty feesProperty() {
            return fees;
        }

        public StringProperty taxesProperty() {
            return taxes;
        }

        public StringProperty noteProperty() {
            return note;
        }
    }

    public static class AccountTransactionRow {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty type = new SimpleStringProperty();
        private final StringProperty amount = new SimpleStringProperty();
        private final StringProperty currency = new SimpleStringProperty();
        private final StringProperty sourceAccount = new SimpleStringProperty();
        private final StringProperty targetAccount = new SimpleStringProperty();
        private final StringProperty note = new SimpleStringProperty();

        public AccountTransactionRow(
                String date,
                String type,
                String amount,
                String currency,
                String sourceAccount,
                String targetAccount,
                String note) {
            this.date.set(date);
            this.type.set(type);
            this.amount.set(amount);
            this.currency.set(currency);
            this.sourceAccount.set(sourceAccount);
            this.targetAccount.set(targetAccount);
            this.note.set(note);
        }

        public StringProperty dateProperty() {
            return date;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public StringProperty amountProperty() {
            return amount;
        }

        public StringProperty currencyProperty() {
            return currency;
        }

        public StringProperty sourceAccountProperty() {
            return sourceAccount;
        }

        public StringProperty targetAccountProperty() {
            return targetAccount;
        }

        public StringProperty noteProperty() {
            return note;
        }
    }

    public static class SecurityAccountTransactionRow {
        private final StringProperty date = new SimpleStringProperty();
        private final StringProperty type = new SimpleStringProperty();
        private final StringProperty instrument = new SimpleStringProperty();
        private final StringProperty shares = new SimpleStringProperty();
        private final StringProperty amount = new SimpleStringProperty();
        private final StringProperty currency = new SimpleStringProperty();
        private final StringProperty cashAccount = new SimpleStringProperty();
        private final StringProperty fees = new SimpleStringProperty();
        private final StringProperty taxes = new SimpleStringProperty();
        private final StringProperty note = new SimpleStringProperty();

        public SecurityAccountTransactionRow(
                String date,
                String type,
                String instrument,
                String shares,
                String amount,
                String currency,
                String cashAccount,
                String fees,
                String taxes,
                String note) {
            this.date.set(date);
            this.type.set(type);
            this.instrument.set(instrument);
            this.shares.set(shares);
            this.amount.set(amount);
            this.currency.set(currency);
            this.cashAccount.set(cashAccount);
            this.fees.set(fees);
            this.taxes.set(taxes);
            this.note.set(note);
        }

        public StringProperty dateProperty() {
            return date;
        }

        public StringProperty typeProperty() {
            return type;
        }

        public StringProperty instrumentProperty() {
            return instrument;
        }

        public StringProperty sharesProperty() {
            return shares;
        }

        public StringProperty amountProperty() {
            return amount;
        }

        public StringProperty currencyProperty() {
            return currency;
        }

        public StringProperty cashAccountProperty() {
            return cashAccount;
        }

        public StringProperty feesProperty() {
            return fees;
        }

        public StringProperty taxesProperty() {
            return taxes;
        }

        public StringProperty noteProperty() {
            return note;
        }
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
