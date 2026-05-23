package com.example.scheduler.service;

import com.example.scheduler.model.Slot;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SlotService {
    private final List<Slot> slots = new ArrayList<>();

    public void addSlot(Slot slot) {
        slots.add(slot);
    }

    public List<Slot> getAllSlots() {
        return slots;
    }
}
