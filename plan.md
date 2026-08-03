# Waila 配置界面 Jade 化移植计划

## 1. 目标

把 Jade（`Jade/` 参考目录，MC 1.21+ / Fabric）的配置界面**交互设计与视觉风格**移植到本项目
（Waila，MC 1.6.4-MITE，**FishModLoader**，LWJGL 2，Java 17）。

明确一点：**不做 Java 类的 1:1 搬运**。Jade 的界面代码几乎每一行都绑在现代 MC 的
`Screen` / `GuiGraphicsExtractor` / `Component` / `Identifier` / `AbstractSelectionList` /
sprite 九宫格体系上，1.6.4 一个都没有。我们要移植的是**布局、导航、交互、观感**，
底座换成 1.6.4 原生 GUI + ManyLib（`fi.dy.masa.malilib`）的 widget 框架。

配置数据模型（`moddedmite.waila.config.WailaConfig` 里的 `ConfigBase` 体系）**保持不变**，
配置文件 `config/Waila.json` 的格式与键名**保持兼容**，用户不丢设置。

## 2. 现状

### 2.1 目标项目现在的配置界面

- `moddedmite.waila.config.WailaConfig extends SimpleConfigs implements IWailaConfigHandler`
  - 44 个配置项，分 4 个 `ConfigTab`：`waila.general`(8) / `waila.features`(20) /
    `waila.screen`(11) / `waila.keybinding`(5 个 `ConfigHotkey`)
  - 类型覆盖：`ConfigBoolean`、`ConfigInteger`、`ConfigDouble`、`ConfigEnum<EnumTooltipTheme>`、
    `ConfigColor`、`ConfigHotkey`
  - `save()` / `load()` 走 `ConfigUtils.readConfigBase/writeConfigBase` +
    `JsonUtils`，落盘到 `config/Waila.json`，四个 section 分别是
    `general` / `features` / `screen` / `keybinding`
  - `save()` 末尾调 `OverlayConfig.updateColors()` 刷新渲染缓存
- `moddedmite.waila.config.WailaConfigScreen extends DefaultConfigScreen`
  - 除了一个被注释掉的 `tickScreen` 预览尝试和 `tickClient()`（构造一个
    `runestoneAdamantium` 的假 Tooltip），基本就是 ManyLib 默认界面
- 注册链：`Waila.onInitialize()` → `WailaConfig.getInstance().load()` →
  `ConfigManager.getInstance().registerConfig(...)`；`ModMenu` 通过
  `IConfigHandler.getConfigScreen(parent)` 进入
- `mcp.mobius.waila.gui.*`（185 个源文件里约 40 个）是 Forge 时代的自制 widget 框架
  （`ScreenBase` / `LayoutCanvas` / `ViewportScrollable` / `ViewTable` / `ButtonBoolean*` /
  `ScreenWailaConfig` / `ScreenHUDConfig` …）。
  **已经是死代码**：全项目只有 `gui/truetyper/*` 和 `gui/helpers/UIHelper` 还被引用，
  `ScreenConfig` / `ScreenHUDConfig` / `KeyEvent` 的调用点全被注释掉了。
- `mcp.mobius.waila.api.impl.ConfigHandler` 整个文件被注释掉，
  `WailaConfig` 实现的 `IWailaConfigHandler.getConfig(key)` **永远返回 false**，
  `getModuleNames()` 返回空集，`getConfigKeys()` 是个会 ClassCastException 的坑。
  → 意味着 Jade 的「插件配置屏幕」在本项目**没有数据源可对应**。

### 2.2 Jade 的配置界面结构

```
HomeConfigScreen                     大标题 + 两行渐变描述 + 3 个入口按钮 + 作者致谢彩蛋
├── WailaConfigScreen                mod 本体设置（extends PreviewOptionsScreen）
├── PluginsConfigScreen              插件设置（本项目无对应数据源）
└── ProfileConfigScreen              4 套配置档位切换/重命名

BaseOptionsScreen (abstract)
├── 左侧 120px  OptionsNav           分类导航栏，跟随右侧滚动高亮，可点跳转
├── 左上 120x18 NotUglyEditBox       搜索框（Ctrl+F / 任意字母数字键聚焦）
├── 右侧        OptionsList          选项列表，行高 26，行宽 min(w,300)
├── 右下 90x20  save_and_quit        绿色「保存并退出」，有非法值时禁止并提示
├── 其左 90x20  cancel               取消（回滚 invalidate）
└── 左下 85x20  preview toggle       仅在世界内，实时预览 overlay
```

`OptionsList` 的能力（这是移植的核心）：

