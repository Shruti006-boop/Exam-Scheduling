/* =====================================================
   EXAM SCHEDULER PRO - ULTIMATE INTERACTIVE LOGIC
   ===================================================== */

let currentSchedule = null;
let distributionChart = null;
let execTimeChart = null;
let calendar = null;
let allStudents = [];
let slotPool = {}; 

const COLORS = [
    '#6366f1', '#0ea5e9', '#10b981', '#f59e0b', '#f43f5e',
    '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#3b82f6'
];

document.addEventListener('DOMContentLoaded', () => {
    initCalendar();
    initFileUpload();
    initGenerateBtn();
    initDownloadButtons();
    initStudentSelection();
    fetchComparativeData();
    fetchStudents();
    resetSlotsOnLoad();
});

async function resetSlotsOnLoad() {
    try {
        await fetch('/api/clear-slots', { method: 'POST' });
        slotPool = {};
        updateCalendar();
    } catch (e) {}
}

// ============================================
//  UI INTERACTIONS (SMOOTH TOGGLES)
// ============================================
function toggleSection(id) {
    const el = document.getElementById(id);
    if (!el) return;
    
    const isCollapsed = el.classList.contains('collapsed');
    if (isCollapsed) {
        el.classList.remove('collapsed');
        if (id === 'analysis-panel') {
            setTimeout(() => {
                renderExecutionTimeChart();
                el.scrollIntoView({ behavior: 'smooth', block: 'end' });
            }, 300);
        }
    } else {
        el.classList.add('collapsed');
    }
}

// ============================================
//  STUDENT SELECTION
// ============================================
function initStudentSelection() {
    const dropdown = document.getElementById('student-dropdown');
    const dlBtn = document.getElementById('btn-download-individual');
    
    dropdown.addEventListener('change', () => {
        dlBtn.disabled = !dropdown.value;
    });
    
    dlBtn.addEventListener('click', () => {
        if (!currentSchedule) {
            showToast('Warning: Generate timetable first', 'error');
            return;
        }
        const sid = encodeURIComponent(dropdown.value);
        if (sid) downloadFile(`/api/download-student/${sid}`, `Schedule_${dropdown.value}.pdf`);
    });
}

function updateStudentDropdown() {
    const dropdown = document.getElementById('student-dropdown');
    const currentVal = dropdown.value;
    dropdown.innerHTML = '<option value="">Select Student</option>';
    allStudents.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = `${s.id} - ${s.name}`;
        dropdown.appendChild(opt);
    });
    dropdown.value = currentVal;
}

// ============================================
//  CALENDAR
// ============================================
function initCalendar() {
    const el = document.getElementById('calendar');
    if (!el) return;
    calendar = new FullCalendar.Calendar(el, {
        initialView: 'dayGridMonth',
        headerToolbar: { left: 'prev,next', center: 'title', right: '' },
        height: 'auto',
        dayMaxEvents: 2,
        events: [],
        eventDidMount: (info) => {
            info.el.title = info.event.title; // Simple tooltip
        }
    });
    calendar.render();
}

function updateCalendar() {
    if (!calendar) return;
    calendar.removeAllEvents();
    const events = Object.entries(slotPool)
        .filter(([_, data]) => data.date)
        .map(([id, data]) => ({
            title: `Slot ${id}`,
            start: data.date,
            allDay: true,
            display: 'block',
            backgroundColor: COLORS[(parseInt(id) - 1) % COLORS.length],
            borderColor: 'transparent'
        }));
    calendar.addEventSource(events);
}

