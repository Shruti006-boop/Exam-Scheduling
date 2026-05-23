package com.example.scheduler.service;

import com.example.scheduler.model.ConflictGraph;
import com.example.scheduler.model.Course;
import com.example.scheduler.model.ScheduleResult;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GreedyScheduler {

    public ScheduleResult schedule(ConflictGraph graph) {
        long startTime = System.currentTimeMillis();
        Map<String, Integer> schedule = new HashMap<>();
        List<String> logs = new ArrayList<>();
        int maxSlot = 0;

        for (Course course : graph.getCourses()) {
            String courseId = course.getId();
            Set<Integer> usedSlots = new HashSet<>();
            
            for (String neighbor : graph.getAdjacencyList().get(courseId)) {
                if (schedule.containsKey(neighbor)) {
                    usedSlots.add(schedule.get(neighbor));
                }
            }
            
            int slot = 1;
            while (usedSlots.contains(slot)) {
                slot++;
            }
            
            schedule.put(courseId, slot);
            maxSlot = Math.max(maxSlot, slot);
            logs.add("Assigned course " + courseId + " to slot " + slot);
        }

        long endTime = System.currentTimeMillis();
        ScheduleResult result = new ScheduleResult("Greedy", schedule, maxSlot, endTime - startTime, 1.0 / (maxSlot == 0 ? 1 : maxSlot), logs, null);
        result.setComplexity("O(V^2)");
        result.setSpaceComplexity("O(V + E)");
        result.setComparativeAnalysis("First-fit heuristic coloring.");
        result.setEfficiency("Very fast, near-optimal.");
        return result;
    }
}
