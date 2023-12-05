package com.rumlor.model

data class SelectedProduct(val foodCartId:String, val quantity:Int,val productId:String)
data class ChangeProductQuantity(val foodCartId:String,val newQuantity:Int,val productId:String)
data class DeSelectedProduct(val foodCartId:String, val quantity:Int,val productId:String)
data class CreateProduct(val name:String,val stock:Int)