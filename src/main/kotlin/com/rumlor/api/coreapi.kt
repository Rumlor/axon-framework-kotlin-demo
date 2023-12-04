package com.rumlor.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

class CreateFoodCartCommand
data class CreateProductCommand(val productId: UUID,val name:String,val stock:Int)

data class SelectProductCommand(
    @TargetAggregateIdentifier val foodCartId:UUID,
    val productId: UUID,
    val quantity:Int,
    val stock: Int,
    val name: String
)

data class DeSelectProductCommand(
    @TargetAggregateIdentifier val foodCartId:UUID,
    val productId: UUID,
    val quantity:Int
)

data class ConfirmOrderCommand(
    @TargetAggregateIdentifier val foodCardId: UUID
)

