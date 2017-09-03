package py_interface;

import jep.JepException;
import util.JepHolder;
import util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static util.JepHolder.jepInitedOrWarn;

@SuppressWarnings("unchecked")
public class PyVisualizerInterface extends PyInterface {

    private static final String[] initScripts = {"draw_plots.py"};
    private static final String SCRIPTS_DIR_KEY = "visualizer_scripts_dir";

    public PyVisualizerInterface() {
        super(SCRIPTS_DIR_KEY, initScripts);
    }

    /**
     * Get top money-consuming categories
     *
     * @param filepath name of .csv file with user data
     * @param amountOfItems how many top categories we need
     * @return sorted List of Pairs, each pair mapping category to amount of money spent, or null on jep error
     */
    @SuppressWarnings("Duplicates")
    public List<Pair<String, Integer>> maxSpendings(String filepath, int amountOfItems) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            return (List<Pair<String, Integer>>) JepHolder.execute(jep -> {
                try {
                    if (jep == null) {
                        return null;
                    }
                    Object result = jep.invoke("max_spending", filepath, amountOfItems);
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
            });
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Draw plot of weekly spendings
     *
     * @param filepath name of .csv file with data
     * @return name of image file, or null on jep error
     */
    public String weeklySpendings(String filepath) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            return (String) JepHolder.execute(jep -> {
                try {
                    if (jep == null) {
                        return "";
                    }
                    jep.invoke("days_of_week_spending", filepath);
                    return filepath.substring(0, filepath.lastIndexOf('.')) + "_plot.png";
                } catch (JepException e) {
                    System.err.println("Error in python interface: " + e.getMessage());
                    return null;
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Draw plot of total spendings by category
     *
     * @param filepath name of .csv file with data
     * @return name of image file, or null on jep error
     */
    // todo should we read the image here?
    public String categoriesSpendings(String filepath) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            return (String) JepHolder.execute(jep -> {
                try {
                    if (jep == null) {
                        return "";
                    }
                    jep.invoke("categories_spending", filepath);
                    return filepath.substring(0, filepath.lastIndexOf('.')) + "_plot.png";
                } catch (JepException e) {
                    System.err.println("Error in python interface: " + e.getMessage());
                    return null;
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }

    /**
     * Acts like maxSpendings(filepath, Integer.MAX_VALUE) plus draws plot in filename_plot.png
     *
     * @param filepath name of .csv file with user data
     * @return sorted List of Pairs, each pair mapping category to amount of money spent, or null on jep error
     */
    public List<Pair<String, Integer>> generalStats(String filepath) {
        if (!jepInitedOrWarn()) {
            return null;
        }
        try {
            return (List<Pair<String, Integer>>) JepHolder.execute(jep -> {
                try {
                    if (jep == null) {
                        return null;
                    }
                    Object result = jep.invoke("general_stats", filepath);
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
            });
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Error in python interface: " + e.getMessage());
            return null;
        }
    }
}
