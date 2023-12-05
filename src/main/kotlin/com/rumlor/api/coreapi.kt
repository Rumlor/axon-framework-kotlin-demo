package com.rumlor.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.util.UUID

class CreateFoodCartCommand
data class CreateProductCommand(val productId: UUID,val name:String,val stock:Int)

data class AddProductCommand(
    @TargetAggregateIdentifier val foodCartId:UUID,
    val productId: UUID,
    var quantity:Int,
    var stock: Int,
    val name: String
)
data class ChangeFoodCartProductQuantityCommand(
    @TargetAggregateIdentifier val productId:UUID,
    val foodCartId: UUID,
    val newQuantity:Int
    )

data class RemoveProductCommand(
    @TargetAggregateIdentifier val foodCartId:UUID,
    val productId: UUID,
    val quantity:Int
)

data class ConfirmOrderCommand(
    @TargetAggregateIdentifier val foodCardId: UUID
)

