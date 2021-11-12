package com.github.h4de5ing.base

/*** 时间处理工具类
 * Calendar 日历类
 * LocalDate 本地日期类
 * LocalTime 本地时间类
 * LocalDateTime 本地日期时间类
 * Instant 瞬间，某一刻
 * Period 时期，一段时间 between 比较2个日期
 * Duration 期间 持续时间
 * //long -> LocalDateTime
LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

//String -> LocalDateTime
DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
LocalDateTime.parse("2021-10-28 00:00:00", dateTimeFormatter1);

//LocalDateTime -> long
LocalDateTime对象.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

//LocalDateTime -> String
DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
LocalDateTime对象.format(dateTimeFormatter1)
 *
 */