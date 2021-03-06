package com.kakao.smartmemo.Model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.smartmemo.Contract.AddTodoContract
import com.kakao.smartmemo.Contract.DialogContract
import com.kakao.smartmemo.Contract.MapContract
import com.kakao.smartmemo.Contract.TodoContract
import com.kakao.smartmemo.Data.PlaceAlarmData
import com.kakao.smartmemo.Data.PlaceData
import com.kakao.smartmemo.Data.TodoData
import com.kakao.smartmemo.Object.FolderObject

class TodoModel {
    private var firebase = FirebaseDatabase.getInstance().reference
    private var firebaseTodo = firebase.child("Todo")
    private var firebaseGroup = firebase.child("Group")
    private var firebasePlace = firebase.child("Place")
    private var firebaseTodoId = firebase.child("TodoId")
    private lateinit var onTodoListener: TodoContract.OnTodoListener
    private lateinit var onPlaceListener: MapContract.OnPlaceListener
    private lateinit var onAddTodoListener: AddTodoContract.OnAddTodoListener
    private lateinit var onDialogListener : DialogContract.OnDialogListener

    constructor(onTodoListener: TodoContract.OnTodoListener) {
        this.onTodoListener = onTodoListener
    }

    constructor(onPlaceListener: MapContract.OnPlaceListener) {
        this.onPlaceListener = onPlaceListener
    }

    constructor(onAddTodoListener: AddTodoContract.OnAddTodoListener) {
        this.onAddTodoListener = onAddTodoListener
    }

    constructor(onDialogListener: DialogContract.OnDialogListener) {
        this.onDialogListener = onDialogListener
    }

    data class TodoTmp(
        var title: String = "",
        var groupId: String = ""
    )

    data class TimeAlarm(
        var setTimeAlarm: Boolean = false,
        var timeDate: String = "",
        var timeAgain: Int = 0,
        var timeTime: String = ""
    )

    data class PlaceAlarm(
        var setPlaceAlarm: Boolean = false,
        var placeDate: String = "",
        var placeAgain: Int = 0
    )

