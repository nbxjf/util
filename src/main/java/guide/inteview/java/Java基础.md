## Java基础知识整理
#### 面向过程与面向对象的区别
##### 面向过程
* 优点
    * 性能高，无需进行类的实例化，常用于单片机、嵌入式开发
* 缺点
    * 不易维护、不易拓展、代码复用率低
##### 面向对象
* 优点
    * 易维护，易拓展，代码可重用
* 缺点
    * 性能较低

#### Java 面向对象编程三大特性
##### 封装
* 封装把一个对象的属性私有化，同时提供一些可以被外界访问的属性的方法
##### 继承
* 继承是指在已有的类的基础上拓展新的类
    * 子类可以继承父类的非private的方法和属性
    * 子类可以定义自己的方法和属性

##### 多态
* 多态是指程序中定义的变量的具体类型及其调用的具体方法在编程时期并不确定，而需要等到程序运行期才能确定
* 实现多态的方法：
    * 继承（对子类的相同方法的重写）
    * 接口（覆盖接口中的某一方法）

#### String StringBuffer 和 StringBuilder 的区别
* String
    * String是 `private　final　char　value[]` 修饰的，故String对象是不可变得
    * String是不可变的，可以理解成常量
    * 每次对String的修改，其实是生成的一个新的String对象，然后将指针指向新的对象
* StringBuffer
    * StringBuffer 继承自AbstractStringBuilder类，是用 `char　value[]`修饰的，所以可变
    * StringBuffer 是被 `synchronized` 关键字修饰的，是线程安全的
* StringBuilder
    * StringBuilder 继承自AbstractStringBuilder类，是用 `char　value[]`修饰的，所以可变
    * 非线程安全的
    
#### 重载与重写
* Overload:发生在同一个类中，方法名必须相同，参数类型不同、个数不同、顺序不同，方法返回值和访问修饰符可以不同，发生在编译时。 
* Override: 发生在父子类中，方法名、参数列表必须相同，返回值范围小于等于父类，抛出的异常范围小于等于父类，访问修饰符范围大于等于父类；如果父类方法访问修饰符为 private 则子类就不能重写该方法。

#### == 与 equals
* == 
    * == 是判断的两个对象的地址是否相等
* equals
    * equals判断的是两个对象是否相等，若对象未覆写equals()方法，等价与 『==』，若覆写了equals()方法，则判断内容是否相等

#### hashCode 与 equals
* Object类下面有哪些方法：
    * getClass()
    * equals()
    * hashcode()
    * wait()
    * notify()
    * notifyAll()
    * toString()
*  为什么重写equals()方法一定要重写hashCode()方法?
    * 如果两个对象相等，那么他们的hashCode一定相等，反之则不然，如果两个的hashCode相等，未必对象是相等的，所以在重写equals方法的时候一定要重写hashCode()方法

#### Java序列化中如果有些字段不想进行序列化 怎么办
* transient

