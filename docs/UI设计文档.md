# jDbx UI 设计文档

## 1. 设计理念

### 1.1 设计目标

- **现代化** - 参考 Beekeeper Studio、TablePlus 等现代数据库工具
- **简洁** - 摒弃不必要的视觉元素，专注于内容
- **高效** - 快捷键驱动，减少鼠标操作
- **舒适** - 柔和的配色，适当的留白，不伤眼

### 1.2 设计原则

1. **内容优先** - 数据和查询是核心，UI 元素不应喧宾夺主
2. **一致性** - 相同操作使用相同的交互模式
3. **可发现性** - 重要功能容易找到，次要功能不干扰
4. **反馈及时** - 每个操作都有明确的视觉反馈

---

## 2. 配色方案

### 2.1 亮色主题 (Light Theme)

```css
/* 主色调 */
--primary: #0078D4;           /* 主蓝色 */
--primary-hover: #106EBE;     /* 主蓝色悬停 */
--primary-active: #005A9E;    /* 主蓝色激活 */

/* 背景色 */
--bg-base: #FFFFFF;           /* 基础背景 */
--bg-surface: #F5F5F5;        /* 表面背景 */
--bg-elevated: #FFFFFF;       /* 抬升背景（卡片） */
--bg-overlay: rgba(0,0,0,0.4); /* 遮罩层 */

/* 边框色 */
--border-light: #E0E0E0;      /* 浅边框 */
--border-default: #D0D0D0;    /* 默认边框 */
--border-focus: #0078D4;      /* 聚焦边框 */

/* 文本色 */
--text-primary: #1A1A1A;      /* 主文本 */
--text-secondary: #666666;    /* 次要文本 */
--text-tertiary: #999999;     /* 三级文本 */
--text-inverse: #FFFFFF;      /* 反色文本 */

/* 状态色 */
--success: #107C10;           /* 成功/连接正常 */
--warning: #FF8C00;           /* 警告 */
--error: #D13438;             /* 错误/断开 */
--info: #0078D4;              /* 信息 */
```

### 2.2 暗色主题 (Dark Theme)

```css
/* 背景色 */
--bg-base: #1E1E1E;           /* 基础背景 */
--bg-surface: #252526;         /* 表面背景 */
--bg-elevated: #2D2D30;        /* 抬升背景 */
--bg-overlay: rgba(0,0,0,0.6); /* 遮罩层 */

/* 边框色 */
--border-light: #3E3E42;       /* 浅边框 */
--border-default: #4E4E52;     /* 默认边框 */
--border-focus: #007ACC;       /* 聚焦边框 */

/* 文本色 */
--text-primary: #CCCCCC;       /* 主文本 */
--text-secondary: #999999;     /* 次要文本 */
--text-tertiary: #666666;      /* 三级文本 */
--text-inverse: #1E1E1E;       /* 反色文本 */

/* 状态色 */
--success: #6A9955;            /* 成功 */
--warning: #DCDCAA;            /* 警告 */
--error: #F44747;              /* 错误 */
--info: #569CD6;               /* 信息 */
```

### 2.3 语义化颜色

```css
/* 连接状态 */
--connection-active: #107C10;   /* 已连接 */
--connection-inactive: #666666; /* 未连接 */
--connection-error: #D13438;    /* 连接错误 */

/* 数据库类型颜色 */
--color-mysql: #00758F;         /* MySQL 蓝 */
--color-postgresql: #336791;    /* PostgreSQL 蓝 */
--color-sqlite: #003B57;        /* SQLite 深蓝 */
--color-redis: #DC382D;         /* Redis 红 */
--color-mongodb: #47A248;       /* MongoDB 绿 */
--color-elasticsearch: #FED10A; /* ES 黄 */
```

---

## 3. 字体规范

### 3.1 字体族

```css
/* UI 界面字体 */
--font-ui: "Microsoft YaHei", "PingFang SC", "Segoe UI", sans-serif;

/* 代码/编辑器字体 */
--font-code: "Cascadia Code", "JetBrains Mono", "Consolas", monospace;

/* 数据表格字体 */
--font-data: "Segoe UI", "Microsoft YaHei", sans-serif;
```

### 3.2 字号规范

| 用途 | 字号 | 字重 |
|------|------|------|
| 窗口标题 | 14px | Bold |
| 区域标题 | 13px | Bold |
| 正文 | 13px | Regular |
| 次要文本 | 12px | Regular |
| 辅助文本 | 11px | Regular |
| SQL 编辑器 | 14px | Regular |
| 数据表格 | 13px | Regular |
| 状态栏 | 12px | Regular |

---

## 4. 组件规范

