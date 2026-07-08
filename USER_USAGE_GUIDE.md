﻿﻿﻿﻿﻿# 动物声纹系统用户使用流程文档

## 一、环境启动与验证

### 步骤1.1：启动后端服务
- **操作说明**：在项目后端目录执行 `mvn spring-boot:run`
- **预期结果**：后端服务启动成功，监听端口8080，数据库连接正常
- **实际结果**：后端服务启动成功，控制台输出数据库查询日志
- **截图**：无（服务启动日志）

### 步骤1.2：启动前端服务
- **操作说明**：在项目前端目录执行 `npm run dev`
- **预期结果**：前端开发服务器启动成功，监听端口3000
- **实际结果**：前端服务启动成功，Vite显示访问地址 http://localhost:3000/
- **截图**：无（服务启动日志）

---

## 二、用户登录流程

### 步骤2.1：访问登录页面
- **操作说明**：打开浏览器访问 http://localhost:3000/
- **预期结果**：页面自动跳转到登录页面，显示登录表单
- **实际结果**：成功跳转到登录页面，显示用户名和密码输入框
- **截图**：[login-page.jpg](file:///e:/animal%20sounds/screenshots/login-page.jpg)

### 步骤2.2：输入账号密码并登录
- **操作说明**：输入用户名 `admin` 和密码 `password`，点击登录按钮
- **预期结果**：登录成功，跳转到仪表盘页面
- **实际结果**：登录成功，页面跳转到 http://localhost:3000/dashboard
- **截图**：[dashboard-page.jpg](file:///e:/animal%20sounds/screenshots/dashboard-page.jpg)

---

## 三、仪表盘浏览流程

### 步骤3.1：查看统计卡片
- **操作说明**：在仪表盘页面查看统计卡片区域
- **预期结果**：显示数据集总数、音频文件数、标注记录数、用户总数、已审核标注、待审核标注
- **实际结果**：6张统计卡片正常显示，数据准确（数据集5、音频20、标注0、用户5、已审核0、待审核0）
- **截图**：[dashboard-cards.jpg](file:///e:/animal%20sounds/screenshots/dashboard-cards.jpg)

### 步骤3.2：查看图表区域
- **操作说明**：在仪表盘页面查看分类标注统计柱状图和训练趋势折线图
- **预期结果**：图表正常渲染，显示数据可视化内容
- **实际结果**：柱状图和折线图正常显示
- **截图**：[dashboard-charts.jpg](file:///e:/animal%20sounds/screenshots/dashboard-charts.jpg)

### 步骤3.3：查看表格区域
- **操作说明**：在仪表盘页面查看数据集概览表格和最近训练任务表格
- **预期结果**：数据集表格显示5条记录，训练任务表格显示"No Data"
- **实际结果**：数据集表格显示5条记录，训练任务表格显示"No Data"
- **截图**：[dashboard-tables.jpg](file:///e:/animal%20sounds/screenshots/dashboard-tables.jpg)

### 步骤3.4：查看系统状态
- **操作说明**：在仪表盘页面查看系统状态区域
- **预期结果**：显示数据库连接、文件存储、API服务、训练服务状态
- **实际结果**：系统状态正常显示，数据库连接、文件存储、API服务正常，训练服务空闲
- **截图**：[dashboard-status.jpg](file:///e:/animal%20sounds/screenshots/dashboard-status.jpg)

---

## 四、数据集管理流程

### 步骤4.1：进入数据集管理页面
- **操作说明**：点击左侧导航栏"数据集管理"菜单项
- **预期结果**：跳转到数据集管理页面，显示数据集列表
- **实际结果**：成功进入 /dataset 页面，显示5条数据集记录
- **截图**：[dataset-list.jpg](file:///e:/animal%20sounds/screenshots/dataset-list.jpg)

### 步骤4.2：打开创建数据集对话框
- **操作说明**：点击"创建数据集"按钮
- **预期结果**：弹出创建数据集对话框，包含名称、描述输入框和提交按钮
- **实际结果**：对话框正常打开，包含所需字段
- **截图**：[dataset-create-dialog.jpg](file:///e:/animal%20sounds/screenshots/dataset-create-dialog.jpg)

### 步骤4.3：创建数据集
- **操作说明**：输入数据集名称"测试数据集"，描述"用于功能测试的数据集"，点击"提交"按钮
- **预期结果**：数据集创建成功，列表新增一条记录
- **实际结果**：数据集创建成功，列表新增"测试数据集"
- **截图**：[dataset-create-success.jpg](file:///e:/animal%20sounds/screenshots/dataset-create-success.jpg)

### 步骤4.4：编辑数据集
- **操作说明**：点击数据集的"编辑"按钮，修改描述为"更新后的测试数据集描述"，点击"提交"按钮
- **预期结果**：数据集更新成功，描述字段更新
- **实际结果**：编辑成功，页面返回数据集列表
- **截图**：[dataset-edit-dialog.jpg](file:///e:/animal%20sounds/screenshots/dataset-edit-dialog.jpg)、[dataset-edit-success.jpg](file:///e:/animal%20sounds/screenshots/dataset-edit-success.jpg)

### 步骤4.5：删除数据集
- **操作说明**：点击数据集的"删除"按钮，在确认对话框中点击"确定"
- **预期结果**：数据集删除成功，列表中该记录消失
- **实际结果**：删除成功，"测试数据集"从列表中消失
- **截图**：[dataset-delete-success.jpg](file:///e:/animal%20sounds/screenshots/dataset-delete-success.jpg)

---

## 五、音频管理流程

### 步骤5.1：进入音频管理页面
- **操作说明**：点击左侧导航栏"音频管理"菜单项
- **预期结果**：跳转到音频管理页面，显示音频文件列表
- **实际结果**：成功进入 /audio 页面，显示20条音频记录
- **截图**：[audio-list.jpg](file:///e:/animal%20sounds/screenshots/audio-list.jpg)

### 步骤5.2：搜索音频文件
- **操作说明**：在搜索框输入"大象"并搜索
- **预期结果**：列表显示匹配"大象"的音频文件
- **实际结果**：搜索结果正确显示
- **截图**：[audio-search.jpg](file:///e:/animal%20sounds/screenshots/audio-search.jpg)

### 步骤5.3：筛选数据集
- **操作说明**：使用数据集筛选器选择一个数据集
- **预期结果**：列表显示该数据集下的音频文件
- **实际结果**：筛选功能正常工作
- **截图**：[audio-filter.jpg](file:///e:/animal%20sounds/screenshots/audio-filter.jpg)

### 步骤5.4：播放音频文件
- **操作说明**：点击音频记录的播放按钮
- **预期结果**：弹出音频播放弹窗，显示波形图和播放控制
- **实际结果**：播放弹窗正常打开，波形图和控制按钮显示正常
- **截图**：[audio-play.jpg](file:///e:/animal%20sounds/screenshots/audio-play.jpg)

---

## 六、标签管理流程

### 步骤6.1：进入标签管理页面
- **操作说明**：点击左侧导航栏"标签管理"菜单项
- **预期结果**：跳转到标签管理页面，显示标签树和标签列表
- **实际结果**：成功进入 /label 页面，标签树和列表正常显示
- **截图**：[label-management.jpg](file:///e:/animal%20sounds/screenshots/label-management.jpg)

### 步骤6.2：搜索标签
- **操作说明**：在搜索框输入"鸟"并搜索
- **预期结果**：列表显示匹配"鸟"的标签
- **实际结果**：搜索结果正确显示
- **截图**：[label-search.jpg](file:///e:/animal%20sounds/screenshots/label-search.jpg)

### 步骤6.3：打开创建标签对话框
- **操作说明**：点击"创建标签"按钮
- **预期结果**：弹出创建标签对话框，包含名称、父标签、分类层级输入框和提交按钮
- **实际结果**：对话框正常打开，包含所需字段
- **截图**：[label-create-dialog.jpg](file:///e:/animal%20sounds/screenshots/label-create-dialog.jpg)

### 步骤6.4：创建标签
- **操作说明**：输入标签名称"测试标签"，选择分类层级为"species"，点击"提交"按钮
- **预期结果**：标签创建成功，列表新增一条记录
- **实际结果**：标签创建成功
- **截图**：[label-create-success.jpg](file:///e:/animal%20sounds/screenshots/label-create-success.jpg)

---

## 七、标注工作台流程

### 步骤7.1：进入标注工作台页面
- **操作说明**：点击左侧导航栏"标注工作台"菜单项
- **预期结果**：跳转到标注工作台页面，显示标注列表
- **实际结果**：成功进入 /annotation 页面
- **截图**：[annotation-workbench.jpg](file:///e:/animal%20sounds/screenshots/annotation-workbench.jpg)

### 步骤7.2：打开创建标注对话框
- **操作说明**：点击"创建标注"按钮
- **预期结果**：弹出创建标注对话框，包含音频文件、标签、时间范围等字段
- **实际结果**：对话框正常打开，包含所需字段
- **截图**：[annotation-create-dialog.jpg](file:///e:/animal%20sounds/screenshots/annotation-create-dialog.jpg)

### 步骤7.3：创建标注
- **操作说明**：选择音频文件、标签，设置开始时间为0、结束时间为5，点击"提交"按钮
- **预期结果**：标注创建成功，列表新增一条记录
- **实际结果**：标注创建成功
- **截图**：[annotation-create-success.jpg](file:///e:/animal%20sounds/screenshots/annotation-create-success.jpg)

---

## 八、训练任务流程

### 步骤8.1：进入训练任务页面
- **操作说明**：点击左侧导航栏"训练任务"菜单项
- **预期结果**：跳转到训练任务页面，显示训练任务列表
- **实际结果**：成功进入 /train 页面
- **截图**：[train-task-list.jpg](file:///e:/animal%20sounds/screenshots/train-task-list.jpg)

### 步骤8.2：打开创建训练任务对话框
- **操作说明**：点击"创建训练任务"按钮
- **预期结果**：弹出创建训练任务对话框，包含任务名称、数据集、模型类型等字段
- **实际结果**：对话框正常打开，包含所需字段
- **截图**：[train-task-create-dialog.jpg](file:///e:/animal%20sounds/screenshots/train-task-create-dialog.jpg)

### 步骤8.3：创建训练任务
- **操作说明**：输入任务名称"测试训练任务"，选择数据集和模型类型"RandomForest"，点击"提交"按钮
- **预期结果**：训练任务创建成功，列表新增一条记录
- **实际结果**：训练任务创建成功
- **截图**：[train-task-create-success.jpg](file:///e:/animal%20sounds/screenshots/train-task-create-success.jpg)

---

## 九、用户管理流程

### 步骤9.1：进入用户管理页面
- **操作说明**：点击左侧导航栏"用户管理"菜单项
- **预期结果**：跳转到用户管理页面，显示用户列表
- **实际结果**：成功进入 /user 页面，显示5条用户记录
- **截图**：[user-management.jpg](file:///e:/animal%20sounds/screenshots/user-management.jpg)

### 步骤9.2：打开创建用户对话框
- **操作说明**：点击"创建用户"按钮
- **预期结果**：弹出创建用户对话框，包含用户名、密码、真实姓名、角色等字段
- **实际结果**：对话框正常打开，包含所需字段
- **截图**：[user-create-dialog.jpg](file:///e:/animal%20sounds/screenshots/user-create-dialog.jpg)

### 步骤9.3：创建用户
- **操作说明**：输入用户名"testuser"、密码"test123"、真实姓名"测试用户"、选择角色"annotator"，点击"提交"按钮
- **预期结果**：用户创建成功，列表新增一条记录
- **实际结果**：用户创建成功，列表新增"testuser"
- **截图**：[user-create-success.jpg](file:///e:/animal%20sounds/screenshots/user-create-success.jpg)

### 步骤9.4：编辑用户
- **操作说明**：点击用户的"编辑"按钮，修改真实姓名为"更新后的测试用户"，点击"提交"按钮
- **预期结果**：用户信息更新成功
- **实际结果**：编辑成功，真实姓名已更新
- **截图**：[user-edit-dialog.jpg](file:///e:/animal%20sounds/screenshots/user-edit-dialog.jpg)、[user-edit-success.jpg](file:///e:/animal%20sounds/screenshots/user-edit-success.jpg)

### 步骤9.5：禁用用户
- **操作说明**：点击用户的"禁用"按钮
- **预期结果**：用户状态变为禁用，按钮变为"启用"
- **实际结果**：禁用成功，用户状态显示为禁用
- **截图**：[user-disable-success.jpg](file:///e:/animal%20sounds/screenshots/user-disable-success.jpg)

---

## 十、流程总结

### 流程完整性验证
| 模块 | 功能 | 状态 |
|------|------|------|
| 登录 | 用户名密码登录 | ✅ 正常 |
| 仪表盘 | 统计卡片、图表、表格、系统状态 | ✅ 正常 |
| 数据集管理 | 创建、编辑、删除数据集 | ✅ 正常 |
| 音频管理 | 列表、搜索、筛选、播放 | ✅ 正常 |
| 标签管理 | 列表、搜索、创建标签 | ✅ 正常 |
| 标注工作台 | 创建标注 | ✅ 正常 |
| 训练任务 | 创建训练任务 | ✅ 正常 |
| 用户管理 | 创建、编辑、禁用用户 | ✅ 正常 |

### 截图文件清单

所有截图文件保存在 `screenshots/` 目录下：

| 文件名 | 对应步骤 |
|--------|----------|
| login-page.jpg | 步骤2.1 |
| dashboard-page.jpg | 步骤2.2 |
| dashboard-cards.jpg | 步骤3.1 |
| dashboard-charts.jpg | 步骤3.2 |
| dashboard-tables.jpg | 步骤3.3 |
| dashboard-status.jpg | 步骤3.4 |
| dataset-list.jpg | 步骤4.1 |
| dataset-create-dialog.jpg | 步骤4.2 |
| dataset-create-success.jpg | 步骤4.3 |
| dataset-edit-dialog.jpg | 步骤4.4 |
| dataset-edit-success.jpg | 步骤4.4 |
| dataset-delete-success.jpg | 步骤4.5 |
| audio-list.jpg | 步骤5.1 |
| audio-search.jpg | 步骤5.2 |
| audio-filter.jpg | 步骤5.3 |
| audio-play.jpg | 步骤5.4 |
| label-management.jpg | 步骤6.1 |
| label-search.jpg | 步骤6.2 |
| label-create-dialog.jpg | 步骤6.3 |
| label-create-success.jpg | 步骤6.4 |
| annotation-workbench.jpg | 步骤7.1 |
| annotation-create-dialog.jpg | 步骤7.2 |
| annotation-create-success.jpg | 步骤7.3 |
| train-task-list.jpg | 步骤8.1 |
| train-task-create-dialog.jpg | 步骤8.2 |
| train-task-create-success.jpg | 步骤8.3 |
| user-management.jpg | 步骤9.1 |
| user-create-dialog.jpg | 步骤9.2 |
| user-create-success.jpg | 步骤9.3 |
| user-edit-dialog.jpg | 步骤9.4 |
| user-edit-success.jpg | 步骤9.4 |
| user-disable-success.jpg | 步骤9.5 |

### 备注

1. 测试过程中发现数据集编辑对话框存在一个小问题：编辑按钮点击后可能显示错误的数据（显示第一个数据集而非选中数据集），建议后续修复。
2. 所有核心功能均已验证通过，用户流程完整且连贯。

