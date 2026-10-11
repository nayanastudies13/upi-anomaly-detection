package com.upianomaly.engine.service;

import com.upianomaly.engine.api.ApiDtos.AlertView;
import com.upianomaly.engine.api.ApiDtos.DashboardView;
import com.upianomaly.engine.api.ApiDtos.TransactionView;
import com.upianomaly.engine.alert.AlertService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final TransactionService transactionService;
    private final AlertService alertService;

    public DashboardService(TransactionService transactionService, AlertService alertService) {
        this.transactionService = transactionService;
        this.alertService = alertService;
    }

    public DashboardView getDashboard() {
        List<TransactionView> recentTransactions = transactionService.findRecent(10);
        List<AlertView> recentAlerts = alertService.findRecent(10);
        long velocityAlerts = alertService.countByType("VELOCITY_ALERT")
            + alertService.countByType("HIGH_VELOCITY");
        return new DashboardView(
                transactionService.count(),
                alertService.count(),
                velocityAlerts,
                alertService.countByType("ABOVE_MEAN_AMOUNT"),
                recentTransactions,
                recentAlerts);
    }
}