package com.rumlor.events

import java.util.*


data class FoodCartCreatedEvent( val foodCardId: UUID)
data class ProductCreatedEvent(val productId: UUID,val name:String,val stock:Int)

data class AddedProductEvent(
    val foodCartProductsId: UUID,
    val foodCardId: UUID,
    val productId: UUID,
    val name: String,
    val stock: Int,
    val quantity:Int
)
data class ChangeQuantityEvent(
    val productId: UUID,
    val foodCardId: UUID,
    val quantity:Int
    )

data class RemovedProductEvent(
     val foodCardId: UUID,
     val productId: UUID,
     val quantity:Int
)

data class RemovedProductAppliedEvent(
    val foodCardId: UUID,
    val productId: UUID,
    val quantity:Int
)

data class ConfirmedOrderEvent(
     val foodCardId: UUID
)

