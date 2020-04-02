package com.kakao.smartmemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kakao.smartmemo.DTO.AlarmDTO
import kotlinx.android.synthetic.main.alarm_list_item.view.*

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.ViewHolder>(){

    var datas:MutableList<AlarmDTO> = mutableListOf(AlarmDTO("한성대학교", "2020.03.14", "도서관 책 반납"), AlarmDTO("녹십자약국", "2020.03.15", "마스크 사기"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        datas[position].let {
            with(holder) {
                alarm_place.text = it.place
                alarm_date.text = it.date
                alarm_content.text = it.content
            }
        }
    }

    //데이터들 업데이트
    fun setDataList(dataList: List<AlarmDTO>?) {
        if (dataList == null) {
            return
        }
        this@AlarmAdapter.datas = dataList.toMutableList()
    }

    inner class ViewHolder(parent:ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.alarm_list_item, parent, false)) {
        val alarm_place = itemView.textView_alarm_place
        val alarm_date = itemView.textView_date
        val alarm_content = itemView.textView_alarm_content
    }

}