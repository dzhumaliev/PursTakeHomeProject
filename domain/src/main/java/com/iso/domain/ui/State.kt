package com.iso.domain.ui

sealed class State {

    class LoadingState(val isLoading:Boolean):State()
    class SuccessListState<T>(val data: ArrayList<T>):State()
    class SuccessObjectState<T>(val data: T):State()
    object NoItemState : State()
    class ErrorState(val message:String,val errorCode:Int):State()

}