| 能力 | 说明 |
|---|---|
| Entry 类型 | `Title`（分类标题，文字居中）、`OptionValue<T>` 抽象基类、`CycleOptionValue`（开关/枚举循环）、`SliderOptionValue`（滑条，带 aligner 对齐步长）、`InputOptionValue`（文本框 + validator，非法值标红）、`OptionButton`（右侧一个按钮）、`KeybindOptionButton`（按键绑定捕获） |
| 行布局 | 文字 x = `indent + 10`，y 偏移 -3；值控件靠右浮动，x = `contentWidth - 110 + offsetX`，垂直居中；控件默认 100x20，文本框 98x18 |
| 父子关系 | `entry.parent(other)`，子项 `indent += 12`；`Title` 自动成为后续项的默认父 |
| 搜索 | 空格分词，多关键词 AND；命中项连带其所有子孙 + 所有祖先一起显示；无结果显示「no results」标题 |
| 分类导航 | `OptionsNav.refresh()` 只收集 `Title`，滚动时 `currentTitle` 反查高亮，键盘左右方向键在导航栏与列表间跳 |
| 滚动 | `SmoothChasingValue` 平滑滚动（speed 0.6），Ctrl 加速 3x，scissor 裁剪 |
| 保存 | `save()` 逐项 `setter.accept(value)` 后调 `diskWriter`；`updateSaveState()` 扫非法值并给保存按钮挂 tooltip |
| tooltip | 只在鼠标悬停**标签文字区域**时弹；`_desc` 语言键自动挂载；Shift 追加 `getDescriptionOnShift()`；`${SHOW_DETAILS}` / `${SHOW_OVERLAY}` 变量替换成实际按键名 |
| 额外搜索词 | `_extra_msg` 语言键的内容也参与搜索匹配，但不显示 |
| 禁用态 | `setDisabled(true)` 灰化标题 + 关掉所有控件 |
| 预览联动 | `forcePreview` 集合里的项被拖动时强制显示 overlay 预览 |

`WailaConfigScreen`（Jade 本体设置）还有：
- overlay 位置调整模式：全屏半透明遮罩 + 九宫格点击吸附 + 拖拽 + 方向键微调 +
  中心/边缘辅助线 + Esc 退出
- danger zone：红色标题 + 「重载插件」+ 「重置全部设置」（带 `ConfirmScreen` 二次确认）

### 2.3 1.6.4 + ManyLib 能提供什么

已经 `javap` 核过 `minecraft-merged-empty-intermediate` 里的真实签名：

- `net.minecraft.GuiScreen`：`drawScreen(int,int,float)` / `keyTyped(char,int)` /
  `mouseClicked(int,int,int)` / `mouseMovedOrUp(int,int,int)` /
  `mouseClickMove(int,int,int,long)` / `initGui()` / `updateScreen()` /
  `onGuiClosed()` / `confirmClicked(boolean,int)` / `doesGuiPauseGame()`
- `net.minecraft.Gui`：`drawRect` / `drawGradientRect` / `drawString` /
  `drawCenteredString` / `drawTexturedModalRect`
- `net.minecraft.FontRenderer`：`getStringWidth` / `drawString(±shadow)` /
  `trimStringToWidth` / `listFormattedStringToWidth` / `splitStringWidth` / `FONT_HEIGHT`
- `net.minecraft.GuiTextField`：`setText/getText/textboxKeyTyped/mouseClicked/drawTextBox/
  setFocused/setEnableBackgroundDrawing/setTextColor/setMaxStringLength`
- `net.minecraft.GuiButton`、`net.minecraft.GuiSlot`（分页式，不好用）、
  `net.minecraft.ScaledResolution`
- 输入是**轮询式**：`org.lwjgl.input.Mouse.getDWheel()` / `Keyboard.enableRepeatEvents`

ManyLib（`com.github.MinecraftIsTooEasy:ManyLib:2.3.x`，包名 `fi.dy.masa.malilib`）：

