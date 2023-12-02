package com.rumlor.domain

import com.rumlor.query.FoodCartView
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.util.UUID


@Entity
class FoodCart(var confirmed:Boolean = false,id:UUID = UUID.randomUUID()):BaseEntity(id) {

    @OneToMany
    @JoinColumn(name = "food_cart")
    var foodCartProducts:Set<FoodCartProducts> = HashSet()

    companion object {
        fun from(view: FoodCartView):FoodCart = FoodCart(id = view.foodCartId)
    }

}