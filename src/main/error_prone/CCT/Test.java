class Test {
    void f() {
        g();
        h();
    }

    void g() {  
    }

    void h() {}

    public static void main(String args[]) {
        Test t = new Test();
        t.f();
        t.f();
    }
}