| 组件 | 能力 |
|---|---|
| `gui.screen.ModernScreen` | 把 1.6.4 的老回调翻译成现代事件模型：`mouseClicked(double,double,int)` / `mouseReleased` / `mouseMoved` / `mouseScrolled`（内部自己 poll `Mouse.getDWheel()`）/ `charTyped(char,int)` / `tick()`，并带 parent 栈与 `close()` |
| `gui.screen.LayeredScreen` | 图层栈：`addLayer/removeTopLayer/toggleLayer`，事件从上到下传递、可 `blocksInteraction()`、Esc 逐层退出 |
| `gui.screen.DefaultConfigScreen` | 现有 ManyLib 配置界面：顶部 tab 按钮 + reset-all + 排序 + mod 跳转 + 分页列表 + 搜索 |
| `gui.widgets.WidgetBase / WidgetContainer` | widget 树、`render` / `postRenderHovered` / `onMouseClicked` / `onCharTyped` / `hoverStrings` |
| `gui.widgets.WidgetListView` | **分页**列表（`getPageCapacity()` 固定 7 行）+ `ScrollBar` + `WidgetScrollHandler` |
| `gui.screen.util.ConfigItem*` | 每种配置类型的整行渲染：`ConfigItemToggle` / `ConfigItemPeriodic`（Boolean+Enum 循环）/ `ConfigItemSlideable`（滑条 ⇄ 文本框可切换）/ `ConfigItemInputBox` / `ConfigItemColor`（带取色盘弹层）/ `ConfigItemHotkey`（按键捕获 + 冲突检测）/ `ConfigItemStringList` |
| `gui.layer.*` | `ColorEditLayer`（HSV 取色）/ `KeySettingsLayer`（按键高级设置）/ `StringListEditLayer` / `ModLinkLayer` |
| `render.RenderUtils` | `drawRect` / `drawOutline` / `drawGradientRect` / `drawTexturedRect` / `drawHoverText`（Waila 风格边框的多行 tooltip）/ `drawTextList` / **`startScissor` / `endScissor`**（GL11 裁剪，已按 scaleFactor 换算） |
| `util.StringUtils` | `translate` / `getTranslatedOrFallback` / **`stringMatchesInput`（含拼音匹配，走 PinIn）** / `getColor` |
| `config.*` | `ConfigBase` 体系 + `ConfigTab` + `SimpleConfigs` + `ConfigManager` + `ConfigUtils` 读写 |
| `gui.button.*` | `ButtonGeneric`（builder 式，带 icon / hoverStrings / onUpdate）/ `PeriodicButton`（循环）/ `SliderButton` / `ScrollBar` / `SearchField` / `ResetButton` |
| i18n 约定 | `config.name.<配置名>` = 显示名，`config.comment.<配置名>` = tooltip，`config.enum.<配置名>.<常量>` = 枚举项名，`config.tab.<tab名>` = 标签名 |

## 3. 能力差距矩阵

| Jade 依赖 | 1.6.4 / ManyLib 情况 | 处置 |
|---|---|---|
| `Screen` + `addRenderableWidget` | `GuiScreen` + ManyLib `LayeredScreen` | **复用 ManyLib**，我们的屏幕全部 `extends LayeredScreen` |
| `GuiGraphicsExtractor`（保留渲染状态提取） | 无。1.6.4 是即时模式 GL | 直接用 `RenderUtils` + `Tessellator`，`DrawContext` 传递「是否顶层」 |
| `Component` / `Style` / `MutableComponent` | 无。只有 `String` + `§` 颜色码 | 全部降级为 `String`，用 `GuiBase.TXT_*` 常量拼颜色；多行拆分用 `FontRenderer.listFormattedStringToWidth` |
| `Identifier` + sprite + `.mcmeta` 九宫格 | 无。只有 `ResourceLocation` + 手工 UV | P1 用纯色/渐变矩形替代；P5 可选实现 `NineSlice` 工具（9 个 quad，Tessellator 手绘） |
| `ContainerObjectSelectionList` / `ObjectSelectionList` | ManyLib 只有**分页固定 7 行**的 `WidgetListView` | **自己写连续滚动列表**（见 §5.2），复用 `RenderUtils.startScissor` 做裁剪 |
| `CycleButton<T>` | ManyLib `PeriodicButton` 已等价 | 复用，包一层 |
| `AbstractSliderButton` | ManyLib `SliderButton<T>` 已等价 | 复用 |
| `EditBox` + `setResponder` + hint + 圆角背景 | `GuiTextField` + ManyLib `WidgetTextField` | 复用 `WidgetTextField`，自己补 hint 绘制与响应回调 |
| `KeyMapping` + `InputConstants` | ManyLib `ConfigHotkey` / `IKeybind` / `KeybindMulti`（支持组合键、冲突检测、高级设置） | **比 Jade 还强**，直接复用 |
| `Tooltip` / `MultilineTooltip` / `ClientTooltipPositioner` | 无定位器体系 | 用 `RenderUtils.drawHoverText`（视觉上就是 Waila 紫框 tooltip，正好对味） |
| 焦点导航 `FocusNavigationEvent` / `ComponentPath` / narration | 无，也没有无障碍朗读 | **不移植**。键盘操作只保留：Esc、Ctrl+F 聚焦搜索、上下滚动 |
| `SmoothChasingValue` | 无 | 30 行小类，直接抄一份（纯数学，无 MC 依赖） |
| 鼠标滚轮事件 | 轮询 `Mouse.getDWheel()` | `ModernScreen` 已经代劳 |
| 鼠标拖拽 | `GuiScreen.mouseClickMove(int,int,int,long)`（`ModernScreen` **没有**转发） | 在我们的基类里 override `mouseClickMove` 转成 `onMouseDragged` |
| 键盘重复 | `Keyboard.enableRepeatEvents(true)` | 进屏幕开、`onGuiClosed` 关（`DefaultConfigScreen` 已有先例） |
| `ConfirmScreen` | `net.minecraft.GuiYesNoMITE` + `confirmClicked(boolean,int)` | 复用（`DefaultConfigScreen` 已有先例） |
| profiles（4 套配置档位） | 无 | **P6 可选**，非核心 |
| plugin config 屏幕 | 无数据源（`ConfigHandler` 已废） | **不移植**，其内容由现有 `features` tab 承担 |
| accessibility / TTS / narrate | 无 | 不移植 |
| 节日彩蛋粒子 | 纯 2D 文字粒子，可实现 | **P6 可选**，轻量版 |