// ============================================
//  FILE UPLOAD (INTERACTIVE FEEDBACK)
// ============================================
function initFileUpload() {
    const dropZone = document.getElementById('drop-zone');
    const status = document.getElementById('upload-status');
    const success = document.getElementById('file-success');
    const fileInput = document.getElementById('file-input');

    dropZone.querySelector('button').addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', e => {
        if (e.target.files.length) handleFileSelection(e.target.files[0]);
    });

    async function handleFileSelection(file) {
        status.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Processing ${file.name}...`;
        const reader = new FileReader();
        reader.onload = async e => {
            try {
                const json = JSON.parse(e.target.result);
                const resp = await fetch('/upload-json', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(json)
                });
                if (resp.ok) {
                    showToast('Data Integrated Successfully', 'success');
                    success.classList.remove('d-none');
                    status.innerHTML = `Loaded: ${file.name}`;
                    slotPool = {};
                    updateCalendar();
                    fetchStudents();
                }
            } catch { 
                showToast('Format Error: Invalid JSON', 'error');
                status.innerHTML = 'Upload Failed';
            }
        };
        reader.readAsText(file);
    }
}

// ============================================
//  DATA SERVICES
// ============================================
async function fetchInitialSlots() {
    try {
        const resp = await fetch('/api/slots');
        const data = await resp.json();
        slotPool = {}; // Clear old
        data.forEach(s => slotPool[s.slotId] = { date: s.assignedDate, time: s.assignedTime });
        updateCalendar();
    } catch (e) {}
}

async function fetchStudents() {
    try {
        const resp = await fetch('/students');
        allStudents = await resp.json();
        updateStudentDropdown();
    } catch (e) {}
}

// ============================================
//  GENERATION ENGINE
// ============================================
function initGenerateBtn() {
    const btn = document.getElementById('btn-run-algo');
    btn.addEventListener('click', async () => {
        const algo = document.getElementById('algo-choice').value;
        btn.disabled = true;
        btn.innerHTML = `<span>⏳ Optimizing...</span>`;
        
        try {
            const resp = await fetch(`/generate-schedule?algorithm=${algo}`, { method: 'POST' });
            if (resp.ok) {
                currentSchedule = await resp.json();
                updateDashboard();
                showToast(`Success: ${algo.toUpperCase()} Solution Active`, 'success');
            }
        } catch { showToast('Connection Failure', 'error'); }
        finally { 
            btn.disabled = false; 
            btn.innerHTML = `<span>⚡ Generate Schedule</span>`; 
        }
    });
}

function updateDashboard() {
    if (!currentSchedule) return;
    document.getElementById('btn-download-all').classList.remove('d-none');
    document.getElementById('btn-download-csv').classList.remove('d-none');
    renderSlotDetails();
    renderConflictGraph();
    updateDistributionChart();
    renderAnalysisSection();
}

// ============================================
//  INTERACTIVE RENDERING
// ============================================
function renderSlotDetails() {
    const container = document.getElementById('slot-details-container');
    const slots = {};
    Object.entries(currentSchedule.schedule).forEach(([course, slot]) => {
        if (!slots[slot]) slots[slot] = [];
        slots[slot].push(course);
    });
    
    container.innerHTML = '';
    Object.entries(slots).sort((a,b) => a[0] - b[0]).forEach(([id, courses], index) => {
        const slotData = slotPool[id] || { date: '', time: '' };
        const card = document.createElement('div');
        card.className = 'slot-card-mini';
        card.style.animationDelay = `${index * 0.05}s`;
        card.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="slot-badge">SLOT ${id}</span>
                <span class="text-muted fw-700" style="font-size: 10px">${courses.length} SUBJECTS</span>
            </div>
            <div class="d-flex flex-wrap">${courses.map(c => `<span class="subject-tag">${c}</span>`).join('')}</div>
            <div class="slot-controls">
                <input type="date" class="slot-input" id="date-${id}" value="${slotData.date || ''}">
                <input type="time" class="slot-input" id="time-${id}" value="${slotData.time || ''}">
                <button class="btn-save-slot" onclick="saveSlotAssignment(${id})">Save</button>
            </div>
        `;
        container.appendChild(card);
    });
}

