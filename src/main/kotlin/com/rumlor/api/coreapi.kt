package com.rumlor.api

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.beans.ConstructorProperties
import java.util.UUID

class CreateFoodCartCommand



data class SelectProductCommand(
    @TargetAggregateIdentifier val foodCardId:UUID,
    val productId: UUID,
    val quantity:Int
)

data class DeSelectProductCommand(
    @TargetAggregateIdentifier val foodCardId:UUID,
    val productId: UUID,
    val quantity:Int
)

data class ConfirmOrderCommand(
    @TargetAggregateIdentifier val foodCardId: UUID
)

