package br.edu.utfpr.tsi.xenon.domain.notification.model;

public enum TopicApplication {
    CHANGE_WORKSTATION("/change-workstation"),
    RECOGNIZER("/workstation"),
    APPLICATION("/application");

    private final String topicName;

    TopicApplication(String topicName) {
        this.topicName = topicName;
    }

    public String topicTo(String path) {
        return "%s/%s".formatted(topicName, path);
    }
}