### 4.1 按钮 (Button)

#### 主要按钮

```css
.btn-primary {
    background: var(--primary);
    color: var(--text-inverse);
    border: none;
    border-radius: 4px;
    padding: 8px 16px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.15s ease;
}

.btn-primary:hover {
    background: var(--primary-hover);
}

.btn-primary:active {
    background: var(--primary-active);
}
```

#### 次要按钮

```css
.btn-secondary {
    background: var(--bg-surface);
    color: var(--text-primary);
    border: 1px solid var(--border-default);
    border-radius: 4px;
    padding: 8px 16px;
    font-size: 13px;
    cursor: pointer;
}

.btn-secondary:hover {
    background: var(--bg-elevated);
    border-color: var(--primary);
}
```

#### 图标按钮

```css
.btn-icon {
    background: transparent;
    border: none;
    border-radius: 4px;
    padding: 6px;
    cursor: pointer;
}

.btn-icon:hover {
    background: rgba(0, 0, 0, 0.05);
}
```

### 4.2 输入框 (Input)

```css
.input {
    background: var(--bg-base);
    border: 1px solid var(--border-default);
    border-radius: 4px;
    padding: 8px 12px;
    font-size: 13px;
    color: var(--text-primary);
    outline: none;
    transition: border-color 0.15s ease;
}

.input:focus {
    border-color: var(--border-focus);
    box-shadow: 0 0 0 2px rgba(0, 120, 212, 0.2);
}

.input::placeholder {
    color: var(--text-tertiary);
}
```

### 4.3 标签页 (Tab)

```
┌────────┬────────┬────────┐
│ 查询 1 │ 查询 2 │  查询 3│ ← 未激活
├────────┴────────┴────────┤
│                          │
│        内容区域          │
│                          │
└──────────────────────────┘
```

```css
.tab {
    background: transparent;
    border: none;
    border-bottom: 2px solid transparent;
    padding: 10px 16px;
    font-size: 13px;
    color: var(--text-secondary);
    cursor: pointer;
}

.tab:hover {
    color: var(--text-primary);
    background: rgba(0, 0, 0, 0.03);
}

.tab.active {
    color: var(--primary);
    border-bottom-color: var(--primary);
}
```

### 4.4 树形视图 (Tree View)

```
▼ 连接
  ▼ MySQL (localhost:3306)
    ▼ mydb
      📋 users
      📋 orders
      👁 v_summary
    ▼ testdb
      📋 products
  ▼ Redis (localhost:6379)
    ▸ db0
```

```css
.tree-item {
    padding: 4px 8px;
    font-size: 13px;
    cursor: pointer;
}

.tree-item:hover {
    background: rgba(0, 0, 0, 0.04);
}

.tree-item.selected {
    background: var(--primary);
    color: var(--text-inverse);
}
```

### 4.5 数据表格 (Data Grid)

```
┌────┬────────────┬──────────┬───────────┐
│ #  │ name       │ email    │ created_at│
├────┼────────────┼──────────┼───────────┤
│ 1  │ 张三       │ a@b.com  │ 2024-01-01│
│ 2  │ 李四       │ c@d.com  │ 2024-01-02│
│ 3  │ 王五       │ e@f.com  │ 2024-01-03│
└────┴────────────┴──────────┴───────────┘
        1 - 3 / 100 条          ⟵ 1 2 3 ... ⟶
```

```css
/* 表头 */
.grid-header {
    background: var(--bg-surface);
    border-bottom: 1px solid var(--border-default);
    font-weight: 600;
    font-size: 12px;
    color: var(--text-secondary);
    text-transform: uppercase;
}

/* 单元格 */
.grid-cell {
    border-bottom: 1px solid var(--border-light);
    padding: 8px 12px;
    font-size: 13px;
}

/* 选中行 */
.grid-row.selected {
    background: rgba(0, 120, 212, 0.1);
}

/* 悬停行 */
.grid-row:hover {
    background: rgba(0, 0, 0, 0.03);
}
```

### 4.6 状态栏 (Status Bar)

```
┌─────────────────────────────────────────────────────────────────┐
│ ✓ 就绪 │ 连接: MySQL@localhost │ 数据库: mydb │ 查询: 0.23s     │
└─────────────────────────────────────────────────────────────────┘
```

```css
.status-bar {
    background: var(--bg-surface);
    border-top: 1px solid var(--border-light);
    padding: 4px 12px;
    font-size: 12px;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 16px;
}

.status-item {
    display: flex;
    align-items: center;
    gap: 4px;
}
```

---

## 5. 图标规范

### 5.1 图标库

使用 **Ikonli** 图标库（Material Design Icons）：

