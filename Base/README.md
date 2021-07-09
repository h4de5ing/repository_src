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
``` 自定义Adapter 点击事件
package com.github.h4de5ing.ammini.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.h4de5ing.ammini.R
import com.github.h4de5ing.ammini.bean.HistoryList
import com.github.h4de5ing.ammini.utils.API
import com.github.h4de5ing.imageselecter.utils.GlideEngine

class HistoryListAdapter :
    BaseQuickAdapter<HistoryList, BaseViewHolder>(R.layout.item_recy_cardview_iv), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: HistoryList) {
        holder.setText(R.id.tv_item, "$item")
        val iv = holder.getView<ImageView>(R.id.sign)
        GlideEngine.createGlideEngine()
            .loadImage(context, "${API.APP_DOMAIN}${API.getRepairfile}?path=${item.pdqm}", iv)
        iv.setOnClickListener { imgItemClickListener(holder.itemView, item) }
    }

    private lateinit var imgItemClickListener: (view: View, item: HistoryList) -> Unit
    fun addImgClickListener(listener: (view: View, item: HistoryList) -> Unit) {
        imgItemClickListener = listener
    }

    fun addOnItemLongClickListener(listener: (view: View, item: HistoryList) -> Unit) {
        itemLongClickListener = listener
    }

    private lateinit var itemClickListener: (item: HistoryList) -> Unit
    private lateinit var itemLongClickListener: (view: View, item: HistoryList) -> Unit
    private fun registerClickListener(view: View, position: Int) {
        if (::itemClickListener.isInitialized) {
            view.setOnClickListener(fun(_: View) {
                getItem(position).let {
                    itemClickListener.invoke(it)
                }
            })
            view.setOnLongClickListener(fun(_: View): Boolean {
                getItem(position).let { item ->
                    itemLongClickListener.invoke(view, item)
                }
                return false
            })
        }
    }
}
```