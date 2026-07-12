package dino.jdbx.app.web;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.ConnectionConfig;
import dino.jdbx.core.api.DatabasePlugin;
import dino.jdbx.core.api.QueryHistory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal JSON builders for WebView payloads (avoid JPMS Gson wiring in app module).
 */
public final class WebJson {

    private WebJson() {
    }

    public static String connectionsJson(
            Collection<ConnectionConfig> configs,
            Map<String, Connection> active,
            Set<String> failed,
            Map<String, List<EntityGroup>> entitiesByConn) {

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (ConnectionConfig c : configs) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            String status = "disconnected";
            if (failed != null && failed.contains(c.getId())) {
                status = "error";
            } else if (active != null && active.containsKey(c.getId())) {
                status = "connected";
            }
            List<EntityGroup> entities = entitiesByConn != null ? entitiesByConn.get(c.getId()) : null;
            boolean expanded = entities != null && !entities.isEmpty();

            sb.append('{')
                    .append("\"id\":").append(str(c.getId())).append(',')
                    .append("\"name\":").append(str(c.getName())).append(',')
                    .append("\"type\":").append(str(c.getType())).append(',')
                    .append("\"host\":").append(str(c.getHost())).append(',')
                    .append("\"port\":").append(c.getPort()).append(',')
                    .append("\"database\":").append(str(c.getDatabase())).append(',')
                    .append("\"color\":").append(str(resolveColor(c))).append(',')
                    .append("\"status\":").append(str(status)).append(',')
                    .append("\"expanded\":").append(expanded).append(',')
                    .append("\"entities\":").append(entitiesJson(entities))
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    public static String historyJson(List<QueryHistory> history) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        if (history != null) {
            for (QueryHistory h : history) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('{')
                        .append("\"sql\":").append(str(h.getSql())).append(',')
                        .append("\"connectionId\":").append(str(h.getConnectionId())).append(',')
                        .append("\"connectionName\":").append(str(h.getConnectionId())).append(',')
                        .append("\"executedAt\":").append(str(h.getExecutedAt() != null ? h.getExecutedAt().toString() : "")).append(',')
                        .append("\"success\":").append(h.isSuccess())
                        .append('}');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private static String entitiesJson(List<EntityGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (EntityGroup g : groups) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append("{\"label\":").append(str(g.label())).append(",\"items\":[");
            boolean firstItem = true;
            for (EntityItem item : g.items()) {
                if (!firstItem) {
                    sb.append(',');
                }
                firstItem = false;
                sb.append("{\"name\":").append(str(item.name()))
                        .append(",\"kind\":").append(str(item.kind()))
                        .append('}');
            }
            sb.append("]}");
        }
        sb.append(']');
        return sb.toString();
    }

    private static String resolveColor(ConnectionConfig config) {
        if (config.getColor() != null && !config.getColor().isBlank()) {
            return config.getColor();
        }
        if (config.getType() == null) {
            return "#efc524";
        }
        return switch (config.getType().toLowerCase()) {
            case "mysql" -> "#00758F";
            case "postgresql" -> "#336791";
            case "sqlite" -> "#0F7B8A";
            case "redis" -> "#DC382D";
            case "mongodb" -> "#47A248";
            case "elasticsearch" -> "#FED10A";
            default -> "#efc524";
        };
    }

    public static String pluginsJson(Collection<DatabasePlugin> plugins) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        if (plugins != null) {
            for (DatabasePlugin p : plugins) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('{')
                        .append("\"id\":").append(str(p.getId())).append(',')
                        .append("\"name\":").append(str(p.getName())).append(',')
                        .append("\"defaultPort\":").append(p.getDefaultPort())
                        .append('}');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public static String connectionFormJson(ConnectionConfig c) {
        if (c == null) {
            return "{}";
        }
        return "{"
                + "\"id\":" + str(c.getId()) + ","
                + "\"name\":" + str(c.getName()) + ","
                + "\"type\":" + str(c.getType()) + ","
                + "\"host\":" + str(c.getHost()) + ","
                + "\"port\":" + c.getPort() + ","
                + "\"database\":" + str(c.getDatabase()) + ","
                + "\"username\":" + str(c.getUsername()) + ","
                + "\"password\":" + str(c.getPassword()) + ","
                + "\"color\":" + str(c.getColor()) + ","
                + "\"ssl\":" + c.isUseSsl()
                + "}";
    }

    public static ConnectionConfig parseConnection(String json) {
        ConnectionConfig config = new ConnectionConfig();
        String id = jsonStringField(json, "id");
        if (id != null && !id.isBlank()) {
            config.setId(id);
        }
        config.setName(jsonStringField(json, "name"));
        config.setType(jsonStringField(json, "type"));
        config.setHost(jsonStringField(json, "host"));
        config.setPort(jsonIntField(json, "port"));
        config.setDatabase(jsonStringField(json, "database"));
        config.setUsername(jsonStringField(json, "username"));
        config.setPassword(jsonStringField(json, "password"));
        config.setColor(jsonStringField(json, "color"));
        config.setUseSsl(jsonBooleanField(json, "ssl"));
        return config;
    }

    public static String jsString(String value) {
        return str(value);
    }

    private static final Pattern STRING_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern NUMBER_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*(-?\\d+)");
    private static final Pattern BOOL_FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*(true|false)");

    private static String jsonStringField(String json, String key) {
        if (json == null) {
            return null;
        }
        Matcher m = STRING_FIELD.matcher(json);
        while (m.find()) {
            if (key.equals(m.group(1))) {
                return unescapeJson(m.group(2));
            }
        }
        return null;
    }

    private static int jsonIntField(String json, String key) {
        if (json == null) {
            return 0;
        }
        Matcher m = NUMBER_FIELD.matcher(json);
        while (m.find()) {
            if (key.equals(m.group(1))) {
                try {
                    return Integer.parseInt(m.group(2));
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private static boolean jsonBooleanField(String json, String key) {
        if (json == null) {
            return false;
        }
        Matcher m = BOOL_FIELD.matcher(json);
        while (m.find()) {
            if (key.equals(m.group(1))) {
                return Boolean.parseBoolean(m.group(2));
            }
        }
        return false;
    }

    private static String unescapeJson(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < value.length()) {
                            String hex = value.substring(i + 1, i + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String str(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public record EntityItem(String name, String kind) {
    }

    public record EntityGroup(String label, List<EntityItem> items) {
    }
}
