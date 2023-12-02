package com.rumlor.domain

import jakarta.persistence.Entity

@Entity
class Product():BaseEntity() {
    var name:String? = null
    var stock:Int? = null
}