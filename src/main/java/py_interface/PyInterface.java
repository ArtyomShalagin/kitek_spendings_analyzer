package py_interface;

import jep.Jep;
import jep.JepException;

public abstract class PyInterface {
    protected Jep jep;

    protected PyInterface(String... initScripts) {
        try {
            jep = new Jep();
            for (String scriptPath : initScripts) {
                jep.runScript(scriptPath);
            }
        } catch (JepException e) {
            System.err.println("Unable to initialize Jep: " + e.getMessage());
        }
    }

    protected boolean jepInited() {
        return jep != null;
    }
}
