package example;

import static example.PluginSettings.logsEnabled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.STRING;
import static org.neo4j.configuration.SettingValueParsers.listOf;

import com.neo4j.harness.EnterpriseNeo4jBuilders;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoggingTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();
    private Neo4j embeddedDatabaseServer;
    private Driver driver;

    @BeforeAll
    void initializeNeo4j() {
        // dbms.setConfigValue is only available in enterprise edition
        this.embeddedDatabaseServer = EnterpriseNeo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withFunction(Join.class)
                .withConfig(newBuilder("dbms.security.procedures.unrestricted", listOf(STRING), List.of()).build(), List.of("example.*"))
                .withConfig(logsEnabled, true)
                .build();

        driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
    }

    @Test
    void should_dynamically_control_logging_in_procedure() {

        callProcedure();
        assertThat(countOccurrencesInLogs()).isEqualTo(1);

        // disable the logging in function
        try(Session session = driver.session()) {
            session.run( "CALL dbms.setConfigValue('example.logsEnabled', 'false')").consume();
        }

        callProcedure();
        assertThat(countOccurrencesInLogs()).isEqualTo(1);

        // re-enable logging in function
        try(Session session = driver.session()) {
            session.run( "CALL dbms.setConfigValue('example.logsEnabled', 'true')").consume();
        }

        callProcedure();
        assertThat(countOccurrencesInLogs()).isEqualTo(2);
    }

    private long countOccurrencesInLogs() {
        ByteArrayOutputStream logs = new ByteArrayOutputStream();
        embeddedDatabaseServer.printLogs(new PrintStream(logs));
        Pattern pattern = Pattern.compile("Hello,World");
        Stream<MatchResult> occurrences = pattern.matcher(logs.toString()).results();
        // logs contains both neo4j.log and debug.log, string appears twice for every invocation
        return occurrences.count() / 2;
    }

    private void callProcedure() {
        try(Session session = driver.session()) {
            String result = session.run( "RETURN example.join(['Hello', 'World']) AS result").single().get("result").asString();
        }
    }
}