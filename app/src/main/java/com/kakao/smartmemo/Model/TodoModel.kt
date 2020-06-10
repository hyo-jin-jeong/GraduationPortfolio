package com.kakao.smartmemo.Model

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kakao.smartmemo.Contract.MapContract
import com.kakao.smartmemo.Contract.TodoContract
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

    constructor()
    constructor(onTodoListener: TodoContract.OnTodoListener) {
        this.onTodoListener = onTodoListener
    }
    constructor(onPlaceListener: MapContract.OnPlaceListener) {
        this.onPlaceListener = onPlaceListener
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
    fun addTodo(todoData: TodoData, placeList: ArrayList<PlaceData>) { // Todo create와 update를 모두 하는 함수
        var todoId = ""
        todoId = if (todoData.todoId == "") {
            todoData.title + System.currentTimeMillis()
        } else {
            todoData.todoId
        }
        Log.e("placeList Model", placeList.toString())
        var todotmp = TodoTmp(todoData.title,todoData.groupId)
        var timeAlarm = TimeAlarm(todoData.setTimeAlarm,todoData.timeDate,todoData.timeAgain,todoData.timeTime)
        var placeAlarm = PlaceAlarm(todoData.setPlaceAlarm,todoData.placeDate,todoData.placeAgain)
        firebaseGroup.child(todoData.groupId).child("TodoInfo").updateChildren(mapOf(todoId to todoId))
        with(firebaseTodo.child(todoId)){
            setValue(todotmp)
            child("TimeAlarm").setValue(timeAlarm)
            child("PlaceAlarm").setValue(placeAlarm)
        }
        placeList.forEach {
            with(firebaseTodoId.child(todoId)) {
                updateChildren(mapOf(it.place to "${it.latitude}!${it.longitude}"))
            }

            with(firebasePlace.child(it.place)) {
                child("PlaceId").setValue("${it.latitude}!${it.longitude}")
                updateChildren(mapOf(todoId to todoId))
            }

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
                    if(todoIdSanpshot.children.count()!=0){
                        for(todoId in todoIdSanpshot.children){
                            firebaseTodo.child(todoId.value.toString()).addValueEventListener(object :ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {}
                                override fun onDataChange(todoSnapshot: DataSnapshot) {
                                    var timeAlarm = todoSnapshot.child("TimeAlarm").getValue(TimeAlarm::class.java)
                                    var placeAlarm = todoSnapshot.child("PlaceAlarm").getValue(PlaceAlarm::class.java)
                                    if (timeAlarm != null&&placeAlarm != null) {
                                        todoList.add(TodoData(todoId.value.toString(),todoSnapshot.child("title").value.toString(),
                                            todoSnapshot.child("groupId").value.toString(),timeAlarm.setTimeAlarm,timeAlarm.timeDate,
                                            timeAlarm.timeTime,timeAlarm.timeAgain,placeAlarm.setPlaceAlarm,placeAlarm.placeDate,placeAlarm.placeAgain))
                                    }

                                    if(i == FolderObject.folderInfo.size-1&&j == todoIdSanpshot.children.count()-1){
                                        onTodoListener.onSuccess(todoList)
                                        i=0
                                    }else if(j == todoIdSanpshot.children.count()-1){
                                        j=0
                                        i++
                                    }else{
                                        j++
                                    }
                                }
                            })
                        }
                    }else{
                        if(i == FolderObject.folderInfo.size-1){
                            i=0
                            onTodoListener.onSuccess(todoList)
                        }
                        i++
                    }
                }
            })
        }
    }

    fun getGroupTodo(groupId: String) {
        var todoData = mutableListOf<TodoData>()
        var groupColor:Long  = 0

        onTodoListener.onSuccess(todoData)
    }

    fun deleteTodo(deleteTodoList: MutableList<TodoData>) {
        deleteTodoList.forEach{todoData->
            firebaseGroup.child(todoData.groupId).child("TodoInfo").child(todoData.todoId).removeValue()
            firebaseTodo.child(todoData.todoId).removeValue()
        }
    }

    fun getPlaceTodo() {
        var i = 0
        var j = 0
        var m = 0
        var placeList = mutableListOf<PlaceData>()
        FolderObject.folderInfo.forEach {
            firebaseGroup.child(it.key).child("TodoInfo").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) { }

                override fun onDataChange(todoSnapshot: DataSnapshot) {
                    todoSnapshot.children.forEach {todoId ->
                        firebaseTodoId.child(todoId.value.toString()).addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) { }

                            override fun onDataChange(placeSnapshot: DataSnapshot) {
                                placeSnapshot.children.forEach { place->
                                    var latitude = place.value.toString().substringBefore("!")
                                    var longitude = place.value.toString().substringAfter("!")

                                    placeList.add(PlaceData(place.key.toString(), latitude.toDouble(), longitude.toDouble()))
                                    if(m == placeSnapshot.children.count()-1&&j==todoSnapshot.children.count()-1&&i==FolderObject.folderInfo.size-1){
                                        Log.e("dddd", "FASDFASDF")
                                        onPlaceListener.onSuccess(placeList, "todo")
                                    }else if(m == placeSnapshot.children.count()-1&&j==todoSnapshot.children.count()-1){
                                        m=0
                                        j=0
                                        i++
                                    }else if(m == placeSnapshot.children.count()-1){
                                        m=0
                                        j++
                                    }else{
                                        m++
                                    }
                                }

                            }

                        })
                    }
                }

            })
        }
    }
}