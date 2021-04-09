package example;

import java.util.List;
import org.neo4j.common.DependencyResolver;
import org.neo4j.configuration.Config;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

/**
 * This is an example how you can create a simple user-defined function for Neo4j.
 */
public class Join {

	@Context
	public DependencyResolver resolver;

	@Context
	public Log log;

	@UserFunction
	@Description("example.join(['s1','s2',...], delimiter) - join the given strings with the given delimiter.")
	public String join(
			@Name("strings") List<String> strings,
			@Name(value = "delimiter", defaultValue = ",") String delimiter) {
		if (strings == null || delimiter == null) {
			return null;
		}

		Config config = resolver.resolveDependency(Config.class);

		String result = String.join(delimiter, strings);

		boolean logsEnabled = config.get(PluginSettings.logsEnabled);
		if (logsEnabled) {
		    log.info(result);
        }

		return result;
	}
}