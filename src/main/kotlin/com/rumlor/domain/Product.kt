package com.rumlor.domain

import com.rumlor.query.ProductView
import jakarta.persistence.Entity
import java.util.UUID

@Entity
class Product(
    var name:String? = null,
              var stock:Int? = null,
              id:UUID= UUID.randomUUID()) :BaseEntity(id.toString()) {
    companion object {
        fun from(view:ProductView) = Product(view.name,view.stock)

    }

}