import java.util.List;
import java.util.Map;

abstract public class Macro {
	abstract public Object call(List args, Map<String, Object> scope);
}
