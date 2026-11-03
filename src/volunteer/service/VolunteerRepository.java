package volunteer.service;

import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerCertificate;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository {
    int nextId(String sequence);

    void save(Volunteer volunteer);
    Optional<Volunteer> findById(int volunteerId);
    List<Volunteer> findAll();

    void saveTask(VolunteerTask task);
    Optional<VolunteerTask> findTaskById(int taskId);
    List<VolunteerTask> findAllTasks();

    void saveAssignment(VolunteerAssignment assignment);
    Optional<VolunteerAssignment> findAssignmentById(int assignmentId);
    List<VolunteerAssignment> findAssignmentsByVolunteer(int volunteerId);
    List<VolunteerAssignment> findAssignmentsByTask(int taskId);
    List<VolunteerAssignment> findAllAssignments();

    void saveHourRecord(VolunteerHourRecord record);
    Optional<VolunteerHourRecord> findHourRecordById(int recordId);
    List<VolunteerHourRecord> findHourRecordsByVolunteer(int volunteerId);
    List<VolunteerHourRecord> findAllHourRecords();

    void saveCertificate(VolunteerCertificate certificate);
    List<VolunteerCertificate> findCertificatesByVolunteer(int volunteerId);
}