## 4. 架构设计

### 4.1 新增包结构

全部放在 `moddedmite.waila.gui` 下（新包，与死掉的 `mcp.mobius.waila.gui` 隔离）：

```
src/main/java/moddedmite/waila/gui/
├── WailaHomeScreen.java            ← Jade HomeConfigScreen
├── BaseOptionsScreen.java          ← Jade BaseOptionsScreen（abstract）
├── WailaSettingsScreen.java        ← Jade WailaConfigScreen（本体设置，含预览/位置调整）
├── PreviewOptionsScreen.java       ← Jade PreviewOptionsScreen（预览 + 拖拽定位，abstract）
├── list/
│   ├── OptionsList.java            ← 连续滚动列表容器
│   ├── OptionsNav.java             ← 左侧分类导航栏
│   ├── Entry.java                  ← 行基类（标题文字 + 右浮控件 + 父子 + 搜索词 + tooltip）
│   ├── TitleEntry.java             ← 分类标题行
│   ├── OptionEntry.java            ← 绑定 ConfigBase 的行基类
│   ├── ToggleEntry.java            ← Boolean / Enum → PeriodicButton
│   ├── SliderEntry.java            ← Integer / Double → SliderButton（+ 文本框切换）
│   ├── InputEntry.java             ← String → WidgetTextField + validator
│   ├── ColorEntry.java             ← ConfigColor → 色块 + ColorEditLayer
│   ├── KeybindEntry.java           ← ConfigHotkey → 捕获按钮 + KeySettingsLayer
│   └── ButtonEntry.java            ← 纯动作按钮行
├── widget/
│   ├── SearchBox.java              ← 带 hint + 清除叉 + 响应回调的搜索框
│   ├── ScrollBarV.java             ← 像素级连续滚动条（ManyLib ScrollBar 是分页语义）
│   └── NineSlice.java              ← （P5 可选）九宫格贴图绘制
└── util/
    ├── SmoothChasingValue.java     ← 从 Jade 抄（无 MC 依赖）
    ├── ScreenTheme.java            ← 配色常量（背景/导航栏/高亮/分隔线/文字）
    └── TextUtil.java               ← §码安全的截断、多行拆分、变量替换
```

改动的既有文件：

| 文件 | 改动 |
|---|---|
| `moddedmite/waila/config/WailaConfig.java` | `getConfigScreen()` 返回 `new WailaHomeScreen(parent)`；补齐配置项的父子关系元数据；清掉 `IWailaConfigHandler` 里三个假实现的坑 |
| `moddedmite/waila/config/WailaConfigScreen.java` | 保留 `tickClient()` 作为预览 Tooltip 工厂（挪到 `PreviewOptionsScreen`），文件本身删除或降级成 deprecated 转发 |
| `mcp/mobius/waila/overlay/OverlayRenderer.java` | 抽出「按给定 x/y/scale/alpha 渲染一个 Tooltip」的入口，供预览复用（不改动实际游戏内渲染路径） |
| `src/main/resources/assets/waila/lang/*.lang` | 新增 `gui.waila.*` 界面键、各配置项的 `config.comment.*` 描述、`config.enum.*` 枚举项名 |

### 4.2 布局参数（照抄 Jade 的数值，保证观感一致）

```
搜索框    (0, 0, 120, 18)
导航栏    (0, 18, 120, height - 32 - 18)，行高 18，选中左侧 2px 白条
列表      (120, 0, width - 120, height - 32)，行高 26，行内容宽 min(listWidth, 300)
  标题文字  x = contentX + indent + 10,  y 居中 - 3
  值控件    x = contentX + contentWidth - 110 + offsetX,  100x20 垂直居中
  子项缩进  indent += 12
底部分隔线 y = height - 32，1~2px
保存按钮  (width - 100, height - 25, 90, 20)   文字 §a 保存并退出
取消按钮  (width - 195, height - 25, 90, 20)
预览开关  (10, height - 25, 85, 20)            仅 theWorld != null
```

## 5. 关键技术方案

### 5.1 屏幕基座

`BaseOptionsScreen extends LayeredScreen`：

- `initBaseLayer(Layer)` 里装配 searchBox / nav / list / 按钮，
  `reload()` 用于窗口尺寸变化或需要重建时
- override `mouseClickMove(int,int,int,long)` → 分发 `onMouseDragged`
  （`ModernScreen` 把 `mouseClicked`/`mouseMovedOrUp`/`keyTyped` 都 final 了，
  但 `mouseClickMove` 没有，可以安全 override）
- `initGui` 时 `Keyboard.enableRepeatEvents(true)`，`onGuiClosed` 关掉
- 取色盘 / 按键高级设置 复用 `LayeredScreen.toggleLayer` + ManyLib 的 Layer
- `doesGuiPauseGame()` → `false`（预览需要世界继续跑）

