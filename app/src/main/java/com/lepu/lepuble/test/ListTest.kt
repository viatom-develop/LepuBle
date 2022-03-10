package com.lepu.lepuble.test

class ListTest {



}


fun main() {
    var l = mutableListOf<Int>(1,2,9,9,3,4,5,6,7,9,9,9,9)
    var s = l.size-1

    while (l[s] == 9) {
        l.removeAt(s)
        s--
    }

    print(l)
}