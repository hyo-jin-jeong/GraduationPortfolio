package com.kakao.smartmemo.Presenter

import android.app.Activity
import com.kakao.smartmemo.Contract.LoginContract
import com.kakao.smartmemo.Model.UserModel

class LoginPresenter : LoginContract.Presenter, LoginContract.OnLoginListener {
    var userModel:UserModel
    private var view : LoginContract.View

    constructor(view: LoginContract.View){
        this.view = view
        this.userModel = UserModel(this)
    }

    override fun checkUser(context: Activity, email:String, password:String) {
        userModel.checkUser(context, email, password)
    }

    override fun checkCurrentUser(): Boolean {
        return userModel.checkCurrentUser()
    }

    override fun getProfile() {
        userModel.getProfile()
    }

    override fun onSuccess() {
        view.onLoginSuccess()
    }

    override fun onFailure(message: String) {
        view.onLoginFailure(message)
    }

}