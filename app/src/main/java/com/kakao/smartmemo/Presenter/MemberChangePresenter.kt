package com.kakao.smartmemo.Presenter

import android.app.Activity
import com.kakao.smartmemo.Contract.MemberChangeContract
import com.kakao.smartmemo.Model.UserModel

class MemberChangePresenter : MemberChangeContract.Presenter, MemberChangeContract.OnPasswordChangeSuccessListener {
    private var view : MemberChangeContract.View
    var userModel: UserModel

    constructor(view:MemberChangeContract.View){
        this.view= view
        this.userModel = UserModel(this)
    }

    override fun getProfile() {
        userModel.getProfile()
    }

    override fun signOutUser() {
        userModel.signOutUser()
    }

    override fun checkPassword(confirmPassword: String) :Boolean{
        return userModel.checkPassword(confirmPassword)
    }

    override fun deleteUser() {
//        userModel.deleteUser()
//        userModel.deleteAuth()
    }

    override fun updateUser(context: Activity, pw: String, name: String, addr: String, kakaoAlarmTime: String) {
        userModel.updateDatabaseUser(pw, name, addr, kakaoAlarmTime)
    }

    override fun updatePassword(pw: String) {
        userModel.updateUserPassword(pw)
    }

    override fun onSuccess() {
        view.onSuccess()
    }

    override fun onFailure() {
        view.onFailure()
    }
}