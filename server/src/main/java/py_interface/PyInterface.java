package py_interface;

import jep.JepException;
import util.JepHolder;

import java.util.concurrent.ExecutionException;

public abstract class PyInterface {

    protected PyInterface(String scriptsDirKey, String... initScripts) {
        try {
            JepHolder.execute(jep -> {
                try {
                    if (jep == null) {
                        return null;
                    }
                    String scriptsDir = PyInterfaceProperties.getInstance().getProperty(scriptsDirKey);
                    for (String scriptName : initScripts) {
                        jep.runScript(scriptsDir + scriptName);
                    }
                } catch (JepException e) {
                    System.err.println("Unable to initialize py interface: " + e.getMessage());
                }
                return null;
            });
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Unable to initialize python interface: " + e.getMessage());
        }
    }
}
