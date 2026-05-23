package com.example.scheduler.controller;

import com.example.scheduler.model.Slot;
import com.example.scheduler.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    @Autowired
    private SlotService slotService;

    @PostMapping("/add")
    public ResponseEntity<String> addSlot(@RequestBody Slot slot) {
        slotService.addSlot(slot);
        return ResponseEntity.ok("Slot added successfully");
    }

    @GetMapping
    public ResponseEntity<List<Slot>> getSlots() {
        return ResponseEntity.ok(slotService.getAllSlots());
    }
}
