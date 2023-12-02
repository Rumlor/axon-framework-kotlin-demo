package com.rumlor.domain

import com.rumlor.query.ProductView
import jakarta.persistence.Entity

@Entity
class Product(var name:String? = null, var stock:Int? = null) :BaseEntity() {
    companion object {
        fun from(view:ProductView) = Product(view.name,view.stock)

    }

}