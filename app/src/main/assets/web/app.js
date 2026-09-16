// Default Seed Data
const initialTeachers = [
    {
        id: "t1",
        name: "Dr. Sarah Jenkins",
        title: "Associate Professor",
        department: "Computer Science",
        deskNumber: "Desk #304, Block B",
        timings: "Mon-Fri: 10:00 AM - 01:00 PM",
        status: "AT_DESK",
        email: "s.jenkins@university.edu"
    },
    {
        id: "t2",
        name: "Prof. David Miller",
        title: "Senior Mentor",
        department: "Data Science",
        deskNumber: "Stall #102, Innovation Lab",
        timings: "Mon-Thu: 02:00 PM - 05:00 PM",
        status: "BUSY",
        email: "d.miller@university.edu"
    },
    {
        id: "t3",
        name: "Dr. Emily Carter",
        title: "Department Chair",
        department: "Software Engineering",
        deskNumber: "Desk #412, Tech Tower",
        timings: "Tue-Fri: 11:00 AM - 03:00 PM",
        status: "IN_CLASS",
        email: "e.carter@university.edu"
    },
    {
        id: "t4",
        name: "Prof. Robert Chen",
        title: "Assistant Professor",
        department: "Cybersecurity",
        deskNumber: "Desk #208, Block C",
        timings: "Mon-Wed: 09:30 AM - 12:30 PM",
        status: "AWAY",
        email: "r.chen@university.edu"
    }
];

const initialAppointments = [
    {
        id: "a101",
        teacherId: "t1",
        teacherName: "Dr. Sarah Jenkins",
        studentName: "Alex Rivera",
        studentEmail: "alex@student.edu",
        date: "2026-09-18",
        slot: "10:00 AM - 10:30 AM",
        purpose: "AI Model Review",
        status: "CONFIRMED"
    }
];

let activeTeacherId = "t1";

// Load or Initialize Local Storage
function getTeachers() {
    const data = localStorage.getItem('edudesk_teachers');
    return data ? JSON.parse(data) : initialTeachers;
}

function saveTeachers(teachers) {
    localStorage.setItem('edudesk_teachers', JSON.stringify(teachers));
}

function getAppointments() {
    const data = localStorage.getItem('edudesk_appointments');
    return data ? JSON.parse(data) : initialAppointments;
}

function saveAppointments(apps) {
    localStorage.setItem('edudesk_appointments', JSON.stringify(apps));
}

// App Initialization
document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('edudesk_teachers')) {
        saveTeachers(initialTeachers);
    }
    if (!localStorage.getItem('edudesk_appointments')) {
        saveAppointments(initialAppointments);
    }

    populateWebTeacherSelect();
    checkWebUpdate();
});

function checkWebUpdate() {
    setTimeout(() => {
        const banner = document.getElementById('web-update-banner');
        if (banner) banner.style.display = 'block';
    }, 1500);
}

function triggerWebUpdate() {
    window.location.href = "https://raw.githubusercontent.com/predator-27/Project_X/main/releases/app-debug.apk";
}

function closeWebUpdate() {
    const banner = document.getElementById('web-update-banner');
    if (banner) banner.style.display = 'none';
}

function populateWebTeacherSelect() {
    const teachers = getTeachers();
    const select = document.getElementById('web-teacher-select');
    if (!select) return;
    select.innerHTML = '';
    teachers.forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = `${t.name} (${t.department})`;
        select.appendChild(opt);
    });
}

function switchLoginTab(role) {
    document.getElementById('tab-btn-teacher').classList.remove('active');
    document.getElementById('tab-btn-student').classList.remove('active');
    document.getElementById('form-login-teacher').style.display = 'none';
    document.getElementById('form-login-student').style.display = 'none';

    if (role === 'teacher') {
        document.getElementById('tab-btn-teacher').classList.add('active');
        document.getElementById('form-login-teacher').style.display = 'block';
    } else {
        document.getElementById('tab-btn-student').classList.add('active');
        document.getElementById('form-login-student').style.display = 'block';
    }
}

