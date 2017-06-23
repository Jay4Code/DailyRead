# DailyRead
一个轻量级的仿[**每日一文**](https://meiriyiwen.com/apps)的阅读app。

----------

app截图
![图一](https://raw.githubusercontent.com/Jay4Code/DailyRead/dev_volley/screenshot/screenshot_1.jpg)  ![图二](https://github.com/Jay4Code/DailyRead/raw/dev_volley/screenshot/screenshot_2.jpg)  ![图三](https://github.com/Jay4Code/DailyRead/raw/dev_volley/screenshot/screenshot_3.jpg)

----------

v1.0.3
- 通过Volley获取JSON数据，fastjson转换数据，DiskLruCache实现内存与磁盘的数据缓存

v1.0.4
- 修复某些情况下无法读取缓存数据的问题
- 调整JSONObject，JSON，Object间的转换
- 添加混淆

v1.0.5
- 优化编译，使用阿里云maven仓库代替jcenter
- 调整资源目录：mipma中只存放启动图标
- 调整退出应用的逻辑：双击退出
- 添加友盟统计
- 添加API使用说明