### 5.2 连续滚动列表（核心自研点）

ManyLib 的 `WidgetListView` 是**分页**语义（`status` = 起始索引，`pageCapacity` 固定 7），
Jade 是**像素级平滑滚动**。必须自研 `OptionsList`：

- `extends WidgetContainer`，持有 `List<Entry> allEntries`（全量）与
  `List<Entry> visibleEntries`（搜索过滤后）
- 滚动状态：`SmoothChasingValue scrollAmount`，`maxScroll = max(0, rows*26 - viewHeight)`
- 渲染：`RenderUtils.startScissor(x, y, w, h)` → 只画
  `[scroll/26, (scroll+h)/26]` 区间的行 → `endScissor()`
- 每行 `Entry.setPosition(x, y - scroll + i*26)` 后再委托子控件渲染，
  等价于 Jade 的 `EntryWidget.offsetX/offsetY` + `setX/setY` 逻辑
- 滚轮：`onMouseScrolled` → `scrollAmount.target(clamp(target ∓ 26*(ctrl?9:3)))`
- 拖拽滚动条：`ScrollBarV`，把「拖到 ratio」换算成像素 offset（不是页索引）
- 命中测试：`getEntryAt(mouseX, mouseY)`，超出裁剪区返回 null
- hover 时更新 `currentTitle`（用于导航栏高亮），逻辑照抄 Jade：
  hover 的是 `TitleEntry` 就直接设，否则取 `entry.root()`

### 5.3 Entry 与 ConfigBase 的桥接

Jade 的 `OptionValue<T>` 走 `Supplier/Consumer`，我们直接绑 `ConfigBase`：

```java
// 语义映射
ConfigBoolean / ConfigEnum<E>   → ToggleEntry   （复用 PeriodicButton）
ConfigInteger / ConfigDouble    → SliderEntry   （复用 SliderButton + 文本框切换）
ConfigColor                     → ColorEntry    （色块 + ColorEditLayer）
ConfigHotkey                    → KeybindEntry  （复用 ConfigItemHotkey 的捕获与冲突检测逻辑）
ConfigString / ConfigStringList → InputEntry    （目前 Waila 没用到，留接口）
```

每个 `OptionEntry` 从 `ConfigBase` 自动取：
- 显示名 ← `getConfigGuiDisplayName()`（即 `config.name.<name>`）
- tooltip ← `getConfigGuiDisplayComment()`（即 `config.comment.<name>`）
- 是否已改 ← `isModified()` → 显示 reset 按钮（Jade 没有单项 reset，ManyLib 有，**保留**，是净增益）
- 搜索词 ← 显示名 + tooltip + `config.name.<name>.extra_msg`（对应 Jade 的 `_extra_msg`）

**不需要**Jade 的 `save()`/`invalidate()` 双缓冲：ManyLib 的 `ConfigBase` 是即时写内存值，
落盘在 `onGuiClosed` 时由 `IConfigHandler.save()` 统一做。
代价：没有「取消」语义。
→ 折中方案：进屏幕时把所有 `ConfigBase.getAsJsonElement()` 快照一份，
「取消」时 `setValueFromJsonElement` 回滚。这是低成本实现，放 P3。

### 5.4 搜索

复用 ManyLib 的 `StringUtils.stringMatchesInput`（**自带拼音匹配**，比 Jade 的
纯 `contains` 更适合中文用户），但过滤算法照抄 Jade：

1. 按空白分词，每个关键词都要在该项的某个搜索词里命中（AND）
2. 命中项：连带 `walkChildren` 全部子孙 + 向上所有祖先一起保留
3. 无结果：插入一个 `§7无匹配结果` 的 `TitleEntry`
4. 过滤后调 `OptionsNav.refresh()` 重建导航栏

### 5.5 tooltip

- 只在鼠标落在**标签文字的横向区间**内才弹（Jade 行为，避免整行都弹）
- 用 `RenderUtils.drawHoverText(mouseX, mouseY, lines, ctx)`
  —— 它画的就是 Waila 紫色渐变边框，风格自洽
- 多行：`FontRenderer.listFormattedStringToWidth(text, 255)`
- Shift 追加：枚举项列出全部可选值并高亮当前项（`ConfigItemPeriodic` 已有此实现，抄过来）
- 变量替换：`${SHOW_OVERLAY}` / `${SHOW_LIQUID}` 等 → 从 `ConfigHotkey.getKeybind()
  .getKeysDisplayString()` 取实际按键名（对应 Jade 的 `processBuiltInVariables`）

### 5.6 预览与位置调整（替换死掉的 ScreenHUDConfig）

现有 `WailaConfigScreen.tickClient()` 已经会构造一个假 Tooltip
（`Block.runestoneAdamantium`，头部显示名 / 主体 "Show" / 尾部命名空间）。挪到
`PreviewOptionsScreen` 并接上：

