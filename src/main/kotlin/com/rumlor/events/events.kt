package com.rumlor.events

import java.util.*


data class FoodCartCreatedEvent( val foodCardId: UUID,)


data class SelectedProductEvent(
    val foodCardId: UUID,
    val productId: UUID,
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