async function saveSlotAssignment(id) {
    const dateInput = document.getElementById(`date-${id}`);
    const timeInput = document.getElementById(`time-${id}`);
    const date = dateInput.value;
    const time = timeInput.value;
    
    if (!date || !time) {
        showToast('Please select both date and time', 'error');
        return;
    }

    // ISSUE 2: SLOT COLLISION CHECK (FRONTEND)
    const conflict = Object.entries(slotPool).find(([sid, data]) => 
        sid !== id.toString() && data.date === date && data.time === time
    );

    if (conflict) {
        showToast('❌ Slot conflict detected! This time is already assigned.', 'error');
        dateInput.classList.add('error-glow');
        timeInput.classList.add('error-glow');
        setTimeout(() => {
            dateInput.classList.remove('error-glow');
            timeInput.classList.remove('error-glow');
        }, 3000);
        return;
    }

    try {
        const resp = await fetch('/api/assign-slot', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ slot: parseInt(id), date, time })
        });
        
        if (resp.ok) {
            slotPool[id] = { date, time };
            updateCalendar();
            showToast('✔ Slot assigned successfully', 'success');
        } else {
            const errorMsg = await resp.text();
            showToast(`❌ ${errorMsg}`, 'error');
        }
    } catch (e) { showToast('System Error: Failed to save assignment', 'error'); }
}

async function renderConflictGraph() {
    try {
        const resp = await fetch('/api/graph');
        const data = await resp.json();
        const container = document.getElementById('d3-container');
        container.innerHTML = '';
        const rect = container.getBoundingClientRect();
        const width = rect.width;
        const height = rect.height || 350;

        const svg = d3.select('#d3-container').append('svg').attr('width', '100%').attr('height', '100%')
                      .attr('viewBox', [0, 0, width, height])
                      .call(d3.zoom().on('zoom', (event) => {
                          svgGroup.attr('transform', event.transform);
                      }));

        const svgGroup = svg.append('g');

        const sim = d3.forceSimulation(data.nodes)
            .force('link', d3.forceLink(data.links).id(d => d.id).distance(120))
            .force('charge', d3.forceManyBody().strength(-300))
            .force('center', d3.forceCenter(width / 2, height / 2));

        const link = svgGroup.append('g').attr('stroke', '#cbd5e1').attr('stroke-opacity', 0.5).selectAll('line').data(data.links).join('line');
        const node = svgGroup.append('g').selectAll('g').data(data.nodes).join('g')
            .call(d3.drag()
                .on('start', dragstarted)
                .on('drag', dragged)
                .on('end', dragended));

        node.append('circle').attr('r', 18)
            .attr('fill', d => COLORS[(currentSchedule.schedule[d.id]-1)%COLORS.length])
            .attr('stroke', '#fff').attr('stroke-width', 3)
            .style('cursor', 'grab')
            .on('mouseover', function() {
                d3.select(this).transition().duration(200).attr('r', 22).attr('stroke', '#6366f1').style('filter', 'drop-shadow(0 0 8px #6366f1)');
            })
            .on('mouseout', function() {
                d3.select(this).transition().duration(200).attr('r', 18).attr('stroke', '#fff').style('filter', 'none');
            });
        
        node.append('text').text(d => d.id).attr('dy', 5).attr('text-anchor', 'middle').attr('font-size', '10px').attr('font-weight', '800').attr('fill', '#fff');

        sim.on('tick', () => {
            link.attr('x1', d => d.source.x).attr('y1', d => d.source.y).attr('x2', d => d.target.x).attr('y2', d => d.target.y);
            node.attr('transform', d => `translate(${d.x},${d.y})`);
        });

        function dragstarted(event) {
            if (!event.active) sim.alphaTarget(0.3).restart();
            event.subject.fx = event.subject.x;
            event.subject.fy = event.subject.y;
        }
        function dragged(event) {
            event.subject.fx = event.x;
            event.subject.fy = event.y;
        }
        function dragended(event) {
            if (!event.active) sim.alphaTarget(0);
            event.subject.fx = null;
            event.subject.fy = null;
        }
    } catch (e) {}
}

