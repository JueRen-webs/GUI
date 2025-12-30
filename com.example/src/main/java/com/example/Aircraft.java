package com.example;

// 定义一个公开的类，名字叫 Aircraft（飞机）
public class Aircraft {
    // --- 定义飞机的属性 (变量) ---
    // private 表示这些数据是私有的，外部不能直接随意修改，必须通过我们提供的方法
    private String registrationNumber; // 注册号 (例如: 9M-ABC)
    private String brand;              // 品牌 (例如: Boeing)
    private String model;              // 型号 (例如: 737)
    private int capacity;              // 载客量 (例如: 180)
    private String status;             // 状态 (例如: "Available", "Maintenance")

    // --- 构造方法 (Constructor) ---
    // 当你 new Aircraft(...) 时，就会调用这个方法来创建一个新飞机
    public Aircraft(String registrationNumber, String brand, String model, int capacity, String status) {
        this.registrationNumber = registrationNumber; // 把传入的注册号赋值给当前飞机的属性
        this.brand = brand;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    // --- Getter 方法 (获取数据) ---
    // 其他类想知道这架飞机的注册号，就调用这个方法
    public String getRegistrationNumber() { return registrationNumber; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    
    // --- Setter 方法 (修改数据) ---
    // [新手修改]: 如果你想添加修改飞机的其他属性的功能，可以在这里加类似的方法
    // 目前只允许修改状态
    public void setStatus(String status) { 
        this.status = status; // 更新状态
    }

}