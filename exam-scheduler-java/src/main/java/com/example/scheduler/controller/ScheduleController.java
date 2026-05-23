package com.example.scheduler.controller;

import com.example.scheduler.model.Course;
import com.example.scheduler.model.ScheduleResult;
import com.example.scheduler.model.Student;
import com.example.scheduler.model.Slot;
import com.example.scheduler.service.*;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

@RestController
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private GreedyScheduler greedyScheduler;
    
    @Autowired
    private BacktrackingScheduler backtrackingScheduler;

    @PostMapping("/upload-json")
    public ResponseEntity<String> uploadJson(@RequestBody Map<String, List<String>> payload) {
        scheduleService.processJsonUpload(payload);
        return ResponseEntity.ok("Data uploaded successfully");
    }

    @PostMapping("/add-student")
    public ResponseEntity<String> addStudent(@RequestBody Student student) {
        scheduleService.addStudent(student);
        return ResponseEntity.ok("Student added successfully");
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getStudents() {
        return ResponseEntity.ok(scheduleService.getAllStudents());
    }

    @PostMapping("/generate-schedule")
    public ResponseEntity<ScheduleResult> generateSchedule(@RequestParam String algorithm) {
        ScheduleResult result;
        switch (algorithm.toLowerCase()) {
            case "greedy":
                result = greedyScheduler.schedule(scheduleService.getGraph());
                break;
            case "backtracking":
                result = backtrackingScheduler.schedule(scheduleService.getGraph());
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        scheduleService.setCurrentSchedule(result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/performance")
    public ResponseEntity<List<Map<String, Object>>> getPerformanceData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Static comparative data for the table as requested
        data.add(createPerfMap("Greedy", "O(V²)", "O(V + E)", "Fast execution", "No", "Large datasets"));
        data.add(createPerfMap("Backtracking", "Exponential", "O(V)", "Slow execution", "Yes", "Smaller datasets"));
        
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/assign-slot")
    public ResponseEntity<String> assignSlot(@RequestBody Map<String, Object> payload) {
        int slotId = (int) payload.get("slot");
        String date = (String) payload.get("date");
        String time = (String) payload.get("time");

        // ISSUE 2: SLOT COLLISION CHECK (BACKEND)
        for (Slot s : scheduleService.getSlots().values()) {
            if (s.getSlotId() != slotId && date.equals(s.getAssignedDate()) && time.equals(s.getAssignedTime())) {
                return ResponseEntity.badRequest().body("Slot conflict detected! This time is already assigned.");
            }
        }

        scheduleService.assignSlot(slotId, date, time);
        return ResponseEntity.ok("Slot assigned successfully");
    }

    @PostMapping("/api/clear-slots")
    public ResponseEntity<String> clearSlots() {
        scheduleService.getSlots().clear();
        return ResponseEntity.ok("Slots cleared successfully");
    }

    @GetMapping("/api/slots")
    public ResponseEntity<Collection<Slot>> getSlots() {
        return ResponseEntity.ok(scheduleService.getSlots().values());
    }

    @GetMapping("/api/slot-distribution")
    public ResponseEntity<Map<Integer, Integer>> getSlotDistribution() {
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (result == null) return ResponseEntity.ok(Collections.emptyMap());

        Map<Integer, Integer> distribution = new HashMap<>();
        Map<String, Integer> courseToSlot = result.getSchedule();

        for (Student student : scheduleService.getAllStudents()) {
            Set<Integer> studentSlots = new HashSet<>();
            for (Course course : student.getCourses()) {
                Integer slot = courseToSlot.get(course.getId());
                if (slot != null) studentSlots.add(slot);
            }
            for (Integer slot : studentSlots) {
                distribution.put(slot, distribution.getOrDefault(slot, 0) + 1);
            }
        }
        return ResponseEntity.ok(distribution);
    }

    private Map<String, Object> createPerfMap(String name, String complexity, String space, String speed, String optimal, String useCase) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("complexity", complexity);
        map.put("space", space);
        map.put("speed", speed);
        map.put("optimal", optimal);
        map.put("useCase", useCase);
        return map;
    }

    @GetMapping("/timetable")
    public ResponseEntity<ScheduleResult> getTimetable() {
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/export-pdf/{studentId}")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String studentId) {
        return downloadPdf(studentId);
    }

    @GetMapping("/api/timetable/download")
    public ResponseEntity<byte[]> downloadFullTimetable() {
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (result == null) return ResponseEntity.notFound().build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            document.add(new Paragraph("Full Exam Timetable", new Font(Font.HELVETICA, 20, Font.BOLD)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table, "Student", "Subject", "Slot", "Date", "Time");

            for (Student student : scheduleService.getAllStudents()) {
                for (Course course : student.getCourses()) {
                    Integer slotId = result.getSchedule().get(course.getId());
                    Slot slot = scheduleService.getSlots().get(slotId);
                    table.addCell(student.getName());
                    table.addCell(course.getId());
                    table.addCell(slotId != null ? "Slot " + slotId : "N/A");
                    table.addCell(slot != null ? slot.getAssignedDate() : "TBD");
                    table.addCell(slot != null ? slot.getAssignedTime() : "TBD");
                }
            }
            
            document.add(table);
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "full_timetable.pdf");
            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/download-student/{studentId}")
    public ResponseEntity<byte[]> downloadIndividualTimetable(@PathVariable String studentId) {
        Student student = scheduleService.getStudent(studentId);
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (student == null || result == null) return ResponseEntity.notFound().build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Header Section
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("EXAM SCHEDULER", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            document.add(new Paragraph("Student ID: " + studentId, new Font(Font.HELVETICA, 12, Font.BOLD)));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            document.add(new Paragraph(" "));

            // Timetable Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            addTableHeader(table, "Subject", "Slot", "Date", "Time");

            for (Course course : student.getCourses()) {
                Integer slotId = result.getSchedule().get(course.getId());
                Slot slot = scheduleService.getSlots().get(slotId);
                table.addCell(new PdfPCell(new Paragraph(course.getId())));
                table.addCell(new PdfPCell(new Paragraph(slotId != null ? "Slot " + slotId : "N/A")));
                table.addCell(new PdfPCell(new Paragraph(slot != null && slot.getAssignedDate() != null ? slot.getAssignedDate() : "TBD")));
                table.addCell(new PdfPCell(new Paragraph(slot != null && slot.getAssignedTime() != null ? slot.getAssignedTime() : "TBD")));
            }
            
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            
            document.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Schedule_" + studentId + ".pdf");
            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download-pdf/{studentId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String studentId) {
        return downloadIndividualTimetable(studentId);
    }

    @GetMapping("/api/timetable/download-csv")
    public ResponseEntity<String> downloadFullTimetableCsv() {
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (result == null) return ResponseEntity.notFound().build();

        StringBuilder csv = new StringBuilder("StudentID,Subject,Slot,Date,Time\n");
        for (Student student : scheduleService.getAllStudents()) {
            for (Course course : student.getCourses()) {
                Integer slotId = result.getSchedule().get(course.getId());
                Slot slot = scheduleService.getSlots().get(slotId);
                csv.append(student.getId()).append(",")
                   .append(course.getId()).append(",")
                   .append(slotId != null ? "Slot " + slotId : "N/A").append(",")
                   .append(slot != null && slot.getAssignedDate() != null ? slot.getAssignedDate() : "TBD").append(",")
                   .append(slot != null && slot.getAssignedTime() != null ? slot.getAssignedTime() : "TBD").append("\n");
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setContentDispositionFormData("attachment", "full_timetable.csv");
        return ResponseEntity.ok().headers(headers).body(csv.toString());
    }

    @GetMapping("/api/student/download-csv/{studentId}")
    public ResponseEntity<String> downloadStudentCsv(@PathVariable String studentId) {
        Student student = scheduleService.getStudent(studentId);
        ScheduleResult result = scheduleService.getCurrentSchedule();
        if (student == null || result == null) return ResponseEntity.notFound().build();

        StringBuilder csv = new StringBuilder("StudentID,Subject,Slot,Date,Time\n");
        for (Course course : student.getCourses()) {
            Integer slotId = result.getSchedule().get(course.getId());
            Slot slot = scheduleService.getSlots().get(slotId);
            csv.append(student.getId()).append(",")
               .append(course.getId()).append(",")
               .append(slotId != null ? "Slot " + slotId : "N/A").append(",")
               .append(slot != null && slot.getAssignedDate() != null ? slot.getAssignedDate() : "TBD").append(",")
               .append(slot != null && slot.getAssignedTime() != null ? slot.getAssignedTime() : "TBD").append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/csv"));
        headers.setContentDispositionFormData("attachment", "Schedule_" + studentId + ".csv");
        return ResponseEntity.ok().headers(headers).body(csv.toString());
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    @GetMapping("/api/graph")
    public ResponseEntity<Map<String, Object>> getGraph() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> nodes = new ArrayList<>();
        for (Course c : scheduleService.getGraph().getCourses()) {
            Map<String, String> node = new HashMap<>();
            node.put("id", c.getId());
            nodes.add(node);
        }

        List<Map<String, String>> links = new ArrayList<>();
        Set<String> seenLinks = new HashSet<>();
        
        for (Map.Entry<String, Set<String>> entry : scheduleService.getGraph().getAdjacencyList().entrySet()) {
            String source = entry.getKey();
            for (String target : entry.getValue()) {
                String linkId = source.compareTo(target) < 0 ? source + "-" + target : target + "-" + source;
                if (!seenLinks.contains(linkId)) {
                    seenLinks.add(linkId);
                    Map<String, String> link = new HashMap<>();
                    link.put("source", source);
                    link.put("target", target);
                    links.add(link);
                }
            }
        }
        
        response.put("nodes", nodes);
        response.put("links", links);
        return ResponseEntity.ok(response);
    }
}
