package com.upianomaly.engine.alert;

import com.upianomaly.engine.model.Alert;
import com.upianomaly.engine.model.AnomalyType;
import com.upianomaly.engine.api.ApiDtos.AlertView;
import com.upianomaly.engine.api.ApiExceptions;
import com.upianomaly.engine.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Receives alerts from worker threads and is responsible for reporting them.
 *
 * For this project, "reporting" just means printing to the console and
 * keeping an in-memory list for a final summary - that is enough to
 * demonstrate the concept without dragging in a database or a logging
 * framework. CopyOnWriteArrayList is used because several worker threads
 * can call raise(...) at the same time.
 */
@Service
public class AlertService {

    private final List<Alert> allAlerts = new CopyOnWriteArrayList<>();
    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void raise(Alert alert) {
        alertRepository.save(alert);
        allAlerts.add(alert);
        System.out.println(alert);
    }

    public List<AlertView> findAll() {
        return alertRepository.findAll();
    }

    public List<AlertView> findRecent(int limit) {
        return alertRepository.findRecent(limit);
    }

    public AlertView findById(long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.ResourceNotFoundException("Alert"));
    }

    public long count() {
        return alertRepository.count();
    }

    public long countByType(String anomalyType) {
        return alertRepository.countByType(anomalyType);
    }

    public void printSummary() {
        System.out.println();
        System.out.println("===== ALERT SUMMARY =====");
        System.out.println("Total alerts raised: " + allAlerts.size());

        Map<AnomalyType, Long> countByType = allAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getType, Collectors.counting()));

        for (AnomalyType type : AnomalyType.values()) {
            System.out.printf("  %-20s : %d%n", type, countByType.getOrDefault(type, 0L));
        }
        System.out.println("==========================");
    }

    public List<Alert> getAllAlerts() {
        return allAlerts;
    }
}
