package com.rumlor.domain

import com.rumlor.query.FoodCartView
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany
import java.util.UUID


@Entity
class FoodCart(
    var confirmed:Boolean = false,

    @OneToMany(targetEntity = FoodCartProducts::class, mappedBy = "foodCart", cascade = [CascadeType.ALL])
    var foodCartProducts:Set<FoodCartProducts> = HashSet(),

    id:UUID = UUID.randomUUID()):BaseEntity(id.toString()) {

    companion object {
        fun from(view: FoodCartView):FoodCart = FoodCart(id = view.foodCartId)
    }

}