```java
// 连接相关
IconNode icon = new IconNode(MaterialDesignIcon.DATABASE);
IconNode icon = new IconNode(MaterialDesignIcon.PLUG);
IconNode icon = new IconNode(MaterialDesignIcon.REFRESH);

// 操作相关
IconNode icon = new IconNode(MaterialDesignIcon.PLAY);
IconNode icon = new IconNode(MaterialDesignIcon.STOP);
IconNode icon = new IconNode(MaterialDesignIcon.CONTENT_SAVE);

// 导航相关
IconNode icon = new IconNode(MaterialDesignIcon.MENU);
IconNode icon = new IconNode(MaterialDesignIcon.CHEVRON_RIGHT);
IconNode icon = new IconNode(MaterialDesignIcon.CHEVRON_DOWN);
```

### 5.2 图标尺寸

| 用途 | 尺寸 | 说明 |
|------|------|------|
| 菜单图标 | 16x16 | 菜单项前的图标 |
| 工具栏图标 | 20x20 | 工具栏按钮 |
| 树形图标 | 16x16 | 连接/表/列图标 |
| 标签页图标 | 14x14 | 标签页前的图标 |
| 状态图标 | 12x12 | 状态指示 |

---

## 6. 间距规范

### 6.1 间距系统

使用 4px 为基础单位：

| 名称 | 值 | 用途 |
|------|------|------|
| xs | 4px | 紧凑间距 |
| sm | 8px | 小间距 |
| md | 12px | 中等间距 |
| lg | 16px | 大间距 |
| xl | 24px | 特大间距 |
| xxl | 32px | 区域间距 |

### 6.2 常用间距

```css
/* 区域内边距 */
.panel { padding: 16px; }
.card { padding: 24px; }
.form-group { margin-bottom: 16px; }

/* 元素间距 */
.button + .button { margin-left: 8px; }
.form-row { margin-bottom: 12px; }
.list-item + .list-item { margin-top: 4px; }
```

---

## 7. 圆角规范

| 用途 | 圆角值 |
|------|--------|
| 按钮 | 4px |
| 输入框 | 4px |
| 卡片 | 8px |
| 对话框 | 12px |
| 工具提示 | 4px |
| 标签 | 12px (胶囊形) |

---

## 8. 阴影规范

```css
/* 浅阴影 */
--shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.06);

/* 中等阴影 */
--shadow-md: 0 4px 8px rgba(0, 0, 0, 0.1);

/* 深阴影 */
--shadow-lg: 0 8px 16px rgba(0, 0, 0, 0.15);

/* 弹出层阴影 */
--shadow-popup: 0 8px 24px rgba(0, 0, 0, 0.2);
```

---

## 9. 动画规范

### 9.1 过渡时间

| 类型 | 时间 | 用途 |
|------|------|------|
| 快速 | 0.1s | 悬停、按下 |
| 正常 | 0.2s | 状态切换 |
| 慢速 | 0.3s | 展开/折叠 |

### 9.2 缓动函数

```css
/* 标准缓动 */
transition-timing-function: ease;

/* 加速缓动 */
transition-timing-function: ease-in;

/* 减速缓动 */
transition-timing-function: ease-out;
```

---

## 10. 响应式设计

### 10.1 断点

| 断点 | 宽度 | 说明 |
|------|------|------|
| 小屏 | < 1200px | 折叠侧边栏 |
| 中屏 | 1200-1600px | 标准布局 |
| 大屏 | > 1600px | 宽松布局 |

### 10.2 最小尺寸

```css
/* 窗口最小尺寸 */
min-width: 1000px;
min-height: 600px;

/* 连接面板最小宽度 */
min-width: 200px;

/* 编辑器最小宽度 */
min-width: 400px;
```

---

## 11. 无障碍设计

### 11.1 对比度

- 主文本与背景对比度 ≥ 4.5:1
- 次要文本与背景对比度 ≥ 3:1

### 11.2 键盘导航

- 所有交互元素可通过 Tab 键访问
- 焦点状态有明确的视觉指示
- 支持快捷键操作

---

## 12. 主题切换

### 12.1 切换方式

- 跟随系统设置
- 手动切换（亮色/暗色）

### 12.2 实现方式

```java
public class ThemeManager {
    
    public enum Theme {
        LIGHT, DARK, SYSTEM
    }
    
    public void setTheme(Theme theme) {
        // 1. 更新 CSS 变量
        // 2. 通知所有组件更新
        // 3. 保存用户偏好
    }
    
    public void applyTheme(Scene scene) {
        String css = theme == Theme.DARK ? "dark.css" : "light.css";
        scene.getStylesheets().setAll(css);
    }
}
```
