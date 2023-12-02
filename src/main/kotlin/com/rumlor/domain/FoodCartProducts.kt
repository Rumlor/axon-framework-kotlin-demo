package com.rumlor.domain

import com.fasterxml.jackson.databind.ser.Serializers.Base
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class FoodCartProducts(
    val quantity:Int = 0
):Base() {

    @ManyToOne
    @JoinColumn(name = "product")
    var product:Product? = null

}