    fun addTodo(todoData: TodoData, placeList: ArrayList<PlaceData>) {
        var i = 0
        var todoId = ""
        todoId = if (todoData.todoId == "") {
            placeList.forEach {
                it.id = todoId
            }
            (System.currentTimeMillis() * 5000).toInt().toString()
        } else {
            todoData.todoId
        }

        var todotmp = TodoTmp(todoData.title, todoData.groupId)
        var timeAlarm = TimeAlarm(
            todoData.setTimeAlarm,
            todoData.timeDate,
            todoData.timeAgain,
            todoData.timeTime
        )
        var placeAlarm = PlaceAlarm(todoData.setPlaceAlarm, todoData.placeDate, todoData.placeAgain)

        firebaseTodo.child(todoId).child("PlaceAlarm").child("setPlaceAlarm").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(setPlaceAlarmSnapshot: DataSnapshot) {
                if (setPlaceAlarmSnapshot.value.toString().toBoolean()) {
                    firebaseTodoId.child(todoId).removeValue()
                    if (placeAlarm.setPlaceAlarm) {
                        placeList.forEach {
                            if (it.placeId == "") {
                                it.placeId = (System.currentTimeMillis() * 4321).toInt().toString()
                            } else {
                                it.placeId
                            }
                            firebaseTodoId.child(todoId).child(it.placeId).setValue(it)
                            if (placeList.size - 1 == i) {
                                onAddTodoListener.onAddSuccess()
                            } else {
                                i++
                            }
                        }
                    } else {
                        onAddTodoListener.onAddSuccess()
                    }
                }
            }

        })

        firebaseGroup.child(todoData.groupId).child("TodoInfo")
            .updateChildren(mapOf(todoId to todoId))
        with(firebaseTodo.child(todoId)) {
            setValue(todotmp)
            child("TimeAlarm").setValue(timeAlarm)
            child("PlaceAlarm").setValue(placeAlarm)
        }
    }

    fun getAllTodo() {
        var i = 0
        var j = 0
        var todoList = mutableListOf<TodoData>()
        FolderObject.folderInfo.forEach {
            firebaseGroup.child(it.key).child("TodoInfo").addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(todoIdSanpshot: DataSnapshot) {
                    if (todoIdSanpshot.children.count() != 0) {
                        for (todoId in todoIdSanpshot.children) {
                            firebaseTodo.child(todoId.value.toString())
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}
                                    override fun onDataChange(todoSnapshot: DataSnapshot) {
                                        var timeAlarm = todoSnapshot.child("TimeAlarm")
                                            .getValue(TimeAlarm::class.java)
                                        var placeAlarm = todoSnapshot.child("PlaceAlarm")
                                            .getValue(PlaceAlarm::class.java)
                                        if (timeAlarm != null && placeAlarm != null) {
                                            todoList.add(
                                                TodoData(
                                                    todoId.value.toString(),
                                                    todoSnapshot.child("title").value.toString(),
                                                    todoSnapshot.child("groupId").value.toString(),
                                                    timeAlarm.setTimeAlarm,
                                                    timeAlarm.timeDate,
                                                    timeAlarm.timeTime,
                                                    timeAlarm.timeAgain,
                                                    placeAlarm.setPlaceAlarm,
                                                    placeAlarm.placeDate,
                                                    placeAlarm.placeAgain
                                                )
                                            )
                                        }

                                        if (i == FolderObject.folderInfo.size - 1 && j == todoIdSanpshot.children.count() - 1) {
                                            onTodoListener.onSuccess(todoList)
                                            i = 0
                                        } else if (j == todoIdSanpshot.children.count() - 1) {
                                            j = 0
                                            i++
                                        } else {
                                            j++
                                        }
                                    }
                                })
                        }
                    } else {
                        if (i == FolderObject.folderInfo.size - 1) {
                            i = 0
                            onTodoListener.onSuccess(todoList)
                        }
                        i++
                    }
                }
            })
        }
    }

    fun getOnePlaceTodo(todoId: String) {
        var i = 0
        var placeList = mutableListOf<PlaceData>()
        firebaseTodoId.child(todoId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {     }

            override fun onDataChange(placeSnapshot: DataSnapshot) {
                placeSnapshot.children.forEach {placeId ->
                    placeId.getValue(PlaceData::class.java)?.let { placeList.add(it) }
                    if (placeSnapshot.children.count()-1 == i) {
                        onTodoListener.onOnePlaceSuccess(placeList)
                    } else {
                        i++
                    }
                }
            }
        })
    }

    fun getGroupTodo(groupId: String) {
        var todoList = mutableListOf<TodoData>()
        var i = 0

        firebaseGroup.child(groupId).child("TodoInfo").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {  }
            override fun onDataChange(todoSnapshot: DataSnapshot) {
                if(todoSnapshot.hasChildren()){
                    todoSnapshot.children.forEach { todoId ->
                        firebaseTodo.child(todoId.key.toString()).addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) { }
                            override fun onDataChange(todoDataSnapshot: DataSnapshot) {
                                val placeAlarm = todoDataSnapshot.child("PlaceAlarm").getValue(PlaceAlarm::class.java)
                                val timeAlarm = todoDataSnapshot.child("TimeAlarm").getValue(TimeAlarm::class.java)
                                if (timeAlarm != null && placeAlarm != null) {
                                    todoList.add(i, TodoData(
                                        todoId.key.toString(),
                                        todoDataSnapshot.child("title").value.toString(),
                                        groupId,
                                        timeAlarm.setTimeAlarm, timeAlarm.timeDate, timeAlarm.timeTime, timeAlarm.timeAgain,
                                        placeAlarm.setPlaceAlarm, placeAlarm.placeDate, placeAlarm.placeAgain
                                    ))
                                    if (todoSnapshot.children.count() - 1 == i) {
                                        onTodoListener.onGroupSuccess(todoList)
                                    }
                                }
                            }
                        })
                    }
                } else{
                    onTodoListener.onGroupSuccess(todoList)
                }
            }
        })
    }

    fun deleteTodo(deleteTodoList: MutableList<TodoData>) {
        deleteTodoList.forEach { todoData ->
            firebaseGroup.child(todoData.groupId).child("TodoInfo").child(todoData.todoId).removeValue()
            firebaseTodo.child(todoData.todoId).removeValue()
            firebaseTodoId.child(todoData.todoId).removeValue()
        }
    }

    fun getPlaceTodo(status: String) {
        var i = 0 // FolderObject.folderInfo
        var j = 0 // todoSnapshot.children.count()
        var m = 0 // placeSnapshot.children.count()
        var placeList = mutableListOf<PlaceData>()
        FolderObject.folderInfo.forEach {
            firebaseGroup.child(it.key).child("TodoInfo").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(todoSnapshot: DataSnapshot) {
                    todoSnapshot.children.forEach { todoId ->
                        firebaseTodoId.child(todoId.value.toString()).addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(placeSnapshot: DataSnapshot) {
                                placeSnapshot.children.forEach { placeId ->
                                    if(placeId.hasChildren()){
                                        placeId.getValue(PlaceData::class.java)?.let { it1 ->
                                            placeList.add(
                                                it1
                                            )
                                        }
                                        if (m == placeSnapshot.children.count()-1 && j == todoSnapshot.children.count()-1
                                            && i == FolderObject.folderInfo.size - 1 && placeList[placeSnapshot.children.count()-1] != null) {
                                            if (status == "addTodo") {
                                                onAddTodoListener.onSuccess(placeList)
                                            } else if (status == "map") {
                                                onPlaceListener.onPlaceSuccess(placeList, "todo")
                                            }

                                        } else if (m == placeSnapshot.children.count() - 1 && j == todoSnapshot.children.count() - 1) {
                                            m = 0
                                            j = 0
                                            i++
                                        } else if (m == placeSnapshot.children.count() - 1) {
                                            m =0
                                            j++
                                        }
                                        else {
                                            m++
                                        }
                                    } else {
                                        if (j == todoSnapshot.children.count() - 1 && i == FolderObject.folderInfo.size - 1) {
                                            if (status == "addTodo") {
                                                onAddTodoListener.onSuccess(placeList)
                                            } else if (status == "map") {
                                                onPlaceListener.onPlaceSuccess(placeList, "todo")
                                            }
                                        } else if (j == todoSnapshot.children.count() - 1) {
                                            j=0
                                            i++
                                        } else {
                                            j++
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            })
        }
    }

    fun deleteTodoInfo(groupId: String, todoId: String) {
        firebaseGroup.child(groupId).child("TodoInfo").child(todoId).removeValue()
    }

    fun getMapDialogTodo(todo: MutableList<PlaceData>) {
        var todoList = mutableListOf<PlaceAlarmData>()
        var i = 0
        todo.forEach {
            firebaseTodo.child(it.id).addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(todoSnapshot: DataSnapshot) {
                    var placeAlarm = todoSnapshot.child("PlaceAlarm").getValue(PlaceAlarm::class.java)
                    if (placeAlarm != null) {
                        todoList.add(PlaceAlarmData(
                            it.id,
                            it.placeId,
                            it.place,
                            placeAlarm.placeDate,
                            todoSnapshot.child("title").value.toString(),
                            placeAlarm.setPlaceAlarm)
                        )
                    }
                    if(todo.size-1 == i){
                        onDialogListener.onSuccessTodo(todoList)
                    }
                    i++
                }
            })
        }
    }

    fun cancelPlaceAlarm(placeAlarmTodoId: Int) {
        firebaseTodo.child(placeAlarmTodoId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {  }
            override fun onDataChange(placeAlarmSnapshot: DataSnapshot) {
                var placeAlarm = placeAlarmSnapshot.child("PlaceAlarm").getValue(PlaceAlarm::class.java)
                if (placeAlarm != null) {
                    placeAlarm.setPlaceAlarm = false
                    placeAlarmSnapshot.ref.child("PlaceAlarm").setValue(placeAlarm)
                    firebaseTodoId.child(placeAlarmTodoId.toString()).removeValue()
                }
            }
        })
    }
}