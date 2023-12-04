package com.rumlor.events

import com.rumlor.domain.FoodCartProducts
import java.util.*


data class FoodCartCreatedEvent( val foodCardId: UUID)
data class ProductCreatedEvent(val productId: UUID,val name:String,val stock:Int)

data class SelectedProductEvent(
    val foodCartProductsId: UUID,
    val foodCardId: UUID,
    val productId: UUID,
    val name: String,
    val stock: Int,
    val quantity:Int
)

data class DeSelectedProductEvent(
     val foodCardId: UUID,
     val productId: UUID,
     val quantity:Int
)

data class ConfirmedOrderEvent(
     val foodCardId: UUID
)

