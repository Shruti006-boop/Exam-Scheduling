package com.example.scheduler.model;

import java.util.*;

public class ConflictGraph {
    private List<Course> courses = new ArrayList<>();
    private Map<String, Set<String>> adjacencyList = new HashMap<>();

    public ConflictGraph() {}

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public Map<String, Set<String>> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(Map<String, Set<String>> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public void addCourse(Course course) {
        if (!adjacencyList.containsKey(course.getId())) {
            courses.add(course);
            adjacencyList.put(course.getId(), new HashSet<>());
        }
    }

    public void addConflict(String courseId1, String courseId2) {
        if (courseId1.equals(courseId2)) return;
        adjacencyList.get(courseId1).add(courseId2);
        adjacencyList.get(courseId2).add(courseId1);
    }
    
    public int getDegree(String courseId) {
        return adjacencyList.getOrDefault(courseId, Collections.emptySet()).size();
    }
}
