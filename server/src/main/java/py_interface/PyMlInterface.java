package py_interface;

import jep.JepException;
import util.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class PyMlInterface extends PyInterface {

    private static final String[] initScripts = {"trainings.py"};
    private static final String SCRIPTS_DIR_KEY = "ml_scripts_dir";

    public PyMlInterface() {
        super(SCRIPTS_DIR_KEY, initScripts);
    }

    /**
     * Retrain using new data from user
     *
     * @param filename name of file for retraining
     * @param newData data to train on: first value is number of category, second number is product name
     */
    public void trainingSvm(String filename, List<Pair<Integer, String>> newData) {
        if (!jepInitedOrWarn()) {
            return;
        }
        try {
            List<Integer> ids = newData.stream()
                    .map(pair -> pair.first)
                    .collect(Collectors.toList());
            List<String> names = newData.stream()
                    .map(pair -> pair.second)
                    .collect(Collectors.toList());
            PyList pyGroupId = new PyList(ids, String::valueOf, jep);
            PyList pyName = new PyList(names, s -> "'" + s + "'", jep);
            jep.eval(String.format("_new_raws = {'GROUP_ID': %s, 'NAME': %s}", pyGroupId.name, pyName.name));
            jep.eval(String.format("training_svm('%s', _new_raws)", filename));
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
        }
    }

    /**
     * Cast machine learning spell to map products to their categories
     *
     * @param products names of products
     * @return return list of numbers with size equal to products.size or null if jep is not ok
     */
    public List<Integer> getCategories(List<String> products) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            PyList pyProducts = new PyList(products, s -> "'" + s + "'", jep);
            Object result = jep.getValue(String.format("predict_categories('svm', %s)", pyProducts.name));
            if (result instanceof List) {
                //noinspection unchecked
                return (List<Integer>) result;
            } else {
                System.err.println("Ml interface returned unexpected value");
                return null;
            }
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }
}