function handleTeacherLogin(event) {
    event.preventDefault();
    const teacherId = document.getElementById('web-teacher-select').value;
    const teachers = getTeachers();
    const teacher = teachers.find(t => t.id === teacherId) || teachers[0];

    activeTeacherId = teacher.id;

    document.getElementById('view-login').classList.remove('active');
    document.getElementById('view-admin').classList.add('active');

    document.getElementById('user-session-bar').style.display = 'flex';
    document.getElementById('session-user-badge').textContent = `👨‍🏫 ${teacher.name}`;

    loadAdminTeacher();
}

function handleStudentLogin(event) {
    event.preventDefault();
    const email = document.getElementById('web-student-email').value;

    document.getElementById('view-login').classList.remove('active');
    document.getElementById('view-student').classList.add('active');

    document.getElementById('user-session-bar').style.display = 'flex';
    document.getElementById('session-user-badge').textContent = `👨‍🎓 ${email}`;

    renderTeachers();
    renderAppointments();
}

function webLogout() {
    document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
    document.getElementById('view-login').classList.add('active');
    document.getElementById('user-session-bar').style.display = 'none';
}

// Student View Navigation
function switchStudentTab(tab) {
    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    if (tab === 'directory') {
        document.querySelector('.nav-tab:nth-child(1)').classList.add('active');
        document.getElementById('tab-directory').classList.add('active');
    } else {
        document.querySelector('.nav-tab:nth-child(2)').classList.add('active');
        document.getElementById('tab-appointments').classList.add('active');
        renderAppointments();
    }
}

// Render Teacher Cards in Directory
function renderTeachers() {
    const teachers = getTeachers();
    const query = document.getElementById('search-input').value.toLowerCase();
    const dept = document.getElementById('dept-filter').value;

    const filtered = teachers.filter(t => {
        const matchesQuery = t.name.toLowerCase().includes(query) ||
            t.department.toLowerCase().includes(query) ||
            t.deskNumber.toLowerCase().includes(query);
        const matchesDept = dept === 'All' || t.department === dept;
        return matchesQuery && matchesDept;
    });

    const grid = document.getElementById('teachers-grid');
    grid.innerHTML = '';

    filtered.forEach(t => {
        const card = document.createElement('div');
        card.className = 'card teacher-card';
        card.innerHTML = `
            <div>
                <div class="card-header">
                    <div>
                        <div class="teacher-name">${t.name}</div>
                        <div class="teacher-title">${t.title} • ${t.department}</div>
                    </div>
                    <span class="status-badge status-${t.status}">${t.status.replace('_', ' ')}</span>
                </div>
                <div class="card-info" style="margin-top: 0.75rem;">
                    <p><strong>📍 Desk:</strong> ${t.deskNumber}</p>
                    <p><strong>🕒 Timings:</strong> ${t.timings}</p>
                </div>
            </div>
            <button class="primary-btn" onclick="openBookingModal('${t.id}')">Book Appointment</button>
        `;
        grid.appendChild(card);
    });
}

