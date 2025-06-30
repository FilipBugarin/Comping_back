package hr.demo.demoProject.config.exception;

public class DemoProjectAlreadyExistsException extends DemoProjectException {
    public DemoProjectAlreadyExistsException(String message) {
        super(message);
    }
}
