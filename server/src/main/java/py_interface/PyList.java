package py_interface;

import jep.Jep;
import jep.JepException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Passing java List to python method via Jep's invoke results in PyJList argument type
 * which is not very convenient. Better use this when lists are not that large
 */
public class PyList {
    public final String name = PyUtil.randomIdentifier();

    public <T> PyList(List<T> data, Function<T, String> converter, Jep jep) throws JepException {
        String args = data.stream()
                .map(converter)
                .collect(Collectors.joining(", "));
        jep.eval(String.format("%s = [%s]", name, args));
    }
}
