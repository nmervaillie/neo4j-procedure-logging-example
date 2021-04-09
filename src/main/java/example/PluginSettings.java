package example;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.configuration.SettingsDeclaration;
import org.neo4j.graphdb.config.Setting;

import static org.neo4j.configuration.SettingImpl.newBuilder;
import static org.neo4j.configuration.SettingValueParsers.BOOL;

@ServiceProvider
public class PluginSettings implements SettingsDeclaration {

    public static final Setting<Boolean> logsEnabled = newBuilder("example.logsEnabled", BOOL, false).dynamic().build();

}
