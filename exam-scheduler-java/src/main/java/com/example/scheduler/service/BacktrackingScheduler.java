package com.example.scheduler.service;

import com.example.scheduler.model.ConflictGraph;
import com.example.scheduler.model.Course;
import com.example.scheduler.model.ScheduleResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BacktrackingScheduler {

    public ScheduleResult schedule(ConflictGraph graph) {
        long startTime = System.currentTimeMillis();
        List<Course> courses = new ArrayList<>(graph.getCourses());
        
        // Optimization: Sort courses by degree descending to prune search tree faster
        courses.sort((c1, c2) -> Integer.compare(graph.getDegree(c2.getId()), graph.getDegree(c1.getId())));
        
        Map<String, Integer> bestSchedule = new HashMap<>();
        int n = courses.size();
        
        if (n == 0) {
            return new ScheduleResult("Backtracking", new HashMap<>(), 0, 0, 0, new ArrayList<>(), null);
        }

        // Find an initial upper bound using Greedy to speed up backtracking or limit k
        int upperBound = n; 
        
        for (int k = 1; k <= upperBound; k++) {
            Map<String, Integer> currentSchedule = new HashMap<>();
            if (solve(0, k, courses, currentSchedule, graph.getAdjacencyList())) {
                bestSchedule = currentSchedule;
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        int maxSlot = 0;
        for (int slot : bestSchedule.values()) {
            maxSlot = Math.max(maxSlot, slot);
        }

        List<String> logs = new ArrayList<>();
        logs.add("Backtracking found optimal solution with " + maxSlot + " slots.");

        ScheduleResult result = new ScheduleResult("Backtracking", bestSchedule, maxSlot, endTime - startTime, 1.0 / (maxSlot == 0 ? 1 : maxSlot), logs, null);
        result.setComplexity("O(k^V)");
        result.setSpaceComplexity("O(V)");
        result.setComparativeAnalysis("Recursive search for optimal coloring.");
        result.setEfficiency("Slow but guaranteed optimal.");
        
        return result;
    }

    private boolean solve(int courseIdx, int k, List<Course> courses, Map<String, Integer> currentSchedule, Map<String, Set<String>> adj) {
        if (courseIdx == courses.size()) return true;

        String courseId = courses.get(courseIdx).getId();
        for (int color = 1; color <= k; color++) {
            if (isSafe(courseId, color, currentSchedule, adj)) {
                currentSchedule.put(courseId, color);
                if (solve(courseIdx + 1, k, courses, currentSchedule, adj)) return true;
                currentSchedule.remove(courseId);
            }
        }
        return false;
    }

    private boolean isSafe(String courseId, int color, Map<String, Integer> currentSchedule, Map<String, Set<String>> adj) {
        for (String neighbor : adj.getOrDefault(courseId, Collections.emptySet())) {
            if (currentSchedule.getOrDefault(neighbor, -1) == color) {
                return false;
            }
        }
        return true;
    }
}
