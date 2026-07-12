# jDbx Beekeeper 风格 UI / 操作逻辑重构设计

**日期：** 2026-07-12  
**状态：** 已批准（对话确认）  
**方法：** 方案 1 — 渐进增强（在现有 `jdbx-app` 上重构，不新建 `jdbx-ui` 模块）

---

## 1. 背景与目标

### 1.1 现状

jDbx 是 Java 21 + JavaFX 桌面中间件/数据库客户端。插件与核心 API 已具备，但 UI 仍是早期壳层：

- `BorderPane` + `MenuBar` + `TreeView` + `TextArea`，观感偏传统
- `MainController` 职责过重；侧栏搜索/`[+]`/新建连接等控件未接线
- 查询在 FX 线程同步执行，易卡死 UI
- 连接配置仅内存保存，重启丢失
- 设计文档（`docs/UI设计文档.md`）已对齐 Beekeeper，实现未跟上

### 1.2 目标

对齐 Beekeeper Studio 的核心体验：**扁平连接侧栏 + Tab 工作区 + 可拖拽编辑器/结果区 + 快捷键驱动 + 深色默认**，并修通操作逻辑（持久化、异步查询、状态反馈）。

### 1.3 非目标

- 不新建 `jdbx-ui` 模块，不把 UI 迁入 `jdbx-core/ui`
- 不引入 VS Code 式 Activity Bar
- 不在本期实现 WebView/Monaco 编辑器（接口预留）
- 不重写 Backup / Compare / Performance 等次要工具对话框
- 不在本期做密码加密 / 密钥链

---

## 2. 已确认决策

| 决策点 | 选择 |
|--------|------|
| 范围 | 全面设计（壳 + 行为 + 编辑器），分阶段实现 |
| 编辑器 | 一期 `SqlEditorPane`（原生 FX）；`SqlEditor` 接口预留 WebView |
| 侧栏 | 扁平连接列表（非按类型分组） |
| 主题 | 默认 Dark |
| 架构路径 | 渐进增强现有 `jdbx-app` |

---

## 3. 主壳布局

```
┌──────────────────────────────────────────────────────────────┐
│ MenuBar（文件 / 连接 / 查询 / 工具 / 帮助）                      │
├────────────────┬─────────────────────────────────────────────┤
│ Sidebar ~280px │  TabBar（查询 Tab / 表数据 Tab）               │
│ 搜索 + [+]     ├─────────────────────────────────────────────┤
│ 扁平连接树     │  活动 Tab：                                   │
│ ● 已连 / ○ 未连│  ┌─ SQL Editor ───────────────────────────┐ │
│   └ db/tables  │  ├──── SplitPane（可拖） ──────────────────┤ │
│ [新建连接]     │  │ Results：结果 | 消息 |（可选）历史        │ │
├────────────────┴─────────────────────────────────────────────┤
│ StatusBar：就绪 · 连接(色点) · DB · 行数 · 耗时 · 主题入口      │
└──────────────────────────────────────────────────────────────┘
```

**约定：**

- 无 Activity Bar；侧栏即连接面板，可折叠、可拖宽
- 连接类型用 Ikonli 图标 + 用户色条区分；状态点：绿=已连 / 灰=未连 / 红=失败
- 查询 Tab：上编辑器、下结果，`SplitPane` 可拖
- 启动加载 `dark.css`；可切换 Light / System

---

## 4. 组件拆分与职责

全部留在 `jdbx-app`，从 `MainController` 拆出清晰边界：

| 组件 | 职责 |
|------|------|
| `MainShellController`（可由现有 `MainController` 演进改名，或保留类名、收窄职责） | 壳组装、菜单路由、全局快捷键、主题切换 |
| `ConnectionSidebarController` | 扁平树、搜索、新建/编辑/删除、双击连接、右键、折叠 |
| `WorkspaceController` | Tab 开/关/切；当前连接与活动 Tab 绑定 |
| `QueryTabController` | 编辑器 ↔ 异步执行 ↔ 结果；状态上报 |
| `SqlEditor`（接口） | `getText` / `setText` / `getSelection` / `focus`；一期 `SqlEditorPane` |
| `ResultPanelController`（由现有 `ResultTableController` 演进，不平行新建第二套结果控制器） | 结果表 / 消息 / 可选历史；导出、复制 |
| `StatusBarController` | 连接、DB、行数、耗时、忙碌态 |
| `ConnectionDialogController` | 新建与编辑共用；测试连接；色条 |

**边界规则：**

- UI 只通过 `jdbx-core` 的 `Connection` / `ConnectionManager` / `PluginManager` 访问数据
- 每个 Query Tab 绑定一个 `connectionId`；断开后标记不可执行，不强制关 Tab
- Sidebar 与 Workspace 通过回调/轻量事件通信，不互相持有内部控件

**本期保留原样：** Backup / Compare / Performance / Chart 等菜单入口与现有实现。

---

## 5. 操作行为与数据流

### 5.1 连接