async function updateDistributionChart() {
    try {
        const resp = await fetch('/api/slot-distribution');
        const data = await resp.json();
        const ctx = document.getElementById('slot-pie-chart').getContext('2d');
        if (distributionChart) distributionChart.destroy();
        distributionChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(data).map(s => `Slot ${s}`),
                datasets: [{ data: Object.values(data), backgroundColor: COLORS, borderWidth: 0 }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '75%',
                plugins: { legend: { position: 'right', labels: { boxWidth: 10, font: { size: 11, weight: '600' } } } }
            }
        });
    } catch (e) {}
}

async function renderAnalysisSection() {
    if (!currentSchedule) return;
    document.getElementById('metric-exec-time').textContent = `${currentSchedule.executionTimeMs}ms`;
    try {
        const resp = await fetch('/api/graph');
        const graphData = await resp.json();
        document.getElementById('metric-slots').textContent = currentSchedule.numSlots;
    } catch (e) {}
    renderExecutionTimeChart();
    
    const isG = currentSchedule.algorithm.toLowerCase() === 'greedy';
    document.getElementById('summary-insight-text').textContent = isG 
        ? "Heuristic Optimization: Identified a valid coloring solution in sub-millisecond time."
        : "Optimal Verification: Confirmed the minimum chromatic number for the given constraints.";
}

async function fetchComparativeData() {
    try {
        const resp = await fetch('/performance');
        const data = await resp.json();
        const tbody = document.getElementById('comparative-table-body');
        if (tbody) {
            tbody.innerHTML = data.map(p => `
                <tr style="font-size: 11px">
                    <td class="fw-800 text-primary">${p.name}</td>
                    <td class="text-muted"><code>${p.complexity}</code></td>
                    <td class="fw-600">${p.speed}</td>
                    <td><span class="badge ${p.optimal === 'Yes' ? 'bg-success' : 'bg-secondary'}" style="font-size: 9px">${p.optimal}</span></td>
                </tr>
            `).join('');
        }
    } catch (e) {}
}

function renderExecutionTimeChart() {
    const canvas = document.getElementById('exec-time-bar-chart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (execTimeChart) execTimeChart.destroy();
    
    const isG = currentSchedule?.algorithm?.toLowerCase() === 'greedy';
    const d = [isG ? currentSchedule.executionTimeMs : 10, isG ? 100 : currentSchedule.executionTimeMs];

    execTimeChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Greedy', 'Backtrack'],
            datasets: [{ data: d, backgroundColor: ['#6366f1', '#f43f5e'], borderRadius: 5 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: { y: { display: false }, x: { grid: { display: false }, ticks: { font: { size: 10, weight: '700' } } } },
            plugins: { legend: { display: false } }
        }
    });
}

// ============================================
//  DOWNLOADS
// ============================================
function initDownloadButtons() {
    document.getElementById('btn-download-all').addEventListener('click', () => {
        downloadFile('/api/timetable/download', 'Full_Timetable.pdf');
    });
    document.getElementById('btn-download-csv').addEventListener('click', () => {
        downloadFile('/api/timetable/download-csv', 'Full_Timetable.csv');
    });
}

function downloadStudentCsv(studentId) {
    downloadFile(`/api/student/download-csv/${studentId}`, `Schedule_${studentId}.csv`);
}

async function downloadFile(url, filename) {
    try {
        const resp = await fetch(url);
        if (resp.ok) {
            const blob = await resp.blob();
            const a = document.createElement('a');
            a.href = window.URL.createObjectURL(blob);
            a.download = filename;
            a.click();
            showToast('System: Exporting Document', 'success');
        } else {
            showToast('Error: File generation failed', 'error');
        }
    } catch (e) { showToast('System Error: Export Failed', 'error'); }
}

function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast-item toast-${type}`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 400); }, 3000);
}
