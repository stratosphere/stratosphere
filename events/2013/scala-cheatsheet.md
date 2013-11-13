---
layout: inner_complex
title:  "Scala Hands-on Cheatsheet"
---


# Scala Session Cheatsheet



## Part I: The interpreter

```scala
1> 1 + 2
2> println("Hello, world!")
3> var x: Int = 42
4> x = 43
5> val msg: String = "Hello, world!"
6> msg = "Goodbye cruel world!" 
6> def my_max(x: Int, y: Int) = if (x > y) x else y
7> my_max(3, 5)
8> for (c <-"Stratosphere") println(c)
9> :q
```

## Part II: Basic OOP

```scala
10> abstract class Shape {
     def getArea(): Int
	}
11> class Rectangle(x: Int, y: Int) extends Shape {
		def getArea(): Int = x*y
	}
12> class Point(val x: Int, val y:Int)
13> class Circle(radius: Int, center: Point) extends Shape {
    	def getArea(): Int = radius*radius*3
    	override def toString = "{(" + center.x + "," + center.y + ")," + radius + "}"
	}
```

## Part III: Lists and Higher-order Functions

```scala
14> def fact(n: Int): Int = if (n == 0) 1 else n*fact(n-1)
15> def squares(lst: List[Int]) = lst.map(x => x*x)
```
