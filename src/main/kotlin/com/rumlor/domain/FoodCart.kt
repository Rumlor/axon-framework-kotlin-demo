package com.rumlor.domain

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany


@Entity
class FoodCart():BaseEntity() {

    var confirmed:Boolean = false

    @OneToMany(orphanRemoval = true, targetEntity = Product::class)
    @JoinTable(name = "food_cart_products",
        joinColumns = [JoinColumn(name = "food_cart")],
        inverseJoinColumns = [JoinColumn(name = "product")])
    var products:Set<Product> = HashSet()

}