- **预览开关**：左下角按钮，`theWorld != null` 时可用。开启后每帧在配置界面上
  按当前 `posX/posY/alpha/scale/theme/颜色` 调 `OverlayRenderer.renderOverlay(tooltip)`
  → 改配色/缩放时**所见即所得**（Jade 的核心体验）
- **位置调整模式**：点「调整位置」进入
  - 全屏 `0x80808080` 遮罩 + 居中提示文字（`config.jade.overlay_pos.exit` 的对应键）
  - 九宫格点击吸附：`xIndex = clamp(mx/(w/3), 0, 2)`，中心格 = 退出
  - 拖拽：`mouseClickMove` → 更新 `posX/posY`；Ctrl 关闭吸附
  - 方向键微调：1 步 = 2px，Shift = 20px
  - 居中辅助线：`posX == 50` 时画竖蓝线，`posY == 50` 时画横蓝线
  - Esc 退出
  - **这一步同时干掉** `mcp.mobius.waila.gui.screens.config.ScreenHUDConfig`
    和 `assets/waila/textures/config_template.png`（180x62 静态占位图）

注意：Waila 的 `posX/posY` 是 `ConfigInteger` 0~100 的百分比，
Jade 的是 `float posX/posY + anchorX/anchorY` 四个量。
**不引入 anchor 概念**，只做百分比定位 + 九宫格吸附，保持配置文件兼容。

### 5.7 视觉降级方案

| Jade 元素 | 1.6.4 实现 |
|---|---|
| `navbar_background` 九宫格贴图 | `RenderUtils.drawRect` 半透明深色块 + 右侧 1px 分隔线 |
| `search_box_background` 九宫格 | `drawOutlinedBox`（ManyLib 已有）或 `GuiTextField` 原生背景 |
| 列表底部 `FOOTER_SEPARATOR` | `drawGradientRect` 上下渐变的 2px 条 |
| 选中行高亮 `0x33FFFFFF` | `RenderUtils.drawRect` 同色（`ConfigItem` 已在用 `ManyLibConfig.HighlightColor`） |
| 导航栏当前项左侧白条 | `drawRect(left, top, 2, 14, 0xFFFFFFFF)` |
| 按钮 / 滑条纹理 | 复用原版 `GuiButton.buttonTextures`（`ManyLib SliderButton` 已经这么干） |
| 搜索图标 | 复用 `ManyLibIcons.SEARCH` |
| 渐变标题 + 扫光 | 逐字符改色，`ARGB.scaleRGB` → 自己写 `scaleRGB(int,float)`，用 `§` 无法逐字调色，需逐字符 `drawString` |

**结论：P1~P4 不需要任何新贴图资源。** P5 才可选加一张 `assets/waila/textures/gui/config_widgets.png`。

## 6. 分阶段任务

### P0 — 准备与清理

1. 删除确认无引用的死代码：`mcp/mobius/waila/gui/screens/**`、
   `mcp/mobius/waila/gui/widgets/**`、`mcp/mobius/waila/gui/interfaces/**`、
   `mcp/mobius/waila/gui/events/**`、`mcp/mobius/waila/gui/testing/**`
   - **保留** `gui/truetyper/**`（被 `TTRenderTrueTyper` / `ProxyClient` 用）
   - **保留** `gui/helpers/UIHelper`（被 `HUDDecoratorVanilla` 用）
   - 删前先 `grep` 复核，`compileJava` 必须通过
2. 抄入 `util/SmoothChasingValue.java`（Jade 原文件无 MC 依赖，可直接搬）
3. 建 `util/ScreenTheme.java` 收敛所有配色常量
4. 建 `util/TextUtil.java`：`§` 安全截断、`listFormattedStringToWidth` 包装、
   `scaleRGB(int,float)`、变量替换

**验收**：`./gradlew compileJava` 通过，游戏能启动，现有 ManyLib 配置界面照旧可用。

### P1 — 骨架：三栏布局跑起来

1. `BaseOptionsScreen`：`LayeredScreen` 子类，装配 searchBox / nav / list /
   保存 / 取消，override `mouseClickMove`，管 `Keyboard.enableRepeatEvents`
2. `list/OptionsList`：连续滚动 + scissor 裁剪 + 滚轮 + `ScrollBarV`
3. `list/Entry` + `list/TitleEntry`：行渲染、父子关系、搜索词收集、hover tooltip 区间
4. `list/OptionsNav`：分类导航栏，滚动跟随高亮 + 点击跳转
5. `WailaSettingsScreen`：把 `WailaConfig.general/features/screen/keybinding`
   四组当成四个 `TitleEntry` 分区，全部塞进**一个**列表
   （Jade 是单列表多分区 + 左侧导航，取代 ManyLib 的顶部 tab）
6. `WailaConfig.getConfigScreen()` 暂时指向 `WailaSettingsScreen`

**验收**：进界面能看到 44 个选项名、能滚、能搜（先只按名字）、导航栏能跳。
控件先都渲染成占位文字。

### P2 — 控件：各类型行可交互

