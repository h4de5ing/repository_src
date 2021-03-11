# 纯Kotlin库的封装,主要对kt和java的基础库封装

- isNotEmpty() 判断对象是否为Null
- nDecimal(n) 保留n位小数点
- date() 将long时间戳格式化为yyyy-MM-dd HH:mm:ss 样式
- date(style) 将long时间戳格式化为自定义格式
- now() 获取当前时间格式yyyy-MM-dd HH:mm:ss
- string2Date("2021-02-01 00:00","yyyy-MM-dd HH:mm:ss") 将格式化的时间转成date
- today() 获取今天日期
- todayZero() 获取今天零点时间戳
- thisMonth() 获取这个月
- toTime 获取当前的时间
- startZeroStr("00") 以几位数补零("01")
- delayed 延迟执行
- timer 定时任务


TODO
delayed 用kotlin实现
timer 用kotlin实现
Thread 用kotlin实现

将资产中的工具类移植