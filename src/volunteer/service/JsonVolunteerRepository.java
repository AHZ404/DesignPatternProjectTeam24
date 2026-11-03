package volunteer.service;

import database.JsonRepository;
import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerCertificate;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonVolunteerRepository implements VolunteerRepository {
    private final JsonRepository db;

    public JsonVolunteerRepository(JsonRepository db) {
        this.db = Objects.requireNonNull(db);
    }

    @Override
    public int nextId(String sequence) {
        return db.update(d -> "volunteer".equals(sequence) ? d.nextUserId++ : d.nextId++);
    }

    @Override
    public void save(Volunteer v) {
        db.update(d -> d.volunteers.put(v.getUserId(), v));
    }

    @Override
    public Optional<Volunteer> findById(int id) {
        return db.read(d -> Optional.ofNullable(d.volunteers.get(id)));
    }

    @Override
    public List<Volunteer> findAll() {
        return db.read(d -> new ArrayList<>(d.volunteers.values()));
    }

    @Override
    public void saveTask(VolunteerTask t) {
        db.update(d -> d.volunteerTasks.put(t.getTaskId(), t));
    }

    @Override
    public Optional<VolunteerTask> findTaskById(int id) {
        return db.read(d -> Optional.ofNullable(d.volunteerTasks.get(id)));
    }

    @Override
    public List<VolunteerTask> findAllTasks() {
        return db.read(d -> new ArrayList<>(d.volunteerTasks.values()));
    }

    @Override
    public void saveAssignment(VolunteerAssignment a) {
        db.update(d -> d.volunteerAssignments.put(a.getAssignmentId(), a));
    }

    @Override
    public Optional<VolunteerAssignment> findAssignmentById(int id) {
        return db.read(d -> Optional.ofNullable(d.volunteerAssignments.get(id)));
    }

    @Override
    public List<VolunteerAssignment> findAssignmentsByVolunteer(int volunteerId) {
        return db.read(d -> d.volunteerAssignments.values().stream()
                .filter(a -> a.getVolunteerId() == volunteerId).collect(Collectors.toList()));
    }

    @Override
    public List<VolunteerAssignment> findAssignmentsByTask(int taskId) {
        return db.read(d -> d.volunteerAssignments.values().stream()
                .filter(a -> a.getTaskId() == taskId).collect(Collectors.toList()));
    }

    @Override
    public List<VolunteerAssignment> findAllAssignments() {
        return db.read(d -> new ArrayList<>(d.volunteerAssignments.values()));
    }

    @Override
    public void saveHourRecord(VolunteerHourRecord r) {
        db.update(d -> d.volunteerHours.put(r.getRecordId(), r));
    }

    @Override
    public Optional<VolunteerHourRecord> findHourRecordById(int id) {
        return db.read(d -> Optional.ofNullable(d.volunteerHours.get(id)));
    }

    @Override
    public List<VolunteerHourRecord> findHourRecordsByVolunteer(int volunteerId) {
        return db.read(d -> d.volunteerHours.values().stream()
                .filter(r -> r.getVolunteerId() == volunteerId).collect(Collectors.toList()));
    }

    @Override
    public List<VolunteerHourRecord> findAllHourRecords() {
        return db.read(d -> new ArrayList<>(d.volunteerHours.values()));
    }

    @Override
    public void saveCertificate(VolunteerCertificate c) {
        db.update(d -> d.volunteerCertificates.put(c.getCertificateId(), c));
    }

    @Override
    public List<VolunteerCertificate> findCertificatesByVolunteer(int volunteerId) {
        return db.read(d -> d.volunteerCertificates.values().stream()
                .filter(c -> c.getVolunteerId() == volunteerId).collect(Collectors.toList()));
    }
}
