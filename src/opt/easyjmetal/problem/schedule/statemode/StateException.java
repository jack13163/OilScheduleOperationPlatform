package opt.easyjmetal.problem.schedule.statemode;

import java.io.Serializable;

/**
 * @author Administrator
 */
public class StateException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    public StateException() {
        super();
    }

    public StateException(String message) {
        super(message);
    }

    public StateException(Throwable cause) {
        super(cause);
    }
}
