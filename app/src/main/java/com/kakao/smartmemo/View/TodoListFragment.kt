package com.kakao.smartmemo.View

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.smartmemo.Adapter.TodoAdapter
import com.kakao.smartmemo.Adapter.TodoDeleteAdapter
import com.kakao.smartmemo.Contract.TodoContract
import com.kakao.smartmemo.Data.PlaceData
import com.kakao.smartmemo.Data.TodoData
import com.kakao.smartmemo.Object.FolderObject
import com.kakao.smartmemo.Presenter.TodoPresenter
import com.kakao.smartmemo.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.todolist_fragment.*
import java.time.LocalDateTime

class TodoListFragment : Fragment(), TodoContract.View {

    private lateinit var presenter : TodoContract.Presenter
    private lateinit var todoList : ListView
    private lateinit var bottomNavigationView : BottomNavigationView
    private lateinit var textViewTodoList : TextView
    private lateinit var adapter : TodoAdapter
    private lateinit var todoDeleteAdapter : TodoDeleteAdapter
    private lateinit var cont: Context
    private lateinit var noTodoTextView: TextView
    private var todoArrayList = mutableListOf<TodoData>()
    private var placeList = arrayListOf<PlaceData>()
    private lateinit var onePlaceIntent:Intent
    val date: LocalDateTime = LocalDateTime.now()
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.todolist_fragment, container, false)
        cont = view.context // view의 컨텍스트
        bottomNavigationView = view.findViewById(R.id.navigationview_bottom)
        textViewTodoList = view.findViewById(R.id.textView_todolist)
        noTodoTextView = view.findViewById(R.id.textView_non_todo)

        presenter = TodoPresenter(this)

        todoList = view.findViewById(R.id.todolist) as ListView
        todoList.setOnItemClickListener { parent, view, position, id ->
            onePlaceIntent = Intent(cont, AddTodo::class.java)
            if (todoArrayList[position].setPlaceAlarm) {
                presenter.getOnePlaceTodo(todoArrayList[position].todoId)
                onePlaceIntent.putExtra("todoData", todoArrayList[position])
            } else {
                onePlaceIntent.putExtra("todoData", todoArrayList[position])
                startActivity(onePlaceIntent)
            }
        }
        bottomNavigationView.visibility = GONE; //하단메뉴 안보이게

        return view
    }

    override fun onStart() {
        super.onStart()
        presenter.getAllTodo()

        if(navigationview_bottom.visibility == VISIBLE) {
            navigationview_bottom.visibility = GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        (activity as MainActivity).toolbar.title="Todo List"
        (activity as MainActivity).fab.visibility = View.VISIBLE
        (activity as MainActivity).fab_todo.visibility = View.VISIBLE
        (activity as MainActivity).fab_memo.visibility = View.VISIBLE
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.select_group_in_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item?.itemId) {
            R.id.select_group -> {
                selectGroup()
                return true
            }
            R.id.delete_memo ->{
                count++
                if(count%2 == 0) {
                    presenter.getAllTodo()
                    count = 0
                } else {
                    deleteTodo()
                }
                return true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun selectGroup(){
        var i = 1
        val groupValues:Array<CharSequence> = Array(FolderObject.folderInfo.size+1) {""}
        val groupIds:Array<CharSequence> = Array(FolderObject.folderInfo.size+1) {""}
        groupValues[0] = "전체 TodoList"
        FolderObject.folderInfo.forEach {
            groupIds[i] = it.key
            groupValues[i] = it.value
            i++
        }

        val listDialog: AlertDialog.Builder = AlertDialog.Builder(
            this.context,
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        )
        listDialog.setTitle("폴더 선택")
            .setItems(groupValues, DialogInterface.OnClickListener { _, which ->
                if (which == 0) {
                    (activity as MainActivity).toolbar.title = "Todo List"
                    presenter.getAllTodo()
                } else {
                    (activity as MainActivity).toolbar.title = groupValues[which]
                    presenter.getGroupTodo(groupIds[which].toString())
                }
            })
            .show()
    }

    override fun showAllTodo(todoData: MutableList<TodoData>) {
        count = 0
        if (todoData.isEmpty()) {
            todoArrayList.clear()
            noTodoTextView.visibility = VISIBLE
            todoList.visibility = GONE
        } else {
            todoArrayList.clear()
            todoArrayList = todoData
            noTodoTextView.visibility = GONE
            todoList.visibility = VISIBLE
        }

        adapter = TodoAdapter(cont, todoArrayList)
        todoList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        todoList.isClickable = true
        todoList.adapter = adapter
        presenter.setTodoAdapterModel(adapter)
        presenter.setTodoAdapterView(adapter)
        adapter.notifyAdapter()
        bottomNavigationView.visibility = GONE
    }

    override fun sendPlaceData(placeList: MutableList<PlaceData>) {
        this.placeList.clear()
        this.placeList = placeList as ArrayList<PlaceData>
        onePlaceIntent.putExtra("placeList", this.placeList)

        startActivity(onePlaceIntent)
    }

    private fun deleteTodo() {
        if (todoArrayList.size != 0) {
            //하단 메뉴
            bottomNavigationView.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.removeItem -> {
                        count = 0
                        var selectedItem = mutableListOf<TodoData>()
                        var count = todoDeleteAdapter.count
                        var checkedItems = todoDeleteAdapter.selectedTodo()
                        for( i in count-1 downTo 0) {
                            for (item in checkedItems) {
                                if(i == item.key) {
                                    selectedItem.add(item.value)
                                    todoArrayList.removeAt(i)
                                }
                            }
                        }
                        presenter.deleteTodo(selectedItem)
                        todoList.clearChoices()
                        adapter = TodoAdapter(cont, todoArrayList)
                        todoList.adapter = adapter
                        bottomNavigationView.visibility = GONE //하단메뉴 안보이게
                        presenter.setTodoAdapterModel(adapter)
                        presenter.setTodoAdapterView(adapter)
                        adapter.notifyAdapter()
                        true
                    }
                    R.id.cancelItem -> {
                        showAllTodo(todoArrayList)
                        bottomNavigationView.visibility = GONE //하단메뉴 안보이게
                        true
                    }
                }
                true
            }
        }
        todoDeleteAdapter = TodoDeleteAdapter(cont, todoArrayList)
        todoList.adapter = todoDeleteAdapter
        presenter.setTodoDeleteAdapterModel(todoDeleteAdapter)
        presenter.setTodoDeleteAdapterView(todoDeleteAdapter)
        bottomNavigationView.visibility = VISIBLE //하단메뉴 보이게
    }
}