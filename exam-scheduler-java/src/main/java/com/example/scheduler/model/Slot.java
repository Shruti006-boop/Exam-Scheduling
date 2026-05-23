package com.example.scheduler.model;

public class Slot {
    private int slotId;
    private String assignedDate;
    private String assignedTime;

    public Slot() {}

    public Slot(int slotId, String assignedDate, String assignedTime) {
        this.slotId = slotId;
        this.assignedDate = assignedDate;
        this.assignedTime = assignedTime;
    }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getAssignedDate() { return assignedDate; }
    public void setAssignedDate(String assignedDate) { this.assignedDate = assignedDate; }

    public String getAssignedTime() { return assignedTime; }
    public void setAssignedTime(String assignedTime) { this.assignedTime = assignedTime; }
}
