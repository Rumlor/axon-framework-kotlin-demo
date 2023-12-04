package com.rumlor.exception

class ProductDeSelectionException(msg:String = "product deselection exception") : Exception(msg)
class ProductSelectException(msg: String="product selection exception") : Exception(msg)
class InvalidProductStockException(msg: String="product sstock should be greater than zero") : Exception(msg)