package volunteer.service;

import volunteer.model.Volunteer;

public interface VolunteerNotifier {
    void notify(Volunteer recipient, String subject, String message);
}
