package volunteer.service;

import volunteer.model.Volunteer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConsoleVolunteerNotifier implements VolunteerNotifier {
    private final List<String> log = new ArrayList<>();

    @Override
    public void notify(Volunteer recipient, String subject, String message) {
        String line = "[Notification to " + recipient.getName() + " <" + recipient.getEmail() + ">] "
                + subject + ": " + message;
        log.add(line);
        System.out.println(line);
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }
}
