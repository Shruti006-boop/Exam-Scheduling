package com.example.scheduler.service;

import com.example.scheduler.model.ConflictGraph;
import com.example.scheduler.model.Course;
import com.example.scheduler.model.ScheduleResult;
import com.example.scheduler.model.Student;
import com.example.scheduler.model.Slot;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScheduleService {

    private final ConflictGraph graph = new ConflictGraph();
    private final Map<String, Student> studentsMap = new HashMap<>();
    private final Map<Integer, Slot> slots = new HashMap<>();
    private ScheduleResult currentSchedule;

    public void processJsonUpload(Map<String, List<String>> payload) {
        graph.getCourses().clear();
        graph.getAdjacencyList().clear();
        studentsMap.clear();
        slots.clear();
        currentSchedule = null;

        for (Map.Entry<String, List<String>> entry : payload.entrySet()) {
            String studentId = entry.getKey();
            List<String> courseIds = entry.getValue();

            List<Course> studentCourses = new ArrayList<>();
            for (String courseId : courseIds) {
                Course course = new Course(courseId, courseId);
                graph.addCourse(course);
                studentCourses.add(course);
            }

            studentsMap.put(studentId, new Student(studentId, studentId, studentCourses));

            for (int i = 0; i < courseIds.size(); i++) {
                for (int j = i + 1; j < courseIds.size(); j++) {
                    graph.addConflict(courseIds.get(i), courseIds.get(j));
                }
            }
        }
    }

    public void addStudent(Student student) {
        studentsMap.put(student.getId(), student);
        for (Course course : student.getCourses()) {
            graph.addCourse(course);
        }
        List<Course> courses = student.getCourses();
        for (int i = 0; i < courses.size(); i++) {
            for (int j = i + 1; j < courses.size(); j++) {
                graph.addConflict(courses.get(i).getId(), courses.get(j).getId());
            }
        }
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(studentsMap.values());
    }

    public ConflictGraph getGraph() {
        return graph;
    }

    public Student getStudent(String id) {
        return studentsMap.get(id);
    }

    public ScheduleResult getCurrentSchedule() {
        return currentSchedule;
    }

    public void setCurrentSchedule(ScheduleResult currentSchedule) {
        this.currentSchedule = currentSchedule;
    }

    public Map<Integer, Slot> getSlots() { return slots; }
    
    public void assignSlot(int slotId, String date, String time) {
        slots.put(slotId, new Slot(slotId, date, time));
    }
}
