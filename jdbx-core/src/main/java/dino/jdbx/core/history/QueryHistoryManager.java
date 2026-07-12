package dino.jdbx.core.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dino.jdbx.core.api.QueryHistory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询历史管理器
 */
public class QueryHistoryManager {

    private static final String HISTORY_DIR = System.getProperty("user.home") + "/.jdbx";
    private static final String HISTORY_FILE = HISTORY_DIR + "/query_history.json";
    private static final int MAX_HISTORY_SIZE = 1000;

    private List<QueryHistory> history;
    private final Gson gson;

    public QueryHistoryManager() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
        this.history = new ArrayList<>();
        loadHistory();
    }

    /**
     * 添加查询历史
     */
    public void addHistory(QueryHistory entry) {
        history.add(0, entry); // 添加到开头

        // 限制历史记录数量
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }

        saveHistory();
    }

    /**
     * 获取所有历史记录
     */
    public List<QueryHistory> getAllHistory() {
        return new ArrayList<>(history);
    }

    /**
     * 获取指定连接的历史记录
     */
    public List<QueryHistory> getHistoryByConnection(String connectionId) {
        return history.stream()
            .filter(h -> h.getConnectionId().equals(connectionId))
            .collect(Collectors.toList());
    }

    /**
     * 搜索历史记录
     */
    public List<QueryHistory> searchHistory(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllHistory();
        }

        String lowerKeyword = keyword.toLowerCase();
        return history.stream()
            .filter(h -> h.getSql().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }

    /**
     * 清空历史记录
     */
    public void clearHistory() {
        history.clear();
        saveHistory();
    }

    /**
     * 清空指定连接的历史记录
     */
    public void clearHistoryByConnection(String connectionId) {
        history.removeIf(h -> h.getConnectionId().equals(connectionId));
        saveHistory();
    }

    /**
     * 删除指定历史记录
     */
    public void deleteHistory(String id) {
        history.removeIf(h -> h.getId().equals(id));
        saveHistory();
    }

    /**
     * 加载历史记录
     */
    private void loadHistory() {
        Path path = Paths.get(HISTORY_FILE);
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = new FileReader(HISTORY_FILE)) {
            Type listType = new TypeToken<List<QueryHistory>>() {}.getType();
            List<QueryHistory> loaded = gson.fromJson(reader, listType);
            if (loaded != null) {
                this.history = loaded;
            }
        } catch (Exception e) {
            System.err.println("加载查询历史失败: " + e.getMessage());
        }
    }

    /**
     * 保存历史记录
     */
    private void saveHistory() {
        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(HISTORY_DIR));

            try (Writer writer = new FileWriter(HISTORY_FILE)) {
                gson.toJson(history, writer);
            }
        } catch (Exception e) {
            System.err.println("保存查询历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取历史记录数量
     */
    public int size() {
        return history.size();
    }

    /**
     * 获取最近的历史记录
     */
    public List<QueryHistory> getRecentHistory(int count) {
        int size = Math.min(count, history.size());
        return new ArrayList<>(history.subList(0, size));
    }
}
