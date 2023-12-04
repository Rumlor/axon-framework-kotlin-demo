package com.rumlor.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class FoodCartProducts(

    var quantity:Int = 0,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "food_cart")
    val foodCart: FoodCart? = null,

    @ManyToOne
    @JoinColumn(name = "product")
    val product: Product? = null,

    id :UUID = UUID.randomUUID(),

    ):BaseEntity(id)