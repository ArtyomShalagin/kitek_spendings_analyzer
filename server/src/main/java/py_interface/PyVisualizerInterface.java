package py_interface;

import jep.JepException;
import util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PyVisualizerInterface extends PyInterface {

    private static final String[] initScripts = {"draw_plots.py"};
    private static final String SCRIPTS_DIR_KEY = "visualizer_scripts_dir";

    public PyVisualizerInterface() {
        super(SCRIPTS_DIR_KEY, initScripts);
    }

    /**
     * Get top money-consuming categories
     *
     * @param filename name of .csv file with user data (in /visualization/, we will move that to configs I swear)
     * @param amountOfItems how many top categories we need
     * @return sorted List of Pairs, each pair mapping category to amount of money spent, or null on jep error
     */
    @SuppressWarnings("Duplicates")
    public List<Pair<String, Integer>> maxSpendings(String filename, int amountOfItems) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            Object result = jep.invoke("max_spendings", filename, amountOfItems);
            if (!(result instanceof HashMap)) {
                System.err.println("Error in python interface: unexpected return value in method max_spending");
                return null;
            }
            //noinspection unchecked,ConstantConditions
            HashMap<String, Integer> map = (HashMap<String, Integer>) result;
            return map.keySet().stream()
                    .map(key -> new Pair<>(key, map.get(key)))
                    .sorted(Comparator.comparingInt(p -> p.second))
                    .collect(Collectors.toList());
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Draw plot of weekly spendings
     *
     * @param filename name of .csv file with data (in /visualization/, we will move that to configs I swear)
     * @return name of image file, or null on jep error
     */
    // todo should we read the image here?
    public String weeklySpendings(String filename) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            jep.invoke("days_of_week_spending", filename);
            return filename + "_plot.png";
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Draw plot of total spendings by category
     *
     * @param filename name of .csv file with data (in /visualization/, we will move that to configs I swear)
     * @return name of image file, or null on jep error
     */
    // todo should we read the image here?
    public String categoriesSpendings(String filename) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            jep.invoke("categories_spending", filename);
            return filename + "_plot.png";
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Acts like maxSpendings(filename, Integer.MAX_VALUE) plus draws plot in filename_plot.png
     *
     * @param filename name of .csv file with user data (in /visualization/, we will move that to configs I swear)
     * @return sorted List of Pairs, each pair mapping category to amount of money spent, or null on jep error
     */
    public List<Pair<String, Integer>> generalStats(String filename) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            Object result = jep.invoke("general_stats", filename);
            if (!(result instanceof HashMap)) {
                System.err.println("Error in python interface: unexpected return value in method max_spending");
                return null;
            }
            //noinspection unchecked,ConstantConditions
            HashMap<String, String> map = (HashMap<String, String>) result;
            return map.keySet().stream()
                    .map(key -> new Pair<>(key, Integer.parseInt(map.get(key))))
                    .sorted(Comparator.comparingInt(p -> p.second))
                    .collect(Collectors.toList());
        } catch (JepException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }
}
