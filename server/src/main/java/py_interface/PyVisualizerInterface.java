package py_interface;

public class PyVisualizerInterface extends PyInterface {

    private static final String[] initScripts = {};
    private static final String SCRIPTS_DIR_KEY = "visualizer_scripts_dir";

    public PyVisualizerInterface() {
        super(SCRIPTS_DIR_KEY, initScripts);
    }
}
