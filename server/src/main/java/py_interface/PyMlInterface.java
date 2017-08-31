package py_interface;

public class PyMlInterface extends PyInterface {

    private static final String[] initScripts = {};

    public PyMlInterface() {
        super(initScripts);
        if (!jepInited()) {
            return;
        }

    }
}
