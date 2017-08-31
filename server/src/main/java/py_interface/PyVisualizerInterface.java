package py_interface;

public class PyVisualizerInterface extends PyInterface {

    private static final String[] initScripts = {};

    public PyVisualizerInterface() {
        super(initScripts);
        if (!jepInited()) {
            return;
        }

    }
}