1. `ToggleEntry`（`ConfigBoolean` / `ConfigEnum`）→ `PeriodicButton`
2. `SliderEntry`（`ConfigInteger` / `ConfigDouble`）→ `SliderButton` +
   滑条/文本框切换小按钮（照搬 `ConfigItemSlideable`）
3. `ColorEntry`（`ConfigColor`）→ 文本框 + 色块，点色块开 `ColorEditLayer`
4. `KeybindEntry`（`ConfigHotkey`）→ 捕获按钮（`§e> KEY <`）+ 冲突检测 tooltip +
   高级设置按钮开 `KeySettingsLayer`
5. `ButtonEntry`：纯动作行
6. 单项 reset 按钮（`ResetButton`，`isModified()` 时才亮）

**验收**：44 项全部可改可存，`config/Waila.json` 内容与改动前格式一致，
`OverlayConfig.updateColors()` 被正确触发。

### P3 — Jade 化交互细节

1. 父子关系与缩进：
   - `showcrop` → `showcropdetails`
   - `showEnts` → `showhp` / `showatk` / `showarmor` / `showanimal` /
     `showlivestock` / `showzombieconversion` / `showspiderweb` / `showphaseevasions`
   - `theme`（== `Custom` 时才启用）→ `bgcolor` / `gradient1` / `gradient2` / `fontcolor`
   - `shiftblock` / `shiftents` 挂在 `showTooltip` 下
   - 父项关闭时子项 `setDisabled(true)`（灰化 + 禁用控件）
2. 搜索：多关键词 AND + 连带子孙祖先 + 拼音 + 无结果提示 + `extra_msg` 隐藏搜索词
3. tooltip：只在标签文字区间弹、多行、Shift 展开枚举全值、`${KEY}` 变量替换
4. 保存/取消语义：进屏快照 `getAsJsonElement()`，「取消」回滚，
   「保存并退出」写盘 + `updateUsedKeys()` + `OverlayConfig.updateColors()`
5. 非法值拦截：`InputEntry` validator 不通过时标红 +
   禁用保存按钮并挂 tooltip + 点击时滚动到该项

**验收**：交互与 Jade 一致（除了无障碍/焦点导航）。

### P4 — 首页 + 预览 + 位置调整

1. `WailaHomeScreen`：
   - 放大 2x 的 mod 名标题 + 两行渐变扫光描述（`gui.waila.configuration.desc1/desc2`）
   - 「Waila 设置」按钮（→ `WailaSettingsScreen`）
   - 「关于/致谢」底部按钮
   - 「完成」按钮
   - 入口切换：`WailaConfig.getConfigScreen()` → `WailaHomeScreen`
2. `PreviewOptionsScreen`：预览开关 + 每帧渲染假 Tooltip
3. 位置调整模式：遮罩 + 九宫格吸附 + 拖拽 + 方向键 + 辅助线 + Esc
4. danger zone：红标题 + 「重置全部设置」（`GuiYesNoMITE` 二次确认）
5. 删除 `ScreenHUDConfig` 遗留与 `config_template.png`

**验收**：改颜色/缩放/透明度时右侧实时可见；位置调整手感与 Jade 一致。

### P5 — 打磨（可选）

1. `widget/NineSlice`：Tessellator 手绘九宫格，配一张
   `assets/waila/textures/gui/config_widgets.png`，替换导航栏/搜索框背景
2. 平滑滚动手感调参（Jade `speed = 0.6`，`SmoothChasingValue.eps = 1/4096`）
3. 全局搜索：接 ManyLib 的 `GlobalSearchScreen`（双击 Shift 触发，已有实现）
4. 排序按钮（ManyLib `SortCategory`）

### P6 — 长尾（明确可砍）

1. profiles：4 套配置档位切换/重命名（需要 `WailaConfig` 支持多实例，改动面大）
2. 节日彩蛋粒子（`❄` / `✴` / `UwU`，纯 2D 文字粒子）

## 7. i18n

新增到 `assets/waila/lang/zh_CN.lang` 与 `en_US.lang`（其余语言留英文回退）：

```
# 界面
gui.waila.configuration=Waila 配置
gui.waila.configuration.desc1=...
gui.waila.configuration.desc2=...
gui.waila.waila_settings=Waila 设置
gui.waila.save_and_quit=保存并退出
gui.waila.search=搜索
gui.waila.search.hint=输入以筛选...
gui.waila.no_results=无匹配结果
gui.waila.invalid_value_cant_save=存在非法值，无法保存
gui.waila.preview=预览
gui.waila.overlay_pos.adjust=调整位置
gui.waila.overlay_pos.exit=点击中心区域或按 Esc 退出
gui.waila.danger_zone=危险区域
gui.waila.reset_settings=重置全部设置
gui.waila.reset_settings.confirm=确定要把所有 Waila 设置恢复为默认值吗？

# 现有 config.name.* 保持不动（44 项已有）
# 新增每项的 config.comment.<name>=<描述>       ← tooltip，Jade 的 *_desc
# 新增 config.enum.screen.label.TooltipTheme.<Waila|Dark|TOP|Create|Tooltip|Achievement|Legacy|Custom>
# 可选 config.name.<name>.extra_msg=<隐藏搜索关键词>
```

