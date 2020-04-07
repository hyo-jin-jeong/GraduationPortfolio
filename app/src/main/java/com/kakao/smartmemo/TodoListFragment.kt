package com.kakao.smartmemo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kakao.smartmemo.DTO.TodoDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.todolist_fragment.view.*
import java.util.zip.Inflater


class TodoListFragment : Fragment() {

    private lateinit var todolist : ListView
    private lateinit var todoEditingbtn : ImageButton
    private var todoList = arrayListOf<TodoDTO>(TodoDTO("약먹기"), TodoDTO("도서관 책 반납"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.todolist_fragment, container, false)
        val todoInflate = inflater.inflate(R.layout.todo_list_item, container, false)

        todolist = view.findViewById(R.id.todolist) as ListView
        todolist.adapter = TodoAdapter(view.context, todoList)
        todoEditingbtn = view.findViewById(R.id.imagebtn_editing) as ImageButton
        todoEditingbtn.setOnClickListener(View.OnClickListener {
            val count = todolist.adapter.count

            for(i in 0.. count) {
                /*todolist.adapter.getItem(i)
                val delete = todoInflate.findViewById(R.id.imagebtn_editing) as ImageButton
                delete.visibility = VISIBLE*/
            }
        })
        todolist.clearChoices() //listview 선택 초기화
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        (activity as MainActivity).toolbar.title="Todo List"
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.select_todolist_remove, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item?.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).mDrawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.action_todolist_remove -> {
                return true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }
    fun onCreateDialog() : Dialog {
        return let {
            val builder = AlertDialog.Builder(activity)
            val edit_inflater: LayoutInflater = LayoutInflater.from(context)
            val editDialogView: View = edit_inflater.inflate(R.layout.todolist_editing_dialog, null)
            builder.setTitle("TODO LIST 입력")
            val spinner: Spinner = editDialogView.findViewById(R.id.spinner)
            val adapter = ArrayAdapter.createFromResource(context, R.array.group, android.R.layout.simple_spinner_item)
            spinner.adapter = adapter

            builder.setView(editDialogView).setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener {
                        dialog, which ->
                    dialog.dismiss()
                })
            builder.create()
            builder.show()
        }
    }
}