// Render Student Appointments
function renderAppointments() {
    const appointments = getAppointments();
    const list = document.getElementById('appointments-list');
    const badge = document.getElementById('app-count-badge');
    badge.textContent = appointments.length;

    list.innerHTML = '';
    if (appointments.length === 0) {
        list.innerHTML = '<p style="color: var(--text-muted);">No appointments booked yet.</p>';
        return;
    }

    appointments.forEach(a => {
        const card = document.createElement('div');
        card.className = 'card';
        card.style.marginBottom = '1rem';
        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <h4 style="font-size: 1rem;">${a.teacherName}</h4>
                <span class="status-badge" style="background: #e2e8f0; color: #334155;">${a.status}</span>
            </div>
            <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">📅 ${a.date} @ ${a.slot}</p>
            <p style="font-size: 0.85rem; margin-top: 0.25rem;"><strong>Topic:</strong> ${a.purpose}</p>
        `;
        list.appendChild(card);
    });
}

// Modal Booking
function openBookingModal(teacherId) {
    const teachers = getTeachers();
    const teacher = teachers.find(t => t.id === teacherId);
    if (!teacher) return;

    document.getElementById('modal-teacher-id').value = teacher.id;
    document.getElementById('modal-teacher-name').textContent = `Book Appointment with ${teacher.name}`;
    document.getElementById('modal-teacher-desk').textContent = `Desk Location: ${teacher.deskNumber}`;

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('booking-date').value = today;

    document.getElementById('booking-modal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('booking-modal').style.display = 'none';
}

function handleBookingSubmit(event) {
    event.preventDefault();
    const teacherId = document.getElementById('modal-teacher-id').value;
    const teachers = getTeachers();
    const teacher = teachers.find(t => t.id === teacherId);

    const newApp = {
        id: 'a' + Date.now().toString().slice(-4),
        teacherId: teacher.id,
        teacherName: teacher.name,
        studentName: document.getElementById('student-name').value,
        studentEmail: document.getElementById('student-email').value,
        date: document.getElementById('booking-date').value,
        slot: document.getElementById('booking-slot').value,
        purpose: document.getElementById('booking-purpose').value,
        status: 'PENDING'
    };

    const appointments = getAppointments();
    appointments.unshift(newApp);
    saveAppointments(appointments);

    closeModal();
    alert('Appointment requested successfully!');
    switchStudentTab('appointments');
}

// Admin Teacher View
function loadAdminTeacher() {
    const teachers = getTeachers();
    const teacher = teachers.find(t => t.id === activeTeacherId) || teachers[0];

    if (!teacher) return;

    const badge = document.getElementById('current-status-badge');
    badge.textContent = teacher.status.replace('_', ' ');
    badge.className = `status-badge status-${teacher.status}`;

    document.getElementById('admin-desk-input').value = teacher.deskNumber;
    document.getElementById('admin-timings-input').value = teacher.timings;

    renderAdminRequests(teacher.id);
}

function updateAdminStatus(newStatus) {
    const teachers = getTeachers();
    const idx = teachers.findIndex(t => t.id === activeTeacherId);
    if (idx !== -1) {
        teachers[idx].status = newStatus;
        saveTeachers(teachers);
        loadAdminTeacher();
    }
}

function saveAdminInfo() {
    const teachers = getTeachers();
    const idx = teachers.findIndex(t => t.id === activeTeacherId);
    if (idx !== -1) {
        teachers[idx].deskNumber = document.getElementById('admin-desk-input').value;
        teachers[idx].timings = document.getElementById('admin-timings-input').value;
        saveTeachers(teachers);
        alert('Desk information saved!');
    }
}

function renderAdminRequests(teacherId) {
    const appointments = getAppointments().filter(a => a.teacherId === teacherId);
    const list = document.getElementById('admin-requests-list');
    list.innerHTML = '';

    if (appointments.length === 0) {
        list.innerHTML = '<p style="color: var(--text-muted);">No requests for this teacher.</p>';
        return;
    }

    appointments.forEach(a => {
        const item = document.createElement('div');
        item.style.borderBottom = '1px solid var(--border-color)';
        item.style.padding = '0.75rem 0';
        item.innerHTML = `
            <div style="display: flex; justify-content: space-between;">
                <strong>${a.studentName} (${a.studentEmail})</strong>
                <span class="status-badge">${a.status}</span>
            </div>
            <p style="font-size: 0.85rem; color: var(--text-muted);">📅 ${a.date} @ ${a.slot}</p>
            <p style="font-size: 0.85rem;"><strong>Purpose:</strong> ${a.purpose}</p>
            ${a.status === 'PENDING' ? `
                <div style="margin-top: 0.5rem; display: flex; gap: 0.5rem;">
                    <button class="primary-btn" style="background: #16a34a; padding: 0.3rem 0.8rem; font-size: 0.8rem;" onclick="updateAppStatus('${a.id}', 'CONFIRMED')">Accept</button>
                    <button class="primary-btn" style="background: #dc2626; padding: 0.3rem 0.8rem; font-size: 0.8rem;" onclick="updateAppStatus('${a.id}', 'CANCELLED')">Decline</button>
                </div>
            ` : ''}
        `;
        list.appendChild(item);
    });
}

function updateAppStatus(appId, newStatus) {
    const appointments = getAppointments();
    const idx = appointments.findIndex(a => a.id === appId);
    if (idx !== -1) {
        appointments[idx].status = newStatus;
        saveAppointments(appointments);
        loadAdminTeacher();
    }
}
