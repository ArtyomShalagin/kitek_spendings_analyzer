package py_interface;

import jep.Jep;
import jep.JepException;

public abstract class PyInterface {
    protected Jep jep;

    protected PyInterface(String scriptsDirKey, String... initScripts) {
        try {
            jep = new Jep();
            String scriptsDir = PyInterfaceProperties.getInstance().getProperty(scriptsDirKey);
            for (String scriptName : initScripts) {
                jep.runScript(scriptsDir + scriptName);
            }
        } catch (JepException e) {
            System.err.println("Unable to initialize Jep: " + e.getMessage());
        }
    }

    public boolean jepInited() {
        return jep != null;
    }

    protected boolean jepInitedOrWarn() {
        if (!jepInited()) {
            System.err.println("Warning: Jep is not initialized");
        }
        return jepInited();
    }
}