1. 启动从 `~/.jdbx/connections.json` 加载（密码本期仍明文，与现有 `ConnectionConfig` 一致）
2. 侧栏扁平列表；搜索实时过滤名称 / 主机 / 类型
3. 双击连接 → 后台 `connect()` → 成功展开库表并打开/聚焦 Query Tab；失败红点 + StatusBar
4. 右键连接：连接/断开、新建查询、编辑、删除、刷新
5. 双击表 → Table Data Tab；右键表：查看数据 / 查看结构 / 生成 `SELECT *` 到新查询

### 5.2 查询

1. `Ctrl+Enter`：有选区执行选区，否则执行当前语句（分号切分）
2. `Ctrl+Shift+Enter`：执行全部
3. 一律 `javafx.concurrent.Task` 后台执行；UI 显示忙碌；支持取消（能中断则中断，否则丢弃过期结果）
4. SELECT 类 → 结果 Tab；DML/DDL → 消息 Tab（影响行数 + 耗时）
5. 写入按 `connectionId` 隔离的查询历史

### 5.3 编辑器与结果

- 新建查询绑定侧栏当前选中连接；无选中则提示先选连接
- 工具栏：执行 / 执行选中 / 格式化 / 清空；格式化结果写回当前编辑器
- 结果遵守 `maxRows` 截断并提示；支持复制选中行（TSV）、导出 CSV

### 5.4 反馈

- 连接/查询错误：消息区 + StatusBar，避免频繁 `Alert`
- 成功操作：StatusBar 短暂提示

---

## 6. 数据与持久化

### 6.1 `connections.json`

路径：`~/.jdbx/connections.json`  
由 `DefaultConnectionManager`（或等价持久化层）在增删改后保存，启动时加载。

建议结构（实现时可微调字段名，但语义不变）：

```json
{
  "version": 1,
  "connections": [
    {
      "id": "uuid",
      "name": "MyProd",
      "type": "mysql",
      "host": "127.0.0.1",
      "port": 3306,
      "database": "app",
      "username": "root",
      "password": "",
      "color": "#0078D4",
      "ssl": false
    }
  ]
}
```

### 6.2 主题

- 默认：`DARK`
- 用户选择写入现有 config（`~/.jdbx/config.json`），下次启动恢复

---

## 7. `SqlEditor` 接口（一期 / 二期边界）

```java
public interface SqlEditor {
    String getText();
    void setText(String text);
    String getSelection();
    void replaceSelection(String text);
    void requestFocus();
    javafx.scene.Node getNode();
}
```

- **Phase 1：** `SqlEditorPane` 实现（语法高亮叠加）
- **Phase 2：** `SqlEditorWebView`（或 Monaco）实现同一接口，Query Tab 无感切换

---

## 8. 分阶段与验收

### Phase 1（本次实现重点）

- 主壳布局、侧栏折叠/拖宽、扁平树、搜索与新建接线
- Query `SplitPane`、异步查询、快捷键
- `SqlEditor` + `SqlEditorPane`
- `connections.json`、默认 Dark、Ikonli 基础图标
- StatusBar 状态同步
- Controller 按 §4 拆分落地

**验收：**

1. 重启后连接仍在；双击可连；侧栏可搜
2. 查询不卡 UI；快捷键可用；结果/消息正确
3. 深色主界面具备 Beekeeper 式侧栏+工作区+底栏结构
4. 旧工具菜单仍可用

### Phase 2（设计预留）

WebView 编辑器、自动完成、结果复制完善、表行内编辑、历史回放到编辑器

### Phase 3（抛光）

密码加密、连接管理 UI 现代化、大数据虚拟滚动、插件贡献菜单

---

## 9. 关键文件（实现入口）

| 文件 | 变更意图 |
|------|----------|
| `jdbx-app/.../main.fxml` | Beekeeper 壳布局 |
| `jdbx-app/.../MainController.java` → 拆分为 Shell/Sidebar/Workspace/StatusBar | 职责分离 |
| `jdbx-app/.../query-tab.fxml` | SplitPane |
| `jdbx-app/.../QueryTabController.java` | 异步执行、快捷键、编辑器接口 |
| `jdbx-app/.../SqlEditorPane.java` | 一期编辑器实现 |
| `jdbx-app/.../styles/dark.css` + `light.css` | 壳组件样式与 Dark 默认 |
| `jdbx-core/.../DefaultConnectionManager.java` | `connections.json` 持久化 |
| `jdbx-core/.../DefaultThemeManager.java` / `JdbxApplication` | 默认 Dark |

---

## 10. 风险与缓解

| 风险 | 缓解 |
|------|------|
| `MainController` 拆分易破坏现有菜单 | 先抽 Sidebar/StatusBar，菜单仍由 Shell 转发；每步可运行验证 |
| `SqlEditorPane` 高亮性能 | 大文档可延迟/节流重绘；超长文本降级为纯文本模式 |
| 密码明文存储 | 文档明确为已知债；Phase 3 加密 |
| Ikonli + 模块系统 | 确认 `module-info` 已 `requires` 对应模块后再接线图标 |

---

## 11. 成功标准（产品层面）

全栈开发者打开 jDbx 后，能在 **一分钟内**：选中已存连接 → 执行查询 → 看结果，且界面密度与交互节奏接近 Beekeeper，而不是传统 JavaFX 演示壳。
