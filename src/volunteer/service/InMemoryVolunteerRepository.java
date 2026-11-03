package volunteer.service;

import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerCertificate;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryVolunteerRepository implements VolunteerRepository {
    private final Map<String, Integer> sequences = new HashMap<>();
    private final Map<Integer, Volunteer> volunteers = new LinkedHashMap<>();
    private final Map<Integer, VolunteerTask> tasks = new LinkedHashMap<>();
    private final Map<Integer, VolunteerAssignment> assignments = new LinkedHashMap<>();
    private final Map<Integer, VolunteerHourRecord> hourRecords = new LinkedHashMap<>();
    private final Map<Integer, VolunteerCertificate> certificates = new LinkedHashMap<>();

    @Override
    public synchronized int nextId(String sequence) {
        int next = sequences.getOrDefault(sequence, 0) + 1;
        sequences.put(sequence, next);
        return next;
    }

    @Override public synchronized void save(Volunteer v) { volunteers.put(v.getUserId(), v); }
    @Override public synchronized Optional<Volunteer> findById(int id) { return Optional.ofNullable(volunteers.get(id)); }
    @Override public synchronized List<Volunteer> findAll() { return new ArrayList<>(volunteers.values()); }

    @Override public synchronized void saveTask(VolunteerTask t) { tasks.put(t.getTaskId(), t); }
    @Override public synchronized Optional<VolunteerTask> findTaskById(int id) { return Optional.ofNullable(tasks.get(id)); }
    @Override public synchronized List<VolunteerTask> findAllTasks() { return new ArrayList<>(tasks.values()); }

    @Override public synchronized void saveAssignment(VolunteerAssignment a) { assignments.put(a.getAssignmentId(), a); }
    @Override public synchronized Optional<VolunteerAssignment> findAssignmentById(int id) { return Optional.ofNullable(assignments.get(id)); }
    @Override public synchronized List<VolunteerAssignment> findAllAssignments() { return new ArrayList<>(assignments.values()); }

    @Override
    public synchronized List<VolunteerAssignment> findAssignmentsByVolunteer(int volunteerId) {
        return assignments.values().stream().filter(a -> a.getVolunteerId() == volunteerId).collect(Collectors.toList());
    }

    @Override
    public synchronized List<VolunteerAssignment> findAssignmentsByTask(int taskId) {
        return assignments.values().stream().filter(a -> a.getTaskId() == taskId).collect(Collectors.toList());
    }

    @Override public synchronized void saveHourRecord(VolunteerHourRecord r) { hourRecords.put(r.getRecordId(), r); }
    @Override public synchronized Optional<VolunteerHourRecord> findHourRecordById(int id) { return Optional.ofNullable(hourRecords.get(id)); }
    @Override public synchronized List<VolunteerHourRecord> findAllHourRecords() { return new ArrayList<>(hourRecords.values()); }

    @Override
    public synchronized List<VolunteerHourRecord> findHourRecordsByVolunteer(int volunteerId) {
        return hourRecords.values().stream().filter(r -> r.getVolunteerId() == volunteerId).collect(Collectors.toList());
    }

    @Override public synchronized void saveCertificate(VolunteerCertificate c) { certificates.put(c.getCertificateId(), c); }

    @Override
    public synchronized List<VolunteerCertificate> findCertificatesByVolunteer(int volunteerId) {
        return certificates.values().stream().filter(c -> c.getVolunteerId() == volunteerId).collect(Collectors.toList());
    }
}