`config.tab.waila.*` 四个键在改成单列表 + 导航栏后，
复用为**分区标题**（`TitleEntry` 的文案来源），不删。

## 8. 兼容性约束（硬要求）

1. `config/Waila.json` 的 4 个 section 与所有键名**不变**，老配置直接可读
2. `WailaConfig` 的 `public static final ConfigXxx` 字段**不重命名**
   （`OverlayConfig` / `OverlayRenderer` / 各 `HUDHandler*` 都在直接引用）
3. 仍走 `ConfigManager.registerConfig(...)`，`ModMenu` 入口不变
4. 5 个 `ConfigHotkey` 的 callback（打开配置 / 显示切换 / 流体 / EMI 合成 / EMI 用途）
   逻辑不变
5. 服务端环境不加载任何界面类（`FishModLoader.getEnvironmentType()` 判断已在
   `Waila.onInitialize()`；新界面类只在客户端路径引用，避免 `@Environment` 缺失导致的
   服务端 NoClassDefFound）

## 9. 验证方式

- 每阶段 `./gradlew compileJava` 必过（本机 gradle wrapper 下载失败时用 `--offline`）
- `./gradlew runClient`（`FishModLoader/run` 已有完整运行目录可参考）人工验：
  1. 进入界面：主菜单 ModMenu 路径 + 游戏内 `NUMPAD0` 热键路径
  2. 滚动 / 搜索（中英文 + 拼音）/ 导航跳转 / tooltip
  3. 每种控件类型改值 → 退出 → 重进，值保留
  4. 对比 `config/Waila.json` 改动前后的 diff，确认结构没变
  5. 预览：改 `alpha` / `scale` / `theme` / 4 个颜色，实时生效
  6. 位置调整：九宫格 / 拖拽 / 方向键 / Esc
  7. 取消：改一堆值后点取消，重进确认全部回滚
  8. 服务端 `runServer` 启动不炸（无界面类加载）
- 代码交叉审核：实现用 gpt 系，审核换 `geek2-claude/claude-opus-5`，只读上下文，
  主代理汇总后统一落地

## 10. 风险与取舍

| 风险 | 影响 | 对策 |
|---|---|---|
| ManyLib 的 `WidgetListView` 分页语义与连续滚动不兼容 | 必须自研列表，是最大的工作量块 | P1 单独一个阶段，先做通用列表再填内容 |
| `ModernScreen` 把 `mouseClicked` / `mouseMovedOrUp` / `keyTyped` 都 final 了 | 拿不到原始事件 | 已确认 `mouseClickMove` 未 final，拖拽走它；其余用 ManyLib 的现代回调足够 |
| 1.6.4 无 `Component`，无法逐段染色 | 渐变扫光标题、部分彩色标签要逐字符 `drawString` | `TextUtil` 里封装，性能可接受（标题只有几十字符） |
| scissor 与 `ScaledResolution` 换算 | 裁剪区错位 | 直接用 `RenderUtils.startScissor`（已按 `getScaleFactor()` 换算并翻转 y） |
| `ConfigBase` 即时写值，没有事务 | 「取消」语义缺失 | JSON 快照 + `setValueFromJsonElement` 回滚（P3） |
| 删除 `mcp.mobius.waila.gui` 死代码可能有隐藏引用 | 编译失败 | P0 先 `grep` 复核 + 编译验证，分两次提交（先删再改） |
| 服务端加载客户端界面类 | `NoClassDefFoundError` | 新界面类只从 `getConfigScreen()` 与热键 callback 引用，两者都是客户端路径 |

## 11. 明确不做

- 无障碍朗读（narration）与焦点导航（Tab / 方向键在控件间跳）
- 插件配置屏幕（本项目 `ConfigHandler` 已废，无数据源）
- Jade 的 `anchorX/anchorY` 双锚点定位模型（会破坏配置文件兼容）
- Jade 的主题 sprite 体系（保留现有 `EnumTooltipTheme` 枚举）
- Jade 的 `IWailaConfig` / `IPluginConfig` API 接口体系（本项目 API 面向 `IWailaDataProvider`，
  与 Jade 的 provider 模型不同，不做 API 层移植）

## 12. 提交拆分建议

```
refactor: 移除失效的 mcp.mobius.waila.gui 遗留界面框架        (P0)
feat: 新增 Jade 风格配置界面骨架与连续滚动选项列表             (P1)
feat: 补齐配置界面各类型选项控件                              (P2)
feat: 配置界面支持父子选项、搜索过滤与取消回滚                 (P3)
feat: 新增配置首页、实时预览与提示框位置调整                    (P4)
feat: 配置界面视觉打磨与九宫格背景                            (P5, 可选)
```
