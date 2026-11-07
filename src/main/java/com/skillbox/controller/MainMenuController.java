package com.skillbox.controller;

import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.controller.option.AggregateOption;
import com.skillbox.controller.option.GroupOption;
import com.skillbox.controller.option.MainMenuOption;
import com.skillbox.data.model.Analytic;
import com.skillbox.data.repository.AnalyticRepository;
import com.skillbox.service.TransactionService;
import java.util.Scanner;

public class MainMenuController extends AbstractMenuController<MainMenuOption> {

    private final TransactionService transactionService;
    private final AnalyticRepository saver;
    private final SharedMenu<MainMenuOption> menu;
    private final SearchMenuController searchMenuController;
    private final GroupMenuController groupMenuController;
    private final AggregateMenuController aggregateMenuController;

    public MainMenuController(TransactionService transactionService, AnalyticRepository saver) {
        super(MainMenuOption.class, "Анализ финансов");
        this.transactionService = transactionService;
        this.saver = saver;
        Scanner sharedScanner = new Scanner(System.in).useDelimiter("\\R");
        this.menu = new SharedMenu<>(sharedScanner, MainMenuOption.class, "Анализ финансов");
        this.searchMenuController = new SearchMenuController(sharedScanner);
        this.groupMenuController = new GroupMenuController(sharedScanner);
        this.aggregateMenuController = new AggregateMenuController(sharedScanner);
    }

    public void start() {
        goMainMenu();
    }

    private void goMainMenu() {
        TransactionFilterDto transactionFilter = new TransactionFilterDto();
        GroupOption groupOption = GroupOption.NO_GROUP;
        AggregateOption aggregateOption = AggregateOption.SUM;
        Analytic analytics = null;
        while (true) {
            MainMenuOption i = menu.select();
            switch (i) {
                case SEARCH_CRITERIA:
                    transactionFilter = searchMenuController.getTransactionFilter();
                    break;
                case GROUP_OPTION:
                    groupOption = groupMenuController.getGroupOption();
                    break;
                case AGGREGATION_METHOD:
                    aggregateOption = aggregateMenuController.getAggregateOption();
                    break;
                case CALCULATE_ANALYTICS:
                    analytics = transactionService.calculateAnalytics(transactionFilter,
                            groupOption, aggregateOption);
                    System.out.println(analytics);
                    break;
                case SAVE_ANALYTICS:
                    if (analytics == null) {
                        System.err.println("Необходимо сначала рассчитать аналитику");
                        break;
                    }
                    saver.save(analytics);
                    break;
                case EXIT:
                    return;
            }
        }
    }
}