package com.kakao.smartmemo

import android.annotation.SuppressLint

import android.content.Context

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.*

import com.kakao.smartmemo.DTO.TodoDTO
import kotlinx.android.synthetic.main.todo_list_delete.view.*

class TodoDeleteAdapter(val context: Context, private val todoList: ArrayList<TodoDTO>) : BaseAdapter() {

    @SuppressLint("ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.todo_list_delete, null)
        val todo = todoList[position]
        view.textView_todo.text = todo.todoContent

        var checkedTodo = false

        view.group_color.setBackgroundColor(Color.parseColor("#B2CCFF"))
        view.textView_todo.setOnClickListener() { // 취소선 ( 성 공 )
            if (checkedTodo) { // todolist에 취소선이 그어져 있으면 true
                view.textView_todo.paintFlags = 0
                checkedTodo = false
            } else { // todolist에 취소선이 그어져 있지 않으면 false
                view.textView_todo.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                checkedTodo = true
            }
        }

        view.setOnLongClickListener(View.OnLongClickListener {
            val count = getCount()
            for(i in 0.. count) {

            }
            return@OnLongClickListener false
        })

        return view
    }

    override fun getItem(position: Int) : TodoDTO {
        return todoList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return todoList.size
    }

}